
# Severe code audit — Java implementation of stage 1 astrological engine

You are auditing the Java source code of a stage 1 calculation engine. The output it produces is already known to be numerically correct against the reference fixture. **Numerical correctness is not in scope.** Your job is to audit the code itself: structure, design, idiomaticity, robustness, maintainability, and trap doors.

This is an **adversarial review**. Do not be polite. Do not give credit for intent. Do not soften findings. Assume every line is wrong until you have verified it is right. The author is competent and wants real feedback, not encouragement.

## What you are looking for

### 1. Architectural integrity

- Is the separation between input, calculation, and output models clean, or do calculation classes leak into the API surface?
- Is there a clear boundary between the swisseph-facing layer (raw astronomy) and the arithmetic layer (sign/antiscia/aspects/lots)? Or are they entangled?
- Does the doctrine-parameterized layer (terms, houseSystem, zodiac) live in one place, or is it scattered across multiple classes that each re-read `input.doctrine`?
- Is there any class that knows about more than it should — for instance, a `PointBuilder` that also computes pairwise relations, or a `ChartCalculator` that handles both basic and descriptive (when descriptive comes back)?
- Are the lookup tables (Egyptian terms, Ptolemaic terms, triplicities, faces, domiciles, exaltations) defined once and reused, or duplicated? Are they immutable? Are they loaded lazily or eagerly, and is that the right choice?
- Is the JSON serialization layer decoupled from the domain model, or are Jackson annotations polluting the calculation classes?

### 2. Type safety and modeling

- Are enums used wherever the domain has a closed set of values (`Sign`, `Planet`, `Angularity`, `Sect`, `Orientation`, `MoonPhase`, `SyzygyType`, `PointType`, `HouseSystem`, `ZodiacType`, `TermSet`, `NodeType`, `PositionType`, `Frame`)? Or are strings used in places where an enum would prevent bugs?
- Are angles represented by a dedicated type (`Longitude`, `Declination`, `EclipticAngle`) or by raw `double`? If raw `double`, is the unit ambiguous anywhere (degrees vs radians, signed vs unsigned)?
- Are points modeled with a sealed hierarchy (`Planet`, `Angle`, `Lot`, `SyzygyPoint`) or with a single class that has nullable fields for everything? A planet does not have the same shape as an angle — does the code model that, or does it pretend they are the same and use null as a sentinel?
- Are `null` and `Optional` used consistently? In particular, is `fallRuler` modeled as `Optional<Planet>` or as a nullable `Planet`? Is the choice made deliberately or accidentally?
- Are records used where appropriate (immutable data carriers), or are mutable POJOs used everywhere?
- Are collections returned as immutable views, or do calculation methods leak mutable internal state?

### 3. Numerical and arithmetic robustness

The output is known correct, but verify the **code itself** is robust against edge cases and future inputs:

- Modular arithmetic on longitudes: every `(x + y) mod 360` must handle negative intermediate values correctly. Does the code use `((x % 360) + 360) % 360` or just `x % 360`? Java's `%` returns negative values for negative operands — is that handled everywhere it matters?
- Pairwise angular separation: must clamp to `[0, 180]`. Verify there is no path where a value of 180.0000001 or -0.0000001 leaks out due to floating-point error.
- Antiscia formula: `(180 − longitude + 360) mod 360` and contre-antiscion `(360 − longitude) mod 360`. Are both correctly implemented, are they shared via a single utility, or duplicated?
- Sign distance must always return an integer in `[0, 6]`. Is the modular wrap correct? Does it handle the antipodal case (distance 6) without ambiguity?
- Speed ratios: division by mean speed. Is there any planet whose mean speed could be zero or near-zero in the code path? (No, in current scope, but is the code defensive?) For the lunar node, mean speed is negative — does the ratio computation handle the sign correctly?
- Julian Day → calendar date conversions for the prenatal syzygy: is the algorithm correct, robust around month and year boundaries, and timezone-clean?
- Obliquity: is it the IAU 2006 formula, the Laskar formula, or a constant? Does the choice match what the rest of the engine assumes?
- Are floating-point comparisons ever done with `==` instead of an epsilon? Are equality checks on doubles avoided entirely in business logic?
- Rounding: where is `roundingPolicy: DECIMAL_6` actually applied? At calculation time (wrong — loses precision) or only at serialization time (correct)? Verify.

### 4. Doctrine parameterization

The contract requires that `input.doctrine` parameters drive the output. Audit each:

