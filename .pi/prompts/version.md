---
description: Validate, commit, and push the current work
argument-hint: "[commit message]"
---
Prepare the current project changes for a version commit and complete the git workflow.

Required workflow:

1. Inspect the repo state first:
   - run `git status --short --branch`
   - review the changed files before committing
   - make sure ignored/generated files are not accidentally included

2. If the current changes affect project docs, workflow notes, or durable behavior, update the relevant markdown files before committing:
   - `AGENTS.md`
   - `SESSION_MEMORY.md`
   - `README.md`
   - related files under `.pi/skills/`
   - any other impacted markdown docs

3. Validate the project before committing:
   - run `mvn compile`
   - if runtime behavior changed, also run `mvn exec:java -Dexec.args="--names ilia"`

4. Stage the correct files for commit:
   - include all intentional source/doc/config changes
   - exclude ignored files and accidental local-only artifacts

5. Create a git commit:
   - if the user supplied arguments, use them as the commit message: `$@`
   - otherwise use: `Update project`

6. Push the commit to the current upstream branch.

7. At the end, report:
   - validation commands run and whether they passed
   - commit hash and message
   - branch pushed
   - any files intentionally left unstaged

Important rules:
- Do not commit build output, temporary files, editor-local files, or ignored files.
- If validation fails, stop before committing and explain the failure.
- If the working tree contains unrelated suspicious changes, call them out before committing.
- Be deliberate about what gets staged; do not blindly commit everything unless it is clearly intended.
