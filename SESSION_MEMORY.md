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

Predictive is an architectural target and is not implemented yet. Current implemented interfaces are the CLI descriptive file output and the Spring Boot stateless descriptive REST API.

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
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields. There is no `calculationSetting` object.
- Spring Boot REST support is present as a thin adapter over the existing calculation engine. CLI entrypoint: `app.App`; web entrypoint: `app.MystroSpringApplication`.
- REST endpoints: `GET /api/doctrines` and `POST /api/descriptive`. REST descriptive requests use one explicit singular `doctrine` field and return `{ "report": {...}, "suggestedFilename": "..." }`.
- REST descriptive calls are stateless/local-first: they do not write server output files, set `Cache-Control: no-store`, and are intended for a frontend to download one local JSON file per doctrine.
- REST uses configurable `/api/**` CORS defaults for local React dev origins: `http://localhost:5173` and `http://localhost:3000`.
- REST calculation logging is thread-isolated and ephemeral. CLI logging still uses global `Logger.instance` and writes `output/run-logger.json`.
- The engine targets the Valens-to-Lilly tropical tradition; sidereal zodiac calculation is out of scope for current doctrine modules.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- Shared chart data/model classes live under `app.chart.data` and `app.chart.model`; they are not owned by `app.basic` or `app.descriptive`.
- `NatalChart` is the shared chart container. `BasicCalculator` pours root mechanical data first, then doctrine/descriptive calculators enrich or annotate the same chart.
- Doctrine calculators pour data directly into `NatalChart`; there is no `DescriptiveResult` boundary and no doctrine-specific descriptive data record.
- `natalChart.points` is keyed by point name. Planet points carry point-specific dignities/debilities, solar phase, planet sect info, and doctrine solar condition when calculated. Planet sect and dignity/debility assessment are intentionally limited to the seven traditional planets; lunar nodes remain positional points without point-level sect or dignity/debility assessment.
- Basic chart sect is currently an altitude-based mechanical baseline: Sun above horizon is diurnal, Sun below horizon is nocturnal, using `altitude >= 0.0` as the baseline above-horizon rule. Twilight/refraction/author-specific refinements belong to doctrine descriptive calculation, not silent shared-basic changes.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects. Older hidden raw matrix scaffolding was removed; use `pairwiseRelations` for shared pair geometry.
- `CalculationContext` carries the subject, doctrine-derived calculation choices, Swiss Ephemeris state, full Julian day, cusps, `ascmc`, ARMC, and stateful helpers through basic and descriptive calculation. Pure math helpers are called directly through `AstroMath`, not passed through `CalculationContext`. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant share one source of truth. Public cusp/`ascmc` accessors return defensive copies.
- `BasicCalculator` keeps full internal double precision; JSON output rounds doubles through `RoundedDoubleSerializer`. `BasicCalculator` ordering is dependency-bearing and documented in code; planet sect injection is now a dedicated calculator step.
- Intentional calculation conventions: geocentric apparent planet positions, no topocentric lunar parallax correction, fail-fast Placidus errors with no silent fallback, and exactly 180° Moon-Sun elongation treated as waxing.
- Doctrine implementations live under `src/main/java/app/doctrine/impl/<doctrineId>/`; register new doctrine modules in `DoctrineLoader` and place doctrine-specific descriptive calculators under `src/main/java/app/descriptive/<doctrineId>/calculator/`.
- Java 17 is required.
- Swiss Ephemeris data under `ephe/` is required runtime data. `CalculationContext` explicitly sets the ephemeris path to `ephe`, requests file-backed Swiss Ephemeris (`SEFLG_SWIEPH`), and rejects Moshier fallback for planet positions.
- Current Valens output pours prenatal syzygy, Fortune/Spirit lots, sign-based aspects including conjunction, dignity/debility assessments, and solar condition into `NatalChart`.
- Current Ptolemy output pours prenatal syzygy, Ptolemaic sign configurations excluding conjunction, and dignity/debility assessments into `NatalChart`.
- Dorotheus is present but has no doctrine-poured descriptive sections yet.
- Fixed stars are not implemented.
- `Logger.instance` is intentionally retained for the short-term CLI; REST paths should use isolated logging and must not retain per-request calculation logs globally.

## Commands

Build:

```bash
mvn compile
```

Tests:

```bash
mvn test
```

Representative runtime check:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```


## Read first in future sessions

1. `NEW_ARCHITECTURE_SPEC.md`
2. `AGENTS.md`
3. `README.md`