- Is there a switch on `terms` somewhere that selects between `EGYPTIAN` and `PTOLEMAIC` lookup tables? Is the switch exhaustive (does it handle all enum values, or fall through silently on unknown ones)?
- Is there a switch on `houseSystem`? Currently only `WHOLE_SIGN` is implemented — does the code throw on other values, or silently fall back to whole-sign, or compute something wrong?
- Is there a switch on `zodiac`? Currently only `TROPICAL`. Same question.
- Are there any **other** implicit doctrinal choices baked into the code that should be parameters? (Triplicity ordering: Doroteus vs Ptolemy. Face ordering: always Chaldean, but is that hardcoded with a comment, or hidden? Mean vs true node: declared in metadata but is the code path actually conditional on it, or does it always emit mean?)
- For each `input.doctrine` field, can you write a unit test that flips it and confirms the output changes only in the expected places?

### 5. Error handling and contract enforcement

- What happens when `swisseph` fails or returns an error code? Is it caught, logged, swallowed, or propagated?
- What happens when the input birth time is missing the timezone offset? When latitude is out of `[-90, 90]`? When longitude is out of `[-180, 180]`? Are these validated at the boundary, or do they corrupt the calculation silently?
- What happens at the polar circles where the Ascendant is undefined for certain times? Does the code handle it, throw, or produce garbage?
- What happens at exact noon/midnight where sect determination by horizon is ambiguous (Sun exactly on horizon)? Does the code have a tie-breaking rule, and is it documented?
- Are there assertions or invariant checks anywhere? Is the code defensive against its own bugs, or does it trust everything?
- What happens if the JSON is partially deserialized (missing field, wrong type)? Does Jackson silently accept defaults that mask bugs?

### 6. Concurrency and statefulness

- Are calculation classes stateful or stateless? If stateful, is the state thread-safe? If stateless, are they trivially reentrant?
- Are there any static mutable fields anywhere?
- Is swisseph (which is C-backed and historically not thread-safe) accessed from a single thread, behind a lock, or unsafely from multiple threads?
- Is there any caching, and if so, is the cache key correct (does it include all doctrine parameters)?

### 7. Code smells and idiomatic Java

- Are there `instanceof` chains where polymorphism would be cleaner? Sealed interfaces with pattern matching (Java 21+) are now idiomatic — is the code using them, or stuck on older idioms?
- Are loops written imperatively when streams would be clearer (or vice versa — streams used where a plain loop would be more readable and faster)?
- Magic numbers: any `360`, `180`, `30`, `6`, `90`, `23.4` in the code without a named constant?
- Method length: any method over ~30 lines doing more than one thing?
- Class length: any class over ~300 lines that should be split?
- Naming: are method names verbs, class names nouns, boolean accessors `isX` / `hasX`? Any abbreviations that obscure meaning (`calc`, `proc`, `sep` instead of `calculate`, `process`, `separation`)?
- Are exceptions checked or unchecked, and is the choice consistent and justified?
- Is logging present where it should be (boundary calls, error paths) and absent where it shouldn't (hot calculation loops)?

### 8. Testability

- Can any single calculation be unit-tested in isolation, or does everything require a full chart computation?
- Are the lookup tables exposed for testing (so you can verify the Egyptian terms table independently of the chart pipeline)?
- Is swisseph mocked or stubbed for fast unit tests, or do all tests hit the real ephemeris?
- Is there a test fixture with the Lille 1975 chart, asserted to the sixth decimal? If yes, is it brittle (will break on legitimate refactors) or robust (asserts on a stable contract)?
- Is there a property-based test anywhere (e.g., for any longitude, antiscion of antiscion equals the longitude)?

### 9. Build, dependencies, and packaging

- Is swisseph pinned to a specific version? Is the version recorded in the `ephemerisVersion` metadata derived from the actual jar version, or hardcoded as a string that can drift?
- Is Jackson configured to fail on unknown properties (strict input) and to omit nulls on output, or are the defaults left in place silently?
- Are there transitive dependencies pulled in unnecessarily?
- Is the module structure (Maven/Gradle) clean? Is there a single jar, or proper modularization?
- Is there any reflection, annotation processing, or bytecode manipulation that could surprise a future maintainer?

## How to deliver findings

Organize your audit by severity:

- **Critical** — bugs that produce wrong output under some input, security issues, data corruption risks, thread-safety violations.
- **Major** — architectural problems that will block stage 2 implementation, contract violations (doctrine leakage, redundancy), missing error handling on realistic inputs.
- **Minor** — code smells, idiomatic issues, missing tests, suboptimal but functional code.
- **Nit** — style, naming, formatting.

For each finding, provide:
1. The file and line number.
2. A short description of the problem.
3. Why it matters (concrete failure mode if possible).
4. A suggested fix in code if straightforward, or a description of the refactor if not.

Do not produce a "looks good" verdict at the end. If the code is genuinely clean in some area, say so briefly and move on. Spend your effort on what is wrong.

## What is explicitly out of scope

- Numerical correctness of the outputs (already verified separately).
- Stage 2 (descriptive) and stage 3 (predictive) — they are not implemented yet.
- The JSON schema design itself (already audited and finalized).
- Performance optimization, unless a hot path is so wrong it would matter at 1000 charts/second.

---
 