# Stage 1 — Code Audit V5 (Improvement Comparison vs V4)

Re-running [audit.md](audit.md) against the source as of working-tree HEAD (`2788cc7 add auditing v3` plus uncommitted refactor pass that converts `BaseCalculator` to an interface, drops unused `CalculationSetting` fields, removes `Sect.UNKNOWN`, and updates `AGENTS.md`). Per audit.md, previous reports are read as history. Numerical fixture correctness is assumed; the audit focuses on architectural integrity, type modeling, edge-case robustness, and current-bug risk.

Build verified: `mvn compile` clean, `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"` succeeds.

---

```text
Current assessment: Stage 1 is in a healthy place. The V4 backlog was substantially reduced — five items (the `BaseCalculator` mutable inheritance state, the `CalculationSetting` lying-metadata fields, `Sect.UNKNOWN`, the `MoonPhase.waxing` 180° boundary, and the implicit Mercury-required contract) all shipped, and `AGENTS.md` was updated to capture the new state. With the calculator refactor to a pure interface, calculators are now stateless by construction — the V3 N2 / V4 m-V4-8 latent reentrancy concern is closed structurally, not by convention.

The Logger singleton has been explicitly classified as a deferred migration item pending Spring Boot adoption, in line with `AGENTS.md`'s new technical-debt note. Per audit.md §5, that re-classifies the V4 Logger finding from Major to roadmap/migration guidance unless a concrete current bug is shown — and none is. The remaining structurally-load-bearing items are PointEntry's nullable-everything shape and the still-untyped `Map<String, …>` keys for points/angles/lots.

Highest-priority next action: Replace `PointEntry` with a sealed hierarchy (`PlanetPointEntry` / `AnglePointEntry` / `LotPointEntry` / `SyzygyPointEntry`). This is the highest-value structural change still pending before stage 2 doctrine code starts consuming the points map.
```

---

## What changed since V4

Six items shipped in the working tree:

