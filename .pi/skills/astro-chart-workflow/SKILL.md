---
name: astro-chart-workflow
description: Use for working on Mystro's new astrology calculation architecture and report pipeline.
compatibility: Requires the Mystro Java Maven project.
---

# Astro Chart Workflow

## First step

Read:

- `NEW_ARCHITECTURE_SPEC.md`

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

- Runtime input is REST JSON with `id`, `birthDate`, `birthTime`, `latitude`, `longitude`, `utcOffset`, and one explicit `doctrine` id.
- Current reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields. There is no `calculationSetting` object.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- Doctrine calculators pour descriptive data into `NatalChart`.
- REST endpoints are `GET /api/doctrines` and single-doctrine `POST /api/descriptive`.
- `GET /api/doctrines` returns a direct JSON array.
- `POST /api/descriptive` returns a direct `DescriptiveAstrologyReport` JSON object with top-level `engineVersion`, `subject`, `doctrine`, and `natalChart`.
- `engineVersion` comes from Spring configuration property `mystro.engine-version`.
- REST descriptive calls do not write server output files.
- REST `/api/**` request logging is lifecycle-wide thread-isolated and ephemeral.
- The full `ilia`/Valens REST descriptive response snapshot lives at `src/test/resources/snapshots/descriptive/ilia-valens-response.json`.

## Core rule

A doctrine is a hardcoded Java knowledge module, not a settings profile and not a partial implementation of a universal astrology schema.

## Build and run

After Java code changes, run:

```bash
mvn compile
```

After behavior or web/API changes, also run:

```bash
mvn test
```

For packaging verification, also run:

```bash
mvn package -DskipTests
```

To start the application locally:

```bash
mvn spring-boot:run
```
