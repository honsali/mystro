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
- `input/subject-list.json` contains natal data only.
- Doctrine selection is explicit through `--doctrines ...`.
- No doctrine is selected by default.
- Current doctrine modules: `dorotheus`, `ptolemy`, `valens`.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, `calculationSetting`, and `natalChart` fields.
- The engine targets the Valens-to-Lilly tropical tradition; sidereal zodiac calculation is out of scope for current doctrine modules.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- `NatalChart` contains mechanical chart facts and doctrine-poured descriptive facts.
- Doctrine calculators pour data directly into `NatalChart`; there is no `DescriptiveResult` boundary and no doctrine-specific descriptive data record.
- `natalChart.points` is keyed by point name. Planet points carry point-specific dignities/debilities, solar phase, planet sect info, and doctrine solar condition when calculated. Planet sect and dignity/debility assessment are intentionally limited to the seven traditional planets; lunar nodes remain positional points without point-level sect or dignity/debility assessment.
- Basic chart sect is currently an altitude-based mechanical baseline: Sun above horizon is diurnal, Sun below horizon is nocturnal, using `altitude >= 0.0` as the baseline above-horizon rule. Twilight/refraction/author-specific refinements belong to doctrine descriptive calculation, not silent shared-basic changes.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects.
- `CalculationContext` carries the subject, doctrine-derived calculation choices, calculation settings, Swiss Ephemeris state, full Julian day, cusps, `ascmc`, ARMC, and shared helpers through basic and descriptive calculation. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant share one source of truth.
- `BasicCalculator` keeps full internal double precision; JSON output rounds doubles through `RoundedDoubleSerializer`.
- Intentional calculation conventions: geocentric apparent planet positions, no topocentric lunar parallax correction, fail-fast Placidus errors with no silent fallback, and exactly 180° Moon-Sun elongation treated as waxing.
- Doctrine implementations live under `src/main/java/app/doctrine/impl/<doctrineId>/`; register new doctrine modules in `DoctrineLoader` and place doctrine-specific descriptive calculators under `src/main/java/app/descriptive/<doctrineId>/calculator/`.
- Java 17 is required.
- Swiss Ephemeris data under `ephe/` is required runtime data.
- Optional `input/settings.properties` may set `calculation.precision`.
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


## Read first in future sessions

1. `NEW_ARCHITECTURE_SPEC.md`
2. `AGENTS.md`
3. `README.md`
