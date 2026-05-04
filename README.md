# Mystro

Mystro is a self-contained Java traditional astrology calculation engine.

The authoritative architecture specification is:

- [`NEW_ARCHITECTURE_SPEC.md`](NEW_ARCHITECTURE_SPEC.md)

## Current architecture

```text
Input loading
→ Input validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ Formatting / printing
→ Reference validation
```

Current output families:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
```

A doctrine is a hardcoded Java knowledge module, not a settings profile.

## Current implementation status

Implemented now:

- natal input loading from `input/native-list.json`
- explicit CLI subject/doctrine selection
- doctrine-owned descriptive calculation
- shared Swiss Ephemeris-backed `BasicCalculator`
- unified `NatalChart` descriptive output
- Valens and Ptolemy descriptive doctrine calculations
- JSON report writing
- run logging

Current descriptive reports expose top-level:

```text
engineVersion, subject, doctrine, calculationSetting, natalChart
```

There is no top-level `basicChart` key and no top-level `descriptive` key.

## Input

`input/native-list.json` contains natal data only.

Current natal entry shape:

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

## Run

Build:

```bash
mvn compile
```

Run a subject with explicit doctrine modules:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

No hidden default doctrine should be introduced.

## Current output

```text
output/{subjectId}/{doctrineId}-descriptive.json
output/run-logger.json
```

Target predictive output path:

```text
output/{subjectId}/{doctrineId}-predictive.json
```
