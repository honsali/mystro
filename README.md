# Mystro

Mystro is a self-contained Java traditional astrology calculation engine.

The authoritative project specification is:

- [`NEW_ARCHITECTURE_SPEC.md`](NEW_ARCHITECTURE_SPEC.md)

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

Core principle:

```text
Natal data × Doctrine modules → descriptive output
Natal data × Doctrine modules × inquiry periods → predictive output
Natal data × Doctrine modules × comparison inputs → comparative output
```

A doctrine is a hardcoded knowledge module, not a settings profile and not a partial implementation of a universal astrology schema.

## Current implementation status

The fresh app skeleton is implemented under `src/main/java/app/`.

Implemented now:

- `input`, `common`, `basic`, `doctrine`, and `output` package skeletons
- `NatalInput` with one subject identifier: `id`
- natal input loading from `input/native-list.json`
- CLI doctrine selection through `--doctrines ...`
- basic calculation placeholder with resolved UTC instant and Julian Day
- descriptive report writing
- run manifest writing
- placeholder doctrine modules for `dorotheus`, `ptolemy`, and `valens`

Code under `app.old` is migration/reference material only.

## Input

`input/native-list.json` contains natal data only. Doctrine choices do not live in natal input.

Current natal entry shape:

```json
{
  "id": "ilia",
  "birthDate": "1975-07-14",
  "birthTime": "22:55:00",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utcOffset": "+01:00"
}
```

## Run

Build:

```bash
mvn compile
```

Run a subject with explicit doctrine modules:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

`--subjects` is also accepted as an alias for `--subjects`.

If no doctrines are passed, the app writes an execution-level error to `output/run-manifest.json`.

## Current output

```text
output/descriptive/{subjectId}/{doctrineId}.json
output/run-manifest.json
```

Report metadata currently contains only:

```json
{
  "engineVersion": "0.1.0"
}
```
