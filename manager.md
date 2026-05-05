# Manager Requirements

## Iteration 1 — 2026-05-05

### Goal
Prepare the existing CLI calculation flow for a thin Spring Boot wrapper by extracting reusable runtime/application services, without adding web endpoints or changing astrology behavior yet.

### Scope
- Refactor orchestration currently embedded in `app.App` into reusable application-layer classes.
- Preserve the existing CLI entrypoint and output behavior.
- Do **not** add REST controllers in this iteration.
- Spring Boot dependencies may be deferred to the next iteration unless a minimal dependency is needed for compile-safe structure.

### Requirements
1. Create a small reusable service/facade for descriptive report generation, for example under `src/main/java/app/runtime/` or another clearly application-layer package.
   - It should accept the same effective inputs the CLI uses today, or an already loaded `InputListBundle`.
   - It should perform the current loop over selected subjects and doctrines.
   - It should call `doctrine.calculateDescriptive(subject, basicCalculator)` exactly as today.
   - It should build `DescriptiveAstrologyReport` with the same top-level shape: `engineVersion`, `subject`, `doctrine`, `natalChart`.
   - It should write reports to the same path: `output/{subjectId}/{doctrineId}-descriptive.json`.
2. Move project version reading out of `App` into a reusable helper/service so both CLI and future Spring code can use the same engine version.
3. Keep `app.App` as the CLI main class configured by `exec-maven-plugin`.
   - `App.main(...)` should become mostly orchestration: load input, call the new service, write the run logger in `finally`.
4. Preserve run logging behavior:
   - Keep writing `output/run-logger.json` in the CLI `finally` path.
   - Keep existing logger messages or equivalent messages for successfully written reports.
5. Do not alter doctrine implementations, chart model/data classes, calculation conventions, input file format, or output JSON structure.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"` succeeds.
- The representative CLI run still writes `output/ilia/valens-descriptive.json` and `output/run-logger.json`.
- The descriptive report still has top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` fields; no `basicChart`, `descriptive`, or `calculationSetting` top-level field should be introduced.
- `pom.xml` should still point `exec-maven-plugin` at `app.App`.

### Constraints
- Keep this iteration small and reviewable.
- No hidden default doctrine selection.
- No sidereal zodiac or unrelated astrology feature work.
- No broad package relocation of `app.basic`, `app.chart`, `app.descriptive`, or `app.doctrine`.
- If a blocker appears, stop expanding scope and document it in `worker.md`.

### Feedback Requested
Please append an Iteration 1 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Any deviations from scope.
- Suggested next step for adding the Spring Boot application shell.

## Iteration 2 — 2026-05-05

### Manager Review Notes
Iteration 1 is accepted. The extracted `DescriptiveReportService` and `EngineVersion` give us a usable seam for web integration while preserving the CLI path.

### Goal
Add the initial Spring Boot web application shell and expose descriptive calculation through a thin REST endpoint, while keeping the existing CLI entrypoint working unchanged.

### Scope
- Add Spring Boot web support.
- Add a separate Spring Boot main class.
- Add a simple REST endpoint for descriptive report generation from request natal data.
- Make the runtime descriptive service usable both for file-writing CLI runs and in-memory REST responses.
- Do **not** add predictive endpoints yet.

### Requirements
1. Update `pom.xml` for Spring Boot web support.
   - Prefer dependency management or explicit dependency versions; do **not** use a Spring Boot parent POM in this iteration, because `EngineVersion` currently reads the project version from this project `pom.xml`.
   - Add `spring-boot-starter-web`.
   - Add the Spring Boot Maven plugin if needed for `mvn spring-boot:run`, but keep `exec-maven-plugin` configured with `<mainClass>app.App</mainClass>` for the CLI.
2. Add a separate Spring Boot application entrypoint, for example `app.MystroSpringApplication`.
   - Annotate it with `@SpringBootApplication`.
   - Do not replace or rename `app.App`.
3. Refine `app.runtime.DescriptiveReportService` so it has an in-memory generation method usable by REST, for example:
   - `List<DescriptiveAstrologyReport> generateDescriptiveReports(InputListBundle inputListBundle)` or equivalent.
   - The existing file-writing CLI method should delegate to the in-memory method and then write `output/{subjectId}/{doctrineId}-descriptive.json`.
   - Preserve report construction and `doctrine.calculateDescriptive(subject, basicCalculator)` behavior.
4. Add simple web DTOs under a clear package such as `app.web` or `app.web.dto`.
   - Request should include exactly one natal subject using the native input fields: `id`, `birthDate`, `birthTime`, `latitude`, `longitude`, `utcOffset`.
   - Request must include an explicit non-empty doctrine id list, e.g. `doctrines: ["valens"]`.
   - Do not introduce hidden default doctrines.
5. Add a thin REST controller, for example `POST /api/descriptive`.
   - It should convert the request DTO to `Subject`.
   - It should resolve doctrine ids through `DoctrineLoader` or equivalent existing registry logic.
   - It should call the descriptive service in-memory generation method.
   - It should return JSON shaped as a small wrapper such as `{ "reports": [ ... ] }`, where each report is the existing `DescriptiveAstrologyReport` object.
   - The REST endpoint should not write output files by default.
6. Add basic HTTP error handling for bad requests.
   - Missing/blank subject id, missing date/time/offset, invalid latitude/longitude, empty doctrines, or unknown doctrine ids should return HTTP 400 with a simple JSON error body.
   - Keep this simple; no broad validation framework is required unless it stays small.
7. Preserve existing CLI behavior.
   - `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"` must still use `app.App`, load `input/subject-list.json`, write descriptive output files, and write `output/run-logger.json`.

