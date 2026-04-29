# Stage 1 — Adversarial Code Audit V3 (Improvement Comparison vs V2)

Re-running [audit.md](audit.md) against the source after commit `6f734f1 — Address stage 1 audit corrections`. Each V2 finding is restated as **FIXED**, **PARTIAL**, **NOT FIXED**, or **REGRESSED**. New issues introduced since V2 are listed in the final section. Numerical correctness is taken as given.

---

## Headline

The author shipped a focused round of corrections that retired most of V2's input-side and Swiss-Ephemeris-error issues and addressed two of the three highest-priority items on the V2 backlog:

- **C5 sect — FIXED.** [SectCalculator.java:20-24](src/main/java/app/basic/calculator/SectCalculator.java#L20-L24) now derives `diurnal` from `horizontalAltitude` via `swe_azalt`, with `>= 0` resolving the horizon-tiebreak in favor of DIURNAL. The chart's sect is no longer coupled to whole-sign houses.
- **C7 / M6 — PARTIAL (genuine progress).** The published Map is now `Map<String, PointEntry>` ([PointEntry.java:12](src/main/java/app/model/basic/PointEntry.java#L12)), and sect serialises through typed `BasicSect`/`PlanetSectInfo`/`TriplicityRulers`. Field renames on `PointEntry` will now break callers at compile time. **What did not happen:** the audit-§2 sealed hierarchy (`Planet`, `Angle`, `Lot`, `SyzygyPoint`). PointEntry is a 30-field kitchen-sink with nullable everything, and the *keys* of the Map are still raw strings.
- **Boundary fixes — FIXED.** `Subject` constructor validates ([Subject.java:13-40](src/main/java/app/model/input/Subject.java#L13-L40)); date is ISO-only ([SubjectListParser.java:77](src/main/java/app/input/SubjectListParser.java#L77)); time requires `HH:mm:ss` regex ([SubjectListParser.java:91-94](src/main/java/app/input/SubjectListParser.java#L91-L94)); `HouseSystem.UNKNOWN` is gone ([HouseSystem.java](src/main/java/app/model/data/HouseSystem.java)); `Terms.parse` and `Zodiac.parse` now throw on unknown values ([Terms.java:17](src/main/java/app/model/data/Terms.java#L17), [Zodiac.java:14](src/main/java/app/model/data/Zodiac.java#L14)).
- **Swiss Ephemeris failures — FIXED uniformly.** Houses ([BasicCalculationContext.java:36-40](src/main/java/app/basic/BasicCalculationContext.java#L36-L40)) and obliquity ([SimpleCalculator.java:15-18](src/main/java/app/basic/calculator/SimpleCalculator.java#L15-L18)) now throw on failure, matching planets. The mixed log-vs-throw policy (V2 N3/N4) is resolved.
- **Syzygy not-found — FIXED.** [SyzygyCalculator.java:43-44](src/main/java/app/basic/calculator/SyzygyCalculator.java#L43-L44) now throws instead of silently returning `birthJulianDay - step`.

What has **not** moved: the `Logger` singleton (C2/N7), the three empty doctrine classes (M3), the fact that `BasicCalculator` is constructed once in `App.main` (C3), `CalculationSetting` advertising configurability that is never wired (M4), `InputLoader` using `Logger.instance.hasErrors()` as its only validation gate (M15), Jackson `@JsonIgnore` on the domain (M13), rounding at calculation time (m19), per-call calculator allocation (N5), mutable inheritance state in `BaseCalculator` (N2), the syzygy 30-day-window/50-iteration/sign-flip heuristic (C6 minor), and most of the m1–m20 backlog.

One **regression** introduced by the partial C5 fix: `LotCalculator` still uses the *old* house-number-based sect logic, so the Lot of Fortune/Spirit can disagree with the chart's altitude-based `BasicSect.sect` at extreme latitudes. This is a divergence the V2 fix did not propagate.

---

## CRITICAL — V2 → V3

### C1. South Node speed/retrograde — **STILL FIXED**
[PlanetCalculator.java:33-35](src/main/java/app/basic/calculator/PlanetCalculator.java#L33-L35) — unchanged from V2.

### C2. `Logger` global singleton, no thread safety — **NOT FIXED**
[Logger.java:10-13](src/main/java/app/output/Logger.java#L10-L13) — still `public static final Logger instance = new Logger();` over an unsynchronized `ArrayList`, no `clear()`, no per-run scope. [LoggerWriter.java:14](src/main/java/app/output/LoggerWriter.java#L14) still serialises `Logger.instance` reflectively. [InputLoader.java:18](src/main/java/app/input/InputLoader.java#L18) still uses it as the cross-cutting validation gate. Every concrete failure path V2 named is still wired in.

### C3. `BasicCalculator`/`SwissEph` not thread-safe, undocumented — **NOT FIXED**
[App.java:24](src/main/java/app/App.java#L24) constructs the calculator once and reuses it. `BasicCalculationContext.swissEph` is now per-call ([BasicCalculationContext.java:20](src/main/java/app/basic/BasicCalculationContext.java#L20)) — a quiet improvement that V2 already noted — but no `@NotThreadSafe` annotation, no Javadoc, nothing in `BasicCalculator` warning a future maintainer that wrapping it in a Spring `@Bean` is unsafe. Same comment as V2.

### C4. Moon-phase boundary cascade — **STILL FIXED (lingering nit)**
[MoonPhaseCalculator.java:28-45](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L28-L45) — boundaries epsilon-bracketed at 0° and 180°. The `waxing` flag at exactly 180° is still `<` not `<=` ([line 20](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L20)) — V2 nit, unchanged. Trivial.

### C5. Sect by house number, not altitude — **FIXED**
[SectCalculator.java:20-24](src/main/java/app/basic/calculator/SectCalculator.java#L20-L24)
```java
double sunAltitude = ctx.round(ctx.horizontalAltitude(sun.getLongitude(), sun.getLatitude()));
double moonAltitude = ctx.round(ctx.horizontalAltitude(moon.getLongitude(), moon.getLatitude()));
boolean sunAboveHorizon = sunAltitude >= 0.0;
boolean moonAboveHorizon = moonAltitude >= 0.0;
boolean diurnal = sunAboveHorizon;
```
Altitude is computed via `swe_azalt` ([BasicCalculationContext.java:139-145](src/main/java/app/basic/BasicCalculationContext.java#L139-L145)). The `>= 0.0` resolves the "Sun exactly on the horizon" tiebreak in favor of DIURNAL, matching the convention V1 called for. SectCalculator now throws if Sun or Moon are missing instead of silently going nocturnal (resolves V2 m14 for the chart's primary sect).

**Lingering nit:** `horizontalAltitude` does not check the `swe_azalt` return code or NaN — and `swe_azalt` returns `void`-ish (no useful status). If swisseph is misconfigured, you'll get whatever's in `horizontalCoordinates[1]`, possibly `0.0`. Minor, but inconsistent with the new "throw on failure" policy elsewhere.

**Critical regression of partial fix → see L1 below.**

### C6. Prenatal syzygy search — **PARTIAL (one improvement, one regression-of-attention)**
[SyzygyCalculator.java:31-69](src/main/java/app/basic/calculator/SyzygyCalculator.java#L31-L69)
- **N8 / V2 silent fallback — FIXED.** `previousSyzygyCandidate` now throws when no syzygy is found in 30 days ([line 43-44](src/main/java/app/basic/calculator/SyzygyCalculator.java#L43-L44)) instead of returning a fake `birthJulianDay - step`.
- **m6 — FIXED.** `earlierValue == 0.0` is now `Math.abs(earlierValue) < 1e-9` ([line 37](src/main/java/app/basic/calculator/SyzygyCalculator.java#L37)).
- **Records — partial m4.** Internal `SyzygyCandidate` is a record ([line 14](src/main/java/app/basic/calculator/SyzygyCalculator.java#L14)). Good; the user-facing carrier `BasicSyzygy` is still a POJO with explicit constructor and getters.
- **NOT FIXED:** still 30-day window (line 35), still 50 bisection iterations (line 51), still the `Math.abs(earlierValue - laterValue) < 90.0` heuristic for sign-flip (line 37), still two `swe_calc_ut` calls per delta evaluation (line 64-66), still no fallback test for the 360°-wrap edge case at conjunction. The bisection at 50 iterations on a 0.25-day bracket is still ~10× the precision needed.

### C7. `BasicChart.points` is `Map<String, Map<String, Object>>` — **PARTIAL**
[BasicChart.java:15, 69-75](src/main/java/app/model/basic/BasicChart.java#L15), [PointEntry.java](src/main/java/app/model/basic/PointEntry.java), [PointCalculator.java](src/main/java/app/basic/calculator/PointCalculator.java)

The big shape change V2 demanded landed: `Map<String, Map<String, Object>>` is now `Map<String, PointEntry>`. PointEntry is `@JsonInclude(NON_NULL)`, so absent fields don't appear. Builder API; `PointType` discriminator. Renaming a field on `PlanetPosition` no longer silently breaks JSON; renaming a getter on `PointEntry` *will* break compile.

**This is a substantial improvement over V2.** What it didn't do — and what audit §2 explicitly asked for — is the sealed hierarchy:

> Are points modeled with a sealed hierarchy (`Planet`, `Angle`, `Lot`, `SyzygyPoint`) or with a single class that has nullable fields for everything? A planet does not have the same shape as an angle — does the code model that, or does it pretend they are the same and use null as a sentinel?

PointEntry has 30 fields, almost all `Double`/`Boolean`/`Integer` boxed nullables. An angle entry has fields for `triplicityRulers`, `syzygyType`, `julianDay` etc. that are simply `null`. Building a malformed entry (e.g. `PointEntry.builder(PointType.ANGLE).house(1).julianDay(2451545.0).build()`) is allowed. The JSON serialises whatever the builder set, which means the *type discriminator no longer constrains the shape*. Sealed `record PlanetPoint`, `AngleEntry`, `LotEntry`, `SyzygyPointEntry` would.

Map *keys* are also still untyped strings:
- [PointCalculator.java:45](src/main/java/app/basic/calculator/PointCalculator.java#L45) `points.put(planet.getPlanet().name(), ...)`
- [PointCalculator.java:53](src/main/java/app/basic/calculator/PointCalculator.java#L53) `points.put(angle.getName(), ...)` — and `angle.getName()` is itself a free-form String.
- [PointCalculator.java:64](src/main/java/app/basic/calculator/PointCalculator.java#L64) `points.put(lot.getName(), ...)` — same.
- [PointCalculator.java:82](src/main/java/app/basic/calculator/PointCalculator.java#L82) `"PRENATAL_SYZYGY"` literal.

Verdict: V2's "next move is one calculator's worth of work" is half done. The remainder (sealed hierarchy + typed keys) is still pending.

---

## MAJOR — V2 → V3

### M1. `BasicCalculator` god class — **STILL FIXED**
[BasicCalculator.java](src/main/java/app/basic/BasicCalculator.java) — 43 lines, unchanged.

### M2. Lookup tables duplicated, dead `traditional/` package — **STILL FIXED**
[TraditionalTables.java](src/main/java/app/basic/TraditionalTables.java) — V2's nit about `signOf`/`degreeInSign`/`normalize` being reimplemented as private statics is **unchanged** (lines 92-103). Still re-implements what `BasicCalculationContext` already has. Move them to a `Longitudes` utility class shared by both, or expose them statically on the context.

### M3. Doctrine implementations are empty shells — **NOT FIXED**
[DorotheusDoctrine.java](src/main/java/app/doctrine/impl/dorotheus/DorotheusDoctrine.java), [PtolemyDoctrine.java](src/main/java/app/doctrine/impl/ptolemy/PtolemyDoctrine.java), [ValensDoctrine.java](src/main/java/app/doctrine/impl/valens/ValensDoctrine.java) — three classes, ~43 lines each, `describe(...)` returns `Map.of()`. Still YAGNI ceremony. V2 fix-vector unchanged: collapse to a single `Doctrine` enum or record.

### M4. `CalculationSetting` advertises configurability never wired — **NOT FIXED**
[CalculationSetting.java:25-29](src/main/java/app/model/input/CalculationSetting.java#L25-L29) — constructor still hardcodes `nodeType = MEAN`, `positionType = APPARENT`, `frame = GEOCENTRIC`, `ephemerisVersion = "swisseph-2.10.03"`. No setter, no parser. `SE_MEAN_NODE` is still hardcoded at [PlanetCalculator.java:29](src/main/java/app/basic/calculator/PlanetCalculator.java#L29). The metadata still lies if someone changes it.

### M5. `HouseSystem.UNKNOWN` and silent enum-parse fallbacks — **FIXED**
- [HouseSystem.java](src/main/java/app/model/data/HouseSystem.java): `UNKNOWN` removed; only `WHOLE_SIGN`, `PLACIDUS`. The switch in [BasicCalculationContext.java:167-172](src/main/java/app/basic/BasicCalculationContext.java#L167-L172) is now exhaustive on real values, so adding `REGIOMONTANUS` would cause a compile error — exactly the safety property V2 wanted.
- [Terms.java:14-18](src/main/java/app/model/data/Terms.java#L14-L18): `default -> throw new IllegalArgumentException(...)`. Also accepts `"PTOLEMY"` as an alias for `PTOLEMAIC`.
- [Zodiac.java:11-15](src/main/java/app/model/data/Zodiac.java#L11-L15): same — throws on unknown.

### M6. Sect/dignities use `Map<String, Object>` — **PARTIAL**
- Sect: `BasicSect` is now a typed POJO with all enum-typed fields ([BasicSect.java](src/main/java/app/model/basic/BasicSect.java)), and per-planet info is `PlanetSectInfo` ([PlanetSectInfo.java](src/main/java/app/model/basic/PlanetSectInfo.java)). The container `planetSects` is still `Map<String, PlanetSectInfo>` keyed by `Planet.name()` — should be `Map<Planet, PlanetSectInfo>`.
- Dignities: now typed via `PointEntry.domicileRuler/exaltationRuler/...`, with `TriplicityRulers` as a record. Big improvement over V2's `Map<String, Object>` triplicity values.

### M7. `Subject` constructor validation — **FIXED**
[Subject.java:13-40](src/main/java/app/model/input/Subject.java#L13-L40) — validates id non-blank, dates non-null, finite numbers, lat∈[-90,90], lon∈[-180,180]. Direct construction is now safe. (No polar-circle warning still — but that wasn't a hard requirement.)

### M8. Date `dd/MM/yyyy` ambiguity — **FIXED**
[SubjectListParser.java:76-81](src/main/java/app/input/SubjectListParser.java#L76-L81) — only ISO `LocalDate.parse(value)` (i.e. `yyyy-MM-dd`). The European fallback is gone.

### M9. Time parsing length heuristic — **FIXED**
[SubjectListParser.java:91-94](src/main/java/app/input/SubjectListParser.java#L91-L94) — explicit regex `\d{2}:\d{2}:\d{2}` with a thrown `DateTimeParseException` on mismatch, caught by the surrounding try/catch.

### M10. `swe_calc_ut` errors return zeros — **FIXED**
[PlanetCalculator.java:52-55, 66-69](src/main/java/app/basic/calculator/PlanetCalculator.java#L52-L55), [BasicCalculationContext.java:108-114](src/main/java/app/basic/BasicCalculationContext.java#L108-L114) — throw. **And now [SimpleCalculator.java:15-18](src/main/java/app/basic/calculator/SimpleCalculator.java#L15-L18) (obliquity) throws too** (V2 N4). **And [BasicCalculationContext.java:36-40](src/main/java/app/basic/BasicCalculationContext.java#L36-L40) (houses) throws** (V2 N3). The error policy is now uniform: any swisseph failure → IllegalArgumentException → halts the chart. Logger still records the message before throwing, so the run-logger.json captures the cause.

**One outlier:** [BasicCalculationContext.horizontalAltitude](src/main/java/app/basic/BasicCalculationContext.java#L139-L145) does not check anything — `swe_azalt`'s return is ignored. Tiny inconsistency.

### M11. House-system switch silent fallback — **FIXED (consequence of M5)**
Switch at [BasicCalculationContext.java:167-172](src/main/java/app/basic/BasicCalculationContext.java#L167-L172) now `case PLACIDUS -> 'P'; case WHOLE_SIGN -> 'W';` and the enum has only those two values. Compile-checked exhaustiveness.

### M12. Houses recomputed 4× — **STILL FIXED**
[BasicCalculationContext.java:36](src/main/java/app/basic/BasicCalculationContext.java#L36) — once-and-cached, unchanged from V2.

### M13. Jackson annotations on domain models — **NOT FIXED**
[BasicChart.java](src/main/java/app/model/basic/BasicChart.java) still has six `@JsonIgnore` (planets, angles, three raw matrices, syzygy, lots — actually seven). [DescriptiveAstrologyReport.java](src/main/java/app/output/DescriptiveAstrologyReport.java) still annotated. [PointEntry](src/main/java/app/model/basic/PointEntry.java) and [PlanetSectInfo](src/main/java/app/model/basic/PlanetSectInfo.java) carry `@JsonInclude` directly on the domain. Same DTO-vs-domain leak as V2.

### M14. `JsonReportWriter` Jackson defaults — **PARTIAL (unchanged from V2)**
[JsonReportWriter.java:15-20](src/main/java/app/output/JsonReportWriter.java#L15-L20) — `JavaTimeModule`, `Jdk8Module`, `WRITE_DATES_AS_TIMESTAMPS=false`. Still missing `FAIL_ON_UNKNOWN_PROPERTIES` (writer-only, low impact) and a global `@JsonInclude(NON_NULL)`. Currently `NON_NULL` is opt-in per-class.

### M15. `Logger.instance.hasErrors()` is the validation gate — **NOT FIXED**
[InputLoader.java:18-20](src/main/java/app/input/InputLoader.java#L18-L20) — identical to V2. Same race-with-singleton concerns.

---

## MINOR / NIT — V2 → V3

| V2 ID | V3 status | Notes |
|---|---|---|
| m1 magic numbers | NOT FIXED | `15.0` ([SimpleCalculator.java:23](src/main/java/app/basic/calculator/SimpleCalculator.java#L23)), mean speeds inline ([PlanetCalculator.java:75-84](src/main/java/app/basic/calculator/PlanetCalculator.java#L75-L84)), `1_000_000.0` ([BasicCalculationContext.java:86](src/main/java/app/basic/BasicCalculationContext.java#L86)), `2440587.5` & `86400.0` ([SyzygyCalculator.java:72](src/main/java/app/basic/calculator/SyzygyCalculator.java#L72)). |
| m2 strings → enums | MOSTLY FIXED (unchanged) | Lingering: `ChartAngle.name`, `LotPosition.name`, `Map<String, …>` keys throughout. |
| m3 `Optional` | NOT FIXED | `exaltationRuler` returns nullable `Planet` ([TraditionalTables.java:42](src/main/java/app/basic/TraditionalTables.java#L42)); `termRuler` returns null for `Terms.NONE` ([line 79](src/main/java/app/basic/TraditionalTables.java#L79)); `BasicCalculationContext.planet(...)` returns null ([line 81](src/main/java/app/basic/BasicCalculationContext.java#L81)). |
| m4 records | PARTIAL | New: `SyzygyCandidate`, `TriplicityRulers`, `TermBoundary`. Still POJOs: `PlanetPosition`, `BasicSyzygy`, `BasicSect`, `PlanetSectInfo`, `PointEntry` (builder), `ChartAngle`, `LotPosition`, `MoonPhase`, `HousePosition`, `LogEntry`, `RawAspect/Declination/SignDistanceMatrixEntry`, `PairwiseRelation`. |
| m5 `LinkedHashMap` everywhere | PARTIAL | Sect's outer container is now typed (`BasicSect`); `PointCalculator` and `SectCalculator.planetSects` still build `LinkedHashMap`s. Better, not gone. |
| m6 `==` on doubles | FIXED | [SyzygyCalculator.java:37](src/main/java/app/basic/calculator/SyzygyCalculator.java#L37) — replaced with `Math.abs(...) < 1e-9`. |
| m7 `degreeInSign` re-normalises input | NOT FIXED, benign | Unchanged; both copies (`BasicCalculationContext`, `TraditionalTables`) call `normalize` first. |
| m8 `signDistance` duplicated | NOT FIXED | Still a private method on [ChartPointCalculator.java:39-42](src/main/java/app/basic/calculator/ChartPointCalculator.java#L39-L42). Move onto `ZodiacSign`. |
| m9 oriental/occidental no doc | NOT FIXED | [SolarPhaseCalculator.java:30-31](src/main/java/app/basic/calculator/SolarPhaseCalculator.java#L30-L31) and [SectCalculator.java:62, 76](src/main/java/app/basic/calculator/SectCalculator.java#L62) — no comment explains the `delta > 180` convention. |
| m10 LogEntry no timestamp | NOT FIXED | [LogEntry.java](src/main/java/app/output/LogEntry.java) — still only level/scope/message. |
| m11 Logger no WARN | NOT FIXED | Still `info`/`error` only ([Logger.java:25-35](src/main/java/app/output/Logger.java#L25-L35)). |
| m12 hard-coded paths | NOT FIXED | Unchanged. |
| m13 method length | STILL FIXED | All calculator methods <70 lines. |
| m14 sect with missing Sun | PARTIAL | `SectCalculator.requiredPlanet` ([line 42-48](src/main/java/app/basic/calculator/SectCalculator.java#L42-L48)) **throws** if Sun or Moon are missing — better than V2's silent NOCTURNAL. The Mercury sect still null-checks ([line 72-74](src/main/java/app/basic/calculator/SectCalculator.java#L72-L74)) and returns `Sect.UNKNOWN`, but since SectCalculator already required Sun, the `sun == null` branch is unreachable; the `mercury == null` branch handles a planet that may legitimately not be requested. |
| m15 BasicSyzygy enum discriminator | STILL FIXED | [BasicSyzygy.java:35](src/main/java/app/model/basic/BasicSyzygy.java#L35). |
| m16 `App.main throws Exception` | NOT FIXED | [App.java:21](src/main/java/app/App.java#L21). |
| m17 deleted spec/lord/profections | INFO | Same as V2 — deliberate stage-1 narrowing. |
| m18 lookup tables not testable | STILL FIXED | `TraditionalTables` exposes static methods. |
| m19 rounding at calculation time | NOT FIXED | [BasicCalculationContext.round](src/main/java/app/basic/BasicCalculationContext.java#L84-L89). Same architectural defect as V1/V2: `PlanetCalculator` line 33-35 rounds longitude before passing it to `houseOf`/`signDistance`/`pairwiseRelation`. Currently safe at DECIMAL_6; a finer rounding policy would corrupt aspect calculations. |
| m20 `InputListBundle` mutable bag | NOT FIXED | [InputListBundle.java](src/main/java/app/model/input/InputListBundle.java) — five setters, two-stage construction. |
| m21 Logger race | NOT FIXED | Same root cause as C2. |
| n1 `getRetrograde` not `isRetrograde` | NOT FIXED | [PlanetPosition.java:70](src/main/java/app/model/basic/PlanetPosition.java#L70). |

---

## V2 NEW issues — V3 status

| V2 ID | V3 status | Notes |
|---|---|---|
| N1 PointCalculator drift risk | PARTIAL | The Map values are typed (`PointEntry`); the keys still aren't, and `PointEntry`'s 30 nullable fields mean a misuse of the wrong field per-`PointType` is silent. Snapshot tests are still warranted. |
| N2 BaseCalculator mutable inheritance | NOT FIXED | [BaseCalculator.java:7-14](src/main/java/app/basic/BaseCalculator.java#L7-L14) — identical. |
| N3 House calc failure not aborting | FIXED | Now throws ([BasicCalculationContext.java:37-40](src/main/java/app/basic/BasicCalculationContext.java#L37-L40)). |
| N4 Obliquity failure not aborting | FIXED | Now throws ([SimpleCalculator.java:15-18](src/main/java/app/basic/calculator/SimpleCalculator.java#L15-L18)). |
| N5 Per-call calculator allocation | NOT FIXED | [BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35) — eleven `new` per chart. |
| N6 TriplicityRulers as Map | FIXED | [TriplicityRulers.java](src/main/java/app/model/basic/TriplicityRulers.java) is now a typed record. Significant readability win. |
| N7 LoggerWriter serialises singleton | NOT FIXED | [LoggerWriter.java:14](src/main/java/app/output/LoggerWriter.java#L14). |
| N8 Syzygy silent fake-value on miss | FIXED | Throws now. |
| N9 More `@JsonIgnore`'d fields | NOT FIXED | [BasicChart.java](src/main/java/app/model/basic/BasicChart.java) still has 7 `@JsonIgnore` (planets, angles, raw aspect matrix, raw declination matrix, raw sign-distance matrix, syzygy, lots). |

---

## NEW issues introduced since V2

### L1. **`LotCalculator` still uses house-number sect, contradicting `BasicSect`** ★
[LotCalculator.java:19](src/main/java/app/basic/calculator/LotCalculator.java#L19)
```java
boolean diurnal = sun.getHouse() >= 7 && sun.getHouse() <= 12;
```
The C5 fix landed in `SectCalculator` only. `LotCalculator` still computes sect by house number — the very anti-pattern V1/V2 attacked. Concrete failure mode:

- At a polar latitude where Sun has altitude > 0 but is in (whole-sign) house 6, `BasicSect.sect = DIURNAL` but Lot of Fortune is computed with the *nocturnal* formula (`asc + sun − moon`). Result: the chart's published sect disagrees with its lots.
- At an arctic-circle birth where Sun is circumpolar in house 12, both produce DIURNAL by accident — but for unrelated reasons.
- More commonly: under the Placidus path, when houseSystem becomes Placidus and `sun.getHouse()` is ambiguous (cusp boundary), the lots can flip sect while the chart's sect stays stable.

This is a **MAJOR** issue: it is a correctness regression of the C5 fix's *intent*. Either copy the altitude check (or read `BasicSect.getSect()` after `SectCalculator` runs — but the calculator order means `SectCalculator` runs *after* `LotCalculator`, so today the Lot side cannot read the chart's sect even if it wanted to).

**Fix:** reorder so `SectCalculator` runs before `LotCalculator`, or pull the altitude computation into a shared helper on `BasicCalculationContext` and call it from both. The first is one line in [BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35).

### L2. `PointEntry` is a 30-field kitchen sink, not a sealed hierarchy
[PointEntry.java:12-72](src/main/java/app/model/basic/PointEntry.java#L12-L72)

V3 chose the simplest possible typing of C7: one nullable-everything class. Audit §2 explicitly called for sealed `Planet`, `Angle`, `Lot`, `SyzygyPoint`. Risks:

- A future maintainer adding a syzygy field to a planet entry compiles fine.
- `PointEntry.builder(PointType.ANGLE).house(1).julianDay(2451545.0).build()` succeeds and serialises both fields (only `julianDay` is a syzygy concept; the wrong choice is silent).
- The `type` field is structurally a discriminator but nothing enforces "if `type == PLANET`, then `julianDay == null`".

**Fix:** sealed interface `PointEntry` with `record PlanetPointEntry`, `record AnglePointEntry`, `record LotPointEntry`, `record SyzygyPointEntry`. Jackson can serialise sealed via `@JsonTypeInfo`/`@JsonSubTypes`.

### L3. `BasicSect.planetSects` keyed by `String` not `Planet`
[BasicSect.java:19](src/main/java/app/model/basic/BasicSect.java#L19), [SectCalculator.java:51-69](src/main/java/app/basic/calculator/SectCalculator.java#L51-L69)
```java
private final Map<String, PlanetSectInfo> planetSects;
…
planets.put(Planet.MERCURY.name(), …);
```
The values are typed; the keys are `Planet.name()` strings. Should be `Map<Planet, PlanetSectInfo>` — typed all the way down. Same micro-pattern as M6 in V2, just localised to one container after the broader fix.

### L4. `Sect.UNKNOWN` enum constant is unreachable for chart-level sect, retained only for Mercury fallback
[Sect.java](src/main/java/app/model/data/Sect.java)
```java
public enum Sect { DIURNAL, NOCTURNAL, UNKNOWN }
```
`SectCalculator` requires Sun and Moon (throws otherwise), so the chart's primary `sect` is always `DIURNAL` or `NOCTURNAL`. The `UNKNOWN` value exists only because `mercurySect()` returns it when Mercury is missing ([SectCalculator.java:72-74](src/main/java/app/basic/calculator/SectCalculator.java#L72-L74)) — but Mercury *is* in the standard planet set, so this is unreachable in practice. This is the `HouseSystem.UNKNOWN` anti-pattern that M5 fixed, smaller scale: an enum value retained "just in case" that future switch statements will have to handle. Either remove it (and have `mercurySect` throw or return `Optional<Sect>`) or document why it's there.

### L5. `ChartAngle.name` and `LotPosition.name` are free-form `String`
[AngleCalculator.java:17-20](src/main/java/app/basic/calculator/AngleCalculator.java#L17-L20)
```java
addAngle(angles, "ASCENDANT", ascendant);
addAngle(angles, "MIDHEAVEN", midheaven);
addAngle(angles, "DESCENDANT", ctx.normalize(ascendant + 180.0));
addAngle(angles, "IMUM_COELI", ctx.normalize(midheaven + 180.0));
```
[LotCalculator.java:31-37](src/main/java/app/basic/calculator/LotCalculator.java#L31-L37) does an `equals("ASCENDANT")` lookup over the angle list. The "set of angles" and "set of lots" are closed, finite, well-known — they should be enums (`AngleType`, `LotType`). Right now a typo in `LotCalculator.angle("ASCENDENT")` silently returns null and falls through. (Currently safe because the strings are co-located in one file each, but the moment someone refactors `AngleCalculator` to use an enum and `LotCalculator` doesn't, the failure is silent.)

### L6. Calculator order is implicit and load-bearing
[BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)

The 11 calculators run in a hand-crafted order — Planet before House before Angle before Lot before Syzygy before Point before ChartPoint before SolarPhase before MoonPhase before Sect. Each step reads from `BasicChart` what the prior wrote. Today this works; but the dependency graph is invisible:

- `SectCalculator` reads `basicChart.getPlanets()` — must run after Planet.
- `LotCalculator` reads planets and angles — must run after Planet and Angle.
- `PointCalculator` reads planets, angles, lots, syzygy — must run after all four.
- `ChartPointCalculator` reads planets and angles — must run after both.

Reshuffling — e.g. moving `Sect` before `Lot` to support L1's fix — would require a maintainer to mentally trace what each calculator reads. No assertion, no ordering interface, no test. A `record CalculatorStep(name, requires, produces)` model would document this; minimum, comments.

### L7. `horizontalAltitude` doesn't propagate `swe_azalt` failures
[BasicCalculationContext.java:139-145](src/main/java/app/basic/BasicCalculationContext.java#L139-L145)
```java
swissEph.swe_azalt(fullJulianDay, SweConst.SE_ECL2HOR, geopos, 0.0, 10.0, eclipticCoordinates, horizontalCoordinates);
return horizontalCoordinates[1];
```
`swe_azalt` is `void` in this port; nothing checks `Double.isNaN(horizontalCoordinates[1])`. If swisseph returns NaN, `sunAltitude` is NaN, `>= 0.0` is `false`, the chart silently goes nocturnal. After the heroics of M10/N3/N4 making error handling uniform, this hole sticks out. Add a NaN check.

### L8. `Subject` adds public-API surface that may surprise callers
[Subject.java:13-23](src/main/java/app/model/input/Subject.java#L13-L23) introduces *two* public constructors. The first delegates `localBirthDateTime.toInstant()` as the `resolvedUtcInstant`. The second accepts an explicit `Instant`. There's no test that the two are consistent; a caller passing `OffsetDateTime` with one offset and an `Instant` representing a different absolute time would silently produce a `Subject` whose `getResolvedUtcInstant()` disagrees with its `getLocalBirthDateTime().toInstant()`. Either drop the two-arg constructor and force callers to compute the instant themselves, or assert consistency in the four-arg constructor.

---

## What is genuinely improved since V2

- **C5 sect** is the headline win: altitude-based, with a documented horizon tiebreaker. The largest correctness landmine that survived V1 is gone.
- **Boundary input handling.** `Subject` validates, date is ISO-only, time is regex-strict, `Terms`/`Zodiac`/`HouseSystem` parses are strict-or-throw. The "garbage in, plausible out" failure mode V1 worried about is closed.
- **Swiss Ephemeris error policy is uniform.** Houses, obliquity, planets, longitudes — all throw on failure. The mixed policy V2 N3/N4 flagged is resolved.
- **Typed JSON shape.** `Map<String, PointEntry>` and `BasicSect`/`PlanetSectInfo`/`TriplicityRulers` replace the V1 `Map<String, Map<String, Object>>` scenario. Renames are now compile-time changes.
- **Syzygy not-found and exact-zero compare** both improved.
- **`HouseSystem.UNKNOWN` removed**, switch is now compile-checked-exhaustive.

---

## Top-priority refactor backlog (post-V3)

V2's 1, 2, 4 mostly landed. Updated ordering:

1. **L1 — Make `LotCalculator` use altitude-based sect, or run `SectCalculator` before `LotCalculator`.** Active correctness regression introduced by partial C5 fix.
2. **C2 / m21 / N7 — De-singleton `Logger`** (still wired into `LoggerWriter`'s call path; serialised reflectively while another thread could be appending).
3. **L2 — Replace `PointEntry` with a sealed hierarchy.** The simplest typing of C7 landed; the audit-recommended one didn't.
4. **M3 — Collapse three doctrine classes to one record/enum.** Pure ceremony; trivial cleanup.
5. **C6 minor — Replace syzygy heuristics** (30-day window → 35; sign-flip via `atan2(sin Δ, cos Δ)`; cap bisection at 30 iterations or 1e-7-day tolerance; pack `swe_calc_ut` for Sun+Moon in one call).
6. **m19 — Move rounding to serialization** (still latent bug for finer rounding policies).
7. **N2 / N5 — Pass `(chart, ctx)` as method params; static or shared calculator instances.**
8. **M4 — Wire `nodeType`/`positionType`/`frame` through to swisseph, or drop the fields.**
9. **L3 / L5 — Make all `Map<String, …>` keys typed (`Map<Planet, …>`, `Map<AngleType, …>`).**
10. **M13 — Extract DTOs; drop `@JsonIgnore` from `BasicChart` / `PointEntry` / `PlanetSectInfo`.**

---

## Net assessment vs V2

V2 closed with three highest-value remaining items: untyped JSON surface (C7), singleton logger (C2), house-based sect (C5). V3 ships ~1.5 of those (C5 fully, C7 typed-but-not-sealed) plus all the input-side defenses and a uniform swisseph error policy. The Logger and the doctrine ceremony are still on the table. The audit-§2 sealed hierarchy is still the most valuable structural change pending. And L1 — the one regression — is a five-minute fix that the current calculator ordering enables but does not perform.

This is real progress. The remaining work is now mostly small, well-scoped items rather than the architectural rewrite V1 demanded.
