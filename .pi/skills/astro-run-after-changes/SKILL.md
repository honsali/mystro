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

When the change affects runtime behavior, run a representative current command:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Doctrines are explicit. Do not rely on hidden default doctrine selection.