### Acceptance Criteria
- `mvn compile` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `pom.xml` still contains `exec-maven-plugin` with `<mainClass>app.App</mainClass>`.
- A Spring Boot app class exists and compiles independently from the CLI class.
- `POST /api/descriptive` is implemented and documented in `worker.md` with an example request body and example `curl` command.
- REST response reports still expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart`; no top-level `basicChart`, `descriptive`, or `calculationSetting` fields are introduced inside report objects.
- REST path requires explicit doctrine ids and does not silently default to any doctrine.

### Constraints
- Keep the Spring Boot layer thin: controller/DTOs should orchestrate existing runtime/domain classes, not duplicate astrology calculation logic.
- Do not alter doctrine implementations, chart model/data classes, calculation conventions, or current output JSON structure.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment packaging yet.
- If Spring Boot setup conflicts with the current version-reading helper, document the issue in `worker.md` instead of switching to a parent POM silently.

### Feedback Requested
Please append an Iteration 2 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Example REST request JSON and `curl` command.
- Any known limitations of the new endpoint.
- Suggested next step.

## Iteration 3 — 2026-05-05

### Manager Review Notes
Iteration 2 is accepted. I also ran `mvn compile` and the representative CLI command locally; both succeeded. The main follow-up is to make the Spring layer more idiomatic and reviewable by using dependency injection and adding tests. The current controller manually constructs `DescriptiveReportService`, `BasicCalculator`, `JsonReportWriter`, and `DoctrineLoader`; please replace that with Spring-managed beans while keeping the CLI path independent and unchanged.

### Goal
Harden the initial Spring Boot endpoint with Spring dependency injection and basic automated tests, without expanding application features.

### Scope
- Add test support for the Spring Boot endpoint.
- Convert Spring web wiring from manual construction to constructor-injected beans.
- Add simple JSON error handling for common web request failures.
- Preserve existing CLI behavior and REST response shape.

### Requirements
1. Update `pom.xml` for tests.
   - Add `spring-boot-starter-test` with `test` scope.
   - Because this project does not use the Spring Boot parent POM, explicitly configure a modern `maven-surefire-plugin` version that runs JUnit 5 tests reliably.
   - Keep `exec-maven-plugin` configured with `<mainClass>app.App</mainClass>`.
2. Add Spring bean wiring for the web path.
   - Add a small configuration class if needed, for example under `app.web` or `app.runtime`.
   - Provide Spring-managed beans for `BasicCalculator`, `JsonReportWriter`, `DoctrineLoader`, and `DescriptiveReportService` or equivalent.
   - Refactor `DescriptiveController` to use constructor injection instead of `new` inside the controller constructor.
   - Do not annotate core astrology/domain classes broadly unless needed; prefer a thin configuration/wiring layer.
3. Keep the CLI path working independently.
   - `app.App` may continue manual construction for CLI simplicity.
   - Do not make CLI execution require a Spring application context.
4. Add basic controller tests using MockMvc or an equivalent Spring Boot test approach.
   - Add at least one successful `POST /api/descriptive` test using the representative natal data and `doctrines: ["valens"]`.
   - Assert HTTP 200 and JSON contains `reports[0].engineVersion`, `reports[0].subject`, `reports[0].doctrine`, and `reports[0].natalChart`.
   - Assert the report object does **not** contain top-level `basicChart`, `descriptive`, or `calculationSetting` fields.
   - Add at least three HTTP 400 tests covering: empty doctrines, unknown doctrine id, and invalid latitude/longitude or malformed date/time.
5. Add simple REST exception handling.
   - Ensure validation errors return a JSON body shaped like `{ "error": "..." }`.
   - Ensure malformed JSON / unreadable request bodies also return HTTP 400 with the same error body shape if practical without excessive code.
6. Do not change endpoint semantics.
   - `POST /api/descriptive` must still require explicit doctrine ids.
   - It must still return `{ "reports": [ ... ] }`.
   - It must not write output files by default.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `DescriptiveController` no longer manually constructs its runtime dependencies.
- The REST success test verifies the expected report top-level fields and absence of forbidden fields.
- HTTP 400 tests verify JSON error bodies.
- No doctrine, chart model/data, or astrology calculation behavior changes.

### Constraints
- Keep this iteration focused on wiring and tests only.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, predictive endpoints, or deployment packaging.
- Avoid broad rewrites of input parsing or doctrine loading unless strictly required for testability.
- If test setup is blocked by Maven/Spring plugin configuration, document the exact blocker in `worker.md` before expanding scope.

### Feedback Requested
Please append an Iteration 3 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results, including `mvn test`.
- A short note on how dependencies are wired in Spring versus CLI.
- Any known limitations or flaky/slow test concerns.
- Suggested next step.

## Iteration 4 — 2026-05-05

### Manager Review Notes
Iteration 3 is accepted. I independently ran `mvn test` successfully, then reran the representative CLI command successfully. Note: do not run separate Maven goals against the same `target/` directory concurrently; a parallel local check briefly caused a transient classpath error while another Maven process was recompiling.

One important follow-up: the CLI/file JSON path uses `RoundedDoubleSerializer` through `JsonReportWriter`, but the REST path is currently serialized by Spring Boot's MVC `ObjectMapper`. The REST output should use the same Mystro JSON conventions, especially six-decimal double rounding and non-timestamp Java time values.

### Goal
Align REST JSON serialization with existing file-output serialization, make the Spring Boot app packaging/run path explicit, and document the new web API.

### Scope
- Share or mirror Mystro's Jackson serialization configuration for Spring MVC responses.
- Verify REST responses round doubles the same way file reports do.
- Configure Spring Boot packaging/run behavior explicitly despite the project having both CLI and web main classes.
- Add concise README documentation for the Spring Boot API.

### Requirements
1. Unify JSON serialization conventions.
   - Introduce a small reusable JSON/ObjectMapper configuration helper if useful, for example under `app.output` or `app.runtime`.
   - Ensure `JsonReportWriter` continues to register `JavaTimeModule`, `Jdk8Module`, and `RoundedDoubleSerializer` for both `Double.class` and `Double.TYPE`.
   - Ensure Spring MVC response serialization also uses `RoundedDoubleSerializer` for both boxed and primitive doubles.
   - Preserve `WRITE_DATES_AS_TIMESTAMPS` disabled behavior for REST responses.
2. Add/adjust tests for REST serialization.
   - Use a request with more than six decimal places for `latitude` and `longitude`, such as the representative `input/subject-list.json` values.
   - Assert the REST response subject latitude/longitude are rounded to six decimal places, e.g. `50.606008` and `3.033377`.
   - Keep the existing success/error coverage from Iteration 3.
3. Make Spring Boot plugin behavior explicit.
   - Configure `spring-boot-maven-plugin` with `mainClass` / `Start-Class` equivalent pointing to `app.MystroSpringApplication`.
   - If adding a `repackage` execution is appropriate, do so; otherwise document why not in `worker.md`.
   - Keep `exec-maven-plugin` configured with `<mainClass>app.App</mainClass>` for CLI.
   - The CLI must not require a Spring application context.
4. Add concise README documentation.
   - Keep the existing CLI docs.
   - Add a Spring Boot section with:
     - how to start the web app (`mvn spring-boot:run` or packaged jar command, depending on what is configured),
     - `POST /api/descriptive`,
     - example request JSON,
     - response wrapper shape `{ "reports": [ ... ] }`,
     - note that doctrine ids are explicit and no default doctrine is assumed,
     - note that REST requests do not write output files by default.
5. Optional but useful: add a very small smoke verification for packaging.
   - At minimum, `mvn package -DskipTests` should pass.
   - If an executable Boot jar is produced, document how it was verified or how to run it.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- REST serialization test confirms six-decimal rounding in the response.
- `exec-maven-plugin` still points at `app.App`.
- Spring Boot plugin is explicitly configured for `app.MystroSpringApplication` or a clear reason is documented.
- README contains concise Spring Boot API usage while preserving CLI usage.
- No doctrine, chart model/data, or astrology calculation behavior changes.

### Constraints
- Do not add new endpoints or predictive support in this iteration.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not change output report structure beyond serialization formatting consistency.
- Keep changes small and focused on JSON configuration, packaging clarity, tests, and docs.

### Feedback Requested
Please append an Iteration 4 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- How REST JSON serialization is wired and how it relates to `JsonReportWriter`.
- Packaging/run behavior for the Spring Boot app.
- Any known limitations.
- Suggested next step.

## Iteration 5 — 2026-05-05

### Manager Review Notes
Iteration 4 is accepted. I reviewed the shared `MystroObjectMapper`, Spring MVC wiring, README additions, and the expanded controller tests. I also independently ran the full sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

One remaining deployment-quality issue: `EngineVersion` still reads only `pom.xml`. That works from the project root, but a packaged Spring Boot jar may be run from a directory without `pom.xml`, causing report `engineVersion` to become `unknown`. The packaged jar already contains Maven metadata (`META-INF/maven/mystro/mystro/pom.properties`), so the runtime version helper should use a classpath/package fallback before returning `unknown`.

### Goal
Make runtime engine-version reporting robust for packaged Spring Boot execution, and lightly clean up the web controller so it remains thin as the app grows.

### Scope
- Improve `EngineVersion` fallback behavior for packaged/runtime contexts.
- Add focused tests for version resolution and current REST version reporting.
- Extract request-to-domain conversion out of `DescriptiveController` if it keeps the code simpler and more testable.
- Preserve endpoint semantics, CLI behavior, and report structure.

### Requirements
1. Harden `app.runtime.EngineVersion`.
   - Keep `EngineVersion.get()` as the public API.
   - Prefer a resolution order that works in both development and packaged jar contexts, for example:
     1. package implementation version if available,
     2. classpath Maven metadata such as `/META-INF/maven/mystro/mystro/pom.properties`,
     3. project-root `pom.xml` for development runs,
     4. `unknown` as the final fallback.
   - Avoid logging errors for normal fallback misses.
   - Do not rely solely on `pom.xml`.
2. Add tests for engine version behavior.
   - Add a small unit test proving `EngineVersion.get()` returns the current project version in the normal test/dev environment.
   - If practical without overengineering, add a test around the classpath Maven metadata reader or helper method using a fake resource/properties source.
   - Keep tests deterministic; do not require starting a real web server.
3. Ensure REST reports expose the resolved engine version.
   - Extend the existing MockMvc success test to assert `reports[0].engineVersion` equals the current project version, not merely that it exists.
   - Avoid hardcoding a stale duplicate version if possible; use `EngineVersion.get()` or a single test helper.
4. Lightly clean up `DescriptiveController` if useful.
   - The controller currently owns request parsing, subject creation, doctrine lookup, and service invocation.
   - Extract request validation/conversion to a small Spring bean or package-private helper if this improves clarity, for example `DescriptiveRequestMapper` / `DescriptiveRequestResolver`.
   - Keep the controller thin: receive request, resolve bundle/domain input, call service, return response/error.
   - Preserve existing HTTP 400 error messages unless there is a strong reason to clarify them.
5. Documentation touch-up.
   - Add a short README note that packaged jar execution uses the embedded Maven version metadata for `engineVersion`.
   - If documenting `java -jar target/mystro-0.18.0.jar`, mention that the `ephe/` directory must be available from the working directory for calculations.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `EngineVersion` no longer depends solely on a project-root `pom.xml`.
- MockMvc success test asserts the exact expected `engineVersion` value.
- REST response shape remains `{ "reports": [ ... ] }` and report objects still expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart`.
- No doctrine, chart model/data, or astrology calculation behavior changes.

