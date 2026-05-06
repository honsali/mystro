# Session Memory

## Current state

Mystro is a self-contained Java traditional astrology calculation engine exposed as a Spring Boot REST application.

Authoritative specification:

- `NEW_ARCHITECTURE_SPEC.md`

The planned REST-only Spring Boot conversion is complete. See `REST_ONLY_ROADMAP.md` for the completed roadmap and optional future work.

## Current architecture

```text
REST request validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ JSON response
```

Current implemented interface:

```text
GET  /api/doctrines
POST /api/descriptive
```

Predictive is an architectural target and is not implemented yet.

## Durable direction

A doctrine is a hardcoded Java knowledge module.

It is not a settings file, not a doctrine profile, and not a partial universal schema implementation.

Basic chart calculation is not a separate report stage. `BasicCalculator` is shared infrastructure called by the doctrine through `Doctrine.calculateNatalChart(...)`.

## Current implementation facts

- Fresh Java app code exists under `src/main/java/app/`.
- Mystro is REST-only. The application entrypoint is `app.MystroSpringApplication`.
- The former CLI entrypoint `app.App`, `exec-maven-plugin`, CLI input loaders, server-side JSON report writer, and run-log writer have been removed.
- The repository may still contain representative natal fixtures such as `input/subject-list.json`, but runtime subjects are supplied by REST request bodies, not by input files.
- No doctrine is selected by default.
- Current doctrine modules: `dorotheus`, `ptolemy`, `valens`.
- REST endpoints:
  - `GET /api/doctrines` lists doctrine choices.
  - `POST /api/descriptive` accepts one natal subject plus one explicit singular `doctrine` id and returns `{ "report": {...}, "suggestedFilename": "..." }`.
- REST descriptive calls are stateless/local-first: they do not write server output files or run-log files, set `Cache-Control: no-store`, and are intended for a frontend to download one local JSON file per doctrine if desired.
- REST descriptive generation flow is: `DescriptiveRequest` → `DescriptiveRequestMapper` resolves `Subject` + `Doctrine` → `DescriptiveReportGenerator.generate(subject, doctrine)` → `DescriptiveAstrologyReport`.
- `DescriptiveReportGenerator` is pure in-memory orchestration. It calls `doctrine.calculateDescriptive(subject, basicCalculator)` and wraps the result in `DescriptiveAstrologyReport`.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields. There is no `calculationSetting` object.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- REST responses use `MystroObjectMapper`/`RoundedDoubleSerializer` conventions through a REST `MappingJackson2HttpMessageConverter`; the global Spring Boot `ObjectMapper` is not intentionally replaced.
- REST `/api/**` CORS defaults live in `src/main/resources/application.yml` under `mystro.cors.allowed-origins` with local React dev origins `http://localhost:5173` and `http://localhost:3000`. Overrides can use Spring Boot relaxed binding, including `MYSTRO_CORS_ALLOWED_ORIGINS`.
- REST `/api/**` request logging is lifecycle-wide thread-isolated and ephemeral via `LoggerIsolationFilter`; request logs are not returned or persisted by default.
- A full REST response snapshot for the representative `ilia`/Valens descriptive calculation is committed at `src/test/resources/snapshots/descriptive/ilia-valens-response.json`; update it only for intentional calculation/report changes.
- `REST_ONLY_ROADMAP.md` records that the REST-only conversion is complete. Optional future work includes predictive endpoints, more doctrine snapshots, frontend integration, auth/persistence/OpenAPI/deployment, or selected performance/test refactors.
- The engine targets the Valens-to-Lilly tropical tradition; sidereal zodiac calculation is out of scope for current doctrine modules.
- Shared chart data/model classes live under `app.chart.data` and `app.chart.model`; they are not owned by `app.basic` or `app.descriptive`.
- `NatalChart` is the shared chart container. `BasicCalculator` pours root mechanical data first, then doctrine/descriptive calculators enrich or annotate the same chart.
- Doctrine calculators pour data directly into `NatalChart`; there is no `DescriptiveResult` boundary and no doctrine-specific descriptive data record.
- `natalChart.points` is keyed by point name. Planet points carry point-specific dignities/debilities, solar phase, planet sect info, and doctrine solar condition when calculated. Planet sect and dignity/debility assessment are intentionally limited to the seven traditional planets; lunar nodes remain positional points without point-level sect or dignity/debility assessment.
- Basic chart sect is currently an altitude-based mechanical baseline: Sun above horizon is diurnal, Sun below horizon is nocturnal, using `altitude >= 0.0` as the baseline above-horizon rule. Twilight/refraction/author-specific refinements belong to doctrine descriptive calculation, not silent shared-basic changes.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects. Use `pairwiseRelations` for shared pair geometry.
- `CalculationContext` carries the subject, doctrine-derived calculation choices, Swiss Ephemeris state, full Julian day, cusps, `ascmc`, ARMC, and stateful helpers through basic and descriptive calculation. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant share one source of truth. Public cusp/`ascmc` accessors return defensive copies.
- `BasicCalculator` keeps full internal double precision; JSON output rounds doubles through `RoundedDoubleSerializer`. `BasicCalculator` ordering is dependency-bearing and documented in code; do not reorder casually.
- Intentional calculation conventions: geocentric apparent planet positions, no topocentric lunar parallax correction, fail-fast Placidus errors with no silent fallback, file-backed Swiss Ephemeris only, and exactly 180° Moon-Sun elongation treated as waxing.
- Doctrine implementations live under `src/main/java/app/doctrine/impl/<doctrineId>/`; register new doctrine modules in `DoctrineLoader` and place doctrine-specific descriptive calculators under `src/main/java/app/descriptive/<doctrineId>/calculator/`.
- Java 17 is required.
- Swiss Ephemeris data under `ephe/` is required runtime data. `CalculationContext` explicitly sets the ephemeris path to `ephe`, requests file-backed Swiss Ephemeris (`SEFLG_SWIEPH`), and rejects Moshier fallback for planet positions.
- Current Valens output pours prenatal syzygy, Fortune/Spirit lots, sign-based aspects including conjunction, dignity/debility assessments, and solar condition into `NatalChart`.
- Current Ptolemy output pours prenatal syzygy, Ptolemaic sign configurations excluding conjunction, and dignity/debility assessments into `NatalChart`.
- Dorotheus is present but has no doctrine-poured descriptive sections yet.
- Fixed stars are not implemented.
- Team workflow note: when the user says "your turn", the manager reviews the latest worker feedback/work and prints only a red completion percentage; the worker executes the latest `manager.md` requirement and appends feedback to `worker.md`.

## Commands

Compile:

```bash
mvn compile
```

Tests:

```bash
mvn test
```

Package:

```bash
mvn package -DskipTests
```

Run locally:

```bash
mvn spring-boot:run
```

Run packaged jar:

```bash
java -jar target/mystro-1.2.0.jar
```

The `ephe/` directory must be available from the working directory for Swiss Ephemeris calculations.

## Read first in future sessions

1. `NEW_ARCHITECTURE_SPEC.md`
2. `AGENTS.md`
3. `REST_ONLY_ROADMAP.md`
4. `README.md`
