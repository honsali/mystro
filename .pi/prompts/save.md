---
description: Update project markdown docs so they reflect the current clean architecture
---

Read `NEW_ARCHITECTURE_SPEC.md` first.

When updating documentation:

- keep `NEW_ARCHITECTURE_SPEC.md` authoritative
- remove stale references to non-current architecture
- remove stale references to former package names, class names, and output paths unless explicitly marked historical
- keep docs concise
- do not create competing architecture descriptions

After Java code changes, run:

```bash
mvn compile
```

For current verification after behavior or web/API changes, run:

```bash
mvn test
```