### Constraints
- Do not add new endpoints in this iteration.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, predictive endpoints, or deployment infrastructure.
- Do not change the current native input file format or CLI flags.
- Keep the refactor small; avoid broad rewrites of input parsing or doctrine loading.

### Feedback Requested
Please append an Iteration 5 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of the new `EngineVersion` resolution order.
- Whether the controller was refactored, and why.
- Any known limitations.
- Suggested next step.

## Iteration 6 — 2026-05-05

### Manager Review Notes
Iteration 5 is accepted. I reviewed the hardened `EngineVersion`, the extracted `DescriptiveRequestMapper`, and the updated tests. I also independently ran the sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Two small polish items remain before considering the simple Spring Boot conversion complete: clients need a safe way to discover available doctrine ids without implying defaults, and the REST layer should consistently return JSON errors for edge-case bad requests such as JSON `null`, blank doctrine ids, or unexpected runtime failures.

### Goal
Polish the web API with doctrine discovery and more consistent JSON error behavior, while preserving the existing descriptive endpoint semantics and CLI behavior.

### Scope
- Add a small doctrine-discovery REST endpoint.
- Tighten request validation edge cases for `POST /api/descriptive`.
- Ensure REST error responses remain JSON-shaped.
- Keep the astrology calculation/report pipeline unchanged.

### Requirements
1. Add a doctrine discovery endpoint.
   - Implement `GET /api/doctrines`.
   - Return a stable wrapper shape, for example:
     ```json
     {
       "doctrines": [
         {
           "id": "valens",
           "name": "Valens",
           "houseSystem": "WHOLE_SIGN",
           "zodiac": "TROPICAL",
           "terms": "EGYPTIAN",
           "triplicity": "DOROTHEAN",
           "nodeType": "MEAN"
         }
       ]
     }
     ```
   - Include all currently registered doctrines from `DoctrineLoader`: `dorotheus`, `ptolemy`, and `valens`.
   - Use DTOs rather than returning the internal loader map directly.
   - This endpoint is for discovery only; it must not introduce a default doctrine.
2. Add or expose a clean way to list registered doctrines.
   - Prefer adding a small method on `DoctrineLoader`, e.g. `list()` returning an immutable/list copy of registered doctrines in registry order.
   - Do not change existing `find(...)` behavior used by the CLI/web request mapper.
   - Preserve explicit doctrine selection everywhere else.
3. Tighten `POST /api/descriptive` validation edge cases.
   - JSON body `null` should return HTTP 400 with `{ "error": "Request body is required" }` or equivalent.
   - Doctrine ids that are `null` or blank strings should return HTTP 400 with a clear error, e.g. `Doctrine id is required`.
   - Preserve existing validation behavior/messages for empty doctrine list, unknown doctrine id, invalid latitude, malformed JSON, etc., unless you document a small wording cleanup.
4. Improve generic REST error handling.
   - Ensure unexpected exceptions from controller/service paths return a JSON body shaped like `{ "error": "Internal server error" }` with HTTP 500.
   - Do not expose stack traces or internal exception messages in the response body.
   - It is okay to keep this minimal; no logging framework migration is required.
5. Clean up version test hardcoding if practical.
   - Replace the hardcoded `"0.18.0"` in `EngineVersionTest` with a small test helper that reads the first project `<version>` from `pom.xml`, or from Maven metadata if simpler.
   - Keep the test deterministic.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `GET /api/doctrines` has MockMvc coverage and returns `dorotheus`, `ptolemy`, and `valens` without implying a default.
- `POST /api/descriptive` has MockMvc coverage for JSON `null` body and blank/null doctrine id validation.
- Malformed/bad request responses and generic error responses are JSON-shaped.
- REST response report shape remains `{ "reports": [ ... ] }` for descriptive generation.
- No doctrine implementations, chart model/data classes, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints in this iteration.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not change the current native input file format or CLI flags.
- Keep the endpoint and DTO additions small and explicit.

### Feedback Requested
Please append an Iteration 6 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Example `GET /api/doctrines` response shape.
- Notes on validation/error behavior changes.
- Any known limitations.
- Suggested next step.

## Iteration 7 — 2026-05-05

### Manager Review Notes
Iteration 6 is accepted with small follow-up corrections. I reviewed the new doctrine discovery endpoint, DTOs, `DoctrineLoader.list()`, validation changes, and tests. I also independently ran the sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

A few polish gaps remain:
- The requirement for JSON body `null` was not fully covered: the current test checks an empty body, not literal JSON `null`.
- The requirement for `null` doctrine ids was not fully covered: the current test checks blank string only.
- `GET /api/doctrines` is implemented but README does not yet document it.
- The Iteration 6 feedback example showed inaccurate Ptolemy metadata; tests should assert exact doctrine metadata so this cannot drift unnoticed. The code appears to use the doctrine implementation values correctly, but test coverage should prove it.

### Goal
Close the remaining Spring Boot API polish gaps so the simple conversion can be considered complete and well documented.

### Scope
- Tighten edge-case validation tests and behavior.
- Strengthen doctrine discovery test assertions.
- Document `GET /api/doctrines` in README.
- Keep all endpoint semantics, report shape, CLI behavior, and calculation behavior unchanged.

### Requirements
1. Handle literal JSON `null` request bodies for `POST /api/descriptive`.
   - A request with body `null` and `Content-Type: application/json` must return HTTP 400.
   - Response body must be JSON-shaped as `{ "error": "Request body is required" }` or equivalent clear wording.
   - Preserve current malformed/empty-body behavior unless a small wording cleanup is documented.
2. Handle `null` doctrine ids explicitly.
   - A request such as `"doctrines": [null]` must return HTTP 400.
   - Response body must be `{ "error": "Doctrine id is required" }` or equivalent clear wording.
   - Blank doctrine ids should continue returning HTTP 400.
3. Strengthen `GET /api/doctrines` tests.
   - Assert exact metadata for all registered doctrines, not just existence of fields.
   - Expected current values from doctrine implementations:
     - `dorotheus`: `houseSystem=WHOLE_SIGN`, `zodiac=TROPICAL`, `terms=EGYPTIAN`, `triplicity=DOROTHEAN`, `nodeType=MEAN`
     - `ptolemy`: `houseSystem=WHOLE_SIGN`, `zodiac=TROPICAL`, `terms=PTOLEMAIC`, `triplicity=PTOLEMAIC`, `nodeType=MEAN`
     - `valens`: `houseSystem=WHOLE_SIGN`, `zodiac=TROPICAL`, `terms=EGYPTIAN`, `triplicity=DOROTHEAN`, `nodeType=MEAN`
   - Ensure the response wrapper remains `{ "doctrines": [ ... ] }`.
   - Do not introduce default doctrine semantics.
4. Add README documentation for doctrine discovery.
   - Add `GET /api/doctrines` with an example `curl` command.
   - Include an example response shape with correct current metadata.
   - State that this endpoint is discovery-only and does not select a default doctrine.
5. Add a minimal test for generic error handler if practical.
   - A unit test directly calling `GlobalExceptionHandler.handleGeneric(...)` is sufficient.
   - Assert HTTP 500 and `{ "error": "Internal server error" }`.
   - Do not add artificial production-only endpoints just for testing this.
6. Apply the updated `team.md` rules going forward.
   - Keep the Spring layer thin.
   - Document architectural notes, limitations, and exact verification commands in `worker.md`.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- Tests cover literal JSON `null` body and `doctrines: [null]`.
