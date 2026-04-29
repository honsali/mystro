---
description: Validate and prepare a version commit
argument-hint: "[commit message]"
---

Before committing:

1. Check repository status.
2. Read `NEW_ARCHITECTURE_SPEC.md` if docs or architecture changed.
3. Ensure markdown files do not reintroduce stale former-architecture guidance.
4. Run:

```bash
mvn compile
```

For a representative current runtime check, run:

```bash
mvn exec:java -Dexec.args="--subjects ilia --doctrines valens"
```

Do not commit generated build output or accidental local files.
