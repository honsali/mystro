---
name: astro-run-after-changes
description: Use when modifying the Mystro Java project so code changes are followed by compilation and current runtime checks.
compatibility: Requires the Mystro Java Maven project.
---

# Astro Run After Changes

After Java code changes, run:

```bash
mvn compile
```

When behavior or web/API changes, also run:

```bash
mvn test
```

For packaging or startup verification, also run:

```bash
mvn package -DskipTests
```

Mystro is REST-only. Do not rely on removed CLI flags or server-side report-file output.