- Tests assert exact `GET /api/doctrines` metadata for Dorotheus, Ptolemy, and Valens.
- README documents both `POST /api/descriptive` and `GET /api/doctrines`.
- Report objects still expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart` only.
- No doctrine implementations, chart model/data classes, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints in this iteration.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not change the current native input file format or CLI flags.
- Do not change doctrine implementation values to satisfy tests; tests should reflect the current authoritative doctrine classes.
- Keep this as a small polish iteration.

### Feedback Requested
Please append an Iteration 7 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Notes on validation/error behavior changes.
- Confirmation that `GET /api/doctrines` docs and tests use correct current doctrine metadata.
- Any known limitations.
- Suggested next step.

## Iteration 8 — 2026-05-05

### Manager Review Notes
Iteration 7 is accepted. I reviewed the literal JSON `null` handling, `doctrines: [null]` validation, exact doctrine metadata tests, README doctrine-discovery docs, and generic error-handler unit test. I also independently ran the full sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

The simple Spring Boot conversion is now functionally complete. This iteration is a final handoff/stabilization pass, not a feature expansion.

### Goal
Prepare the Spring Boot conversion for handoff by adding final smoke-run documentation/checks and tightening small documentation/test polish.

### Scope
- No new application features.
- No new astrology behavior.
- Final documentation and verification polish only.
- Keep the current CLI and REST API behavior unchanged.

### Requirements
1. Add final API/error documentation to README.
   - Keep the existing CLI docs and current Spring Boot docs.
   - Add a short REST error response section showing the standard shape:
     ```json
     { "error": "..." }
     ```
   - Include examples or bullets for common `POST /api/descriptive` 400 cases:
     - missing/empty/null body,
     - empty doctrine list,
     - null/blank doctrine id,
     - unknown doctrine id,
     - malformed JSON,
     - invalid latitude/longitude.
   - Use the phrase "calculation choices" rather than "calculation settings" for doctrine metadata, to avoid confusion with the absent top-level `calculationSetting` object.
2. Add a concise conversion handoff note.
   - Create a small markdown file such as `reports/spring-boot-conversion-summary.md` or another clear location.
   - Include:
     - implemented endpoints,
     - CLI preservation note,
     - Spring Boot entrypoint and CLI entrypoint,
     - important files/classes added,
     - verification commands,
     - known limitations (`Logger.instance` singleton, `ephe/` required at runtime, no predictive endpoint yet).
   - Keep this short and practical.
3. Improve test organization/polish if useful, without broad rewrites.
   - It is acceptable to leave tests as-is if they are clear.
   - If changing tests, keep behavior unchanged.
   - If practical, avoid noisy stack traces from `GlobalExceptionHandlerTest` while still testing the 500 response; otherwise document why it remains acceptable in `worker.md`.
4. Perform a packaged-jar smoke check if practical.
   - After `mvn package -DskipTests`, start `java -jar target/mystro-0.18.0.jar` briefly and verify at least `GET /api/doctrines` responds, then stop the process.
   - If this is not practical in the agent environment, document the reason and the exact manual command instead.
   - Do not leave a server process running.
5. Re-run the normal verification commands sequentially.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- README documents REST error response shape and common 400 cases.
- README uses "calculation choices" rather than "calculation settings" for doctrine metadata.
- A concise handoff summary markdown file exists.
- Packaged-jar smoke check is either completed and documented or clearly documented as not practical.
- No endpoint semantics, report shape, doctrine implementations, chart model/data classes, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not change native input file format or CLI flags.
- Do not introduce hidden default doctrine selection.
- Keep this final pass small and focused on handoff readiness.

### Feedback Requested
Please append an Iteration 8 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Packaged-jar smoke check result, including how the process was stopped.
- Notes on any test/docs polish decisions.
- Known limitations remaining.
- Whether you consider the simple Spring Boot conversion complete.

## Iteration 9 — 2026-05-05

### Manager Final Review Notes
Iteration 8 is accepted and the simple Spring Boot conversion is considered complete.

I reviewed:
- `README.md` Spring Boot API and REST error documentation.
- `reports/spring-boot-conversion-summary.md` handoff summary.
- Current Spring Boot entrypoint, web controllers, DTOs, request mapper, JSON mapper, tests, and Maven plugin configuration.

I independently ran:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

`mvn compile` and `mvn test` passed. The first `mvn package -DskipTests` attempt failed because the packaged-jar smoke-test process from Iteration 8 was still running and locking `target/mystro-0.18.0.jar` despite the feedback saying it had been stopped. I confirmed the lingering process with `jps`, killed PID `5676`, confirmed no Mystro jar process remained, then reran:

```bash
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Both passed successfully.

### Final Acceptance
The Spring Boot conversion scope is complete:

- CLI entrypoint `app.App` is preserved.
- Spring Boot entrypoint `app.MystroSpringApplication` exists and is packaged.
- `POST /api/descriptive` is implemented, tested, and documented.
- `GET /api/doctrines` is implemented, tested, and documented.
- REST responses use the same JSON serialization conventions as file output.
- Descriptive report shape remains `engineVersion`, `subject`, `doctrine`, `natalChart`.
- No hidden default doctrine was introduced.
- No doctrine implementations, chart model/data classes, or astrology calculation behavior were changed.
- Handoff summary exists at `reports/spring-boot-conversion-summary.md`.

### No Further Worker Action Required
No new implementation requirement is issued in this iteration.

If future work resumes, the next manager block should start a new explicit scope, such as predictive endpoint support, additional doctrine tests, logger/request-scope cleanup, or faster web-slice tests.

### Process Note For Future Smoke Tests
When starting a packaged jar for smoke testing, always verify shutdown with `jps -l` or equivalent before reporting completion. A lingering `java -jar target/mystro-*.jar` process can lock the jar and break later `mvn package` runs on Windows.

## Iteration 10 — 2026-05-05

### Manager Context
We are starting a new explicit scope after the Spring Boot conversion was accepted as complete.

The product direction is now privacy-first / local-first frontend support:

```text
Spring Boot backend = ephemeral/stateless calculation API
React frontend = local viewer/export tool
```

The user should be able to calculate a chart, download the returned JSON locally, and keep one downloaded report file per doctrine. The backend should not behave like a chart-history service.

Important product decision for this iteration: the REST descriptive request should use **one doctrine id**, not a doctrine list. A React frontend can call the endpoint once per doctrine and download one report file per doctrine.

### Goal
Simplify the REST descriptive API from multi-doctrine calculation to single-doctrine calculation, and document the privacy/local-first usage model.

### Scope
- Change the REST request DTO and web mapping for `POST /api/descriptive` from plural doctrine ids to a single explicit doctrine id.
- Keep CLI multi-doctrine behavior unchanged.
- Keep doctrine discovery endpoint unchanged.
- Preserve calculation behavior and report shape.
- Add docs/tests for frontend local download usage.

### Requirements
1. Change `DescriptiveRequest` to use a single doctrine id.
   - Replace the REST request field `doctrines: ["valens"]` with a singular field, preferably:
     ```json
     "doctrine": "valens"
     ```
   - The field must be required and explicit.
   - Do not introduce any default doctrine.
   - Remove REST validation logic specific to empty doctrine lists, because the REST API should no longer accept a list.
   - Add validation for missing/null/blank `doctrine` with a clear HTTP 400 JSON error, e.g. `{ "error": "Doctrine id is required" }`.
2. Update request mapping/service usage for single-doctrine REST calls.
   - `DescriptiveRequestMapper` should resolve exactly one doctrine id through `DoctrineLoader.find(...)`.
   - It may still build an internal `InputListBundle` with one subject and one doctrine if that keeps reuse of `DescriptiveReportService` simple.
   - Unknown doctrine id should still return HTTP 400 with `{ "error": "Unknown doctrine: <id>" }`.
   - Keep CLI path unchanged: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"` and multi-doctrine CLI selection should still work.
3. Adjust REST response shape for single-report download use.
   - Prefer a single-report wrapper for the REST endpoint, for example:
     ```json
     {
       "report": { ... },
       "suggestedFilename": "test-subject-valens-descriptive.json"
     }
     ```
   - The `report` object itself must remain the existing `DescriptiveAstrologyReport` shape with top-level `engineVersion`, `subject`, `doctrine`, and `natalChart`.
   - Do not add `basicChart`, `descriptive`, or `calculationSetting` to the report object.
   - The endpoint must still not write output files. The filename is only a frontend hint for local download.
   - If you choose to keep `{ "reports": [...] }` for compatibility instead, document why in `worker.md`; however, the preferred direction is a single `report` because the request now contains exactly one doctrine.
4. Preserve `GET /api/doctrines`.
   - It should still return all available doctrine choices.
   - It should not imply a default doctrine.
   - Tests for exact doctrine metadata should remain.
5. Add privacy/local-first documentation.
   - Update README and/or the handoff summary to explain:
     - backend calculation is intended to be stateless/ephemeral,
     - REST descriptive calls do not write output files,
     - the frontend should download/save report JSON locally,
     - one REST call produces one report for one doctrine,
     - users can download one file per doctrine by calling the endpoint once per selected doctrine,
     - no authentication, database, or chart history exists.
   - Keep wording honest: infrastructure may still see HTTP requests; this is privacy-friendly/local-first, not absolute anonymity.
6. Add/update tests.
   - Update success test to send `"doctrine": "valens"` instead of `"doctrines": ["valens"]`.
   - Assert the REST response has the chosen single-report wrapper shape.
   - Assert `report.engineVersion`, `report.subject`, `report.doctrine`, and `report.natalChart` exist.
   - Assert the report object does not contain `basicChart`, `descriptive`, or `calculationSetting`.
   - Assert `suggestedFilename` if implemented.
   - Update validation tests for missing/null/blank doctrine and unknown doctrine.
   - Remove or rewrite obsolete tests for empty doctrine lists / null doctrine ids in a list.
   - Keep malformed JSON, empty/null body, invalid latitude/longitude, rounding, exact doctrine discovery, and generic error handler coverage.
