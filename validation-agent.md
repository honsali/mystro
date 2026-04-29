# Reference Validation Agent

This file has been reduced for the clean restart.

The current architecture separates:

```text
Input validation / normalization
Reference validation
```

Reference validation is the post-output stage that compares generated reports against selected references.

It must not drive the calculation architecture.

For current architecture and output families, read:

- `NEW_ARCHITECTURE_SPEC.md`
