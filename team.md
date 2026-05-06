# Team Collaboration Rules

This document defines the manager/worker collaboration process for maintaining Mystro as a REST-only Spring Boot application.

## Roles

### Manager

The manager is responsible for planning, reviewing, and issuing work requirements.

Manager duties:

- Maintain the conversion plan.
- Read `worker.md` for worker feedback when prompted by the user.
- Review relevant code changes before issuing the next task.
- Write the next requirement block to `manager.md`.
- Keep each iteration small, testable, and rollback-friendly.
- Preserve existing astrology calculation behavior unless explicitly changing it.
- Stop or redirect work when the implementation drifts beyond scope.

### Worker

The worker is responsible for implementing the latest requirement from `manager.md`.

Worker duties:

- Read the latest requirement block in `manager.md` before making changes.
- Implement only the requested scope unless a blocker requires otherwise.
- Preserve existing behavior unless the requirement explicitly says to change it.
- Keep changes small, reviewable, and aligned with the architecture.
- Run required verification commands sequentially.
- Report completion, changed files, verification results, blockers, risks, and questions in `worker.md`.

## Communication Files

### `manager.md`

The manager writes requirements here. Each requirement block must include:

```md
## Iteration N — YYYY-MM-DD

### Goal
...

### Scope
- ...

### Requirements
- ...

### Acceptance Criteria
- ...

### Constraints
- ...

### Feedback Requested
- ...
```

Rules:

- Append new iterations instead of deleting prior instructions.
- Every block must include an iteration number and date stamp.
- Requirements should be specific and independently verifiable.
- Avoid broad rewrites unless explicitly planned.

### `worker.md`

The worker writes feedback here. Each feedback block should include:

```md
## Iteration N Feedback — YYYY-MM-DD

### Completed
- ...

### Changed Files
- ...

### Verification
- Command:
- Result:

### Architectural Notes
- ...

### Blockers / Questions
- ...

### Known Limitations / Risks
- ...

### Suggested Next Step
- ...
```

Rules:

- The worker should append feedback for each completed iteration.
- Verification results must state the exact commands run and whether they passed or failed.
- Blockers, deviations from scope, and assumptions must be clearly documented.
- If an architectural choice was made, briefly explain why.
- If a requirement was not completed, say so explicitly.

## Iteration Workflow

1. User asks the manager to start or continue.
2. Manager reads `worker.md` if prior work exists.
3. Manager reviews relevant code and project state.
4. Manager appends the next requirement block to `manager.md`.
5. Worker implements the latest requirement.
6. Worker runs verification commands sequentially.
7. Worker appends feedback to `worker.md`.
8. Manager reviews feedback and code before issuing the next iteration.

## Turn Handoff Phrase

When the user says **"your turn"**, interpret it according to the active role:

- **Manager**: read the latest `worker.md` feedback, review the worker's changed files and verification results, independently inspect or verify relevant work when practical, decide whether the iteration is accepted, and append the next requirement block to `manager.md` if more work is needed. After reviewing, the manager must print to the terminal **only** a red-colored percentage showing how much of the last reviewed iteration the worker achieved, so it is visually obvious to the user. Use ANSI red, for example `\u001b[31m85%\u001b[0m`.
- **Worker**: read the latest requirement block in `manager.md`, implement that requirement, run the required verification commands sequentially, and append structured feedback to `worker.md`.

If the active role is unclear, ask the user to clarify whether the current session is acting as manager or worker before making changes.

## Change Strategy

Future REST-only work should remain incremental:

1. Keep runtime/application seams reusable and calculation-focused.
2. Keep the Spring Boot layer a thin adapter over existing calculation code.
3. Expose additional REST endpoints only when explicitly requested.
4. Add request/response DTOs, tests, and documentation alongside endpoint changes.
5. Preserve JSON serialization, logging isolation, and explicit doctrine selection.
6. Keep packaging/documentation aligned with the current REST-only application shape.

## Project Context Rules

- Mystro is a traditional astrology calculation engine first; Spring Boot is only an adapter layer.
- The existing calculation pipeline and doctrine behavior are authoritative unless a requirement explicitly changes them.
- Current descriptive reports expose top-level `engineVersion`, `subject`, `doctrine`, and `natalChart`.
- There must be no top-level `basicChart`, `descriptive`, or `calculationSetting` field unless the architecture spec changes.
- Doctrine selection must remain explicit.
- No hidden default doctrine should be introduced in REST paths.
- Swiss Ephemeris runtime data under `ephe/` is required and must not be deleted or treated as generated output.

