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
Input loading
→ Input validation / normalization
→ Doctrine descriptive calculation, including doctrine-owned natal chart calculation
→ Doctrine predictive calculation
→ Formatting / printing
```

Basic chart calculation is not a separate report stage. `BasicCalculator` is shared infrastructure called through `Doctrine.calculateNatalChart(...)`.

## Current implementation facts

- `input/subject-list.json` contains natal data only.
- Natal records use `id` as the subject identifier.
- Doctrine modules are selected explicitly with `--doctrines ...`.
- Current descriptive output path is `output/{subjectId}/{doctrineId}-descriptive.json`.
- Current reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields. There is no `calculationSetting` object.
- There is no top-level `basicChart` key and no top-level `descriptive` key.
- Doctrine calculators pour descriptive data into `NatalChart`.
- Run logger path is `output/run-logger.json`.
- REST endpoints are `GET /api/doctrines` and single-doctrine `POST /api/descriptive`, returning `{ "report": {...}, "suggestedFilename": "..." }` without writing server output files.
- REST `/api/**` request logging is lifecycle-wide thread-isolated and ephemeral; CLI logging remains global and writes `output/run-logger.json`.
- The full `ilia`/Valens REST descriptive response snapshot lives at `src/test/resources/snapshots/descriptive/ilia-valens-response.json`.

## Core rule

A doctrine is a hardcoded Java knowledge module, not a settings profile and not a partial implementation of a universal astrology schema.

## Build and run

After Java code changes, run:

```bash
mvn compile
```

Representative runtime command:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```
