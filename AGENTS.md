# Mystro Agent Notes

## Authoritative specification

Read first:

- `NEW_ARCHITECTURE_SPEC.md`

## Project goal

Mystro is a self-contained Java traditional astrology calculation engine.

Current output families:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
```

There are only two doctrine report paths:

```text
output/{subjectId}/{doctrineId}-descriptive.json
output/{subjectId}/{doctrineId}-predictive.json
```

Predictive is an architectural target; current implemented runtime output is descriptive JSON plus run logging.

## Doctrine philosophy

A doctrine is a hardcoded Java knowledge module.

It is not:

- a settings file
- a doctrine profile
- a partial implementation of a universal astrology schema

Each doctrine defines what exists for that doctrine, which concepts are meaningful, which calculations it performs, and how it pours doctrine-specific data into `NatalChart` or predictive reports.

Absent concepts are absent from doctrine reports. Execution-level errors belong to the run logger/application layer.

## Current architecture

```text
Input loading
→ Input validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ Formatting / printing
→ Reference validation
```

Basic chart calculation is not a separate report stage. `BasicCalculator` is shared infrastructure called through `Doctrine.calculateNatalChart(...)`.

## Current implementation facts

- Fresh app code lives under `src/main/java/app/`.
- `app.old` is migration/reference material only.
- `input/native-list.json` contains natal data only.
- The current CLI subject selector is `--subjects`.
- Doctrine selection is explicit through CLI `--doctrines ...`.
- No hidden default doctrine should be introduced.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, `calculationSetting`, and `natalChart` fields.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- `NatalChart` contains mechanical chart facts and doctrine-poured descriptive facts.
- `natalChart.points` is a map keyed by point name; planet points carry point-specific dignities, debilities, solar phase, planet sect info, and doctrine solar condition when calculated.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects on matching relations.
- `CalculationContext` is the per-run internal context. It owns Swiss Ephemeris state, subject, doctrine-derived calculation choices, calculation settings, full Julian day, house cusps, `ascmc`, ARMC, and shared helpers.
- `BasicCalculator` currently runs simple metadata, planets, houses, angles, sect, point registry, pairwise relations, solar phase injection, planet sect injection, and moon phase.
- `NatalChart` should remain output-facing; internal fields such as full Julian day, cusps, and `ascmc` belong in `CalculationContext`.
- Current Valens output pours prenatal syzygy, Fortune/Spirit lots, sign-based aspects including conjunction, dignity/debility assessments, and solar condition into `NatalChart`.
- Current Ptolemy output pours prenatal syzygy, Ptolemaic sign configurations excluding conjunction, and dignity/debility assessments into `NatalChart`.
- Dorotheus is present as a doctrine module but has no doctrine-poured descriptive sections yet.
- Fixed stars are not implemented. If added, the star set must be explicitly parameterized.
- JSON output rounds doubles at serialization through `RoundedDoubleSerializer`; calculators keep full internal double precision.
- `Logger.instance` is intentionally retained for the short-term CLI.

## Commands

After Java code changes, run:

```bash
mvn compile
```

Representative runtime check:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```
