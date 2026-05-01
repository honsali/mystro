# Stage 1 — Code Audit V4 (Improvement Comparison vs V3)

Re-running [audit.md](audit.md) against the source as of working-tree HEAD (`2788cc7 add auditing v3` plus uncommitted changes to `BasicCalculationContext`, `LotCalculator`, `SectCalculator`, `BasicSect`). Per audit.md, previous reports are read as history, not authority. Numerical fixture correctness is assumed; the audit focuses on architecture, type-safety, robustness, and current-bug risk.

---

```text
Current assessment: Stage 1 is now in a healthy place. The two highest-priority open items from V3 — the LotCalculator sect divergence (L1) and the swe_azalt NaN hole (L7) — were fixed in the working tree, and the typed-key win (L3, BasicSect.planetSects keyed by Planet) shipped in the same pass. With those three closed, the engine no longer has any known correctness divergence in stage 1: chart sect and lot sect now share one altitude expression and both throw on swisseph anomalies.

What remains is structural rather than correctness-critical. PointEntry is still a 30-field nullable kitchen-sink rather than a sealed hierarchy (V3 L2). The Logger singleton (V3 C2/N7), the three empty doctrine shells (V3 M3), and the configuration-advertised-but-not-wired CalculationSetting fields (V3 M4) are unchanged. None of these block stage 2; all are deliberate technical debt the project owner has flagged.

Highest-priority next action: Replace PointEntry with a sealed hierarchy (PlanetPointEntry / AnglePointEntry / LotPointEntry / SyzygyPointEntry) before stage 2 doctrine code starts consuming it — typing the public surface gets cheaper now and more expensive once the descriptive layer exists.
```

---

## What changed since V3

Three of the V3 "L" findings landed in the working tree:

1. **L1 — LotCalculator sect** ([LotCalculator.java:19](src/main/java/app/basic/calculator/LotCalculator.java#L19)): `boolean diurnal = sun.getHouse() >= 7 && sun.getHouse() <= 12;` → `boolean diurnal = ctx.horizontalAltitude(sun.getLongitude(), sun.getLatitude()) >= 0.0;`. Lots and chart sect now share the same altitude expression. The V3 correctness regression is closed.
2. **L3 — BasicSect.planetSects key type** ([BasicSect.java:19](src/main/java/app/model/basic/BasicSect.java#L19), [SectCalculator.java:50-69](src/main/java/app/basic/calculator/SectCalculator.java#L50-L69)): `Map<String, PlanetSectInfo>` → `Map<Planet, PlanetSectInfo>`. Typed all the way down for sect.
3. **L7 — `horizontalAltitude` NaN propagation** ([BasicCalculationContext.java:144-147](src/main/java/app/basic/BasicCalculationContext.java#L144-L147)): now `if (Double.isNaN(horizontalCoordinates[1])) { Logger.instance.error(...); throw ...; }`. Brings `swe_azalt` into the same uniform throw-on-failure policy as `swe_houses_ex`, `swe_calc_ut`, and obliquity.

These are real wins. **L1 in particular eliminates the only known correctness-regression in stage 1.** No new critical/major findings have been introduced by these changes.

---

## CRITICAL

No findings.

The V3 critical/regression item (L1, LotCalculator using house-number sect) is fixed. Inspection of the rest of the basic pipeline finds no current correctness bug, no swisseph failure that fakes data, and no architectural violation of `NEW_ARCHITECTURE_SPEC.md` §14.4. The `basic` package depends on `swisseph`, `app.model.basic`, `app.model.data`, `app.model.input`, and `app.output.Logger` — and on no concrete doctrine implementation.

---

## MAJOR

### M-V4-1. `PointEntry` is a 30-field nullable kitchen-sink, not a sealed hierarchy — **Major** — Confidence: High
**Files:** [PointEntry.java:12-72](src/main/java/app/model/basic/PointEntry.java#L12-L72), [PointCalculator.java:19-85](src/main/java/app/basic/calculator/PointCalculator.java#L19-L85)
**Status:** Technical debt (already known; restated for prioritisation)

**Problem:**
`PointEntry` carries 30 boxed-nullable fields (`Double`, `Boolean`, `Integer`, `Planet`, `SyzygyType`, `Instant`) plus a `PointType` discriminator. The discriminator is *structural* (it is in the JSON) but not *enforced* — `PointEntry.builder(PointType.ANGLE).house(1).julianDay(2451545.0).syzygyType(SyzygyType.NEW_MOON).build()` compiles, builds, and serialises successfully despite mixing angle, lot, and syzygy fields.

The audit-§2 expectation is a sealed hierarchy:

```java
sealed interface PointEntry permits PlanetPointEntry, AnglePointEntry, LotPointEntry, SyzygyPointEntry { ... }
```

with each variant a record carrying only its real fields. Today the closest the engine gets to this constraint is `PointCalculator` itself, which is the only place that builds entries.

**Why it matters:**
Stage 2 doctrine code will start *reading* `PointEntry`. The moment two doctrine modules reach into the same `PointEntry`, the implicit "if `type == PLANET`, the planet fields are non-null and the syzygy fields are null" contract becomes a real public surface, and a typo in any descriptive calculator (`entry.getJulianDay()` on a planet entry → `null` → silent NaN propagation) is a class of bug stage 1 cannot catch. Sealed records make the same mistake a compile error.

**Suggested fix:**
1. Sealed `PointEntry` with `record PlanetPointEntry(...)`, `record AnglePointEntry(...)`, `record LotPointEntry(...)`, `record SyzygyPointEntry(...)`.
2. Jackson serialises these via `@JsonTypeInfo(use=NAME, property="type") + @JsonSubTypes(...)` — works on records.
3. `PointCalculator` becomes four small `add*` helpers that build the right record directly; the builder goes away.
4. Map *keys* (`Planet.SUN.name()`, `"ASCENDANT"`, `"FORTUNE"`, `"PRENATAL_SYZYGY"`) can stay as strings for one more iteration, but plan to introduce typed keys (`AngleType`, `LotType`) in the same change-set.

This is not stage-2-blocking but the cost of waiting is non-trivial — every doctrine module written before this fix becomes a callsite to update.

---

### M-V4-2. `Logger.instance` static singleton, no per-run scope, no thread safety — **Major** — Confidence: High
**Files:** [Logger.java:8-40](src/main/java/app/output/Logger.java#L8-L40), [LoggerWriter.java:14](src/main/java/app/output/LoggerWriter.java#L14), [InputLoader.java:18](src/main/java/app/input/InputLoader.java#L18)
**Status:** Current bug (CLI repeated runs) + Future-service risk

**Problem:**
`Logger.instance` is `public static final Logger instance = new Logger();` over an unsynchronised `ArrayList`. `startedAt` is captured at JVM startup. There is no `clear()` or `forRun(...)` API. `LoggerWriter` serialises the singleton reflectively while other code is potentially still appending. `InputLoader` uses `Logger.instance.hasErrors()` as the cross-cutting validation gate.

Concrete failure modes:
- **Two charts in one JVM run:** `run-logger.json` for the second run includes the first run's entries, since nothing clears them between charts. (Today `App.main` writes the logger once at the end, so this is masked — but the next consumer of the engine will hit it.)
- **`hasErrors()` as validation gate:** any `Logger.instance.error(...)` from anywhere — `SettingLoader.parsePrecision`'s fallback, `DoctrineLoader`'s "Skipping unknown doctrine" — flips `InputLoader.load` to throw, even if the *actual* subjects+doctrines requested are valid. The "errors are global, validation reads global" coupling is fragile.
- **Future Spring/service:** trivially unsafe.

**Why it matters:**
The CLI single-run case mostly hides this, but the stage-2 doctrine work will multiply log-call sites. Each new error-emitting doctrine increases the chance that a unrelated doctrine request flips the input gate. The fix is small now and gets larger as more code adopts the singleton.

**Suggested fix:**
Smallest practical change:
1. `Logger` becomes a regular class. Construct one in `App.main`, pass it through `InputLoader`, calculator pipeline, and writer.
2. `InputLoader` returns a `LoadResult { bundle, errors }` instead of consulting `Logger.instance.hasErrors()`. The validation gate is local data.
3. `LoggerWriter` serialises the instance it was given, not a static field.
4. Calculators that today take `Logger.instance.error(...)` paths get a `Logger logger` field — or, better, throw and let the caller log (most of these throws happen with "Calculation failed. See output/run-logger.json" anyway, so the message is already redundant with the exception).

This is a 1-2 hour change today; it grows linearly with new callers.

---

### M-V4-3. `CalculationSetting.nodeType / positionType / frame / ephemerisVersion` advertise configurability that is never wired — **Major** — Confidence: High
**Files:** [CalculationSetting.java:25-29](src/main/java/app/model/input/CalculationSetting.java#L25-L29), [PlanetCalculator.java:29](src/main/java/app/basic/calculator/PlanetCalculator.java#L29), [BasicCalculationContext.java:151-157](src/main/java/app/basic/BasicCalculationContext.java#L151-L157)
**Status:** Technical debt + future contract trap

**Problem:**
`CalculationSetting` exposes getters for `nodeType`, `positionType`, `frame`, and `ephemerisVersion`. The constructor hardcodes them to `MEAN`, `APPARENT`, `GEOCENTRIC`, `"swisseph-2.10.03"`. `SettingLoader` does not read them from `settings.properties`. `PlanetCalculator` hardcodes `SE_MEAN_NODE`, ignoring `nodeType`. `BasicCalculationContext.planetFlags()` does not consult `positionType` or `frame`. A future caller — or a future report-metadata writer — that reads `setting.getNodeType()` and prints "MEAN" while the calculation actually used `SE_TRUE_NODE` (because someone changed the calculator without updating the setting, or vice versa) would write a *wrong but plausible* metadata field.

**Why it matters:**
This is a fragile public-contract issue (audit.md §2): metadata that says one thing and is computed as another. It has not yet bitten anyone because the calculation always matches the hardcoded defaults — but the asymmetry guarantees that the first person who tries to change either side will produce silently incorrect reproducibility metadata.

**Suggested fix:**
Two acceptable directions:
1. **Drop the fields.** Remove `nodeType`, `positionType`, `frame` from `CalculationSetting`. Add them back when there is a real plan to wire them through swisseph. `ephemerisVersion` can move to a build-time constant or be derived from a swisseph-bundled identifier.
2. **Wire them.** Have `PlanetCalculator` read `setting.getNodeType() == NodeType.TRUE ? SE_TRUE_NODE : SE_MEAN_NODE`; have `BasicCalculationContext.planetFlags()` add `SEFLG_TRUEPOS` / `SEFLG_TOPOCTR` for the corresponding `positionType`/`frame` values.

Option 1 is the smallest change. Option 2 is a feature; do it when the feature is actually requested.

---

### M-V4-4. Three doctrine implementations are empty shells — **Major** — Confidence: Medium
**Files:** [DorotheusDoctrine.java](src/main/java/app/doctrine/impl/dorotheus/DorotheusDoctrine.java), [PtolemyDoctrine.java](src/main/java/app/doctrine/impl/ptolemy/PtolemyDoctrine.java), [ValensDoctrine.java](src/main/java/app/doctrine/impl/valens/ValensDoctrine.java)
**Status:** Roadmap item — but in a way that risks shape drift

**Problem:**
Three doctrine classes (~43 lines each) each declare `id`, `name`, `houseSystem`, `zodiac`, `terms` as constants and return `new SimpleDescriptiveResult(getId(), Map.of())` from `describe(...)`. They differ only in three constants.

Per audit.md §4, this is correctly classified as a *roadmap item* — until stage-2 doctrine logic is implemented, the descriptive surface is intentionally absent. Replacing them with YAML or a generic Map-of-fields config would violate the project's "doctrine is hardcoded code" principle ([AGENTS.md §Doctrine philosophy](AGENTS.md)).

**However:** the present arrangement is not a real doctrine module either. Three near-identical classes whose only purpose is returning constants are not knowledge modules — they are placeholders shaped like classes. When stage 2 starts, the right doctrine module structure is unknown; the current three classes commit to one structure ("Doctrine implements Doctrine, returns DescriptiveResult from describe(input, chart)") that may not survive contact with real Valens vs. Dorotheus differences.

**Why it matters:**
Risk is shape drift, not correctness. The three classes act as a *contract example* for stage 2. If the contract is wrong (e.g. `describe` should take more inputs, return a doctrine-specific subtype instead of a generic `DescriptiveResult`, expose technique lists, etc.), every new doctrine that follows the example inherits the wrong shape.

**Suggested fix:**
Two options:
1. **Collapse to one example.** Keep `DorotheusDoctrine` as the worked example for stage 2; delete `PtolemyDoctrine` and `ValensDoctrine` until those doctrines actually have descriptive logic to ship. This avoids the "three identical empties" smell without drifting into a config-driven design.
2. **Leave as-is.** The current classes do compile, do exercise `DoctrineLoader.register`, and are explicitly classified in audit.md as roadmap items. If the project owner prefers the visual reminder that three doctrines are coming, this is defensible.

Mark this Major rather than Minor because the contract those three classes encode is the entry point to stage 2 — getting it wrong has compounding cost.

---

## MINOR

### m-V4-1. `Sect.UNKNOWN` is unreachable in practice — Minor — Confidence: High
**Files:** [Sect.java:1-7](src/main/java/app/model/data/Sect.java#L1-L7), [SectCalculator.java:71-77](src/main/java/app/basic/calculator/SectCalculator.java#L71-L77)
**Status:** Technical debt

`Sect.UNKNOWN` exists only as a fallback in `mercurySect(mercury, sun)` when Mercury or Sun is missing. Sun is required by `SectCalculator.requiredPlanet`, and Mercury is in the standard planet set. In current input flows the value is never written. Either delete it (and have `mercurySect` throw or return `Optional<Sect>`) or document it. This is the same anti-pattern V3 M5 closed for `HouseSystem.UNKNOWN`.

**Fix:** delete `UNKNOWN`; change `mercurySect` to return `Optional<Sect>`; the `PlanetSectInfo.condition`/`phaseRelativeToSun` for Mercury becomes `null` if Mercury is absent (already handled by `PlanetSectInfo`'s nullable design — confirm the JSON consumer tolerates a missing `MERCURY` entry, which it should given `@JsonInclude(NON_NULL)`).

### m-V4-2. `ChartAngle.name` and `LotPosition.name` are free-form `String` — Minor — Confidence: High
**Files:** [AngleCalculator.java:17-20](src/main/java/app/basic/calculator/AngleCalculator.java#L17-L20), [LotCalculator.java:22-37](src/main/java/app/basic/calculator/LotCalculator.java#L22-L37)
**Status:** Technical debt

`AngleCalculator` writes `"ASCENDANT" / "MIDHEAVEN" / "DESCENDANT" / "IMUM_COELI"` and `LotCalculator.angle("ASCENDANT")` does string-equals lookup. A typo in either side is a silent null. The set is closed and small. Replace with `enum AngleType { ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI }` and `enum LotType { FORTUNE, SPIRIT }`. Same shape as the V3 M5 fix for `HouseSystem`/`Terms`/`Zodiac`. Couple of hours.

### m-V4-3. `PointCalculator` keys are still raw strings — Minor — Confidence: High
**Files:** [PointCalculator.java:45, 53, 64, 82](src/main/java/app/basic/calculator/PointCalculator.java#L45)
**Status:** Technical debt

`points.put(planet.getPlanet().name(), ...)`, `points.put(angle.getName(), ...)`, `points.put(lot.getName(), ...)`, `points.put("PRENATAL_SYZYGY", ...)`. The Map type is `Map<String, PointEntry>`. Once `PointEntry` is sealed (M-V4-1) and angles/lots have enums (m-V4-2), the key can become a typed sum (e.g. `sealed interface PointKey { record PlanetKey(Planet); record AngleKey(AngleType); record LotKey(LotType); record SyzygyKey() {} }`) or four parallel typed maps. Defer until M-V4-1 lands; doing it earlier creates rework.

### m-V4-4. Calculator order is implicit and load-bearing — Minor — Confidence: High
**Files:** [BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)
**Status:** Technical debt

11 calculators run in a hand-crafted order; each step reads what the prior step wrote (`Sect` reads planets; `Lot` reads planets+angles; `Point` reads planets+angles+lots+syzygy). The dependency graph is invisible. The L1 fix specifically benefited from the existing order; reshuffling for an unrelated reason could silently break something. Lightweight mitigation: a one-line comment per step naming what it reads and writes. Heavier: a `record CalculatorStep(name, requires, produces)` model. The audit recommends the comment for now.

### m-V4-5. Rounding at calculation time, not serialization — Minor — Confidence: High
**Files:** [BasicCalculationContext.java:84-89](src/main/java/app/basic/BasicCalculationContext.java#L84-L89), all callers in `app.basic.calculator.*`
**Status:** Technical debt with latent failure mode

`ctx.round(...)` is called on intermediate longitudes before they feed `houseOf`/`signDistance`/`pairwiseRelation`. At `DECIMAL_6` this is harmless. At a coarser policy (e.g. `DECIMAL_3`) it would corrupt aspect calculations near orb boundaries. The fix is to round at serialization time only — which means either DTOs (today's `BasicChart` is the wire model) or a Jackson serializer that rounds doubles per-policy. Defer until a finer rounding policy is actually requested.

### m-V4-6. Syzygy heuristics — Minor — Confidence: Medium
**Files:** [SyzygyCalculator.java:31-69](src/main/java/app/basic/calculator/SyzygyCalculator.java#L31-L69)
**Status:** Technical debt

Three known soft spots, none currently failing on the Lille fixture:
- 30-day search window assumes a syzygy occurs within a lunar cycle. Always true; reasonable. Leave.
- 0.25-day step; sign-flip detection requires `|earlierValue - laterValue| < 90.0`. Robust enough; mathematically the bracket can fail at the 360°/0° wrap, which the `> 180.0 → −360` reflection in `syzygySignedDelta` already handles.
- 50 bisection iterations on a 0.25-day bracket converges to ~2e-16 days — far past Swiss Ephemeris precision. Cap at 30 or use a tolerance. Cosmetic.

Move from "improve all three" to "leave alone unless a fixture fails."

### m-V4-7. `BasicChart` carries Jackson `@JsonIgnore` and dual public surface — Minor — Confidence: High
**Files:** [BasicChart.java:77, 94, 103, 112, 121, 162, 171](src/main/java/app/model/basic/BasicChart.java)
**Status:** Technical debt

7 `@JsonIgnore` fields (planets, angles, three raw matrices, syzygy, lots) sit beside 8 serialised fields. The internal-vs-output distinction is real (planets/angles/lots are also surfaced via `points`) but expressing it through `@JsonIgnore` annotations on the domain model means a future caller cannot tell which fields are "for downstream calculators" and which are "for JSON" without reading every getter. Extract a `BasicChartReport` DTO when stage 2 starts; for now, the cost-benefit isn't compelling.

### m-V4-8. `BaseCalculator` mutable inheritance state — Minor — Confidence: High
**Files:** [BaseCalculator.java:7-14](src/main/java/app/basic/BaseCalculator.java#L7-L14)
**Status:** Technical debt + Future-service risk

Each calculator reads `protected BasicChart basicChart; protected BasicCalculationContext ctx;` set by the public `calculate(...)` method. This makes calculators non-reentrant: calling `calculate(chartA, ctxA)` and `calculate(chartB, ctxB)` concurrently on one instance corrupts both. Today calculators are constructed per-call ([BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)), so this is hidden. Smallest fix: pass `(chart, ctx)` as method parameters to `executeCalculation`. Five minutes; one-line per calculator.

### m-V4-9. `App.main throws Exception` and per-call calculator allocation — Minor — Confidence: High
**Files:** [App.java:21](src/main/java/app/App.java#L21), [BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)
**Status:** Technical debt

Cosmetic. `throws Exception` should be `throws IOException`. Eleven `new` per chart is fine for the CLI; refactor only if/when calculators become heavyweight or are turned into Spring `@Component`s.

### m-V4-10. `MoonPhaseCalculator.waxing` strict `<` at exactly 180° — Minor — Confidence: High
**Files:** [MoonPhaseCalculator.java:20](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L20)
**Status:** Technical debt

`directedElongation == 180.0` exactly is `waxing = false`; the `MoonPhaseName` boundary on the same line uses `<= 180.0 + epsilon`. Inconsistency for one degenerate case. Either `<= 180.0` or `< 180.0 - epsilon`. One-character fix.

---

## NIT

- **`PlanetPosition.getRetrograde()` should be `isRetrograde()`** ([PlanetPosition.java:70](src/main/java/app/model/basic/PlanetPosition.java#L70)). Boxed `Boolean`-returning getter named `getRetrograde`. Convention mismatch.
- **`oriental/occidental` convention is undocumented** ([SolarPhaseCalculator.java:30-31](src/main/java/app/basic/calculator/SolarPhaseCalculator.java#L30-L31), [SectCalculator.java:62](src/main/java/app/basic/calculator/SectCalculator.java#L62)): `delta > 180 → ORIENTAL` is the standard Hellenistic convention (planet rising before the Sun = oriental) but a one-line comment would help future maintainers.
- **Magic numbers** still inline: `15.0` ([SimpleCalculator.java:23](src/main/java/app/basic/calculator/SimpleCalculator.java#L23)), `1_000_000.0` ([BasicCalculationContext.java:86](src/main/java/app/basic/BasicCalculationContext.java#L86)), `2440587.5` and `86400.0` ([SyzygyCalculator.java:72](src/main/java/app/basic/calculator/SyzygyCalculator.java#L72)). Per-call cost: zero. Cleanup cost: zero.
- **`TraditionalTables.signOf/degreeInSign/normalize` reimplemented** ([TraditionalTables.java:92-103](src/main/java/app/basic/TraditionalTables.java#L92-L103)). Same trivia as V3 M2's lingering nit. A `Longitudes` utility class shared by `BasicCalculationContext` and `TraditionalTables` would dedup. Cosmetic.
- **`LogEntry` lacks a timestamp**. Run-logger entries record level/scope/message, not when. Trivial to add `Instant.now()` to the `LogEntry` constructor.
- **`Logger` has no `WARN` level**. Today only `INFO`/`ERROR`. The "Skipping unknown doctrine" message in `DoctrineLoader` is logged as `ERROR` and triggers `InputLoader`'s validation gate — debatable; if any *other* requested doctrine is valid, this is closer to a warning. Linked to M-V4-2.

---

## Areas that are clean

- **Swiss Ephemeris error policy is uniform.** Houses, obliquity, planets, declination, longitude, and now `swe_azalt` all throw on `result < 0` or `NaN`. No swisseph failure produces plausible fake values.
- **Sect is altitude-based, with the horizon tiebreaker resolved to DIURNAL** ([SectCalculator.java:22-24](src/main/java/app/basic/calculator/SectCalculator.java#L22-L24), [LotCalculator.java:19](src/main/java/app/basic/calculator/LotCalculator.java#L19)). Chart sect and lot sect now agree by construction.
- **Input validation defends the boundary.** `Subject` constructor validates ranges; date is ISO-only; time requires `HH:mm:ss`; `HouseSystem`/`Terms`/`Zodiac` parse-or-throw with no `UNKNOWN` fallback.
- **House recomputation is once-and-cached** ([BasicCalculationContext.java:36-40](src/main/java/app/basic/BasicCalculationContext.java#L36-L40)).
- **`TriplicityRulers` is a typed record**; sect is a fully typed POJO; `BasicSect.planetSects` is now `Map<Planet, …>`.
- **`isTraditionalPlanet`** ([TraditionalTables.java:14-19](src/main/java/app/basic/TraditionalTables.java#L14-L19)) uses an exhaustive switch — adding a planet to `Planet` is a compile error here, exactly the safety property the project's "explicit over clever" rule wants.
- **`basic` does not depend on doctrine implementations.** Confirmed by walking imports in `app.basic.*`.

---

## Suggested implementation order

Ordered by ratio of (correctness/contract risk reduction) to (implementation effort):

1. **m-V4-1 — Delete `Sect.UNKNOWN`.** 10 minutes. Closes a known dead code path before anything else depends on it.
2. **m-V4-8 — Pass `(chart, ctx)` as method parameters in `BaseCalculator`.** 30 minutes. Removes mutable inheritance state. Defensive against any future decision to share calculator instances.
3. **M-V4-3 — Either drop or wire `nodeType/positionType/frame/ephemerisVersion`.** 1 hour for the drop, 2-3 hours for the wire. Closes the lying-metadata trap before stage 2 reads the metadata fields for reproducibility.
4. **M-V4-2 — De-singleton `Logger`.** 1-2 hours. Replaces the validation-gate-via-global-state with local data; future-proofs against per-run state confusion.
5. **M-V4-1 — `PointEntry` → sealed hierarchy.** 4-6 hours. Most valuable structural change before stage 2 starts consuming `points`. Bundle m-V4-2 (`AngleType`/`LotType` enums) and m-V4-3 (typed map keys) into the same change-set since they enable each other.
6. **M-V4-4 — Decide doctrine-shell shape.** Scope-decision, not effort. Make the call before writing the first real `describe(...)` body.
7. The remaining minors and nits — opportunistically, when touching the file.
