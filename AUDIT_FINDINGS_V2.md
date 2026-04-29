# Stage 1 — Adversarial Code Audit V2 (Improvement Comparison vs V1)

Re-running [audit.md](audit.md) against the current source. Each V1 finding is marked **FIXED**, **PARTIAL**, **NOT FIXED**, or **DELETED**. New issues introduced since V1 are listed in the final section. Numerical correctness is taken as given.

---

## Headline

The structural rewrite landed: the 914-line god class is now a 43-line orchestrator delegating to 11 single-purpose calculators behind a shared `BasicCalculationContext`. Lookup tables are extracted to a single `TraditionalTables`. The dead `traditional/` package is gone. ~12 stringly-typed concepts are now real enums. Critical bug C1 (south-node speed/retrograde) is fixed. Swiss Ephemeris failures now throw instead of silently returning zeros.

What did **not** move: the `Map<String, Map<String, Object>>` API surface (C7) is still the published JSON shape — and is now a *post-hoc projection* of typed data, so drift risk shifted from "two parallel writes" to "one model + one Map view that must be kept in sync by hand." Logger is still a process-wide singleton (C2). Sect is still derived from house number, not altitude (C5). Doctrine classes are still 3 nearly-empty implementations (M3). Boundary input validation, rounding-at-calculation-time, hard-coded paths, and silent-fallback enum parsers are unchanged.

---

## CRITICAL — V1 → V2

