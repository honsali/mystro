# REST-Only Spring Boot Application Summary

This document summarizes Mystro's current REST-only Spring Boot application shape.

## Implemented Endpoints

| Method | Path | Response | Description |
|--------|------|----------|-------------|
| `GET` | `/api/doctrines` | Direct JSON array of `DoctrineInfo` | List available doctrine modules and their calculation choices |
| `POST` | `/api/descriptive` | Direct `DescriptiveAstrologyReport` | Generate one descriptive astrology report from natal data |

Both endpoints are explicit about doctrine selection; no default doctrine is assumed.

## Runtime Architecture

- **Entrypoint**: `app.MystroSpringApplication`
- **Application mode**: Spring Boot REST only
- **Descriptive generation**: `DescriptiveController` calls `doctrine.calculateDescriptive(subject, basicCalculator)` directly
- **No server-side report writing**: `POST /api/descriptive` returns JSON directly
- **No CLI entrypoint**: Mystro runs as a REST-only Spring Boot application

## Key Files

### Application layer (`app.runtime`)

| File | Purpose |
|------|---------|
| `EngineVersion.java` | Spring-managed engine version sourced from `mystro.engine-version` |

### Web business layer (`app.web.business`)

| File | Purpose |
|------|---------|
| `DescriptiveController.java` | `POST /api/descriptive` — thin controller, delegates to mapper, calls doctrine directly |
| `DoctrinesController.java` | `GET /api/doctrines` — returns direct array of doctrine info |
| `DescriptiveRequestMapper.java` | Validates request DTOs and resolves `Subject` + `Doctrine` |
| `DescriptiveRequest.java` | Request DTO for descriptive generation |
| `ErrorResponse.java` | Error body `{ "error": "..." }` |

### Web infrastructure layer (`app.web.infra`)

| File | Purpose |
|------|---------|
| `WebConfig.java` | Spring bean configuration (`MappingJackson2HttpMessageConverter`, `BasicCalculator`, `DoctrineLoader`) + CORS |
| `CorsProperties.java` | Typed `mystro.cors` configuration properties with trimming/default fallback |
| `GlobalExceptionHandler.java` | REST exception handler (malformed JSON, validation, generic) |
| `LoggerIsolationFilter.java` | Lifecycle-wide `/api/**` request logging isolation |

### Shared output/runtime support (`app.output`)

| File | Purpose |
|------|---------|
| `MystroObjectMapper.java` | Shared ObjectMapper factory (RoundedDoubleSerializer, JavaTimeModule, Jdk8Module) |
| `Logger.java` | Global logger with nested-safe thread-isolated request logging support |

### Input/runtime support (`app.input`)

| File | Purpose |
|------|---------|
| `DoctrineLoader.java` | Doctrine registry and discovery |
| `DoctrineInfo.java` | Doctrine calculation choices (id, name, houseSystem, zodiac, terms, triplicity, nodeType) |

### Tests

| File | Tests |
|------|-------|
| `LoggerTest.java` | 8 tests: global logging, thread-isolated REST logging, nested isolation |
| `EngineVersionTest.java` | 2 tests: version resolution, never null |
| `DescriptiveControllerTest.java` | 14 tests: CORS preflight, success fields, rounding, doctrines endpoint, validation edge cases |
| `DescriptiveRequestMapperTest.java` | 2 tests: resolved subject/doctrine seam, unknown doctrine validation |
| `DescriptiveSnapshotTest.java` | 1 test: full ilia/valens REST response JSON snapshot |
| `GlobalExceptionHandlerTest.java` | 1 test: generic error handler returns 500 |
| `LoggerIsolationFilterTest.java` | 7 tests: `/api/**` scoping, API isolation, non-API passthrough |
| `CorsPropertiesTest.java` | 5 tests: CORS origin normalization (trim, blanks, defaults) |
| `WebConfigObjectMapperTest.java` | 2 tests: converter uses Mystro rounding, global ObjectMapper does not |

## Verification Commands

Run sequentially (never concurrently against the same `target/` directory):

```bash
mvn compile
mvn test
mvn package -DskipTests
```

To start the web application:

```bash
mvn spring-boot:run
```

To run the packaged jar:

```bash
java -jar target/mystro-<version>.jar
```

## JSON Serialization

REST API responses use `MystroObjectMapper.create()` conventions through the REST `MappingJackson2HttpMessageConverter`:

- doubles rounded to six decimal places (`RoundedDoubleSerializer`)
- Java time values serialized as strings, not timestamps
- JDK8 Optional support enabled

Spring Boot's auto-configured global `ObjectMapper` remains the default for any other infrastructure use; the Mystro mapper is not exposed as the application-wide `ObjectMapper` bean.

## Logging

- `/api/**` requests are wrapped by `LoggerIsolationFilter` in lifecycle-wide thread-isolated logging.
- `Logger.info/error` calls made during REST requests go to ephemeral thread-local entries and are cleared after the response.
- Nested `Logger.runIsolated(...)` calls are safe and restore the previous isolated context.
- REST responses do not expose execution log entries.
- No run-logger file is written by the server.

## Local-first frontend contract

- `POST /api/descriptive` accepts one explicit `doctrine` id.
- `DescriptiveRequestMapper` resolves a `Subject` plus `Doctrine`, then the controller calls `doctrine.calculateDescriptive(subject, basicCalculator)` directly and returns a `DescriptiveAstrologyReport`.
- One request returns one report for one doctrine.
- Response is a direct `DescriptiveAstrologyReport` JSON object with top-level `engineVersion`, `subject`, `doctrine`, `natalChart`.
- `GET /api/doctrines` returns a direct JSON array of doctrine info objects.
- The frontend should save report JSON locally if desired.
- A React-oriented contract reference is available at `reports/react-api-contract.md`.

## Known Limitations

- The `ephe/` directory must be available from the working directory for Swiss Ephemeris calculations, including packaged jar execution.
- The packaged fat jar is ~22MB (includes Spring Boot + all dependencies).
- No predictive endpoint is implemented yet.
- No authentication, database persistence, frontend, OpenAPI, or Actuator.
- Tests use `@SpringBootTest` (full context); they could be narrowed later if faster feedback becomes necessary.
- CORS is configured for `/api/**` with defaults defined in `src/main/resources/application.yml`: `http://localhost:5173` and `http://localhost:3000`. Override via `mystro.cors.allowed-origins` property or `MYSTRO_CORS_ALLOWED_ORIGINS` environment variable.