7. Optional but recommended for privacy/local-first behavior:
   - Add `Cache-Control: no-store` to `POST /api/descriptive` responses and 400/500 error responses if this can be done cleanly.
   - If added, test at least the successful descriptive response header.
   - Do not overbuild this; no security framework is requested.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `POST /api/descriptive` request uses singular `doctrine`, not plural `doctrines`.
- `POST /api/descriptive` returns exactly one descriptive report for the selected doctrine.
- REST endpoint does not write output files.
- React/local-download usage is documented.
- `GET /api/doctrines` remains available and tested.
- CLI multi-doctrine capability remains unchanged.
- No doctrine implementations, chart model/data classes, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints.
- Do not add authentication, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not change native input file format or CLI flags.
- Do not introduce hidden default doctrine selection.
- Keep the Spring layer thin and calculation-free.
- Keep this iteration focused on the single-doctrine REST API and local-first documentation.

### Feedback Requested
Please append an Iteration 10 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Example updated `POST /api/descriptive` request and response shape.
- Notes on REST response shape decision (`report` vs `reports`).
- Notes on privacy/local-first documentation.
- Any known limitations or compatibility breaks from changing `doctrines` to `doctrine`.
- Suggested next step.

## Iteration 11 — 2026-05-05

### Manager Review Notes
Iteration 10 is accepted. I reviewed the single-doctrine REST request/response change, `Cache-Control: no-store`, tests, and README privacy/local-first documentation.

I independently ran the sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

One documentation issue remains: `reports/spring-boot-conversion-summary.md` is now stale. It still describes `DescriptiveResponse` as `{ "reports": [...] }`, but the current REST response is `{ "report": {...}, "suggestedFilename": "..." }`.

Also, because this backend is intended for a React frontend, the next small backend-readiness item is CORS. Keep it simple and configurable.

### Goal
Polish React frontend readiness by adding minimal configurable CORS support and updating stale handoff documentation for the new single-report response shape.

### Scope
- Add CORS support for `/api/**` suitable for local React development.
- Keep CORS configurable rather than hardcoding only one frontend origin.
- Update handoff documentation to reflect Iteration 10's single-doctrine/single-report API.
- Do not change calculation behavior, endpoint semantics, or CLI behavior.

### Requirements
1. Add minimal CORS configuration for the Spring Boot API.
   - Apply CORS only to `/api/**`.
   - Allow `GET`, `POST`, and `OPTIONS`.
   - Allow at least `Content-Type` request header.
   - Do not enable credentials unless there is a specific need; this backend has no auth/session.
   - Add sensible local React development defaults, e.g. `http://localhost:5173` and `http://localhost:3000`.
   - Make allowed origins configurable via Spring configuration property, environment variable, or another simple Spring Boot mechanism.
   - Keep the Spring layer thin; use a small config class or extend existing `WebConfig`.
2. Add tests for CORS behavior.
   - Add MockMvc coverage for an `OPTIONS` preflight request to `/api/descriptive` from an allowed local React origin.
   - Assert the response includes an appropriate `Access-Control-Allow-Origin` header.
   - If practical, assert that a non-configured origin does not receive the allow-origin header.
3. Update documentation.
   - Update `README.md` with a short note for React frontend development:
     - default allowed dev origins,
     - how to configure allowed origins for another frontend URL,
     - CORS does not mean data is stored; backend remains stateless/ephemeral.
   - Update `reports/spring-boot-conversion-summary.md` so it reflects the current API:
     - `POST /api/descriptive` request uses singular `doctrine`,
     - response wrapper is `{ "report": {...}, "suggestedFilename": "..." }`, not `{ "reports": [...] }`,
     - one call produces one report for one doctrine,
     - frontend downloads one JSON file per doctrine.
4. Preserve current behavior.
   - `GET /api/doctrines` response shape unchanged.
   - `POST /api/descriptive` response shape unchanged from Iteration 10.
   - `Cache-Control: no-store` remains on descriptive success/error responses.
   - REST endpoint still does not write output files.
   - CLI multi-doctrine behavior remains unchanged.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- CORS preflight test for an allowed React dev origin passes.
- `reports/spring-boot-conversion-summary.md` no longer mentions the stale `{ "reports": [...] }` descriptive response wrapper.
- README documents CORS configuration for React frontend development.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add authentication, sessions, credentials-based CORS, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not add predictive endpoints.
- Do not reintroduce plural `doctrines` in the REST descriptive request.
- Do not introduce hidden default doctrine selection.
- Keep this iteration small and focused on CORS + documentation correction.

### Feedback Requested
Please append an Iteration 11 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- CORS configuration details and default allowed origins.
- Test coverage added for CORS.
- Confirmation that handoff summary now reflects the single-report response shape.
- Any known limitations.
- Suggested next step.

## Iteration 12 — 2026-05-05

### Manager Review Notes
Iteration 11 is accepted. I reviewed the CORS implementation, CORS tests, README updates, and the handoff summary update. I independently ran the sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Two small polish issues remain:
- The CORS allowed-origin property is split on commas without trimming whitespace or ignoring blank entries. This is easy for users to misconfigure when writing `origin1, origin2`.
- The handoff summary still says the conversion was completed across eight iterations and has stale test-count wording; the conversion has continued through the React/local-first/CORS iterations.

The next step should also produce a frontend-facing API contract that a React implementation can use directly.

### Goal
Harden CORS configuration for real-world property values and create a concise React frontend API contract for local-first report download.

### Scope
- Small CORS parsing robustness improvement.
- Additional CORS negative/edge tests.
- Documentation/contract material for React frontend integration.
- No endpoint behavior changes beyond safer CORS property parsing.
- No calculation or doctrine changes.

### Requirements
1. Harden CORS allowed-origin parsing.
   - Trim whitespace around comma-separated origins.
   - Ignore blank entries.
   - Keep the current defaults: `http://localhost:5173` and `http://localhost:3000`.
   - If the configured property is blank or only commas/spaces, fall back to the defaults or document a clear intentional behavior.
   - Keep credentials disabled.
   - Keep CORS limited to `/api/**`.
2. Add CORS tests.
   - Keep existing allowed-origin tests.
   - Add a test proving a disallowed origin does not receive `Access-Control-Allow-Origin`.
   - Add a test proving configured origins with spaces are handled correctly if practical. This may be a focused unit test for the parsing helper if you extract one, or a Spring test with test properties.
3. Create a React API contract document.
   - Add a concise file, for example `reports/react-api-contract.md`.
   - Include:
     - endpoint list,
     - TypeScript interfaces for `DescriptiveRequest`, `DescriptiveResponse`, `DoctrineInfo`, `DoctrinesResponse`, and `ErrorResponse`,
     - a minimal `fetch` example for `POST /api/descriptive`,
     - a minimal browser download example using `suggestedFilename`,
     - note that one request returns one report for one doctrine,
     - note that reports should be stored locally by the frontend,
     - note that backend REST calls are stateless and do not write server files,
     - note that `Cache-Control: no-store` is sent on descriptive responses.
   - Keep the TypeScript types high-level where report internals are large, e.g. `report: DescriptiveAstrologyReport` with `natalChart: unknown` or a documented placeholder.
4. Update handoff summary documentation.
   - Remove or update stale wording that says the conversion was completed across eight iterations.
   - Update test-count references if present.
   - Mention the new React API contract document.
   - Ensure it still says the current descriptive REST response shape is `{ "report": {...}, "suggestedFilename": "..." }`.
5. Preserve current behavior.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - `POST /api/descriptive` still returns a single `report` plus `suggestedFilename`.
   - `GET /api/doctrines` unchanged.
   - CLI multi-doctrine behavior unchanged.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- CORS parsing trims whitespace and ignores blank entries.
- Tests cover allowed and disallowed CORS origins.
- `reports/react-api-contract.md` exists and is useful for a React implementation.
- `reports/spring-boot-conversion-summary.md` no longer contains stale iteration/test-count wording.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add frontend assets or create the React app in this iteration.
- Do not add authentication, sessions, credentials-based CORS, database persistence, OpenAPI, Actuator, or deployment infrastructure.
- Do not add predictive endpoints.
- Do not reintroduce plural `doctrines` in the REST descriptive request.
- Do not introduce hidden default doctrine selection.
- Keep this iteration small and focused on CORS robustness + frontend contract docs.

### Feedback Requested
Please append an Iteration 12 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- CORS parsing behavior and test coverage.
- Summary of the React API contract document.
- Any known limitations.
- Suggested next step.

## Iteration 13 — 2026-05-05