## Architectural Rules

- Keep the Spring Boot layer thin.
- Controllers should orchestrate only: receive request, delegate mapping/service work, return response.
- Put request/response DTOs and request mappers in the web layer.
- Put reusable application orchestration in runtime/application-layer classes.
- Do not duplicate astrology calculation logic in controllers, DTOs, or tests.
- Do not rewrite doctrine implementations as part of web wiring.
- Do not move shared chart model ownership away from `app.chart.data` / `app.chart.model` without explicit approval.
- Do not reintroduce a CLI execution path unless explicitly requested.
- REST endpoints should not write output files unless explicitly requested.
- Preserve current output/report structure unless a requirement explicitly changes it.
- Do not introduce sidereal zodiac behavior or unrelated astrology features.

## REST API Rules

- REST descriptive generation must require one explicit singular `doctrine` id; call once per selected doctrine for local-first downloads.
- REST doctrine discovery may list available doctrines, but must not imply or select a default doctrine.
- REST responses should use stable wrapper shapes such as `{ "report": {...}, "suggestedFilename": "..." }` or `{ "doctrines": [...] }`.
- REST error responses should be JSON-shaped as `{ "error": "..." }`.
- Do not expose stack traces or internal exception details in REST error bodies.
- REST JSON serialization must match file-output conventions:
  - doubles rounded to six decimal places,
  - Java time values serialized as strings, not timestamps,
  - JDK8 optional/time support preserved.
- The REST path should reuse existing runtime services and domain classes rather than creating a parallel calculation path.

## Verification Rules

Do not run multiple Maven commands concurrently against the same `target/` directory. Run verification sequentially.

After Java code changes, the worker should run:

```bash
mvn compile
```

When tests exist or behavior is changed, also run:

```bash
mvn test
```

When packaging, startup, or executable jar behavior changes, also run:

```bash
mvn package -DskipTests
```

When a runtime smoke test is practical, briefly start the Spring Boot app (`mvn spring-boot:run` or `java -jar target/mystro-<version>.jar`), verify the relevant endpoint, and stop the process cleanly.

The worker must record all verification commands and results in `worker.md`.

## Worker Self-Review Checklist

Before reporting an iteration complete, the worker should check:

- Latest `manager.md` requirement was followed.
- No unrelated features or broad rewrites were introduced.
- No CLI/file-output path was reintroduced unless explicitly requested.
- Spring Boot code remains an adapter layer.
- Controllers remain thin.
- No astrology calculation logic was duplicated in web code.
- Doctrine ids remain explicit; no hidden default doctrine was added.
- Report shape is unchanged unless explicitly requested.
- REST JSON serialization still matches file output.
- Relevant tests were added or updated for changed behavior.
- Verification commands were run sequentially and recorded.
- Known limitations and risks are documented in `worker.md`.

## Definition of Done

An iteration is done only when:

- The requested scope is complete or blockers are clearly documented.
- Acceptance criteria are verified.
- Required Maven commands pass, or failures are documented with details.
- `worker.md` has been updated with structured feedback.
- Changed files are listed.
- Architectural choices, deviations, risks, and limitations are documented.
- No unsanctioned broad rewrite or unrelated feature was introduced.

## Scope Control and Escalation

- Each iteration should be small enough to review easily.
- If the worker discovers a blocker, they should stop expanding scope and document it in `worker.md`.
- If an architectural decision is unclear, the worker should ask in `worker.md` instead of guessing.
- If a requirement conflicts with the architecture spec, the worker should document the conflict and pause that part of the work.
- If implementation requires changing doctrine behavior, chart model ownership, REST request/response shape, input format, or reintroducing CLI/file-output behavior, the worker must call it out before proceeding.
- The manager decides the next step after reviewing worker feedback.

## Prohibited Unless Explicitly Requested

- Authentication or authorization.
- Database persistence.
- Frontend assets/UI.
- OpenAPI/Swagger.
- Spring Actuator.
- Deployment infrastructure.
- Predictive endpoints.
- New astrology features.
- Sidereal zodiac behavior.
- Hidden default doctrine selection.
- Reintroducing CLI flags, native input-file mode, or server-side report-file output.
