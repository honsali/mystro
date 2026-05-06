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

Predictive is an architectural target; the current implemented application mode is stateless REST descriptive calculation JSON.

## Doctrine philosophy

A doctrine is a hardcoded Java knowledge module.

It is not:

- a settings file
- a doctrine profile
- a partial implementation of a universal astrology schema

Each doctrine defines what exists for that doctrine, which concepts are meaningful, which calculations it performs, and how it pours doctrine-specific data into `NatalChart` or predictive reports.

Absent concepts are absent from doctrine reports. Execution-level errors belong to the application/logging layer.

## Current architecture

```text
REST request loading
→ Input validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ JSON response
```

Basic chart calculation is not a separate report stage. `BasicCalculator` is shared infrastructure called through `Doctrine.calculateNatalChart(...)`.

## Current implementation facts

- Fresh app code lives under `src/main/java/app/`.
- The repository may still contain representative natal fixtures such as `input/subject-list.json`, but the application does not load runtime subjects from that file.
- No hidden default doctrine should be introduced.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields. There is no `calculationSetting` object. `GET /api/doctrines` returns a direct JSON array; `POST /api/descriptive` returns a direct `DescriptiveAstrologyReport`.
- Mystro now runs as a Spring Boot REST-only application. The web entrypoint is `app.MystroSpringApplication`.
- REST endpoints: `GET /api/doctrines` lists available doctrine choices and returns a direct JSON array; `POST /api/descriptive` accepts one natal subject plus one explicit `doctrine` id and returns a direct `DescriptiveAstrologyReport` JSON object.
- REST descriptive calls are single-doctrine by design. A frontend should call once per selected doctrine and save one local JSON file per doctrine. Do not reintroduce hidden defaults or plural REST doctrine selection unless explicitly requested.
- REST descriptive calls do not write server output files. They return JSON directly so clients/frontends can save files locally if desired.
- REST responses use the shared `MystroObjectMapper`/`RoundedDoubleSerializer` conventions and set `Cache-Control: no-store` on descriptive responses/errors. CORS for `/api/**` is configurable via `mystro.cors.allowed-origins` with local React defaults.
- REST `/api/**` request logging uses lifecycle-wide thread-isolated ephemeral logging via `LoggerIsolationFilter`; request logs are not retained by default.
- `src/test/resources/snapshots/descriptive/ilia-valens-response.json` is the committed full REST response snapshot for the representative `ilia`/Valens descriptive calculation. Keep it in sync only when calculation/report changes are intentional.
- The engine targets the Valens-to-Lilly tropical tradition; sidereal zodiac calculation is out of scope for current doctrine modules.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- Shared chart data/model classes live under `app.chart.data` and `app.chart.model`; they are not owned by `app.basic` or `app.descriptive`.
- `NatalChart` is the shared chart container. `BasicCalculator` pours root mechanical data first, then doctrine/descriptive calculators enrich or annotate the same chart.
- `natalChart.points` is a map keyed by point name; planet points carry point-specific dignities, debilities, solar phase, planet sect info, and doctrine solar condition when calculated. Planet sect and dignity/debility assessment are intentionally limited to the seven traditional planets; lunar nodes remain positional points without point-level sect or dignity/debility assessment.
- Basic chart sect is currently an altitude-based mechanical baseline: Sun above horizon is diurnal, Sun below horizon is nocturnal, using `altitude >= 0.0` as the baseline above-horizon rule. Twilight/refraction/author-specific refinements belong to doctrine descriptive calculation, not silent shared-basic changes.
- `natalChart.pairwiseRelations` contains raw geometry for point pairs; doctrine-recognized aspects are injected as optional `aspect` objects on matching relations. Older hidden raw matrix scaffolding has been removed; use `pairwiseRelations` for shared pair geometry.
- `CalculationContext` is the per-run internal context. It owns Swiss Ephemeris state, subject, doctrine-derived calculation choices, full Julian day, house cusps, `ascmc`, ARMC, and shared helpers. Julian day is derived from the subject's resolved UTC instant so the recorded instant and calculation instant share one source of truth. Public cusp/`ascmc` accessors return defensive copies.
- `BasicCalculator` currently runs simple metadata, planets, houses, angles, sect, point registry, pairwise relations, solar phase injection, planet sect injection via `PlanetSectInjectionCalculator`, and moon phase. Its ordering is dependency-bearing and documented in code; do not reorder casually.
- `NatalChart` should remain output-facing; internal fields such as full Julian day, cusps, and `ascmc` belong in `CalculationContext`.
- Current Valens output pours prenatal syzygy, Fortune/Spirit lots, sign-based aspects including conjunction, dignity/debility assessments, and solar condition into `NatalChart`.
- Current Ptolemy output pours prenatal syzygy, Ptolemaic sign configurations excluding conjunction, and dignity/debility assessments into `NatalChart`.
- Dorotheus is present as a doctrine module but has no doctrine-poured descriptive sections yet.
- Fixed stars are not implemented. If added, the star set must be explicitly parameterized.
- JSON output rounds doubles at serialization through `RoundedDoubleSerializer`; calculators keep full internal double precision.
- Intentional calculation conventions: geocentric apparent planet positions, no topocentric lunar parallax correction, fail-fast Placidus errors with no silent fallback, and exactly 180° Moon-Sun elongation treated as waxing.
- Doctrine implementations live under `src/main/java/app/doctrine/impl/<doctrineId>/`; register new doctrine modules in `DoctrineLoader` and place doctrine-specific descriptive calculators under `src/main/java/app/descriptive/<doctrineId>/calculator/`.
- Java 17 is required.
- Swiss Ephemeris data under `ephe/` is required runtime data; do not delete it as generated output. `CalculationContext` explicitly sets the ephemeris path to `ephe`, requests file-backed Swiss Ephemeris (`SEFLG_SWIEPH`), and rejects Moshier fallback for planet positions.
- `Logger.instance` remains available for calculation-layer logging. REST `/api/**` requests must use lifecycle-wide logger isolation so request log entries do not accumulate or persist by default.

## Commands

After Java code changes, run:

```bash
mvn compile
```

After behavior or Spring Boot/web changes, also run:

```bash
mvn test
```

For packaging verification, run:

```bash
mvn package -DskipTests
```