### Manager Review Notes
Iteration 12 is accepted. I reviewed the CORS parsing hardening, allowed/disallowed CORS tests, React API contract, and handoff summary updates. I independently ran the full sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

The remaining privacy/local-first concern is the global `Logger.instance`. Some calculators write execution-level errors to this singleton. In the CLI path this is intentional because `output/run-logger.json` is written. In the REST path, however, request-related calculation log entries should not accumulate globally across users/requests.

### Goal
Make REST calculation logging ephemeral/request-isolated while preserving CLI run logging behavior.

### Scope
- Improve `app.output.Logger` so REST calculations can run with isolated per-request log storage.
- Wrap REST descriptive calculation in isolated logging.
- Keep CLI logging and `output/run-logger.json` behavior unchanged.
- Do not put execution log entries into astrology report JSON.
- No endpoint shape or calculation behavior changes.

### Requirements
1. Add request-isolated logging support to `app.output.Logger`.
   - Preserve current public `Logger.instance.info(...)`, `error(...)`, `hasErrors()`, `getEntries()`, and `getStartedAt()` behavior for the CLI/global path.
   - Add a small API to run code with thread-local/isolated log entries, for example:
     ```java
     Logger.instance.runIsolated(() -> ...)
     ```
     or equivalent.
   - While isolated logging is active on the current thread, calls to `Logger.instance.info/error` should write to the isolated request log, not the global CLI log.
   - After the isolated block finishes, isolated entries must be cleared even if an exception occurs.
   - Keep this thread-safe enough for concurrent REST requests; avoid a single shared temporary list.
2. Use isolated logging in the REST descriptive path.
   - Wrap the calculation part of `POST /api/descriptive` so calculator/logger calls made during REST calculation do not accumulate in global `Logger.instance` entries.
   - CLI path must remain unchanged: CLI report generation should still populate `Logger.instance` and write `output/run-logger.json`.
   - REST responses should not include isolated log entries for now. Execution-level logs remain application concerns, not astrology report data.
3. Add tests.
   - Add unit tests for logger isolation:
     - global `info/error` outside isolation still affects global entries,
     - `info/error` inside isolation does not change global entries,
     - isolated entries are cleared after exceptions.
   - Avoid tests that depend on ordering from previous tests if possible.
   - If adding a reset/clear helper for tests, keep it package-private or clearly test-oriented and avoid changing runtime semantics accidentally.
4. Update documentation.
   - Update README privacy/local-first section and/or `reports/spring-boot-conversion-summary.md` to state:
     - CLI still writes `output/run-logger.json`,
     - REST calculations use isolated ephemeral logging and do not write or retain request logs by default.
   - Update `reports/react-api-contract.md` if useful to mention that REST errors are returned as `{ "error": "..." }` and server-side execution logs are not part of the report contract.
5. Preserve current REST API contract.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - Response remains `{ "report": {...}, "suggestedFilename": "..." }`.
   - `Cache-Control: no-store` remains.
   - `GET /api/doctrines` unchanged.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- Logger isolation tests prove REST-style isolated logging does not pollute global CLI logger entries.
- CLI run logging still writes `output/run-logger.json` in representative CLI run.
- REST endpoint shapes and documented React API contract remain unchanged.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not add predictive endpoints.
- Do not introduce report-level execution log data.
- Do not remove the existing CLI logger yet; isolate REST usage only.
- Keep this iteration focused on privacy-oriented logging isolation.

### Feedback Requested
Please append an Iteration 13 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of the logger isolation design.
- Confirmation that CLI logging still works.
- Confirmation that REST API shape did not change.
- Any known limitations.
- Suggested next step.

## Iteration 14 — 2026-05-05

### Manager Review Notes
Iteration 13 is accepted. I reviewed the `Logger` thread-local isolation implementation, REST controller wrapping, logger tests, and documentation updates. I independently ran the full sequential verification set successfully:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

I also checked `output/run-logger.json`; the representative CLI run still writes the expected entry:

```text
Wrote descriptive report for doctrine valens
```

One documentation issue remains: `reports/spring-boot-conversion-summary.md` still contains a stale/contradictory limitation saying `Logger.instance` accumulates entries across CLI and REST requests and that REST entries accumulate but are not written. That was true before Iteration 13, but now REST calculation logging is isolated and ephemeral.

One small robustness improvement is also worth doing: nested `runIsolated(...)` calls currently replace the thread-local list and then remove it, which would lose an outer isolated context if nesting ever happens. This is not currently used, but it is easy to harden now.

### Goal
Polish logger isolation robustness and correct stale documentation after Iteration 13.

### Scope
- Fix stale/contradictory logging documentation.
- Make nested logger isolation safe.
- Add focused tests for nested logger isolation.
- Keep REST API shape, CLI behavior, and calculation behavior unchanged.

### Requirements
1. Fix logger isolation nesting.
   - Update `Logger.runIsolated(...)` and `Logger.runIsolatedVoid(...)` so they preserve any existing isolated context on the same thread.
   - Suggested behavior:
     - save the previous thread-local list before setting a new one,
     - run the isolated block,
     - in `finally`, restore the previous list if it existed, otherwise remove the thread-local.
   - This ensures nested isolation does not accidentally clear the outer context.
2. Add logger tests for nesting.
   - Add a test proving that after an inner `runIsolated(...)` completes, the outer isolated context is still active.
   - Add a test proving that after an inner `runIsolated(...)` throws, the outer isolated context is still restored before the outer block finishes.
   - Existing tests for clearing after exceptions should continue to pass.
3. Correct documentation.
   - Update `reports/spring-boot-conversion-summary.md` so the logging limitation is accurate:
     - CLI uses global `Logger.instance` and writes `output/run-logger.json`.
     - REST descriptive calculations use thread-isolated ephemeral logging and do not retain request calculation logs by default.
   - Remove the stale statement that REST entries accumulate globally.
   - If useful, add the same clarification to README privacy/local-first section.
4. Preserve current behavior.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - `POST /api/descriptive` still returns `{ "report": {...}, "suggestedFilename": "..." }`.
   - `Cache-Control: no-store` remains.
   - `GET /api/doctrines` unchanged.
   - CLI multi-doctrine behavior and CLI run logging unchanged.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- Nested logger isolation tests pass.
- Handoff summary no longer says REST logger entries accumulate globally.
- REST endpoint shapes and React API contract remain unchanged.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints.
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not expose isolated execution logs in report JSON.
- Do not remove the existing CLI logger.
- Keep this iteration small and focused on logger-isolation robustness and documentation correction.

### Feedback Requested
Please append an Iteration 14 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of nested logger isolation behavior.
- Confirmation that stale logging documentation was corrected.
- Confirmation that REST API shape did not change.
- Any known limitations.
- Suggested next step.

## Iteration 15 — 2026-05-05

### Manager Review Notes
Iteration 14 was issued but has not been completed; there is no Iteration 14 feedback block in `worker.md`. Do not assume Iteration 14 is accepted or implemented. This latest block supersedes the old Iteration 14 task for now; do not separately work on the nested logger-isolation polish unless it is explicitly reissued later.

I reviewed `audit_report.md` (Claude Opus 4.7 review). The highest-leverage audit recommendation is to add a calculation-regression snapshot before further refactors. The current Spring Boot tests verify endpoint shape and serialization, but they do not protect the actual chart contents/planetary numbers. Add that safety net first, then later iterations can address object-mapper scoping, service splitting, logger filter/request lifecycle hardening, and other audit findings with less regression risk.

### Goal
Add a full descriptive REST chart snapshot regression test for the representative `ilia` Valens calculation, without changing calculation behavior or endpoint semantics.

### Scope
- Add test-only snapshot coverage for `POST /api/descriptive`.
- Use the current single-doctrine REST API shape: `{ "report": {...}, "suggestedFilename": "..." }`.
- Snapshot the full JSON response for the `ilia` fixture with doctrine `valens`.
- Do not change production calculation code, doctrine code, chart models, or REST API behavior.

### Requirements
1. Add a committed JSON snapshot resource.
   - Suggested path: `src/test/resources/snapshots/descriptive/ilia-valens-response.json`.
   - The snapshot must represent the full current response from:
     ```json
     {
       "id": "ilia",
       "birthDate": "1975-07-14",
       "birthTime": "22:55:00",
       "utcOffset": "+01:00",
       "latitude": 50.60600755996812,
       "longitude": 3.0333769552426793,
       "doctrine": "valens"
     }
     ```
   - Include the response wrapper, `suggestedFilename`, and the entire nested `report`, including `natalChart`.
   - The snapshot should reflect current `MystroObjectMapper` serialization conventions, especially six-decimal double rounding and Java time strings.
