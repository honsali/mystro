# Session Memory

## Current state

Mystro is being restarted from a clean architecture.

The authoritative document is:

- `NEW_ARCHITECTURE_SPEC.md`

## Durable direction

The application is a self-contained Java traditional astrology calculation engine.

Main flow:

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

Output families:

```text
descriptive
predictive
comparative
```

Doctrine concept:

```text
A doctrine is a hardcoded knowledge module.
It is not a settings file, not a doctrine profile, and not a partial universal schema implementation.
```

## Current implementation facts

- Fresh Java skeleton exists under `src/main/java/app/`.
- `app.old` is reference/migration material only.
- `input/native-list.json` contains natal data only.
- Natal entries use ISO-style fields: `id`, `birthDate`, `birthTime`, `latitude`, `longitude`, `utcOffset`.
- `NatalInput` keeps `id` only; no separate subject name is used.
- Doctrine selection is explicit through `--doctrines ...`.
- No doctrine is selected by default.
- Current placeholder doctrines: `dorotheus`, `ptolemy`, `valens`.
- Current report metadata contains only `engineVersion`.
- Current implemented output path: `output/descriptive/{subjectId}/{doctrineId}.json`.
- Execution-level status is written to `output/run-manifest.json`.

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
