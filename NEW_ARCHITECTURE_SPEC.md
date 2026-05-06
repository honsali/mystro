# Mystro New Architecture Specification

## 1. Project goal

Mystro is a self-contained Java traditional astrology calculation engine exposed as a Spring Boot REST application.

Current output families:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
```

Predictive output is still an architectural target. The currently implemented application mode is stateless REST descriptive calculation JSON.

---

## 2. Current pipeline

```text
REST request loading
→ Input validation / normalization
→ Doctrine descriptive calculation
→ Doctrine predictive calculation
→ JSON response
```

Basic chart calculation is not a separate report stage. It is shared infrastructure used inside doctrine-owned descriptive and predictive calculation.

Current descriptive runtime flow:

```text
POST /api/descriptive
  ↓
DescriptiveRequest(id, birth data, doctrine)
  ↓
DescriptiveRequestMapper validates and resolves Subject + Doctrine
  ↓
LoggerIsolationFilter wraps /api/** request lifecycle in thread-isolated ephemeral logging
  ↓
DescriptiveController calls doctrine.calculateDescriptive(subject, BasicCalculator)
  ↓
CalculationContext(subject, doctrine calculation choices)
  ↓
describe(ctx, natalChart)
  ↓
desctrine calculators pour data into NatalChart
  ↓
DescriptiveAstrologyReport
  ↓
Direct JSON response: { engineVersion, subject, doctrine, natalChart }
```

---

## 3. Doctrine concept

A doctrine is a hardcoded Java knowledge module.

It is not:

```text
- a settings file
- a doctrine profile
- a partial implementation of a universal astrology schema
```

Each doctrine defines:

```text
- its identity
- its basic calculation choices
- which concepts are meaningful
- which calculations it performs
- which predictive techniques it exposes
- how it pours doctrine-specific data into NatalChart or predictive reports
```

Absent concepts are simply absent from that doctrine's output. Execution-level errors belong to application handling/logging, not to astrology report data.

---

## 4. Current inputs

The current application input is REST JSON containing natal birth data plus one explicit doctrine id.

Current request shape:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utcOffset": "+01:00",
  "doctrine": "valens"
}
```

No hidden default doctrine should be introduced.

The repository may still contain representative natal fixtures such as `input/subject-list.json`, but the application does not load runtime subjects from that file anymore.

There is currently no calculation settings object. Do not add settings until they are wired into calculation or reporting behavior.

---

## 5. Core Java contracts

Current doctrine contract:

```java
public abstract class Doctrine {
    public NatalChart calculateDescriptive(
        Subject subject,
        BasicCalculator basicCalculator
    );

    public NatalChart calculateNatalChart(
        CalculationContext ctx,
        BasicCalculator basicCalculator
    );

    public abstract void describe(CalculationContext ctx, NatalChart chart);

    public abstract DoctrineInfo getDoctrineInfo();
}
```

`DoctrineInfo` exposes doctrine-owned basic choices:

```text
- id
- name
- house system
- zodiac
- terms
- triplicity
- node type
```

The engine targets the Valens-to-Lilly tropical tradition; sidereal zodiac calculation is out of scope for current doctrine modules.

`CalculationContext` is the per-run internal context. It owns the subject, doctrine-derived calculation choices, Swiss Ephemeris state, Julian day, house cusps, `ascmc`, ARMC, and shared helpers. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant have one source of truth.

### Intentional calculation conventions

These are current calculation choices, not accidental omissions:

- Planet positions are geocentric, matching pre-modern practice.
- Planet positions use Swiss Ephemeris apparent positions; light-time, aberration, and gravitational deflection are retained.
- Planet positions require the file-backed Swiss Ephemeris data shipped under `ephe/`; Moshier fallback is rejected for runtime planet calculations.
- Lunar parallax is not corrected by converting the Moon to a topocentric position.
- Placidus failures, including polar-region failures from Swiss Ephemeris, fail fast and are logged; there is no silent fallback house system.
- A Moon exactly 180° ahead of the Sun is treated as waxing by convention.

---

## 6. NatalChart

`NatalChart` is the single chart object in descriptive output.

It contains mechanical/basic chart facts plus doctrine-poured descriptive facts.

Current top-level report shape:

```json
{
  "engineVersion": "<version>",
  "subject": {},
  "doctrine": {},
  "natalChart": {}
}
```

The REST descriptive endpoint returns exactly one report as a direct JSON object:

```json
{
  "engineVersion": "<version>",
  "subject": {},
  "doctrine": {},
  "natalChart": {}
}
```

`GET /api/doctrines` returns a direct JSON array of doctrine info objects.

The REST adapter is stateless and local-first: it does not write output files. A frontend can call once per doctrine and save one local JSON report file per doctrine.

There is no top-level `basicChart` key and no top-level `descriptive` key.

Current `natalChart` includes mechanical facts such as:

```text
- resolvedUtcInstant
- julianDayUt / julianDayTt / deltaTSeconds
- armc
- localApparentSiderealTimeHours
- trueObliquity / meanObliquity
- nutationLongitude / nutationObliquity
- points
- houses
- pairwiseRelations
- moonPhase
- sect
```

Doctrine calculators pour doctrine data into this same `NatalChart`, for example:

```text
- syzygy
- lots
- point dignity/debility assessments
- pairwise relation aspects
- point solar conditions
```

---

## 7. Points

`natalChart.points` is keyed by point name and contains planet and angle point entries.

Planet points include mechanical placement data, ruler data, and doctrine-poured point-specific assessments.

Current planet point fields include:

```text
- longitude
- sign
- degreeInSign
- latitude
- rightAscension
- declination
- altitude
- aboveHorizon
- speed / meanDailySpeed / speedRatio
- retrograde
- house / wholeSignHouse / quadrantHouse
- angularity
- antisciaLongitude / contraAntisciaLongitude
- domicileRuler
- exaltationRuler
- triplicityRuler
- participatingTriplicityRuler
- termRuler
- faceRuler
- detrimentRuler
- fallRuler
- dignities
- debilities
- solarPhase
- sect
- solarCondition, when calculated by the selected doctrine
```

`triplicityRuler` is the chart-sect-selected ruler. `participatingTriplicityRuler` preserves the auxiliary triplicity participant when the doctrine's triplicity table has one.

`dignities` and `debilities` are arrays on planet points, not a separate `natalChart.dignities` map.

`solarPhase` and planet-specific sect information are injected into planet points. Chart-level `sect` remains for whole-chart sect facts.

Basic chart sect is currently altitude-based: Sun above horizon is diurnal, Sun below horizon is nocturnal. This is a mechanical baseline only. Doctrine modules may refine sect interpretation for twilight births, apparent/refraction-adjusted horizon, or author-specific treatment of the Sun slightly below the horizon. Such refinements belong to doctrine descriptive calculation and should be poured into `NatalChart` explicitly rather than changing the shared baseline silently. Altitude uses `>= 0.0` as the baseline above-horizon rule; exact horizon edge cases can be refined by doctrine logic if needed.

Planet-specific sect membership is intentionally limited to the seven traditional planets. The lunar nodes are chart points, but they do not have planetary sect, so `NORTH_NODE` and `SOUTH_NODE` do not receive a point-level `sect` object.

Essential dignity/debility assessment is likewise limited to the seven traditional planets; nodes remain positional points and intentionally do not receive dignity/debility assessment.

`angularDistanceFromSun` is stored as `0.0` for the Sun itself because the Sun is measured against its own longitude. Doctrine solar-condition calculators must special-case the Sun; `0.0` for the Sun does not mean the Sun has a solar condition such as cazimi or combustion.

`solarCondition` is doctrine-poured. Currently Valens calculates it; Ptolemy does not.

---

## 8. Pairwise relations and aspects

`natalChart.pairwiseRelations` is the raw/mechanical pair matrix for planets and angles.

Every relation has raw geometry:

```json
{
  "pointAName": "SUN",
  "pointBName": "MERCURY",
  "ecliptic": {
    "angularSeparation": 17.884368,
    "signDistance": 0
  },
  "equatorial": {
    "declinationDifference": 0.799742,
    "contraParallelSeparation": 0.799742,
    "sameHemisphere": true
  }
}
```

`declinationDifference` is the parallel-distance primitive. `contraParallelSeparation` is the contraparallel-distance primitive. Both are emitted to avoid forcing downstream code to infer contraparallel distance from asymmetric fields.

Doctrine aspect calculators annotate matching pairwise relations with an `aspect` object when that doctrine recognizes a meaningful aspect/configuration for that pair:

```json
{
  "aspect": {
    "type": "CONJUNCTION",
    "orbFromExact": 17.884368
  }
}
```

Absence of `aspect` means the selected doctrine did not identify a doctrine-relevant aspect for that pair. It does not mean no possible doctrine could treat the pair as meaningful.

---

## 9. Current doctrine coverage

Valens descriptive calculation currently pours:

```text
- prenatal syzygy
- Fortune and Spirit lots
- sign-based aspects, including conjunction among traditional planets
- dignity/debility arrays into planet points
- solar condition into planet points
```

Ptolemy descriptive calculation currently pours:

```text
- prenatal syzygy
- Ptolemaic sign configurations, excluding conjunction
- dignity/debility arrays into planet points
```

Dorotheus is present as a doctrine module but has no descriptive doctrine-poured sections yet.

Fixed stars are not implemented. If added, the star set must be explicitly parameterized; no doctrine-free canonical star list should be assumed.

### Adding a doctrine module

To add a doctrine, implement `app.doctrine.Doctrine` under `app.doctrine.impl.<doctrineId>`, provide a `DoctrineInfo` with its calculation choices via `getDoctrineInfo()`, register it in `DoctrineLoader`, and add doctrine-specific descriptive calculators under `app.descriptive.<doctrineId>.calculator` when the doctrine has descriptive concepts to pour into `NatalChart`.

---

## 10. Output and logging

Current descriptive REST response shape (direct `DescriptiveAstrologyReport` object):

```json
{
  "engineVersion": "<version>",
  "subject": {},
  "doctrine": {},
  "natalChart": {}
}
```

`GET /api/doctrines` returns a direct JSON array:

```json
[
  { "id": "...", "name": "...", ... },
  ...
]
```

The backend does not write descriptive output files. Frontends should save reports locally.

Predictive output remains a target architecture area; no predictive REST endpoint is implemented yet.

Execution-level statuses are not astrological results and do not belong inside doctrine report data.

REST `/api/**` requests use lifecycle-wide thread-isolated ephemeral logging via `LoggerIsolationFilter`, so request calculation log entries do not accumulate or persist by default. REST does not return execution logs in report JSON.

The report `engineVersion` comes from the configured `mystro.engine-version` application property.

JSON output rounds doubles at serialization through `RoundedDoubleSerializer`; calculators keep full internal double precision. REST responses use the same Mystro Jackson configuration as file-oriented helpers such as `MystroObjectMapper`, while Spring Boot's global application `ObjectMapper` is not intentionally replaced. REST descriptive responses and errors use `Cache-Control: no-store`.

---

## 11. Current package layout

Current app code lives under:

```text
src/main/java/app/
  MystroSpringApplication.java
  input/
  basic/
  chart/
  descriptive/
  doctrine/
  output/
  runtime/
  web/
  swisseph/
```

Important current classes:

```text
app.basic.BasicCalculator
app.basic.CalculationContext
app.chart.model.NatalChart
app.doctrine.Doctrine
app.output.DescriptiveAstrologyReport
app.MystroSpringApplication
```

Shared chart data/model classes live under `app.chart.data` and `app.chart.model`. `NatalChart` is the shared chart container: `BasicCalculator` pours root mechanical data first, then doctrine/descriptive calculators enrich or annotate the same chart.

Basic calculators are focused stateless calculators under `app.basic.calculator`.

Descriptive doctrine calculators live under packages such as:

```text
app.descriptive.valens.calculator
app.descriptive.ptolemy.calculator
```

Doctrine implementations live under:

```text
app.doctrine.impl.<doctrineId>
```

Shared descriptive data atoms currently live under:

```text
app.descriptive.common.data
app.descriptive.common.model
```

Spring Boot adapter classes live under:

```text
app.web.business      — controllers, request DTOs, request mapper, error model
app.web.infra         — configuration, CORS properties, filters, global exception handler
```

Reusable application/runtime orchestration lives under:

```text
app.runtime
```

The Spring Boot layer is an adapter only; do not duplicate astrology calculation logic in controllers or DTOs.

---

## 12. Development rules

After Java changes, run:

```bash
mvn compile
```

After behavior or web/API changes, run:

```bash
mvn test
```

For packaging verification, run:

```bash
mvn package -DskipTests
```

To start the application locally:

```bash
mvn spring-boot:run
```

Documentation should keep this file authoritative and remove stale architecture references instead of creating competing descriptions.
