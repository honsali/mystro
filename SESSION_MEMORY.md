# Session Memory

## Current state

Mystro is a self-contained Java traditional astrology calculation engine.

Authoritative specification:

- `NEW_ARCHITECTURE_SPEC.md`

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
descriptive
predictive
```

Current implemented output:

```text
output/{subjectId}/{doctrineId}-descriptive.json
output/run-logger.json
```

Predictive is an architectural target and is not implemented yet.

## Durable direction

A doctrine is a hardcoded Java knowledge module.

It is not a settings file, not a doctrine profile, and not a partial universal schema implementation.

Basic chart calculation is not a separate report stage. `BasicCalculator` is shared infrastructure called by the doctrine through `Doctrine.calculateNatalChart(...)`.

## Current implementation facts

- Fresh Java app code exists under `src/main/java/app/`.
- `app.old` is reference/migration material only.
- `input/native-list.json` contains natal data only.
- Doctrine selection is explicit through `--doctrines ...`.
- No doctrine is selected by default.
- Current doctrine modules: `dorotheus`, `ptolemy`, `valens`.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, `calculationSetting`, and `natalChart` fields.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- `NatalChart` contains mechanical chart facts and doctrine-poured descriptive facts.
- Doctrine calculators pour data directly into `NatalChart`; there is no `DescriptiveResult` boundary and no doctrine-specific descriptive data record.
- `natalChart.points` is keyed by point name. Planet points carry point-specific dignities/debilities, solar phase, planet sect info, and doctrine solar condition when calculated.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects.
- `CalculationContext` carries the subject, doctrine-derived calculation choices, calculation settings, Swiss Ephemeris state, full Julian day, cusps, `ascmc`, ARMC, and shared helpers through basic and descriptive calculation.
- `BasicCalculator` keeps full internal double precision; JSON output rounds doubles through `RoundedDoubleSerializer`.
- Current Valens output pours prenatal syzygy, Fortune/Spirit lots, sign-based aspects including conjunction, dignity/debility assessments, and solar condition into `NatalChart`.
- Current Ptolemy output pours prenatal syzygy, Ptolemaic sign configurations excluding conjunction, and dignity/debility assessments into `NatalChart`.
- Dorotheus is present but has no doctrine-poured descriptive sections yet.
- Fixed stars are not implemented.
- `Logger.instance` is intentionally retained for the short-term CLI.

## Commands

Build:

```bash
mvn compile
```

Representative runtime check:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Current Maven version for the next development cycle is `0.12.0`.

## Read first in future sessions

1. `NEW_ARCHITECTURE_SPEC.md`
2. `AGENTS.md`
3. `README.md`
