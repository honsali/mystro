# Spring Boot Conversion Summary

This document summarizes the incremental Spring Boot conversion and React-readiness enhancements for Mystro, completed on 2026-05-05.

## Implemented Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/doctrines` | List available doctrine modules and their calculation choices |
| `POST` | `/api/descriptive` | Generate descriptive astrology reports from natal data |

Both endpoints are explicit about doctrine selection; no default doctrine is assumed.

## Entrypoints

- **CLI**: `app.App` — configured via `exec-maven-plugin` in `pom.xml`
- **Web**: `app.MystroSpringApplication` — `@SpringBootApplication`, configured via `spring-boot-maven-plugin`

The CLI path does not require a Spring application context.

## Key Files Added

### Application layer (`app.runtime`)

| File | Purpose |
|------|---------|
| `EngineVersion.java` | Multi-source version resolution (package impl, Maven metadata, pom.xml) |
| `DescriptiveReportService.java` | Reusable facade for descriptive report generation (in-memory + file-writing) |

### Web layer (`app.web`)

| File | Purpose |
|------|---------|
| `WebConfig.java` | Spring bean configuration (`ObjectMapper`, `BasicCalculator`, `JsonReportWriter`, `DoctrineLoader`, `DescriptiveReportService`) |
| `DescriptiveController.java` | `POST /api/descriptive` — thin controller, delegates to mapper + service |
| `DoctrinesController.java` | `GET /api/doctrines` — lists registered doctrines |
| `DescriptiveRequestMapper.java` | Validates request DTOs, converts to domain objects (`Subject`, `InputListBundle`) |
| `DescriptiveRequest.java` | Request DTO for descriptive generation |
| `DescriptiveResponse.java` | Response wrapper `{ "report": {...}, "suggestedFilename": "..." }` |
| `DoctrinesResponse.java` | Response wrapper `{ "doctrines": [...] }` |
| `DoctrineInfo.java` | DTO for doctrine summary (id, name, houseSystem, zodiac, terms, triplicity, nodeType) |
| `ErrorResponse.java` | Error body `{ "error": "..." }` |
| `GlobalExceptionHandler.java` | REST exception handler (malformed JSON, validation, generic) |

### Shared output (`app.output`)

| File | Purpose |
|------|---------|
| `MystroObjectMapper.java` | Shared ObjectMapper factory (RoundedDoubleSerializer, JavaTimeModule, Jdk8Module) |

### Input (`app.input`)

| File | Change |
|------|--------|
| `DoctrineLoader.java` | Added `list()` method returning registered doctrines |

### Tests

| File | Tests |
|------|-------|
| `LoggerTest.java` | 6 tests: global logging and thread-isolated REST logging behavior |
| `EngineVersionTest.java` | 2 tests: version resolution, never null |
| `DescriptiveControllerTest.java` | 14 tests: CORS preflight, success fields, rounding, doctrines endpoint, validation edge cases |
| `GlobalExceptionHandlerTest.java` | 1 test: generic error handler returns 500 |
| `WebConfigTest.java` | 5 tests: CORS origin parsing (trim, blanks, defaults) |

## Verification Commands

Run sequentially (never concurrently against the same `target/` directory):

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

To start the web application:

```bash
mvn spring-boot:run
```

To run the packaged jar:

```bash
java -jar target/mystro-1.0.0.jar
```

## JSON Serialization

Both file output and REST responses share the same Jackson configuration via `MystroObjectMapper.create()`:

- Doubles rounded to six decimal places (`RoundedDoubleSerializer`)
- Java time values serialized as strings, not timestamps
- JDK8 Optional support enabled

## Known Limitations

- CLI uses global `Logger.instance` and writes `output/run-logger.json`. REST descriptive calculations use thread-isolated ephemeral logging; request calculation logs do not accumulate globally and are not returned in report JSON.
- The `ephe/` directory must be available from the working directory for Swiss Ephemeris calculations, both for CLI and packaged jar execution.
- The packaged fat jar is ~22MB (includes Spring Boot + all dependencies).
- No predictive endpoint is implemented yet.
- A React frontend API contract is available at `reports/react-api-contract.md`.
- No authentication, database persistence, frontend, OpenAPI, or Actuator.
- Tests use `@SpringBootTest` (full context); could be narrowed to `@WebMvcTest` for faster execution as test count grows.
- CORS is configured for `/api/**` with default allowed origins `http://localhost:5173` and `http://localhost:3000`. Override via `mystro.cors.allowed-origins` property or `MYSTRO_CORS_ALLOWED_ORIGINS` environment variable.
