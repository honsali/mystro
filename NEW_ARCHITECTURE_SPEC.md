# Mystro New Architecture Specification

## 1. Project goal

Mystro is a self-contained Java traditional astrology calculation engine.

Current output families:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
```

There are only two report files per requested subject/doctrine:

```text
output/{subjectId}/{doctrineId}-descriptive.json
output/{subjectId}/{doctrineId}-predictive.json
```

Predictive output is architectural target work; current implemented runtime output is descriptive JSON plus run logging.

---

## 2. Current pipeline

```text
Input loading
→ Input validation / normalization
→ Doctrine descriptive calculation
→ Doctrine predictive calculation
→ Formatting / printing
```

Basic chart calculation is not a separate report stage. It is shared infrastructure used inside doctrine-owned descriptive and predictive calculation.

Current descriptive runtime flow:

```text
InputLoader
  ↓
InputListBundle(subjects, doctrines)
  ↓
for each subject × doctrine:
    doctrine.calculateDescriptive(subject, BasicCalculator)
      ↓
    CalculationContext(subject, doctrine calculation choices)
      ↓
    doctrine.calculateNatalChart(ctx, BasicCalculator)
      ↓
    NatalChart
      ↓
    doctrine.describe(ctx, natalChart)
      ↓
    doctrine calculators pour data into NatalChart
      ↓
    DescriptiveAstrologyReport
      ↓
    output/{subjectId}/{doctrineId}-descriptive.json
```

The application layer writes reports. The doctrine owns when and how `BasicCalculator` is called.

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

Absent concepts are simply absent from that doctrine's output. Execution-level errors belong to run logging/application handling, not to astrology report data.

---

## 4. Current inputs

Natal input records contain birth data only. The current native input file is `input/subject-list.json`.

Current native input shape:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utcOffset": "+01:00"
}
```

Doctrine selection is explicit through CLI:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

No hidden default doctrine should be introduced.

There is currently no calculation settings object. Do not add settings until they are wired into calculation or reporting behavior.

---

## 5. Core Java contracts

Current doctrine contract:

```java
public interface Doctrine extends CalculationDefinition {
    default NatalChart calculateDescriptive(
        Subject subject,
        BasicCalculator basicCalculator
    );

    default NatalChart calculateNatalChart(
        CalculationContext ctx,
        BasicCalculator basicCalculator
    );

    void describe(CalculationContext ctx, NatalChart chart);
}
```

`CalculationDefinition` exposes doctrine-owned basic choices:

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

`CalculationContext` is the per-run internal context. It owns the subject, doctrine-derived calculation choices, calculation settings, Swiss Ephemeris state, Julian day, house cusps, `ascmc`, ARMC, and shared helpers. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant have one source of truth.

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
  "engineVersion": "0.14.0",
  "subject": {},
  "doctrine": {},
  "natalChart": {}
}
```

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

To add a doctrine, implement `app.doctrine.Doctrine` under `app.doctrine.impl.<doctrineId>`, choose its `CalculationDefinition` values, register it in `DoctrineLoader`, and add doctrine-specific descriptive calculators under `app.descriptive.<doctrineId>.calculator` when the doctrine has descriptive concepts to pour into `NatalChart`.

---

## 10. Output and logging

Current descriptive output path:

```text
output/{subjectId}/{doctrineId}-descriptive.json
```

Target predictive output path:

```text
output/{subjectId}/{doctrineId}-predictive.json
```

Run logging currently writes:

```text
output/run-logger.json
```

Execution-level statuses are not astrological results and do not belong inside doctrine report data.

The report `engineVersion` is loaded from the first project `<version>` in `pom.xml`.

JSON output rounds doubles at serialization through `RoundedDoubleSerializer`; calculators keep full internal double precision.

---

## 11. Current package layout

Current app code lives under:

```text
src/main/java/app/
  App.java
  input/
  basic/
  chart/
  descriptive/
  doctrine/
  output/
  swisseph/
```

Important current classes:

```text
app.basic.BasicCalculator
app.basic.CalculationContext
app.chart.model.NatalChart
app.doctrine.Doctrine
app.output.DescriptiveAstrologyReport
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

---

## 12. Development rules

After Java changes, run:

```bash
mvn compile
```

Representative runtime check:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

The CLI subject selector is `--subjects`.

Documentation should keep this file authoritative and remove stale architecture references instead of creating competing descriptions.
