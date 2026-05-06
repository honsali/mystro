# Mystro REST-Only Roadmap

This roadmap is the manager-level plan for tracking where the project is, what has been completed, and what remains. It is intentionally higher-level than `manager.md`, which contains detailed per-iteration requirements.

## Current answer: are we done?

Yes — the planned REST-only Spring Boot conversion is complete.

Mystro is functioning as a Spring Boot REST-only application. The remaining ideas listed below are optional future work, not blockers for the conversion.

## Source of tasks

Manager tasks are derived from:

1. User direction, especially the REST-only decision.
2. `NEW_ARCHITECTURE_SPEC.md` and `AGENTS.md`.
3. Worker feedback in `worker.md`.
4. Review of changed code after each iteration.
5. Remaining relevant items in `audit_report.md`.

## Completed major milestones

### 1. Spring Boot REST foundation — complete

Completed across earlier iterations:

- Added Spring Boot web application entrypoint: `app.MystroSpringApplication`.
- Added REST endpoint: `POST /api/descriptive`.
- Added REST endpoint: `GET /api/doctrines`.
- Added DTOs, request mapper, response wrappers, and JSON error responses.
- Added MockMvc tests.

### 2. REST API contract stabilization — complete

Current contract:

```text
GET  /api/doctrines
POST /api/descriptive
```

`POST /api/descriptive`:

- Accepts one natal subject and one explicit singular `doctrine` id.
- Returns:

```json
{
  "report": { ... },
  "suggestedFilename": "subject-doctrine-descriptive.json"
}
```

- Does not write server output files.
- Sets `Cache-Control: no-store`.

### 3. Serialization alignment — complete

Completed:

- REST report responses use Mystro Jackson conventions:
  - six-decimal double rounding,
  - Java time as strings,
  - JDK8 datatype support.
- Mystro mapper is scoped to REST response conversion and is not the global Spring Boot `ObjectMapper`.
- File-output-specific writer code has now been removed with the REST-only conversion.

### 4. Regression safety — complete

Completed:

- Full ilia/Valens REST response snapshot:
  - `src/test/resources/snapshots/descriptive/ilia-valens-response.json`
- Snapshot test protects report structure and calculation output.
- Tests also cover validation, doctrine discovery, CORS parsing, object mapper scoping, and logger isolation.

### 5. Logger isolation — complete

Completed:

- `/api/**` request lifecycle uses thread-isolated ephemeral logging.
- Nested logger isolation restores previous context correctly.
- REST request logs do not pollute global logger entries.
- Non-API paths are not isolated by the filter.

### 6. REST-only conversion — complete

Completed in Iteration 20:

- Removed CLI entrypoint `app.App`.
- Removed `exec-maven-plugin` CLI wiring.
- Removed CLI argument/input-file loading.
- Removed server-side report file writer and run-logger writer.
- Updated docs/specs to describe REST-only operation.

### 7. REST generation seam cleanup — complete

Completed in Iteration 21:

- Removed obsolete mutable `InputListBundle`.
- `DescriptiveRequestMapper` now resolves direct immutable inputs: `Subject` + `Doctrine`.
- `DescriptiveReportGenerator` now generates exactly one report from `Subject` + `Doctrine`.

## Final completed iterations

### Iteration 22 — Spring Boot configuration/CORS properties — complete

Completed:

- Added `src/main/resources/application.yml` with local React CORS defaults.
- Replaced `WebConfig` field-injected `@Value` with constructor-injected typed `CorsProperties`.
- Preserved `/api/**` CORS behavior and override support through `mystro.cors.allowed-origins` / `MYSTRO_CORS_ALLOWED_ORIGINS`.

### Iteration 23 — Remove dead output model types — complete

Completed:

- Deleted unused Java output types `AstrologyReport`, `DoctrineSummary`, and `ReportMetadata`.
- Removed `implements AstrologyReport` from `DescriptiveAstrologyReport`.
- Preserved REST report shape, snapshot output, and calculation behavior.

### Iteration 24 — Final documentation and startup/package check — complete

Completed:

- Verified README, architecture spec, reports, and agent notes are consistent with REST-only operation.
- Verified no stale active CLI commands remain in active guidance.
- Verified compile, tests, packaging, and packaged-jar startup smoke test.

## Optional later work — not required to finish REST-only conversion

These are not blockers for the current conversion:

- Add predictive REST endpoint when the predictive architecture is ready.
- Add more doctrine snapshots, e.g. Ptolemy and Dorotheus.
- Consider narrowing some `@SpringBootTest` tests for speed.
- Consider Swiss Ephemeris startup checks or pooling.
- Decide whether `Subject` response shape should keep both local and resolved UTC times.
- Add OpenAPI, authentication, persistence, frontend, or deployment only if explicitly requested later.

## Definition of done status

The REST-only conversion is complete because:

1. Iteration 22 passed.
2. Iteration 23 passed.
3. Iteration 24 passed.
4. Documentation no longer has stale active CLI instructions.
5. `mvn compile`, `mvn test`, and `mvn package -DskipTests` pass.
6. REST contract remains stable:
   - `GET /api/doctrines`,
   - `POST /api/descriptive`,
   - explicit singular doctrine,
   - `{ "report": {...}, "suggestedFilename": "..." }`,
   - no server-side report files,
   - no hidden defaults.

## Manager recommendation

No further REST-only conversion iterations are planned.

Future work should be treated as a new feature or a separate refactor, for example predictive endpoints, additional doctrine snapshots, frontend integration, authentication, persistence, OpenAPI, or deployment work.
