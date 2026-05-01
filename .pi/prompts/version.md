---
description: Save project knowledge, validate, commit, push, then advance the Maven version
argument-hint: "<one-line description of the main thing done>"
---

Run the version workflow.

Use the argument as `$desc`. If no argument is provided, derive a concise one-line description from the intended changes before committing.

1. Save durable knowledge into the accurate markdown files before committing.
   - Update `AGENTS.md` for current implementation facts and standing agent instructions.
   - Update `SESSION_MEMORY.md` for future-session context.
   - Update `NEW_ARCHITECTURE_SPEC.md` only when the architecture/specification itself changed.
   - Update relevant `.pi/skills/**/SKILL.md` files when workflow or skill behavior changed.
   - Update README or audit docs only when they are the accurate home for that knowledge.
   - Ensure markdown files do not reintroduce stale former-architecture guidance such as `--names`, hidden default doctrines, config/profile doctrines, or treating `app.old` as active code.
2. Check repository status and identify intended vs accidental/generated files. Commit all intended tracked and untracked project files, including markdown/audit files, but never commit `input/`, `output/`, `target/`, generated build output, or accidental local files.
3. Run:

```bash
mvn compile
```

4. If compilation fails, issue a prominent warning, stop immediately, and do not commit, push, or bump the version.
5. If compilation succeeds:
   - Read the current project version `$v` from the first project `<version>` in `pom.xml`.
   - Commit the intended changes with exactly this message shape:

```text
version $v: $desc
```

6. Push the commit.
7. After the successful push, level up the project version in `pom.xml` for the next development cycle.
   - Increment the minor component and reset patch to zero, e.g. `0.6.0` → `0.7.0`, `0.24.0` → `0.25.0`.
   - Never increment the major component automatically; the project owner does major bumps manually.
   - Do not include the post-push version bump in the version commit unless the user explicitly asks for a release-style version bump before committing.
