---
description: Update AGENTS, session memory, skills, and other markdown docs to reflect the current session
---
Read the project memory/documentation files first, then update them so they accurately reflect what changed in this session.

Required workflow:

1. Read these files if they exist:
   - `AGENTS.md`
   - `SESSION_MEMORY.md`
   - `ASTRO_RULES.md`
   - `README.md`
   - `JAVA_MIGRATION.md`
   - all skill files under `.pi/skills/*/SKILL.md`

2. Inspect current project state before editing:
   - use `rg` to find stale references to removed features, old commands, old files, markdown output, tests, language/i18n flow, obsolete services, or outdated architecture notes
   - read any markdown files that look affected before changing them

3. Update all relevant markdown/docs so they match reality:
   - `AGENTS.md`
   - `SESSION_MEMORY.md`
   - skill files in `.pi/skills/`
   - `README.md`
   - any other affected `.md` files in the repo

4. Focus on durable facts and workflow changes from this session, such as:
   - files/classes added, removed, renamed, or moved
   - architecture changes
   - command/workflow changes
   - output format changes
   - validation/build changes
   - project conventions that are now different

5. Do not add fluff. Remove stale statements. Prefer precise bullet updates.

6. After doc updates:
   - run the current validation command appropriate for the project state
   - report which markdown files were updated
   - summarize the important durable changes captured

Important rules:
- Do not invent changes; derive them from the actual current codebase.
- Prefer updating existing memory/docs over creating new markdown files unless clearly needed.
- If a markdown file is now obsolete and misleading, say so and propose removal or remove it if safe.
- Keep session memory concise but durable for the next session.
