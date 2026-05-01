# Mystro Agent Notes

## Authoritative specification

Read first:

- `NEW_ARCHITECTURE_SPEC.md`

## Project goal

Mystro is a self-contained Java traditional astrology calculation engine.

It produces doctrine-specific astrological data:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
Natal data × Doctrine modules × comparison inputs → comparative output
```

## Doctrine philosophy

A doctrine is a hardcoded knowledge module.

It is not:

- a settings file
- a doctrine profile
- a partial implementation of a universal astrology schema

Each doctrine defines what exists for that doctrine, which concepts are meaningful, which techniques it exposes, which calculations it performs, and which result shape best represents its own logic.

Absent concepts are simply absent from doctrine reports. Execution-level errors belong to the run manifest/application layer.

## Architecture layers

```text
Input loading
→ Input validation / normalization
→ Basic calculation
→ Descriptive calculation
→ Predictive calculation
→ Comparative calculation
→ Formatting / printing
→ Reference validation
```

## Current implementation facts

- Fresh app skeleton lives under `src/main/java/app/`.
- `app.old` is migration/reference material only.
- `input/native-list.json` contains natal data only.
- The current CLI subject selector is `--subjects`; `--names` is stale unless the CLI parser is changed.
- Doctrine selection is explicit through CLI `--doctrines ...`.
- No hidden default doctrine should be introduced.
- Current implemented output is descriptive JSON plus a run logger/manifest.
- Report metadata currently contains only `engineVersion`.
- Stage 1/basic data is emitted inside `basicChart`; the top-level `descriptive` key is absent until real stage 2 doctrine output exists.
- Current `basicChart` JSON keys are: `resolvedUtcInstant`, `julianDay`, `armc`, `localSiderealTime`, `obliquity`, `points`, `houses`, `pairwiseRelations`, `solarPhase`, `moonPhase`, `sect`.
- `basicChart.points` is a map keyed by point name and currently contains 16 points for the Lille fixture: 7 traditional planets, 2 nodes, 4 angles, Fortune, Spirit, and `PRENATAL_SYZYGY`.
- `basicChart.pairwiseRelations` currently covers planets + angles only; the Lille fixture has 78 entries.
- `basicChart.points` is currently `Map<PointKey, PointEntry>`. `PointKey` preserves the serialized point names; `PointEntry` is a sealed hierarchy with planet, angle, lot, and syzygy-point record implementations.
- `sect` is backed by typed `BasicSect` / `PlanetSectInfo`; `planetSects` is keyed by `Planet`.
- `BasicChart` output models live in `app.model.basic`; enums live in `app.model.data`.
- `BasicCalculationContext` in `app.basic` is the per-run internal context. It owns Swiss Ephemeris, input, full Julian day, house cusps, `ascmc`, ARMC, and shared calculation helpers.
- `BasicCalculator` orchestrates focused stateless calculators under `app.basic.calculator` in this order: simple metadata, planets, houses, angles, lots, syzygy, point registry, pairwise relations, solar phase, moon phase, sect.
- `BasicChart` should remain output-facing; internal fields such as full Julian day, cusps, and `ascmc` belong in `BasicCalculationContext`, not in `BasicChart`.
- Current stage 1 includes raw/non-interpretive: planet positions, signs, houses, terms, speeds, declinations, antiscia/contra-antiscia, Fortune/Spirit, syzygy point, raw pairwise ecliptic/equatorial relations, solar orientation, moon phase geometry, altitude-based sect roles, and essential dignity rulers on traditional planets.
- Fixed stars are not implemented. If added, the star set must be explicitly parameterized; no doctrine-free canonical star list should be assumed.
- `Logger.instance` is intentionally retained for the short-term CLI. The project may move to Spring Boot soon; when that happens, prefer injecting/request-scoping the logger instead of doing an interim de-singleton refactor now.
- Remaining known technical debt: global `Logger.instance` until Spring Boot migration, rounding during calculation instead of serialization, fragile syzygy search, Jackson annotations on domain models, and hard-coded calculation-setting assumptions that should not be exposed as configurable metadata until wired.

## Dependency direction

```text
input/common
    ↓
basic
    ↓
doctrine
    ↓
output
```

`basic` must not depend on concrete doctrine implementations.

## Commands

After Java code changes, run:

```bash
mvn compile
```

Representative current runtime command:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

If documentation or prompts mention `--names`, treat that as stale unless the CLI parser has been updated.