1. **V4 m-V4-1 — `Sect.UNKNOWN` removed** ([Sect.java](src/main/java/app/model/data/Sect.java)). `SectCalculator.mercurySect` no longer needs the null fallback because `SectCalculator.calculate` now requires Mercury via `requiredPlanet(Planet.MERCURY, ...)` ([SectCalculator.java:21](src/main/java/app/basic/calculator/SectCalculator.java#L21)). The enum is now strictly `{ DIURNAL, NOCTURNAL }`.
2. **V4 m-V4-8 — `BaseCalculator` mutable inheritance state removed** ([BaseCalculator.java](src/main/java/app/basic/BaseCalculator.java)). Now a single-method functional interface; each calculator receives `(BasicChart, BasicCalculationContext)` as method parameters. Calculators are stateless by construction — reentrancy is structural, not conventional.
3. **V4 M-V4-3 — `CalculationSetting` lying-metadata fields dropped** ([CalculationSetting.java](src/main/java/app/model/input/CalculationSetting.java)). `nodeType`, `positionType`, `frame`, `ephemerisVersion` are gone. Future re-introduction will require simultaneous wiring through swisseph.
4. **V4 m-V4-10 — `MoonPhase.waxing` 180° boundary** ([MoonPhaseCalculator.java:22](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L22)). Changed `< 180.0` → `<= 180.0`. Now consistent with the `MoonPhaseName` boundary on the next line for the exact-180° case (full moon, `waxing=true`).
5. **`SectCalculator` requires Mercury** ([SectCalculator.java:21](src/main/java/app/basic/calculator/SectCalculator.java#L21)). The two `if (mercury == null)` paths in V4's `mercurySect` are gone — Mercury is now required at the entry point alongside Sun and Moon. Cleaner; matches the standard planet set.
6. **`AGENTS.md` updated** to reflect the typed `Map<Planet, PlanetSectInfo>` for sect, the stateless calculator note, the altitude-based sect statement, and explicit deferral of the Logger refactor pending Spring Boot.

The V4 fixes (L1 LotCalculator altitude, L3 typed sect map, L7 `swe_azalt` NaN check) remain in place.

---

## CRITICAL

No findings.

No swisseph failure produces plausible fake values; no formula is internally inconsistent; no `basic` code depends on a concrete doctrine implementation.

---

## MAJOR

### M-V5-1. `PointEntry` is a 30-field nullable kitchen-sink rather than a sealed hierarchy — Major — Confidence: High
**Files:** [PointEntry.java:12-72](src/main/java/app/model/basic/PointEntry.java#L12-L72), [PointCalculator.java:21-87](src/main/java/app/basic/calculator/PointCalculator.java#L21-L87)
**Status:** Technical debt (carried from V4 M-V4-1; explicitly acknowledged in `AGENTS.md` as "remaining technical debt: nullable multi-purpose `PointEntry`")

**Problem:**
`PointEntry` carries 30 boxed-nullable fields plus a `PointType` discriminator. The discriminator is structural (it is in the JSON output) but not enforced — `PointEntry.builder(PointType.ANGLE).house(1).julianDay(2451545.0).syzygyType(SyzygyType.NEW_MOON).build()` compiles and serialises with mixed fields. The only thing keeping the JSON shape correct today is the discipline of one author writing one calculator (`PointCalculator`).

**Why it matters:**
Stage 2 doctrine code will start *reading* `points`. The implicit "if `type == PLANET`, the planet fields are non-null and the syzygy fields are null" contract becomes a real public surface the moment a second writer or a second reader touches it. A typo in any descriptive calculator (`entry.getJulianDay()` on a planet entry → `null` → silent NaN propagation) is a class of bug stage 1 cannot catch. Sealed records would make that mistake a compile error.

**Suggested fix:**
1. Sealed `PointEntry` permitting `record PlanetPointEntry`, `record AnglePointEntry`, `record LotPointEntry`, `record SyzygyPointEntry`, each carrying only its real fields.
2. Jackson serialises sealed via `@JsonTypeInfo(use=NAME, property="type") + @JsonSubTypes(...)` — works on records.
3. `PointCalculator` becomes four small `add*` helpers; the builder goes away.
4. Bundle [m-V5-2](#m-v5-2-chartanglename-and-lotpositionname-are-free-form-string--minor--confidence-high) into the same change-set so `AnglePointEntry.angle()` is `AngleType` rather than `String`.

This is not stage-2-blocking, but every doctrine module written before it lands is a callsite that will need to change later.

### M-V5-2. Three doctrine implementations are empty shells — Major — Confidence: Medium
**Files:** [DorotheusDoctrine.java](src/main/java/app/doctrine/impl/dorotheus/DorotheusDoctrine.java), [PtolemyDoctrine.java](src/main/java/app/doctrine/impl/ptolemy/PtolemyDoctrine.java), [ValensDoctrine.java](src/main/java/app/doctrine/impl/valens/ValensDoctrine.java)
**Status:** Roadmap item with shape-drift risk

**Problem:**
Three doctrine classes (~43 lines each) declare `id`, `name`, `houseSystem`, `zodiac`, `terms` as constants and return `new SimpleDescriptiveResult(getId(), Map.of())` from `describe(...)`. Per audit.md §4, this is correctly classified as a roadmap item — descriptive logic is intentionally absent until stage 2.

The risk is not the absence of logic but the *contract example* the three classes encode: `Doctrine implements Doctrine, returns DescriptiveResult from describe(input, chart)`. When stage-2 doctrine differences come into focus (Valens vs. Dorotheus), that contract may need to change — and three doctrines will need to change with it instead of one.

**Why it matters:**
Maintenance risk, not correctness. Compounds with each new doctrine added before stage 2.

**Suggested fix:**
Two acceptable options:
1. Keep `DorotheusDoctrine` as the worked example; delete `PtolemyDoctrine` and `ValensDoctrine` until they have real descriptive logic to ship. Re-add when needed.
2. Leave as-is — explicitly classified as roadmap and visually documents the intended doctrine set.

Either is defensible; the audit recommends option 1 because the cost is small and the risk is real.

---

## MINOR

### m-V5-1. `BasicChart.points` keys are still raw strings — Minor — Confidence: High
**Files:** [PointCalculator.java:47, 55, 66, 84](src/main/java/app/basic/calculator/PointCalculator.java#L47), [BasicChart.java:15](src/main/java/app/model/basic/BasicChart.java#L15)
**Status:** Technical debt (acknowledged in `AGENTS.md`)

`points.put(planet.getPlanet().name(), …)`, `points.put(angle.getName(), …)`, `points.put(lot.getName(), …)`, `points.put("PRENATAL_SYZYGY", …)`. The Map type is `Map<String, PointEntry>`. After M-V5-1 introduces a sealed hierarchy, the natural follow-up is a typed key (`sealed interface PointKey { record PlanetKey(Planet); record AngleKey(AngleType); record LotKey(LotType); record SyzygyKey() {} }`) or four parallel typed maps. Defer until M-V5-1 lands; doing it earlier creates rework.

### m-V5-2. `ChartAngle.name` and `LotPosition.name` are free-form `String` — Minor — Confidence: High
**Files:** [AngleCalculator.java:19-22](src/main/java/app/basic/calculator/AngleCalculator.java#L19-L22), [LotCalculator.java:22-37](src/main/java/app/basic/calculator/LotCalculator.java#L22-L37)
**Status:** Technical debt

`AngleCalculator` writes `"ASCENDANT"`/`"MIDHEAVEN"`/`"DESCENDANT"`/`"IMUM_COELI"`. `LotCalculator.angle("ASCENDANT")` does string-equals lookup at [line 33](src/main/java/app/basic/calculator/LotCalculator.java#L33). A typo on the lookup side is a silent null and a NullPointerException further down. The set is closed and small. Replace with `enum AngleType { ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI }` and `enum LotType { FORTUNE, SPIRIT }`. Couple of hours; bundle with M-V5-1.

### m-V5-3. Calculator order is implicit and load-bearing — Minor — Confidence: High
**Files:** [BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)
**Status:** Technical debt

Eleven calculators run in a hand-crafted order; each step reads what the prior step wrote (`Sect` reads planets; `Lot` reads planets+angles; `Point` reads planets+angles+lots+syzygy). The dependency graph is invisible. The interface refactor made calculators stateless but did not make the data flow declarative. Lightweight mitigation: a one-line javadoc per calculator naming reads/writes. Heavier: a `record CalculatorStep(name, requires, produces)` model. The audit recommends the comment.

### m-V5-4. Rounding at calculation time, not serialization — Minor — Confidence: High
**Files:** [BasicCalculationContext.java:84-89](src/main/java/app/basic/BasicCalculationContext.java#L84-L89), all callers in `app.basic.calculator.*`
**Status:** Technical debt with latent failure mode (acknowledged in `AGENTS.md`)

`ctx.round(...)` is called on intermediate longitudes before they feed `houseOf` / `signDistance` / `pairwiseRelation`. At `DECIMAL_6` this is harmless. At a coarser policy it would corrupt aspect calculations near orb boundaries. The fix is to round at serialization time only — defer until a coarser policy is actually requested.

### m-V5-5. Syzygy heuristics — Minor — Confidence: Medium
**Files:** [SyzygyCalculator.java:31-69](src/main/java/app/basic/calculator/SyzygyCalculator.java#L31-L69)
**Status:** Technical debt (acknowledged in `AGENTS.md`)

Three soft spots, none currently failing on the Lille fixture:
- 30-day search window assumes a syzygy occurs within a lunar cycle. Always true; reasonable.
- 0.25-day step; sign-flip detection requires `|earlierValue - laterValue| < 90.0`. Robust enough; the 360°/0° wrap is handled by `syzygySignedDelta`'s `> 180.0 → -360` reflection.
- 50 bisection iterations on a 0.25-day bracket converges far past Swiss Ephemeris precision. Cap at 30 or use a tolerance. Cosmetic.

Move from "improve all three" to "leave alone unless a fixture fails."

### m-V5-6. `BasicChart` carries Jackson `@JsonIgnore` and dual public surface — Minor — Confidence: High
**Files:** [BasicChart.java:77, 94, 103, 112, 121, 162, 171](src/main/java/app/model/basic/BasicChart.java)
**Status:** Technical debt (acknowledged in `AGENTS.md`)

7 `@JsonIgnore` fields (planets, angles, three raw matrices, syzygy, lots) sit beside 8 serialised fields. The internal-vs-output distinction is real (planets/angles/lots are also surfaced via `points`) but expressing it through annotations on the domain model means a future reader cannot tell which fields are "for downstream calculators" and which are "for JSON" without reading every getter. Extract a `BasicChartReport` DTO when stage 2 writes its first descriptive output; for now, the cost-benefit isn't compelling.

### m-V5-7. `Logger.instance` global singleton — Minor — Confidence: High
**Files:** [Logger.java:8-40](src/main/java/app/output/Logger.java#L8-L40), [LoggerWriter.java:14](src/main/java/app/output/LoggerWriter.java#L14), [InputLoader.java:18](src/main/java/app/input/InputLoader.java#L18)
**Status:** Roadmap / migration guidance

`AGENTS.md` now explicitly states: *"`Logger.instance` is intentionally retained for the short-term CLI. The project may move to Spring Boot soon; when that happens, prefer injecting/request-scoping the logger instead of doing an interim de-singleton refactor now."*

Per audit.md §5, this re-classifies the V4 Major finding to migration guidance unless a current bug is shown. None is — the CLI runs once per JVM and the singleton's `entries` list is appended sequentially. The two latent failure modes (per-run state confusion across multiple charts in one JVM; `Logger.instance.hasErrors()` as a cross-cutting validation gate flipping on doctrine warnings) remain real but are now deliberate trade-offs against doing two refactors when one Spring-Boot-adoption refactor is planned.

When Spring adoption begins:
- Make `Logger` a request-scoped `@Component` injected through the calculator pipeline.
- Replace `InputLoader.load` returning a flag with returning a `LoadResult { bundle, errors }`.
- `LoggerWriter` writes the instance it was given, not a static field.

Until then, no change recommended.

### m-V5-8. `App.main throws Exception` — Minor — Confidence: High
**Files:** [App.java:21](src/main/java/app/App.java#L21)
**Status:** Technical debt

Cosmetic. `throws Exception` should be `throws IOException`.

---

## NIT

- **`PlanetPosition.getRetrograde()` should be `isRetrograde()`** ([PlanetPosition.java:70](src/main/java/app/model/basic/PlanetPosition.java#L70)). Boxed `Boolean`-returning getter named `getRetrograde`; convention mismatch. Same nit as V3/V4.
- **Oriental/occidental convention is undocumented** ([SolarPhaseCalculator.java:32-34](src/main/java/app/basic/calculator/SolarPhaseCalculator.java#L32-L34), [SectCalculator.java:64](src/main/java/app/basic/calculator/SectCalculator.java#L64)). `delta > 180 → ORIENTAL` is the standard Hellenistic convention (planet rising before the Sun = oriental); a one-line comment would help future maintainers.
- **`SectCalculator.mercurySect(...)` takes `ctx` only for `normalize`** ([SectCalculator.java:73-76](src/main/java/app/basic/calculator/SectCalculator.java#L73-L76)). Could call a static `Longitudes.normalize` helper if one existed. Linked to the `TraditionalTables.signOf/degreeInSign/normalize` re-implementation noted below.
- **`TraditionalTables.signOf/degreeInSign/normalize` reimplemented** ([TraditionalTables.java:92-103](src/main/java/app/basic/TraditionalTables.java#L92-L103)). A `Longitudes` utility class shared by `BasicCalculationContext` and `TraditionalTables` would dedup. Cosmetic.
- **Magic numbers** still inline: `15.0` ([SimpleCalculator.java:25](src/main/java/app/basic/calculator/SimpleCalculator.java#L25)), `1_000_000.0` ([BasicCalculationContext.java:86](src/main/java/app/basic/BasicCalculationContext.java#L86)), `2440587.5` and `86400.0` ([SyzygyCalculator.java:74](src/main/java/app/basic/calculator/SyzygyCalculator.java#L74)).
- **`LogEntry` lacks a timestamp** ([LogEntry.java](src/main/java/app/output/LogEntry.java)). Trivial add.
- **`Logger` has no `WARN` level**. The "Skipping unknown doctrine" message in `DoctrineLoader` ([DoctrineLoader.java:40](src/main/java/app/input/DoctrineLoader.java#L40)) is logged as `ERROR` and triggers the `InputLoader.hasErrors()` validation gate — debatable if any *other* requested doctrine is valid. Linked to m-V5-7; keep the call shape but distinguish severity.

---

## Areas that are clean

- **Calculator state.** Calculators are stateless single-method implementations of the `BaseCalculator` interface; reentrancy is structural.
- **Sect.** Altitude-based, with horizon tiebreak resolved to DIURNAL ([SectCalculator.java:24-26](src/main/java/app/basic/calculator/SectCalculator.java#L24-L26)). Lots use the same expression ([LotCalculator.java:21](src/main/java/app/basic/calculator/LotCalculator.java#L21)). No internal sect divergence remaining. Mercury's sect is no longer a special-cased nullable path.
- **Swiss Ephemeris error policy.** Houses, obliquity, planets, declination, longitude, and `swe_azalt` all throw on `result < 0` or `NaN`. Uniform.
- **Input validation.** `Subject` constructor validates ranges; date is ISO-only; time requires `HH:mm:ss`; `HouseSystem`/`Terms`/`Zodiac` parse-or-throw; no `UNKNOWN` enum values reachable from input.
- **Sect typing.** `BasicSect` POJO with enum-typed fields; `planetSects` is `Map<Planet, PlanetSectInfo>`; `TriplicityRulers` is a typed record.
- **CalculationSetting.** No longer advertises configurability the engine doesn't deliver.
- **`Sect` enum** is now exhaustive: `{ DIURNAL, NOCTURNAL }`. Switch statements over it are compile-checked.
- **`isTraditionalPlanet`** uses an exhaustive switch on `Planet` — adding a planet to the enum is a compile error here.
- **House recomputation** is once-and-cached.
- **`basic` does not depend on doctrine implementations** (verified by walking imports in `app.basic.*`).

---

## Suggested implementation order

1. **M-V5-1 + m-V5-2 + m-V5-1 bundled** — `PointEntry` sealed hierarchy + `AngleType`/`LotType` enums + typed `points` keys. Single change-set, ~1 day. Highest-value structural change before stage 2.
2. **M-V5-2** — Decide whether to collapse `PtolemyDoctrine`/`ValensDoctrine` to one example or keep all three. Scope decision; <1 hour either way.
3. **m-V5-3** — Add a one-line javadoc per calculator describing reads/writes. <1 hour.
4. **m-V5-8 + nits** — Opportunistic, when touching the file.
5. **m-V5-7 (Logger refactor)** — defer until Spring Boot adoption work begins, per `AGENTS.md`.
6. **m-V5-4 (rounding) / m-V5-5 (syzygy heuristics) / m-V5-6 (Jackson on domain)** — defer until a fixture or stage-2 requirement forces the issue.
