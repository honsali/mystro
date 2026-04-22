---
description: Validate, commit, and push the current work
argument-hint: "[commit message]"
---
Prepare the current project changes for a version commit and complete the git workflow.

Versioning rule for this project:
- use the top-level `<version>` in `pom.xml` as the project version
- after a successful version commit, bump the version for the next iteration by incrementing the **middle number** and resetting the last number to zero
- example: `0.2.0 -> 0.3.0 -> 0.4.0`
- do not use snapshot suffixes unless the user explicitly asks
- do not introduce major-version changes unless the user explicitly asks

Required workflow:

1. Inspect the repo state first:
   - run `git status --short --branch`
   - review the changed files before committing
   - make sure ignored/generated files are not accidentally included

2. Read the current version from `pom.xml` and treat it as the version being released by this command.

3. If the current changes affect project docs, workflow notes, or durable behavior, update the relevant markdown files before committing:
   - `AGENTS.md`
   - `SESSION_MEMORY.md`
   - `README.md`
   - related files under `.pi/skills/`
   - any other impacted markdown docs

4. Validate the project before committing:
   - run `mvn compile`
   - if runtime behavior changed, also run `mvn exec:java -Dexec.args="--names ilia"`

5. Stage the correct files for the version commit:
   - include all intentional source/doc/config changes
   - exclude ignored files and accidental local-only artifacts

6. Create the version commit:
   - if the user supplied arguments, use them as the commit message: `$@`
   - otherwise use: `Update project`

7. Push the version commit to the current upstream branch.

8. Then bump `pom.xml` for the next iteration:
   - increment the middle number of the current version and reset the last number to zero
   - example: `0.2.0 -> 0.3.0`
   - stage only the intentional version-bump doc/config updates that are needed because of the new version
   - create a second commit for the bump, for example: `Bump version to <new-version>`
   - push that commit too

9. At the end, report:
   - validation commands run and whether they passed
   - released version
   - version commit hash and message
   - next iteration version
   - version-bump commit hash and message
   - branch pushed
   - any files intentionally left unstaged

Important rules:
- Do not commit build output, temporary files, editor-local files, or ignored files.
- If validation fails, stop before committing and explain the failure.
- If the working tree contains unrelated suspicious changes, call them out before committing.
- Be deliberate about what gets staged; do not blindly commit everything unless it is clearly intended.
