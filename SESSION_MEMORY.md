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
- Basic calculators keep full internal double precision; JSON output rounds doubles through `RoundedDoubleSerializer`.
- Current stage 1 basic output includes UT/TT Julian days, delta T, ARMC, local apparent sidereal time, true/mean obliquity, nutation, planet longitude/latitude/right ascension/declination/altitude/above-horizon facts, houses, essential dignity rulers, antiscia/contra-antiscia, solar phase, moon phase, pairwise relations, and sect.

## Commands

Build:

```bash
mvn compile
```

Representative runtime check:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Version prompt:

```text
.pi/prompts/version.md saves durable knowledge, commits all intended project files except input/output/build artifacts, runs mvn compile, stops on compile failure, commits successful changes as "version $v: $desc", pushes, then bumps the minor version in pom.xml for the next development cycle. Major version bumps are manual.
```

Current Maven version for the next development cycle is `0.8.0`.

## Read first in future sessions

1. `NEW_ARCHITECTURE_SPEC.md`
2. `AGENTS.md`
3. `README.md`
