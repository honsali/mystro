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
→ Basic calculation
→ Descriptive calculation
→ Predictive calculation
→ Comparative calculation
→ Formatting / printing
→ Reference validation
```

## Current implementation facts

- `input/native-list.json` contains natal data only.
- `NatalInput` uses `id` only; no separate subject name.
- Doctrine modules are selected explicitly with `--doctrines ...`.
- Current descriptive output path is `output/descriptive/{subjectId}/{doctrineId}.json`.
- Run manifest path is `output/run-manifest.json`.

## Core rule

A doctrine is a hardcoded knowledge module, not a settings profile and not a partial implementation of a universal astrology schema.

## Build and run

After Java code changes, run:

```bash
mvn compile
```

Representative runtime command:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```