### C1. South Node speed/retrograde — **FIXED**
[PlanetCalculator.java:33-35](src/main/java/app/basic/calculator/PlanetCalculator.java#L33-L35)
South node now inherits `northNode.getSpeed()` (no negation), `northNode.getRetrograde()` (no flip), and the speed ratio is the un-negated quotient. Latitude/declination are correctly mirrored only.

### C2. `Logger` global singleton, no thread safety — **NOT FIXED**
[Logger.java:8-40](src/main/java/app/output/Logger.java#L8-L40)
Still `public static final Logger instance = new Logger();` with unsynchronized `ArrayList`, no `clear()`, used as the cross-cutting validation gate via `Logger.instance.hasErrors()`. Every concern from V1 stands: pollution across runs, `ConcurrentModificationException` between Jackson reading `getEntries()` and a calculator appending, mid-run shutdown of state. The `LoggerWriter`/`Logger.instance` coupling at [App.java:41](src/main/java/app/App.java#L41) bakes the singleton into the call path.

### C3. `BasicCalculator`/`SwissEph` not thread-safe, undocumented — **PARTIAL (worse documentation, slightly better scoping)**
[BasicCalculationContext.java:20](src/main/java/app/basic/BasicCalculationContext.java#L20), [App.java:24](src/main/java/app/App.java#L24)
`SwissEph` is now scoped to a per-call `BasicCalculationContext` rather than a calculator-level field — a quiet improvement, since each `calculate(input)` gets its own instance. But `BasicCalculator` is still constructed once in `App.main` and still has zero documentation around thread safety. A future maintainer wiring this into a Spring service will not know to either pool or lock. Add `@NotThreadSafe` or a class-level Javadoc note.

### C4. Moon-phase boundary cascade — **FIXED**
[MoonPhaseCalculator.java:28-45](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L28-L45)
Boundary at exact 0° is handled: `directedElongation < 45.0 || directedElongation >= 360.0 - epsilon`. The full-moon boundary uses `<= 180.0 + epsilon` for `GIBBOUS_TO_FULL`. Phase names are now an enum (`MoonPhaseName`).
**Minor lingering issue:** `waxing` is still `directedElongation < 180.0` ([line 20](src/main/java/app/basic/calculator/MoonPhaseCalculator.java#L20)) — at exactly 180.0 the moon is reported as not waxing, which contradicts the convention that 180 is the apex of waxing. Use `<= 180.0` or epsilon there too.

### C5. Sect ignores latitude/altitude, uses house number — **NOT FIXED**
[SectCalculator.java:18, 27-28](src/main/java/app/basic/calculator/SectCalculator.java#L18)
```java
boolean diurnal = sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12;
data.put("sunAboveHorizon", sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12);
data.put("moonAboveHorizon", moon != null && moon.getHouse() >= 7 && moon.getHouse() <= 12);
```
Identical anti-pattern to V1: the chart's sect is permanently coupled to whole-sign houses and breaks for arctic latitudes / quadrant systems. No tiebreaker for "Sun on the horizon." The duplicated condition appears 3 times — extract a local. Replace with `swe_azalt` altitude.

### C6. Prenatal syzygy search — **PARTIAL**
[SyzygyCalculator.java:31-69](src/main/java/app/basic/calculator/SyzygyCalculator.java#L31-L69)
- Bisection corrected: `low/lowValue` and `high` are now consistently maintained (V1 noted `highValue` was never read; new code is structurally correct).
- Failure now logs `"Could not find previous ... within 30 days"` ([line 43](src/main/java/app/basic/calculator/SyzygyCalculator.java#L43)) — but **still silently returns** `birthJulianDay - step`, so the chart structure still claims a syzygy that wasn't found. Make it throw, or null out `basicChart.syzygy`.
- Still 30-day window, still 50 bisection iterations, still the `Math.abs(earlierValue - laterValue) < 90.0` heuristic for sign-flip detection ([line 37](src/main/java/app/basic/calculator/SyzygyCalculator.java#L37)).
- Still `earlierValue == 0.0` exact-zero compare on a double ([line 37](src/main/java/app/basic/calculator/SyzygyCalculator.java#L37)).
- Still two `swe_calc_ut` calls per delta evaluation.

### C7. `BasicChart.points` is `Map<String, Map<String, Object>>` — **NOT FIXED (and structurally worse in one way)**
[BasicChart.java:15, 69-75](src/main/java/app/model/basic/BasicChart.java#L15), [PointCalculator.java:16-83](src/main/java/app/basic/calculator/PointCalculator.java#L16-L83)
`points` is still the JSON-published surface; typed lists (`planets`, `angles`, `lots`, `syzygy`) are still `@JsonIgnore`'d. New shape:
- The typed lists are now computed *first* by their respective calculators.
- A separate `PointCalculator` then *re-projects* every typed object into a fresh `Map<String, Object>` keyed by string — duplicating every value.
- Every key (`"longitude"`, `"degreeInSign"`, `"angularity"`, etc.) is a string literal in `PointCalculator`. Renaming a field on `PlanetPosition` does not break the JSON contract; renaming a key in `PointCalculator` does, with no compile-time link between the two.

This is an architectural improvement (single point of projection, not parallel writes) but **the contract is still invisible to the type system**. Fix: replace `Map<String, Map<String, Object>>` with `Map<String, ChartPointEntry>` (sealed interface: `PlanetEntry`, `AngleEntry`, `LotEntry`, `SyzygyEntry`) or drop the Map entirely and serialize the typed lists.

---

## MAJOR — V1 → V2

### M1. `BasicCalculator` 914-line god class — **FIXED**
[BasicCalculator.java](src/main/java/app/basic/BasicCalculator.java) (43 lines), [src/main/java/app/basic/calculator/](src/main/java/app/basic/calculator/) (11 calculators, 20-111 lines each)
Clean orchestrator + per-concern calculators (Simple, Planet, House, Angle, Lot, Syzygy, Point, ChartPoint, SolarPhase, MoonPhase, Sect). The `EphemerisService`/`AstroMath`/`LookupTables` separation called for in V1 §1 is achieved as `BasicCalculationContext` (ephemeris + math) + `TraditionalTables` (lookups). One nit: pure modular-math helpers (`normalize`, `antiscia`, `contraAntiscia`, `rawAngularSeparation`) live on the per-call context object and on `TraditionalTables` (where they are duplicated as private static methods at [TraditionalTables.java:111-122](src/main/java/app/basic/TraditionalTables.java#L111-L122)) — a stateless `AstroMath` utility would deduplicate.

### M2. Lookup tables duplicated, dead `traditional/` package — **FIXED**
The `app.doctrine.traditional` package is gone. Tables live once in [TraditionalTables.java](src/main/java/app/basic/TraditionalTables.java). `PointCalculator` delegates through static helpers. Minor lingering duplication: `signOf`/`degreeInSign`/`normalize` are reimplemented as private statics in `TraditionalTables` ([lines 111-122](src/main/java/app/basic/TraditionalTables.java#L111-L122)) instead of reusing the context's instance methods — a pure-function utility class would cover both callers.

### M3. Doctrine implementations are 99% identical empty shells — **NOT FIXED**
[DorotheusDoctrine.java](src/main/java/app/doctrine/impl/dorotheus/DorotheusDoctrine.java), [PtolemyDoctrine.java](src/main/java/app/doctrine/impl/ptolemy/PtolemyDoctrine.java), [ValensDoctrine.java](src/main/java/app/doctrine/impl/valens/ValensDoctrine.java)
Each still 43 lines, each `describe(...)` still returns `new SimpleDescriptiveResult(getId(), Map.of())`. The `DoctrineDefinition` interface was extracted (slight cleanliness gain) but no consolidation. Replace all three with a single `Doctrine` record or a `Doctrine` enum.

### M4. `CalculationSetting` advertises configurability never wired — **PARTIAL**
[CalculationSetting.java:18-30](src/main/java/app/model/input/CalculationSetting.java#L18-L30)
The fields are now typed enums (`NodeType.MEAN`, `PositionType.APPARENT`, `ReferenceFrame.GEOCENTRIC`) instead of strings — a real improvement to *readability*. But the constructor still hardcodes them, no setter exists, no parser, and `BasicCalculationContext` still always uses `SE_MEAN_NODE` regardless of `nodeType`. `ephemerisVersion` is still the literal string `"swisseph-2.10.03"`. A user setting `nodeType=TRUE` would still get a chart that lies in metadata.

### M5. `HouseSystem.UNKNOWN` and silent enum-parse fallbacks — **NOT FIXED**
- [HouseSystem.java](src/main/java/app/model/data/HouseSystem.java) still has the `UNKNOWN` constant.
- [BasicCalculationContext.java:158-163](src/main/java/app/basic/BasicCalculationContext.java#L158-L163) still maps `WHOLE_SIGN, UNKNOWN -> 'W'`.
- [Terms.java:14](src/main/java/app/model/data/Terms.java#L14) still `default -> EGYPTIAN`.
- [Zodiac.java:9](src/main/java/app/model/data/Zodiac.java#L9) still `equalsIgnoreCase("sidereal") ? SIDEREAL : TROPICAL`.
A typo in any input is silently a different doctrine.

### M6. Sect/dignities use `Map<String, Object>` — **NOT FIXED**
[SectCalculator.java:19-30](src/main/java/app/basic/calculator/SectCalculator.java#L19-L30) still builds a `Map<String, Object>` keyed by string (`"sect"`, `"lightOfSect"`, …) for `BasicChart.sect`. Dignity blocks are inside `PointCalculator`'s point-map (C7). The data is enum-typed *inside* the map (improvement) but the container still has no compile-time shape.

### M7. `Subject` constructor accepts NaN/Infinity, out-of-range — **PARTIAL**
[Subject.java:13-23](src/main/java/app/model/input/Subject.java#L13-L23) still accepts any double silently. [SubjectListParser.parseLocation](src/main/java/app/input/SubjectListParser.java#L115-L122) does range-check at the boundary, so the JSON-loaded path is safe. But constructing a `Subject` directly (tests, future API) still skips validation. `requireFinite` and range-check should move into the constructor.

### M8. Date parsing accepts ambiguous `dd/MM/yyyy` — **NOT FIXED**
[SubjectListParser.java:78-80](src/main/java/app/input/SubjectListParser.java#L78-L80) — identical to V1. American user writing `01/02/2025` still silently gets 1 February.

### M9. `Time` parsing length heuristic — **NOT FIXED**
[SubjectListParser.java:95](src/main/java/app/input/SubjectListParser.java#L95) — identical.

### M10. `swe_calc_ut` errors return zeros — **FIXED (mostly)**
[PlanetCalculator.java:52-55](src/main/java/app/basic/calculator/PlanetCalculator.java#L52-L55), [PlanetCalculator.java:66-68](src/main/java/app/basic/calculator/PlanetCalculator.java#L66-L68), [BasicCalculationContext.java:108-111](src/main/java/app/basic/BasicCalculationContext.java#L108-L111) all throw `IllegalArgumentException` on Swiss Ephemeris failure or NaN.
**Lingering inconsistencies:**
- [SimpleCalculator.java:15-17](src/main/java/app/basic/calculator/SimpleCalculator.java#L15-L17): obliquity failure is logged but execution continues, then `values[0]` (likely NaN) is rounded and stored.
- [BasicCalculationContext.java:36-39](src/main/java/app/basic/BasicCalculationContext.java#L36-L39): house calculation failure is logged but never thrown; `armc = normalize(ascmc[2])` proceeds with whatever was in the array. The whole rest of the chart will compute on NaN/0 ASC/MC/ARMC.

### M11. House-system switch silent fallback — **AS V1 (depends on M5)**
[BasicCalculationContext.java:158-163](src/main/java/app/basic/BasicCalculationContext.java#L158-L163) — collapses `UNKNOWN` to whole-sign. Same fix-vector as V1: drop `UNKNOWN`, drop the merged case.

### M12. Houses recomputed 4× per chart — **FIXED**
[BasicCalculationContext.java:36](src/main/java/app/basic/BasicCalculationContext.java#L36) — `swe_houses_ex` is called once in the context constructor; `cusps[]` and `ascmc[]` are reused. `armc`, `ascendant`, etc. read from those arrays. Numerical drift between callers is no longer possible.

### M13. Jackson annotations on domain models — **NOT FIXED**
[BasicChart.java](src/main/java/app/model/basic/BasicChart.java) — still 4 `@JsonIgnore` (planets, angles, raw matrices, syzygy, lots — actually 6 now), and [DescriptiveAstrologyReport.java:33](src/main/java/app/output/DescriptiveAstrologyReport.java#L33) still has `@JsonInclude`. The "Map is the JSON" pattern (C7) is *what makes* the `@JsonIgnore`s necessary; fix C7 and these go away.

### M14. `JsonReportWriter` Jackson defaults — **PARTIAL**
[JsonReportWriter.java:15-20](src/main/java/app/output/JsonReportWriter.java#L15-L20) — `JavaTimeModule` and `Jdk8Module` are now registered, `WRITE_DATES_AS_TIMESTAMPS` is disabled. Still missing: `FAIL_ON_UNKNOWN_PROPERTIES` (writer-only mapper, less critical) and `@JsonInclude(NON_NULL)` globally — the JSON still emits explicit nulls for absent fields.

### M15. `Logger.instance.hasErrors()` is the only validation gate — **NOT FIXED**
[InputLoader.java:18-20](src/main/java/app/input/InputLoader.java#L18-L20) — identical to V1. The error message still points the user at `output/run-logger.json` *before* it has been written this run, so a `cat` of that file shows the previous run.

---

## MINOR / NIT — V1 → V2

| V1 ID | Status | Notes |
|---|---|---|
| m1 magic numbers | NOT FIXED | `15.0` ([SimpleCalculator.java:22](src/main/java/app/basic/calculator/SimpleCalculator.java#L22)), mean speeds inline ([PlanetCalculator.java:75-84](src/main/java/app/basic/calculator/PlanetCalculator.java#L75-L84)), `1_000_000.0` ([BasicCalculationContext.java:85](src/main/java/app/basic/BasicCalculationContext.java#L85)), `2440587.5` & `86400.0` ([SyzygyCalculator.java:72](src/main/java/app/basic/calculator/SyzygyCalculator.java#L72)). |
| m2 strings → enums | MOSTLY FIXED | New enums: `Angularity`, `Sect`, `SectCondition`, `MoonPhaseName`, `SolarOrientation`, `SyzygyType`, `Element`, `PointType`, `NodeType`, `PositionType`, `ReferenceFrame`. Lingering: `ChartAngle.name`, `LotPosition.name` are still String; `Map<String, Object>` keys throughout `SectCalculator`, `PointCalculator`. |
| m3 `Optional` | NOT FIXED | `exaltationRuler` still returns nullable `Planet`, `termRuler` still returns null for `Terms.NONE`, `BasicCalculationContext.planet(...)` returns null. |
| m4 records | NOT FIXED | All carriers are still POJOs with explicit constructors and getters — `PlanetPosition`, `BasicSyzygy`, etc. |
| m5 `LinkedHashMap` everywhere | NOT FIXED | See `PointCalculator`, `SectCalculator`. |
| m6 `==` on doubles | NOT FIXED | [SyzygyCalculator.java:37](src/main/java/app/basic/calculator/SyzygyCalculator.java#L37) `earlierValue == 0.0`. |
| m7 `degreeInSign` re-normalizes input | NOT FIXED — but benign (callers always pass normalized). |
| m8 `signDistance` duplicated | NOT FIXED | Still a private method on [ChartPointCalculator.java:39-42](src/main/java/app/basic/calculator/ChartPointCalculator.java#L39-L42). Move onto `ZodiacSign`. |
| m9 oriental/occidental no doc | NOT FIXED | [SolarPhaseCalculator.java:30-31](src/main/java/app/basic/calculator/SolarPhaseCalculator.java#L30-L31) — identical undocumented `delta > 180.0`. |
| m10 LogEntry no timestamp | NOT FIXED | [LogEntry.java:3-7](src/main/java/app/output/LogEntry.java#L3-L7) — still only `level/scope/message`. |
| m11 Logger no WARN | NOT FIXED | Still only `info`/`error`. |
| m12 hard-coded paths | NOT FIXED | [App.java:36, 41](src/main/java/app/App.java#L36), [SubjectListParser.java:26](src/main/java/app/input/SubjectListParser.java#L26), [SettingLoader.java:17](src/main/java/app/input/SettingLoader.java#L17). |
| m13 method length | FIXED | All current calculator methods are <70 lines and single-concern. |
| m14 sect with missing Sun | NOT FIXED | Still `diurnal = sun != null && sun.getHouse() ...`; null Sun silently goes nocturnal. |
| m15 BasicSyzygy string discriminator | FIXED | [BasicSyzygy.java:35](src/main/java/app/model/basic/BasicSyzygy.java#L35) now `type == SyzygyType.FULL_MOON`. |
| m16 `App.main throws Exception` | NOT FIXED | [App.java:21](src/main/java/app/App.java#L21). |
| m17 deleted spec/lord/profections | INFO | `JAVA_MIGRATION.md`, `spec.md`, `NativeAnnualProfections`, `NativeLordOfOrb`, `NativePlanetaryHour` etc. all gone — confirms a deliberate stage-1 narrowing rather than a delivery gap. |
| m18 lookup tables not testable | FIXED | `TraditionalTables` exposes static methods directly. |
| m19 rounding at calculation time | NOT FIXED | [BasicCalculationContext.round](src/main/java/app/basic/BasicCalculationContext.java#L83-L88) still rounds inline; every constructor of `PlanetPosition`, `LotPosition`, etc. is fed pre-rounded values. |
| m20 `InputListBundle` mutable bag | NOT FIXED | [InputListBundle.java](src/main/java/app/model/input/InputListBundle.java) — five setters, two-stage construction. |
| m21 Logger race | NOT FIXED | Same root cause as C2. |
| n1 `getRetrograde` not `isRetrograde` | NOT FIXED | [PlanetPosition.java:70](src/main/java/app/model/basic/PlanetPosition.java#L70). |

---

## NEW issues introduced since V1

### N1. `PointCalculator` is a hand-maintained projection layer — likely future drift point
[PointCalculator.java](src/main/java/app/basic/calculator/PointCalculator.java)
The decision to keep the published JSON shape (`Map<String, Map<String, Object>>`) and feed it from typed objects centralizes the drift risk into one place — better than V1's parallel writes — but it remains untyped. Adding a field to `PlanetPosition` is silent unless you also add it here. Adding a field to the JSON is silent unless you also add it on `PlanetPosition`. Worth treating this file as the contract and snapshotting its output in tests until C7 is actually fixed.

### N2. `BaseCalculator` uses mutable inheritance state, instances are single-use
[BaseCalculator.java:7-14](src/main/java/app/basic/BaseCalculator.java#L7-L14)
```java
protected BasicChart basicChart;
protected BasicCalculationContext ctx;
public void calculate(BasicChart basicChart, BasicCalculationContext ctx) { … }
```
Each calculator subclass holds the chart and context as mutable fields, populated on entry to `calculate(...)`. `BasicCalculator.calculate(input)` works around this by `new`-ing every calculator on every call ([BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)) — wasteful and a trap: caching any calculator instance in a Spring `@Bean` would race silently between requests. Either pass `(chart, ctx)` to the abstract method directly so calculators are stateless, or document that subclasses are not reusable.

### N3. House calculation failure does not abort the chart
[BasicCalculationContext.java:36-40](src/main/java/app/basic/BasicCalculationContext.java#L36-L40)
`calculateSwissHouses` failure is logged; the constructor proceeds to compute `armc = normalize(ascmc[2])` from whatever's in the (uninitialized or partial) array. The rest of the calculator pipeline then does work that depends on a broken ASC. M10 was fixed for planets but not for houses; pick one policy and apply consistently.

### N4. Obliquity failure still returns whatever's in `values[0]`
[SimpleCalculator.java:14-23](src/main/java/app/basic/calculator/SimpleCalculator.java#L14-L23)
Same pattern as N3: log on failure, then store. The chart's `obliquity` becomes NaN; downstream consumers will broadcast NaN.

### N5. New per-call object churn
[BasicCalculator.java:25-35](src/main/java/app/basic/BasicCalculator.java#L25-L35)
Eleven `new XxxCalculator()` per chart. Harmless at 1 chart/sec; at 1000 it's allocator pressure for no reason. Inject the calculators (or make them `static` callable) once N2 is fixed.

### N6. `TraditionalTables.triplicityRulers` returns `Map<String, Object>` keyed by `"day"/"night"/"participating"`
[TraditionalTables.java:47-72](src/main/java/app/basic/TraditionalTables.java#L47-L72)
Of all places to introduce a typed record, this is it — `record TriplicityRulers(Planet day, Planet night, Planet participating)`. Right now the keys are stringly-typed *and* the values are `Object` (always `Planet` in practice). Looks like the rest of the codebase was raised to enums while this one path stayed a Map.

### N7. `Logger` is now visibly imported by `LoggerWriter` for serialization
[LoggerWriter.java:14](src/main/java/app/output/LoggerWriter.java#L14)
`writer.write(path, Logger.instance)` serializes the singleton directly — Jackson reads `getStartedAt()` and `getEntries()` reflectively. Combined with C2 the race window is wider than V1: a calculator on another thread (when stage 2 spins up a service) appending to `entries` while Jackson is iterating produces a crash or partial log. Ties C2 to a concrete failure path in code, not just a future risk.

### N8. `SyzygyCalculator` does not record syzygy as missing
[SyzygyCalculator.java:43-44](src/main/java/app/basic/calculator/SyzygyCalculator.java#L43-L44)
On 30-day-window miss, logs error and returns `birthJulianDay - step` — i.e. ~6 hours before birth, with the originally-requested `type`. Then `executeCalculation` proceeds to compute longitudes and a fully-populated `BasicSyzygy` from this fake JD. The chart silently contains a syzygy that does not exist. (V1 noted this; the new code added the log without changing the return.)

### N9. `BasicChart` got more `@JsonIgnore`'d fields, not fewer
[BasicChart.java](src/main/java/app/model/basic/BasicChart.java) now has six `@JsonIgnore`s (planets, angles, rawAspectMatrix, rawDeclinationMatrix, rawSignDistanceMatrix, syzygy, lots — that's seven by count). V1 reported eight total — close, but the *direction of travel* is wrong: each new typed list added since V1 also got `@JsonIgnore`'d to keep the Map view authoritative.

---

## Top-priority refactor backlog (post-V2)

The V1 backlog largely stands. Updated ordering:

1. **C7 / M6 / N1 — Retire `Map<String, Map<String, Object>>`.** Now that `PointCalculator` is the single projection point, deleting the Map and serializing the typed lists is one calculator's worth of work. This kills six `@JsonIgnore`s, fixes M13, and removes drift risk N1.
2. **C2 / m21 / N7 — De-singleton `Logger` with a per-`load()`/per-`calculate()` instance.** The thread-safety risk is now wired into `LoggerWriter`'s call path.
3. **C5 — Replace house-based sect with altitude.** Largest correctness landmine that survived the rewrite.
4. **M10 / N3 / N4 — Make swisseph-failure handling uniform.** Planets throw, houses log, obliquity logs; pick "throw" everywhere.
5. **M3 — Collapse three doctrine classes to one record/enum.**
6. **M5 — Drop `HouseSystem.UNKNOWN`, make `Terms.parse`/`Zodiac.parse` strict.**
7. **m19 — Move rounding to serialization** (still a latent precision bug for finer rounding policies).
8. **C6 / N8 — Either fail loudly on missing syzygy, or make `BasicChart.syzygy` an `Optional`.**
9. **N2 — Pass `(chart, ctx)` as method parameters; make calculator instances stateless.**
10. **M7 — Move input validation into `Subject`'s constructor.**

---

## What is genuinely improved since V1

- **Architecture (M1, M2, M12, m13, m18).** The single largest V1 concern — the god class blocking stage 2 — is resolved. Lookup tables are reusable and unit-testable.
- **Critical bug C1.** South-node speed/retrograde now correct.
- **C4 moon-phase boundaries.** Epsilon-bracketed at 0° and 180°.
- **M10 swisseph error handling.** Throws for planets/longitudes (still inconsistent for houses/obliquity — N3, N4).
- **m2 enums.** Roughly a dozen formerly-stringly typed concepts are now real enums; `mercurySect`, `phaseRelativeToSun`, `Sect`, `SyzygyType`, `Angularity`, `MoonPhaseName`, `Element`, `PointType`, `NodeType`, `PositionType`, `ReferenceFrame`, `SolarOrientation`, `SectCondition`.
- **m15 syzygy type discriminator.** Now enum-based.
- **Jackson.** Time module + Jdk8 module registered; ISO date output.

What did **not** improve and remains the highest-value remaining work: the untyped JSON surface (C7), the singleton logger (C2), and the house-based sect calculation (C5).