2. Add a focused snapshot regression test.
   - Prefer a new test class such as `DescriptiveSnapshotTest`, or keep it clearly separated from controller contract/validation tests.
   - Use `@SpringBootTest` + `MockMvc` unless a simpler existing pattern fits better.
   - POST the request body above to `/api/descriptive`.
   - Assert HTTP 200 and `Cache-Control: no-store`.
   - Compare the full actual response JSON to the committed snapshot as JSON, not as raw text, so formatting and object-field order do not matter.
   - A Jackson `JsonNode` comparison is sufficient; do not introduce a large snapshot library unless there is a clear reason.
3. Keep the snapshot honest.
   - Generate the initial snapshot from the current code once, then make the test compare future responses against that committed file.
   - Do not compute expected chart values in the test from production calculators; that would compare the code to itself.
   - Do not loosen the assertion to only a few fields. The purpose is to catch accidental changes to chart numbers, report structure, aspect data, dignities/debilities, syzygy/lots, and serialization.
   - If the snapshot is intentionally large, that is acceptable; keep it in `src/test/resources` and document its purpose briefly in the test class.
4. Preserve current behavior.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - Response remains `{ "report": {...}, "suggestedFilename": "..." }`.
   - `GET /api/doctrines` unchanged.
   - CLI multi-doctrine behavior unchanged.
   - REST calls still do not write output files.
5. Documentation is optional but useful.
   - If you add a short test comment or README/test note, describe this as a regression snapshot for calculation output, not as a new public API feature.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- A committed snapshot file exists for the full `ilia`/`valens` REST descriptive response.
- A test fails if the full response JSON changes unexpectedly.
- Existing REST API shape remains unchanged and no plural REST `doctrines` request field is reintroduced.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not implement the old Iteration 14 nested logger-isolation task in this iteration.
- Do not address broader audit refactors in this iteration, including object-mapper scoping, service splitting, shared engine assembly, dead-code deletion, or `application.yml`.
- Do not add predictive endpoints.
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not introduce hidden default doctrine selection.
- Keep this iteration test-only unless a tiny test helper is required.

### Feedback Requested
Please append an Iteration 15 feedback block to `worker.md` with:
- Completed work summary.
- Changed files, including the snapshot resource path.
- Exact verification commands and results.
- How the snapshot was generated and how the comparison works.
- Confirmation that REST API shape did not change.
- Confirmation that no calculation/doctrine/model behavior was changed.
- Any known limitations, especially expected snapshot churn if intentional calculation changes are made later.
- Suggested next audit-driven step.

## Iteration 16 — 2026-05-05

### Manager Review Notes
Iteration 15 is accepted. I reviewed `worker.md`, `src/test/java/app/web/DescriptiveSnapshotTest.java`, and `src/test/resources/snapshots/descriptive/ilia-valens-response.json`. The new snapshot test is appropriately separated from controller contract tests, compares full JSON as a `JsonNode`, verifies `Cache-Control: no-store`, and preserves the single-report REST shape.

I independently ran the required verification commands sequentially:

```bash
mvn compile
mvn test
mvn package -DskipTests
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

All passed. `mvn test` now reports 29 tests. The existing `GlobalExceptionHandlerTest` still emits its known intentional stack trace while passing.

Now that the snapshot safety net is in place, address the highest-priority remaining logging concern from `audit_report.md`: REST logging isolation should cover the whole `/api/**` request lifecycle, not only the calculation lambda inside `DescriptiveController`. Also reissue the small nested-isolation robustness improvement that was skipped when Iteration 14 was not completed.

### Goal
Make REST request logging isolation lifecycle-wide and nest-safe, while preserving CLI run logging and the current REST API contract.

### Scope
- Add a small Spring web filter/interceptor or equivalent mechanism that wraps `/api/**` requests in `Logger.instance.runIsolated(...)`.
- Make nested `Logger.runIsolated(...)` calls safe by restoring any previous isolated context.
- Remove or simplify the controller-level logging wrapper if the request-level wrapper makes it redundant.
- Keep CLI logging, report shape, chart calculation, and doctrine behavior unchanged.

### Requirements
1. Harden nested logger isolation.
   - Update `Logger.runIsolated(...)` and `Logger.runIsolatedVoid(...)` so they preserve an existing isolated context on the same thread.
   - Required behavior:
     - save the previous thread-local list before installing a new isolated list,
     - run the block,
     - in `finally`, restore the previous list if one existed, otherwise remove the thread-local.
   - Existing global CLI behavior for `info(...)`, `error(...)`, `hasErrors()`, `getEntries()`, and `getStartedAt()` must remain unchanged.
2. Wrap the REST request lifecycle.
   - Add a focused web component such as `LoggerIsolationFilter` using `OncePerRequestFilter`, or an equivalent Spring MVC mechanism.
   - Apply it to `/api/**` only.
   - Any `Logger.instance.info/error` call made during a REST API request should go to isolated thread-local entries and be cleared after the request, even if the request fails.
   - REST responses must not include isolated log entries.
   - CLI execution must not use this filter and must continue writing global entries to `output/run-logger.json`.
3. Clean up `DescriptiveController` if appropriate.
   - If lifecycle-wide isolation makes the current `Logger.instance.runIsolated(...)` calculation wrapper redundant, remove it from the controller.
   - If removing it, drop the controller method's `throws Exception` if no longer needed.
   - Keep the controller thin: request null check, mapper/service delegation, response construction.
4. Add focused tests.
   - Extend `LoggerTest` for nested isolation:
     - after an inner `runIsolated(...)` completes, the outer isolated context is still active,
     - after an inner `runIsolated(...)` throws, the outer isolated context is restored before the outer block finishes.
   - Add a web/filter test proving that a `Logger.instance.info/error` call inside an `/api/**` request does not change global `Logger.instance.getEntries()`.
     - Prefer a unit-style filter test with mock request/response/filter chain if that stays small.
     - Do not add production-only endpoints just to test this.
   - Existing snapshot and REST controller tests must continue to pass.
5. Preserve current behavior and contracts.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - Response remains `{ "report": {...}, "suggestedFilename": "..." }`.
   - `Cache-Control: no-store` remains on descriptive success/error responses.
   - `GET /api/doctrines` unchanged.
   - REST calls still do not write output files.
   - CLI multi-doctrine behavior and `output/run-logger.json` behavior unchanged.
6. Documentation.
   - Update `reports/spring-boot-conversion-summary.md` and/or `reports/react-api-contract.md` if wording needs to change from calculation-only isolation to lifecycle-wide request isolation.
   - Keep this short; no new feature documentation is required.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes, including the Iteration 15 snapshot test.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- Nested logger isolation tests pass.
- A web/filter test proves `/api/**` request logging does not pollute global CLI logger entries.
- REST endpoint shapes and snapshot remain unchanged.
- CLI run logging still writes `output/run-logger.json` for the representative run.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints.
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not expose isolated execution logs in report JSON.
- Do not remove the existing CLI logger.
- Do not address broader audit refactors in this iteration, including object-mapper scoping, service splitting, shared engine assembly, dead-code deletion, or `application.yml`.
- Keep this iteration focused on request lifecycle logger isolation and nested isolation robustness.

### Feedback Requested
Please append an Iteration 16 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of nested logger restoration behavior.
- Explanation of the request lifecycle isolation mechanism and `/api/**` scoping.
- Confirmation that CLI logging still works.
- Confirmation that REST API shape and the snapshot did not change.
- Any known limitations.
- Suggested next audit-driven step.

## Iteration 17 — 2026-05-05

### Manager Review Notes
Iteration 16 is **partially accepted but not complete**. I reviewed the worker feedback and inspected `Logger`, `LoggerIsolationFilter`, `DescriptiveController`, `LoggerTest`, `LoggerIsolationFilterTest`, and the handoff summary. I also independently ran:

```bash
mvn test
```

It passed with 33 tests. The nested logger restoration work is good, the controller cleanup is good, and the snapshot still passes.

However, one Iteration 16 requirement was missed: the logger isolation filter was required to apply to `/api/**` only. The implementation currently registers `LoggerIsolationFilter` as a Spring component and wraps every request that reaches the filter; it does not implement `shouldNotFilter(...)`, a URL pattern registration, or another path restriction. The worker feedback explicitly says it is "Applied to all requests", which conflicts with the requirement and handoff documentation saying `/api/**` only.

### Goal
Correct the logger isolation filter so it is actually scoped to `/api/**`, and add tests proving both API and non-API behavior.

### Scope
- Small correction to `LoggerIsolationFilter` path scoping.
- Focused filter tests for `/api/**` and non-API requests.
- Minor documentation/test-count cleanup.
- No REST API, CLI, calculation, doctrine, or model behavior changes.

### Requirements
1. Scope `LoggerIsolationFilter` to `/api/**` only.
   - Use `OncePerRequestFilter.shouldNotFilter(...)`, explicit filter registration URL patterns, or another simple Spring mechanism.
   - If using `shouldNotFilter(...)`, ensure `/api/descriptive`, `/api/doctrines`, and nested `/api/...` paths are isolated, while non-API paths are not isolated.
   - Be mindful of context paths; prefer a robust request path source such as `getServletPath()` or a clearly documented equivalent.
2. Strengthen filter tests.
   - Keep a test proving an API request log call does **not** change global `Logger.instance.getEntries()`.
   - Add a test proving a non-API request log call **does** change global `Logger.instance.getEntries()` because the filter does not isolate that request.
   - Add a test for failed API filter-chain execution if not already covered.
   - Do not add production-only endpoints just to test this.
3. Correct documentation.
   - Update `reports/spring-boot-conversion-summary.md` so its test table reflects current tests, including:
     - `LoggerTest` has 8 tests,
     - `DescriptiveSnapshotTest` exists,
     - `LoggerIsolationFilterTest` exists.
   - Keep the logging limitation wording accurate: lifecycle-wide isolation applies to `/api/**`, not every possible web request.
4. Preserve current behavior and contracts.
   - `POST /api/descriptive` still uses singular `doctrine`.
   - Response remains `{ "report": {...}, "suggestedFilename": "..." }`.
   - The Iteration 15 snapshot must remain unchanged and pass.
   - `GET /api/doctrines` unchanged.
   - CLI multi-doctrine behavior and `output/run-logger.json` behavior unchanged.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `LoggerIsolationFilter` is demonstrably scoped to `/api/**` only.
- Tests prove API requests are isolated and non-API requests are not isolated.
- REST endpoint shapes and the snapshot remain unchanged.
- Handoff summary test counts/files are no longer stale.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add predictive endpoints.
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not expose isolated execution logs in report JSON.
- Do not remove the existing CLI logger.
- Do not address broader audit refactors in this iteration, including object-mapper scoping, service splitting, shared engine assembly, dead-code deletion, or `application.yml`.
- Keep this iteration limited to the missed filter scoping requirement and related docs/tests.

### Feedback Requested
Please append an Iteration 17 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of the `/api/**` scoping mechanism.
- Test evidence for API isolated vs non-API non-isolated behavior.
- Confirmation that CLI logging still works.
- Confirmation that REST API shape and the snapshot did not change.
- Any known limitations.
- Suggested next audit-driven step.

## Iteration 18 — 2026-05-05

### Manager Review Notes
Iteration 17 is accepted with one minor documentation correction required. I reviewed `worker.md`, `LoggerIsolationFilter`, `LoggerIsolationFilterTest`, and `reports/spring-boot-conversion-summary.md`. I independently ran:

```bash
mvn test
```

It passed with 38 tests. The filter is now scoped through `shouldNotFilter(...)` using `getServletPath().startsWith("/api/")`, and tests prove API requests are isolated while non-API requests pass through to the global logger.

One small issue remains: the handoff summary test table still says `LoggerIsolationFilterTest.java` has 6 tests, but the class now has 7 tests. Correct that stale count before moving on to broader audit refactors.

### Goal
Fix the remaining stale test-count documentation from Iteration 17.

### Scope
- Documentation-only correction in `reports/spring-boot-conversion-summary.md`.
- No Java production or test behavior changes unless needed to verify the documentation correction.

### Requirements
1. Update `reports/spring-boot-conversion-summary.md`.
   - Change the `LoggerIsolationFilterTest.java` row from 6 tests to 7 tests.
   - Ensure the row still describes `/api/**` scoping, API isolation, and non-API passthrough accurately.
2. Preserve current behavior.
   - Do not change `LoggerIsolationFilter`, `Logger`, controllers, DTOs, snapshot, doctrine code, chart models, input format, or CLI flags.
   - REST API shape remains unchanged.
3. Verification.
   - Because this is documentation-only, `mvn test` is sufficient if no Java files are changed.
   - If any Java file changes, run the full standard verification set.

### Acceptance Criteria
- `reports/spring-boot-conversion-summary.md` no longer has the stale `LoggerIsolationFilterTest.java` count.
- `mvn test` passes.
- No production behavior changes.

### Constraints
- Keep this iteration documentation-only.
- Do not address broader audit refactors yet.

### Feedback Requested
Please append an Iteration 18 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification command and result.
- Confirmation that no Java/behavior/API/calculation changes were made.
- Suggested next audit-driven step.

## Iteration 19 — 2026-05-05

### Manager Review Notes
Iteration 18 is accepted. I reviewed `worker.md` and `reports/spring-boot-conversion-summary.md`; the stale `LoggerIsolationFilterTest.java` count is corrected to 7 tests. I independently ran:

```bash
mvn test
```

It passed with 38 tests. No Java behavior changed.

Next, address audit finding #3 from `audit_report.md`: `MystroObjectMapper` is currently registered as an application-wide Spring `ObjectMapper` bean in `WebConfig`. That means Mystro's report-specific six-decimal double rounding becomes the default mapper for any future Spring MVC endpoint or infrastructure serialization. Keep the Mystro JSON conventions for current REST API responses, but stop exposing the Mystro mapper as the global application `ObjectMapper` bean.

### Goal
Scope Mystro's Jackson serialization configuration to REST response conversion without replacing Spring Boot's application-wide `ObjectMapper` bean.

### Scope
- Adjust Spring web JSON wiring only.
- Preserve current REST serialization behavior, including six-decimal double rounding and Java time strings.
- Preserve file-output serialization through `JsonReportWriter`/`MystroObjectMapper`.
- Do not change report shape, endpoints, calculation behavior, or doctrine behavior.

### Requirements
1. Remove the global Mystro `ObjectMapper` bean from `WebConfig`.
   - Do not expose `MystroObjectMapper.create()` via a method annotated `@Bean ObjectMapper`.
   - Spring Boot may still auto-create its own default `ObjectMapper`; that is fine.
2. Keep Mystro serialization for current REST API responses.
   - Keep or replace the `MappingJackson2HttpMessageConverter` bean so it uses `MystroObjectMapper.create()` directly.
   - Ensure `POST /api/descriptive` still serializes doubles rounded to six decimals and Java time values as strings.
   - Ensure `GET /api/doctrines` remains unchanged.
   - If converter ordering must be adjusted to keep Mystro responses using the Mystro mapper, do so in a small, explicit way and document it in `worker.md`.
3. Preserve file output.
   - `JsonReportWriter` should continue using `MystroObjectMapper.create()`.
   - CLI file output shape and rounding must remain unchanged.
4. Add/update tests as needed.
   - Existing `descriptiveRoundsDoublesToSixDecimals` and the Iteration 15 snapshot test must continue to pass.
   - Add a small test only if needed to prove the wiring no longer declares a Mystro `ObjectMapper` bean from `WebConfig` or to protect converter behavior.
   - Do not add unrelated endpoints just to test mapper scoping.
5. Documentation.
   - Update `reports/spring-boot-conversion-summary.md` if it currently implies that the Mystro mapper is the application-wide `ObjectMapper` bean.
   - The summary should say file output and REST API responses use the same Mystro Jackson conventions, while the Spring Boot global mapper is not intentionally replaced.

### Acceptance Criteria
- `mvn compile` passes.
- `mvn test` passes, including the snapshot test and REST double-rounding test.
- `mvn package -DskipTests` passes.
- Existing CLI check passes: `mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"`.
- `WebConfig` no longer exposes `MystroObjectMapper.create()` as a global `@Bean ObjectMapper`.
- REST descriptive response shape and snapshot remain unchanged.
- File-output serialization remains unchanged.
- No doctrine implementations, chart model/data classes, input file format, CLI flags, or astrology calculation behavior are changed.

### Constraints
- Do not add new endpoints.
- Do not add predictive support.
- Do not add authentication, sessions, database persistence, frontend assets, OpenAPI, Actuator, or deployment infrastructure.
- Do not address broader audit refactors in this iteration, including service splitting, shared engine assembly, dead-code deletion, or `application.yml`.
- Keep this iteration focused on Jackson mapper scoping.

### Feedback Requested
Please append an Iteration 19 feedback block to `worker.md` with:
- Completed work summary.
- Changed files.
- Exact verification commands and results.
- Explanation of the new Jackson/MVC converter wiring.
- Confirmation that REST rounding and snapshot tests still pass.
- Confirmation that CLI file output serialization still works.
- Confirmation that REST API shape did not change.
- Any known limitations.
- Suggested next audit-driven step.
