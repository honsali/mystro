---
description: Respond to audit_report.md by applying agreed points and briefly justifying ignored points
argument-hint: "[optional focus or constraints]"
---

Read `NEW_ARCHITECTURE_SPEC.md` first, then read `audit_report.md` completely.

Treat the audit as recommendations, not commands:

1. Evaluate each audit point against the current repository state and project architecture.
2. Apply the points you agree with.
   - Prefer precise, minimal edits.
   - Keep `NEW_ARCHITECTURE_SPEC.md` authoritative.
   - Update `AGENTS.md`, `SESSION_MEMORY.md`, README, and relevant `.pi/skills/**/SKILL.md` files only when they are the accurate home for the corrected knowledge.
   - Do not introduce hidden default doctrines, config/profile doctrines, stale paths, or competing architecture descriptions.
3. Ignore points you disagree with or cannot safely apply, and track why.
4. If Java code changes are made, run:

```bash
mvn compile
```

5. End with a concise response containing:
   - Applied changes, grouped by file or topic.
   - Ignored/skipped audit points with brief justifications.
   - Verification performed, including compile result if applicable.
