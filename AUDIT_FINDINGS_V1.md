# Stage 1 — Adversarial Code Audit

Numerical correctness against the Lille fixture is taken as given and **not** revisited. This audit attacks the code itself.

---

## CRITICAL

### C1. South Node speed and retrograde flag are derived incorrectly
[BasicCalculator.java:92-110](src/main/java/app/basic/BasicCalculator.java#L92-L110)

```java
round(-northNode.getSpeed(), input),                       // L99 speed
round(meanDailySpeed(Planet.SOUTH_NODE), input),           // L101 mean (positive)
round((-northNode.getSpeed()) / meanDailySpeed(Planet.SOUTH_NODE), input), // L102 ratio
!northNode.getRetrograde(),                                // L103 retrograde
```

The South Node is the antipode of the North Node and **moves in lockstep**. Its derivative `dλ/dt` equals the North Node's, not its negation. Negating the speed implies the south node moves prograde when the north node moves retrograde — that contradicts the geometry. Likewise `meanDailySpeed(Planet.SOUTH_NODE)` returns `-0.05295` (line 287, `NORTH_NODE, SOUTH_NODE -> -0.05295`), but here on line 101 it's used as a positive — actually it's **the same negative constant**, so `speedRatio = -negative / -negative = positive`, but `speed = -negativeNorth = positive`, while declination is also negated. The retrograde flag is then **flipped** to `!northNode.getRetrograde()`, which is straight wrong: both nodes are retrograde at the same time.

**Why it matters:** any downstream consumer checking `southNode.retrograde` or `southNode.speedRatio` will get the wrong sign. The fixture probably doesn't assert these; that's why this passed.

**Fix:** South node should inherit the *same* speed, declination magnitude (sign appropriate), retrograde flag, and meanDailySpeed as the north node — just longitude and latitude are mirrored. At minimum:

```java
planets.add(new PlanetPosition(
    Planet.SOUTH_NODE,
    round(southNodeLongitude, input),
    signOf(southNodeLongitude),
    round(degreeInSign(southNodeLongitude), input),
    round(-northNode.getLatitude(), input),
    round(-northNode.getDeclination(), input),
    round(northNode.getSpeed(), input),                // SAME speed
    round(meanDailySpeed(Planet.SOUTH_NODE), input),
    round(northNode.getSpeed() / meanDailySpeed(Planet.SOUTH_NODE), input),
    northNode.getRetrograde(),                         // SAME flag
    ...
));
```

---

### C2. `Logger` is a process-wide static singleton with no thread safety
[Logger.java:9-35](src/main/java/app/output/Logger.java#L9-L35)

```java
public static final Logger instance = new Logger();
private final List<LogEntry> entries = new ArrayList<>();
```

- Static mutable global state.
- `entries` is an unsynchronized `ArrayList`.
- `hasErrors()` is used as a control-flow side channel: [InputLoader.java:18](src/main/java/app/input/InputLoader.java#L18) reads it to decide whether to throw.
- `getEntries()` returns `List.copyOf(entries)`. If a writer is appending while a reader copies, you get `ConcurrentModificationException`.
- The instance is **never reset**. In a long-lived JVM (which this engine eventually will be — stage 2/3 is described as a service), errors from chart N pollute the log read by chart N+1, causing `InputLoader` to falsely throw on a clean input.

**Fix:** thread the logger through call sites as a dependency, or at minimum make it a per-`InputLoader` instance. If kept singleton, use `CopyOnWriteArrayList` and add a `clear()` method tied to a request scope.

---

### C3. `BasicCalculator` holds a single non-thread-safe `SwissEph` and is used as a singleton
[BasicCalculator.java:34-35](src/main/java/app/basic/BasicCalculator.java#L34-L35) and [App.java:24](src/main/java/app/App.java#L24)

```java
public final class BasicCalculator {
    private final SwissEph swissEph = new SwissEph();
```

`SwissEph` is a port of a C library that is documented as not thread-safe. The calculator is constructed once in `App.main` and reused across every (subject × doctrine) combination. Currently single-threaded so it doesn't blow up — but the class **looks reusable** and would be invoked concurrently as soon as anyone wraps it in a service. There is no annotation, comment, or class-level note saying "not thread-safe". A future maintainer wiring this into a Spring `@Bean` will silently corrupt outputs.

**Fix:** either document `@NotThreadSafe`, or take a `SwissEph` per `calculate()` call, or pool them behind a lock.

---

### C4. `MoonPhase` exact-full and exact-new boundaries fall through wrong
[BasicCalculator.java:868-877](src/main/java/app/basic/BasicCalculator.java#L868-L877)

```java
private String moonPhaseName(double directedElongation) {
    if (directedElongation < 45.0) return "NEW_TO_CRESCENT";
    if (directedElongation < 90.0) return "CRESCENT_TO_FIRST_QUARTER";
    ...
    if (directedElongation < 315.0) return "LAST_QUARTER_TO_BALSAMIC";
    return "BALSAMIC_TO_NEW";
}
```

At exactly `directedElongation == 180.0` (full moon), all `< 180` checks fail and the cascade runs through `< 225`, `< 270`, `< 315`, finally returning `"BALSAMIC_TO_NEW"` — i.e. a full moon is reported as a *balsamic* (waning crescent) phase. Same problem at exactly `0.0` and `360.0` (boundary direction; `normalize` produces `0.0` for an exact conjunction).

`waxing` is also `false` at exactly 180 because `directedElongation < 180.0`, when 180 is conventionally the apex of waxing.

**Fix:** explicit equality buckets, e.g. `if (directedElongation >= 360.0 - eps || directedElongation < 45.0) return "NEW_TO_CRESCENT"`, or use `<=` carefully on segment ends, or define dedicated `"FULL_MOON_EXACT"`/`"NEW_MOON_EXACT"` with epsilon tolerance.

---

### C5. Sect ignores latitude/diurnal-arc geometry and is computed by house number
[BasicCalculator.java:632, 641-642](src/main/java/app/basic/BasicCalculator.java#L632)

```java
boolean diurnal = sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12;
...
data.put("sunAboveHorizon", sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12);
data.put("moonAboveHorizon", moon != null && moon.getHouse() >= 7 && moon.getHouse() <= 12);
```

- This couples `sect` permanently to the **whole-sign** house numbering. The instant houseSystem becomes Placidus, Porphyry, or anything else, the "house 7-12 = above horizon" assumption fails: in quadrant systems, houses 7-12 are above the horizon by construction *only if* the cusps fall between Asc and Desc, which is true *almost* everywhere except in pathological polar configurations and (subtly) at the exact horizon.
- More importantly, this is computing "above horizon" the wrong way. Above-horizon should be tested via **altitude > 0**, computed from RA, Dec, ARMC, and observer latitude. Substituting "house 7-12" is a shortcut that breaks at the polar circle (where the Sun can be circumpolar and `houseOf` becomes undefined or unstable).
- For the Lille (50°N) fixture in summer this happens to work. It will not for arctic latitudes, and it will not for non-whole-sign doctrines.
- No tiebreaker is documented for "Sun exactly on the horizon" (sect ambiguity at sunrise/sunset).

**Fix:** compute Sun altitude directly using ascendant/descendant latitudes or via swisseph's `swe_azalt`, and define an explicit tiebreaker (Hellenistic convention typically picks DIURNAL when altitude == 0).

---

### C6. Prenatal syzygy search has unsafe windows and a duplicated `swe_calc_ut`-heavy loop
[BasicCalculator.java:786-840](src/main/java/app/basic/BasicCalculator.java#L786-L840)

Multiple problems stack:

1. **30-day window** (line 813 `birthJulianDay - 30.0`) is at the absolute edge: synodic month is 29.5306 days. A new moon that occurred 29.6 days before birth would be *exactly* on the boundary; the loop terminates `>= birthJulianDay - 30.0` so it would just barely catch it — but with a 0.25-day step it's possible to scan past without detecting the sign flip if the previous candidate landed past the window. A subject born immediately after a new moon (e.g. ~29 days after the previous one) is in the danger zone.
2. **Sign-flip filter** (line 815) `Math.abs(earlierValue - laterValue) < 90.0` is a heuristic to skip the wraparound at ±180°, not a robust guard. With a coarser step or a chart near ecliptic singularities, this can miss a real crossing.
3. **Bisection without high-side initialization** (line 828): `lowValue` is captured but `highValue` is never read. The bisection is correct **only if** `lowValue` and the implicit `highValue` straddle zero on entry, which `previousSyzygyCandidate` claims it ensures via the sign-flip filter — but the filter has the heuristic above.
4. **50 bisection iterations** for a 0.25-day initial bracket: 0.25 / 2^50 = ~2e-16 days = sub-attosecond. 30 iterations would already give microsecond precision. This wastes ~140 swisseph calls per syzygy.
5. **No fallback if not found**: line 822 returns `birthJulianDay - step` (i.e. 6 hours before birth) labeled with the original `type`. This is silently wrong — better to throw, or mark the result as `UNKNOWN`/null.
6. **Each `syzygySignedDelta` call invokes `swe_calc_ut` twice** via `longitudeFor`. With ~120 scan candidates × 2 syzygy types × 2 = ~500 calls before bisection. Computing both Sun and Moon in a single packed call would halve that.

**Fix:** widen the search to 35 days; replace the heuristic with a robust crossing detector that explicitly handles the 360° wrap (use `Math.atan2(sin Δ, cos Δ)`); cap bisection at 30 iterations or terminate on `|hi - lo| < 1e-7 days`; fail loudly when no syzygy is found.

---

### C7. `BasicChart.points` is `Map<String, Map<String, Object>>` — the engine's actual public surface is untyped
[BasicChart.java:15-16, 40-41](src/main/java/app/model/basic/BasicChart.java#L15-L16) and [BasicCalculator.java:527-593](src/main/java/app/basic/BasicCalculator.java#L527-L593)

The `calculatePoints` method builds a `LinkedHashMap` for every planet, angle, lot, syzygy, with raw `data.put("longitude", ...)` calls. This map is the JSON output. Meanwhile, the *typed* `PlanetPosition`, `ChartAngle`, `LotPosition`, `BasicSyzygy` are computed and *also* held on `BasicChart`, but marked `@JsonIgnore` so they don't serialize.

Consequences:
- The contract is **invisible to the type system**. Any rename (`"degreeInSign"` → `"degree"`) is a silent breaking change.
- The same data is produced *twice*: once typed, once as a Map. They are kept in sync **manually**. Drift is inevitable.
- Stage 2 doctrine code (`describe`) will have to choose: read the typed list or fish through the Map. Either way it sees half the truth.
- Properties like `domicileRuler`, `triplicityRulers`, `faceRuler` are only present on the Map version — they are not on `PlanetPosition`.

**Fix:** delete the Map. Make `BasicChart` carry typed lists (`planets`, `angles`, `lots`, `syzygy`) and a typed `Map<Planet, EssentialDignities>` keyed by enum. Let Jackson serialize the typed objects. If you need ordering, use a `SequencedMap` or `LinkedHashMap<Planet, ...>`.

---

## MAJOR

### M1. `BasicCalculator` is a 914-line god class
[BasicCalculator.java](src/main/java/app/basic/BasicCalculator.java)

It mixes:
- swisseph wrapping (`swe_calc_ut`, `swe_houses_ex`, `swe_julday`)
- modular arithmetic (`normalize`, `antiscia`, `signDistance`)
- doctrine tables (Egyptian terms, Ptolemaic terms, triplicities, faces)
- sect determination
- syzygy bisection
- moon phase classification
- output map construction

The "swisseph-facing" boundary called for in audit §1 does not exist. There is no `EphemerisService`, no `AstroMath`, no `LookupTables`. A clean rewrite would have at least:

```
app.ephemeris/      (SwissEph wrapping, JD conversions, raw planet/angle calls)
app.astromath/      (normalize, antiscia, signDistance, modular helpers — pure)
app.doctrine.tables/(EgyptianTerms, PtolemaicTerms, Triplicities, Faces, Domiciles — immutable)
app.basic.calculator/  (composition over the above)
```

This is the single largest refactor that will block stage 2 — every doctrine impl will want to *reuse* the lookup tables and arithmetic, but they are private methods on `BasicCalculator`.

---

### M2. Lookup tables are duplicated verbatim across two classes
[BasicCalculator.java:340-416](src/main/java/app/basic/BasicCalculator.java#L340-L416) and [TraditionalEssentialDignityCalculator.java:91-167](src/main/java/app/doctrine/traditional/TraditionalEssentialDignityCalculator.java#L91-L167)

`domicileRuler`, `exaltationRuler`, `triplicityRulers`, `element`, `faceRuler`, `opposite`, `isClassicalPlanet` are duplicated **identically**, character-for-character, between `BasicCalculator` and `TraditionalEssentialDignityCalculator`. The latter is also **dead code** — `App.main` never calls it (verify with `grep TraditionalEssentialDignityCalculator src -r`).

Same for `TraditionalSectCalculator` (duplicates `BasicCalculator.calculateSect`, also unreferenced).

**Fix:** delete the `traditional/` package, or extract the tables into shared immutable classes.

---

### M3. Doctrine implementations are 99% identical and behaviorally empty
[DorotheusDoctrine.java](src/main/java/app/doctrine/impl/dorotheus/DorotheusDoctrine.java), [PtolemyDoctrine.java](src/main/java/app/doctrine/impl/ptolemy/PtolemyDoctrine.java), [ValensDoctrine.java](src/main/java/app/doctrine/impl/valens/ValensDoctrine.java)

```java
@Override public DescriptiveResult describe(Input input, BasicChart chart) {
    return new SimpleDescriptiveResult(getId(), Map.of());
}
```

Every doctrine returns an empty map. The three classes differ only in `id`, `name`, and `terms`/`zodiac`/`houseSystem` constants. This should be either:
- A single `Doctrine` class with three static instances, or
- An enum `enum Doctrine { DOROTHEUS(...), PTOLEMY(...), VALENS(...); ... }`, or
- Configuration-driven (load doctrine specs from YAML/JSON).

Three classes with no behavior is YAGNI ceremony.

---

### M4. `CalculationSetting` advertises configurability that is never wired
[CalculationSetting.java:20-26](src/main/java/app/model/input/CalculationSetting.java#L20-L26)

```java
this.nodeType = "MEAN";
this.positionType = "APPARENT";
this.frame = "GEOCENTRIC";
this.ephemerisVersion = "swisseph-2.10.03";
```

These are hard-coded in the constructor; there is no setter, no parser. The calculation always uses `SE_MEAN_NODE` ([BasicCalculator.java:89](src/main/java/app/basic/BasicCalculator.java#L89)) regardless of `nodeType`. They appear in the JSON output as decorative metadata. If `nodeType` is changed to "TRUE", the output JSON lies.

`ephemerisVersion` as a hardcoded string can drift from the actual jar — at minimum derive it from `SwissEph.class.getPackage().getImplementationVersion()` or read it from a Maven-filtered properties file.

**Fix:** drop the fields entirely until they are wired. Don't ship metadata you can't honor.

---

### M5. `HouseSystem.UNKNOWN` is a silent fallback to whole-sign
[HouseSystem.java:5-7](src/main/java/app/model/data/HouseSystem.java) and [BasicCalculator.java:318-323](src/main/java/app/basic/BasicCalculator.java#L318-L323)

```java
case WHOLE_SIGN, UNKNOWN -> 'W';
```

A doctrine with `HouseSystem.UNKNOWN` (which the audit explicitly worries about) silently computes whole-sign. A user mistyping `houseSystem: "regiomontanus"` in a future config will get a chart that *looks* correct but is fundamentally wrong. Same anti-pattern in `Terms.parse` (line 14: `default -> EGYPTIAN`) and `Zodiac.parse` (line 9: silently returns `TROPICAL` on anything not "sidereal").

**Fix:** all three should throw on unknown values, or return `Optional<>` so callers must decide. `UNKNOWN` enum constants are an anti-pattern — they exist only to satisfy a switch.

---

### M6. Sect and dignities use `Map<String, Object>` because the model has no class for them
[BasicCalculator.java:628-669](src/main/java/app/basic/BasicCalculator.java#L628-L669) and [BasicChart.java:25-26](src/main/java/app/model/basic/BasicChart.java#L25-L26)

`Map<String, Object> sect` and `Map<String, Object>` for dignities throughout. Same pattern as C7 — output structure exists only as a JSON shape, not as a typed Java class. Refactor cost compounds when stage 2 needs to read these values: every consumer becomes `((Map<String, Object>) chart.getSect().get("planetSects")).get("MERCURY")` casts.

---

### M7. `Subject` does not validate inputs
[Subject.java:15-25](src/main/java/app/model/input/Subject.java#L15-L25)

The constructor accepts any `double` for lat/lon — including `NaN`, `±Infinity`, `latitude = 200`. Validation happens only in `SubjectListParser.parseLocation`. A unit-test or future consumer that constructs a `Subject` directly skips it.

Latitude outside the polar circles (|φ| > 66.5°) at certain times yields ascendant degeneracy in Placidus. There's no guard.

**Fix:** `Subject` constructor should call `requireFinite` and range-check, or expose a static factory `Subject.of(...)` that does.

---

### M8. `Date` parsing accepts `dd/MM/yyyy` ambiguously
[SubjectListParser.java:78-80](src/main/java/app/input/SubjectListParser.java#L78-L80)

```java
if (value.contains("/")) {
    return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
}
```

`"01/02/2025"` is parsed as 1 February 2025 with no warning. An American user writing JSON expects 2 January 2025. There is no documentation of the date format. The only safe input format is ISO `yyyy-MM-dd`; the European fallback should be removed or made explicit (e.g. require `dd.MM.yyyy` or `dd-MM-yyyy` with dashes — never `/`).

---

### M9. `Time` parsing length heuristic is wrong
[SubjectListParser.java:95](src/main/java/app/input/SubjectListParser.java#L95)

```java
return value.length() == 5 ? LocalTime.parse(value + ":00") : LocalTime.parse(value);
```

`"3:45"` (length 4) and `"3:45 AM"` (length 7) both fall to the else branch and fail. `"03:45:67"` (length 8, invalid second) bypasses the heuristic. This is a brittle string-length test where a regex or `try`-then-fallback would be honest.

---

### M10. Errors from `swe_calc_ut` propagate as zeros, not failures
[BasicCalculator.java:162-171](src/main/java/app/basic/BasicCalculator.java#L162-L171), [BasicCalculator.java:226-247](src/main/java/app/basic/BasicCalculator.java#L226-L247)

```java
private double declination(...) {
    ...
    if (result < 0 || Double.isNaN(values[1])) {
        Logger.instance.error(...);
        return 0.0;          // <-- caller cannot distinguish "0° declination" from "computation failed"
    }
    return values[1];
}
```

Same pattern in `obliquity`, `ascendant`, `armc`. A swisseph failure produces a structurally complete chart with zeros, plus a logger entry. If the logger isn't checked (the loader checks; downstream code does not), the bad chart silently flows to the report. Mercury at house = "ANGULAR" with longitude = 0.0 will look plausible.

**Fix:** propagate failure as `Optional<Double>` or a checked exception; `BasicChart` should refuse to be built on partial data, or expose a `valid` flag.

---

### M11. `houseSystem` switch falls through silently when adding new enum values
[BasicCalculator.java:318-323](src/main/java/app/basic/BasicCalculator.java#L318-L323)

```java
private int houseSystem(HouseSystem houseSystem) {
    return switch (houseSystem) {
        case PLACIDUS -> 'P';
        case WHOLE_SIGN, UNKNOWN -> 'W';
    };
}
```

This is a complete switch *today*, so the compiler accepts. The moment someone adds `REGIOMONTANUS` to the enum, the compiler errors here — *good*. But the current `UNKNOWN` is the silent fallback (M5). Fix M5 and this becomes acceptable.

---

### M12. Houses recomputed four times per chart
[BasicCalculator.java:42-47](src/main/java/app/basic/BasicCalculator.java#L42-L47), via `calculateSwissHouses`

`calculateHouses`, `calculateAngles`, `ascendant`, `armc` each call `swissEph.swe_houses_ex(...)`. Four times the work, four chances of a numerical drift between callers. (And sub-millisecond drift can perturb the ASC across a sign boundary near 0°00'00".)

**Fix:** compute once, cache the cusps/ascmc arrays on a per-`calculate()` context object.

---

### M13. JSON serialization annotations live on domain models
[BasicChart.java](src/main/java/app/model/basic/BasicChart.java), [Subject.java:5,47](src/main/java/app/model/input/Subject.java), [DescriptiveAstrologyReport.java:33](src/main/java/app/output/DescriptiveAstrologyReport.java#L33)

Eight `@JsonIgnore` and one `@JsonInclude` smear Jackson over what should be pure domain. The serialization concern (e.g. "expose `points` Map but not `planets` list") leaks into the model. A consumer using the model in-memory has to know the JSON dance.

**Fix:** introduce DTOs on the output side. `BasicChart` stays pure; `BasicChartJson` (or a Jackson `MixIn`) handles serialization.

---

### M14. `JsonReportWriter` uses Jackson defaults
[JsonReportWriter.java:15-20](src/main/java/app/output/JsonReportWriter.java#L15-L20)

No `FAIL_ON_UNKNOWN_PROPERTIES`, no `@JsonInclude(NON_NULL)`, no `WRITE_NULL_MAP_VALUES`. Output JSON contains explicit `null`s; in deserialization tests, unknown properties would silently bind. The mapper is also re-created per `JsonReportWriter` instance, with no module documentation; on first read I had to check whether `JavaTimeModule` was actually registered.

---

### M15. `Logger.instance.hasErrors()` is the loader's only validation gate
[InputLoader.java:18-20](src/main/java/app/input/InputLoader.java#L18-L20)

```java
if (Logger.instance.hasErrors()) {
    throw new IllegalArgumentException("Input validation failed. See output/run-logger.json");
}
```

- A spurious `Logger.instance.error(...)` from anywhere — including code that should warn but not abort — kills the entire run.
- Tests that share a JVM accumulate errors and trigger the next test's loader to throw.
- The error message tells the user to read a file that **has not yet been written** (the writer runs in `finally`). A user encountering "Input validation failed. See output/run-logger.json" and `cat output/run-logger.json`-ing finds the previous run's log.

**Fix:** the loader should accumulate its own error list and throw with the messages inline.

---

## MINOR

### m1. Magic numbers
- `360`, `180`, `30`, `90`: scattered across [BasicCalculator.java](src/main/java/app/basic/BasicCalculator.java) (modular arithmetic).
- `15.0` for ARMC → LST [BasicCalculator.java:51](src/main/java/app/basic/BasicCalculator.java#L51).
- `0.9856`, `13.1764`, `1.3833`, `1.2`, `0.5240`, `0.0831`, `0.0335`, `-0.05295`: mean speeds [BasicCalculator.java:280-287](src/main/java/app/basic/BasicCalculator.java#L280-L287).
- `2440587.5` for JD epoch [BasicCalculator.java:850](src/main/java/app/basic/BasicCalculator.java#L850).
- `86400.0` seconds/day, same line.
- `1_000_000.0` for DECIMAL_6 [BasicCalculator.java:901](src/main/java/app/basic/BasicCalculator.java#L901).

Pull into named constants in an `AstroMath` class (with doc strings citing the source for mean speeds).

---

### m2. Strings where enums belong
- `angularity` ("ANGULAR"/"SUCCEDENT"/"CADENT") → enum.
- `syzygy.type` ("NEW_MOON"/"FULL_MOON") → enum.
- `moonPhase.name` ("NEW_TO_CRESCENT"/...) → enum.
- `orientationToSun` ("ORIENTAL"/"OCCIDENTAL") → enum.
- `sect` ("DIURNAL"/"NOCTURNAL") → enum.
- `condition` ("OF_SECT"/"CONTRARY_TO_SECT") → enum.
- `nodeType`, `positionType`, `frame` on `CalculationSetting` → enums (per audit §2 explicit checklist).
- `ChartPoint.type` ("PLANET"/"ANGLE"/"LOT") → enum.
- `element` ("FIRE"/"EARTH"/"AIR"/"WATER") → enum.

`String.equals("FULL_MOON")` checks ([BasicSyzygy.java:34](src/main/java/app/model/basic/BasicSyzygy.java#L34), [BasicCalculator.java:792](src/main/java/app/basic/BasicCalculator.java#L792)) and `mercurySect.equals("DIURNAL")` ([BasicCalculator.java:658](src/main/java/app/basic/BasicCalculator.java#L658)) are the bug-prone consequence.

---

### m3. `Optional` not used where appropriate
- `exaltationRuler` returns `Planet` and uses `null` for "no exaltation" ([BasicCalculator.java:361](src/main/java/app/basic/BasicCalculator.java#L361)). Should be `Optional<Planet>`.
- `termRuler` returns `null` for `Terms.NONE` ([BasicCalculator.java:425](src/main/java/app/basic/BasicCalculator.java#L425)).
- `longitudeFor` returns `Double` (boxed, nullable) ([BasicCalculator.java:151](src/main/java/app/basic/BasicCalculator.java#L151)) — and is then **dereferenced unconditionally** in `calculateSyzygy` line 790-791. NPE risk if swisseph fails.

---

### m4. Records would simplify several POJOs
`PlanetPosition`, `HousePosition`, `LotPosition`, `ChartAngle`, `BasicSyzygy`, `MoonPhase`, `SolarPhaseEntry`, `RawAspectMatrixEntry`, `RawDeclinationMatrixEntry`, `RawSignDistanceMatrixEntry`, `PairwiseRelation` (and inner `EclipticRelation`/`EquatorialRelation`), `LogEntry`, `DoctrineSummary`, `ReportMetadata`, `SimpleDescriptiveResult` — all immutable carriers. Java 17 records cut these in half.

`BasicChart` is the only one that *needs* mutability today (built incrementally in `calculate`); refactor `calculate` to a builder and `BasicChart` becomes a record too.

---

### m5. `LinkedHashMap` for ordering implies a structure that should be a record
[BasicCalculator.java:528, 596, 633, 648](src/main/java/app/basic/BasicCalculator.java#L528) — every `LinkedHashMap<String, Object>` is a record-shaped thing wearing a Map costume.

---

### m6. Floating-point comparison with `==`
[BasicCalculator.java:815](src/main/java/app/basic/BasicCalculator.java#L815) `earlierValue == 0.0` — exact-zero compare on a swisseph delta that has already been processed through arithmetic. The comparison is benign (it's an early-exit shortcut, not load-bearing), but it's a code smell.

---

### m7. `degreeInSign` returns negative on negative input
[BasicCalculator.java:329-331](src/main/java/app/basic/BasicCalculator.java#L329-L331)

```java
private double degreeInSign(double longitude) {
    return normalize(longitude) % 30.0;
}
```

`normalize(longitude)` always returns ≥ 0, so this is fine *as currently called*. But the audit asks: would a future caller passing a raw value get burned? Yes. The method should either re-normalize or be renamed to assert the precondition.

---

### m8. `signDistance` is duplicated and could vacate the enum
[BasicCalculator.java:264-267](src/main/java/app/basic/BasicCalculator.java#L264-L267) — uses `signA.ordinal() - signB.ordinal()`. Hardcoded `12 - distance`. Move onto `ZodiacSign.distanceTo(ZodiacSign)`.

---

### m9. `mercurySect` "ORIENTAL"/"OCCIDENTAL" naming with no doc
[BasicCalculator.java:659](src/main/java/app/basic/BasicCalculator.java#L659), [BasicCalculator.java:863-866](src/main/java/app/basic/BasicCalculator.java#L863-L866)

The convention "delta > 180 → DIURNAL → ORIENTAL" relies on the reader knowing that "oriental" = morning star = lower ecliptic longitude than the sun = `delta = (planet - sun) mod 360 > 180` because subtracting a smaller from a larger and wrapping gives ≥ 180. There is no comment. Future maintainers will get this wrong.

---

### m10. `Logger` entries lack timestamps
[LogEntry.java](src/main/java/app/output/LogEntry.java)

Only the run-level `startedAt` is recorded. A long-running process will produce a forensic-useless log. Add per-entry `Instant timestamp = Instant.now()`.

---

### m11. `Logger` has no `WARN` level
[Logger.java:25-31](src/main/java/app/output/Logger.java#L25-L31) — only `info` and `error`. Bad input produces ERROR; deprecated input or "field defaulted" produces nothing or ERROR. There is no middle ground.

---

### m12. Hard-coded paths
- [SubjectListParser.java:26](src/main/java/app/input/SubjectListParser.java#L26): `Path.of("input", "subject-list.json")`.
- [SettingLoader.java:17](src/main/java/app/input/SettingLoader.java#L17): `Path.of("input", "settings.properties")`.
- [App.java:36](src/main/java/app/App.java#L36): `Path.of("output", "descriptive", subject.getId(), doctrine.getId() + ".json")`.
- [App.java:41](src/main/java/app/App.java#L41): `Path.of("output", "run-logger.json")`.

The engine cannot be invoked from anywhere but its CWD. Trivial in CLI, painful in tests and any host process.

---

### m13. Method length
- `BasicCalculator.calculate` (33 lines) — borderline.
- `BasicCalculator.calculatePoints` (66 lines, [527-593](src/main/java/app/basic/BasicCalculator.java#L527-L593)) — way over. Each branch (planet/angle/lot/syzygy) is its own concern.
- `BasicCalculator.calculatePlanets` is fine as a builder.
- `BasicCalculator.calculatePlanet` (28 lines) — fine, but the constructor call has 16 positional arguments. `PlanetPosition` calls for a builder.

---

### m14. `calculateSect` ignores the `chart.getMoon()` for Mercury sect when Sun is missing
[BasicCalculator.java:629-633](src/main/java/app/basic/BasicCalculator.java#L629-L633)

If `sun == null` (swisseph error), `diurnal = false` (NOCTURNAL), `lightOfSect = MOON`, but the chart simultaneously logs an error. The chart structurally claims a sect derived from a missing Sun.

---

### m15. `BasicSyzygy.getLongitude` discriminator-by-string
[BasicSyzygy.java:34](src/main/java/app/model/basic/BasicSyzygy.java#L34)

```java
public double getLongitude() { return "FULL_MOON".equals(type) ? moonLongitude : sunLongitude; }
```

If `type` is misspelled or null, `sunLongitude` is silently returned. Type as enum (m2) would let the compiler verify exhaustiveness.

---

### m16. `App.main throws Exception`
[App.java:21](src/main/java/app/App.java#L21) — a `throws Exception` declaration discards type information about what can go wrong.

---

### m17. `lord` of orb / planetary hour / annual profections are deleted from the source
The git status shows:
```
D src/main/java/app/common/model/NativeAnnualProfections.java
D src/main/java/app/common/model/NativeLordOfOrb.java
D src/main/java/app/common/model/NativePlanetaryHour.java
... etc
```

These are stage-1 spec features. Either they were intentionally cut for this slice (`spec.md` is also deleted — verify intent), or there's a delivery gap.

---

### m18. `domicileRuler`, `triplicityRulers`, `faceRuler` not exposed as a unit
For testability, the lookup tables need to be reachable independently of `BasicCalculator`. Right now they are private methods on the calculator. A unit test for "Egyptian terms in Aries are 0-6 Jupiter, 6-14 Venus, 14-21 Mercury, 21-26 Mars, 26-30 Saturn" cannot be written without instantiating a full calculator and indirecting through `termRuler(longitude, terms)`.

---

### m19. Spec rounding policy applied at calculation time
[BasicCalculator.java:899-904](src/main/java/app/basic/BasicCalculator.java#L899-L904)

```java
private double round(double value, Input input) {
    if (input.getCalculationSetting().getRoundingPolicy() == RoundingPolicy.DECIMAL_6) {
        return Math.round(value * 1_000_000.0) / 1_000_000.0;
    }
    return value;
}
```

Audit §3 calls this out specifically: rounding is applied **at calculation time**, not at serialization. The result is that internal arithmetic propagates rounded values:
- `southNodeLongitude = normalize(northNode.getLongitude() + 180.0)` — but `northNode.getLongitude()` is already rounded ([BasicCalculator.java:92](src/main/java/app/basic/BasicCalculator.java#L92)).
- `houseOf(southNodeLongitude, ascendant)` is called with a rounded longitude.
- `signDistance` and `pairwiseRelation` matrices use rounded longitudes.

For DECIMAL_6 the precision loss is currently below astronomical signal, so the fixture passes. But the **architecture is wrong**: `RoundingPolicy.DECIMAL_4` (when added) would start to corrupt aspect calculations. Rounding belongs in serializers, not in the domain.

---

### m20. `InputListBundle` is a mutable bag of setters
[InputListBundle.java](src/main/java/app/model/input/InputListBundle.java) — eight setters, two-stage construction. Should be a record assembled in `InputLoader.load` from finished pieces.

---

### m21. `BasicChart.getEntries()` race
[Logger.java:22-23](src/main/java/app/output/Logger.java#L22-L23) — `List.copyOf(entries)` while another thread `entries.add(...)` throws `ConcurrentModificationException`. (Same root cause as C2.)

---

## NIT

### n1. Naming inconsistencies
- `getRetrograde()` not `isRetrograde()` ([PlanetPosition.java:69](src/main/java/app/model/basic/PlanetPosition.java#L69)). Standard JavaBean convention says boolean → `is`.
- `EquatorialRelation.isSameHemisphere()` does follow the convention; `PlanetPosition.getRetrograde()` doesn't. Pick one.
- `armc` is lower-case, `ARMC` would be the convention for an acronym constant; as a field, `armc` is fine but `getArmc()` looks odd.

---

### n2. `private record TermBoundary` declared inside `BasicCalculator`
[BasicCalculator.java:911](src/main/java/app/basic/BasicCalculator.java#L911) — fine for now, but if terms are extracted to their own class (M2), this moves with them.

---

### n3. `terms(int, Planet, ...)` builder is a 5-arity hardcoded helper
[BasicCalculator.java:472-480](src/main/java/app/basic/BasicCalculator.java#L472-L480) — varargs would be cleaner: `private TermBoundary[] terms(Object... pairs)`. Or just inline `new TermBoundary[]{...}`.

---

### n4. Wildcard / star imports — none present, good.

---

### n5. `App.java:18` — `final class App` with `private App()` is correct utility-class form.

---

### n6. Indentation/whitespace are fine and consistent.

---

### n7. `if (sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12)` repeated three times
[BasicCalculator.java:632, 641, 642](src/main/java/app/basic/BasicCalculator.java#L632) — extract `sunAboveHorizon` as a local variable.

---

### n8. `final` keyword inconsistent on locals
Sprinkled here and there; pick a convention.

---

### n9. `app.swisseph.*` package layout is huge (50+ files)
This is the vendored swisseph port, not application code; out of scope for this audit, but the 50-file list adds noise to glob results.

---

## What is genuinely clean

- The package partition `app.basic / app.doctrine / app.input / app.model / app.output` is sensible at the top level.
- `ZodiacSign`, `Planet`, `Terms`, `Zodiac`, `HouseSystem` are properly enums (modulo the `UNKNOWN`/`NONE` anti-patterns called out above).
- `signDistance` arithmetic is correct.
- `normalize` is correct and reused.
- `Math.floorMod` is used in `houseOf` (correct).
- `swe_calc_ut` errors are at least *logged*, even if then swallowed.

---

## Top-priority refactor backlog (in order)

1. **C7 / M6 — Kill `Map<String, Object>`.** This is the structural defect that will most slow stage 2.
2. **C1 — Fix south node speed/retrograde.**
3. **C2 / m21 — De-singleton `Logger`.**
4. **C3 — Document or scope `SwissEph` non-thread-safety.**
5. **M1 / M2 — Extract `LookupTables` and `AstroMath`. Delete dead `traditional/` package.**
6. **M5 / m2 — Replace stringly-typed enums and silent-fallback parses.**
7. **M10 — Stop returning `0.0` on swisseph failure.**
8. **C4 / C5 — Fix moon-phase boundary; replace house-based sect with altitude-based sect.**
9. **m19 — Move rounding to serialization.**
10. **M13 / M14 — Decouple Jackson from domain; configure mapper strictly.**
