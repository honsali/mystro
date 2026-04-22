---
name: astro-run-after-changes
description: Use when modifying the astro Java project so every code change is followed by recompiling and, when needed, rerunning the main JSON workflow.
compatibility: Requires the astro Java Maven project.
---

# Astro Run After Changes

## Rule
After every code change in `astro`, recompile the project. When the change affects runtime behavior, also rerun the main JSON workflow on representative saved cases.

## Required command
```bash
mvn compile
```

## Recommended workflow
1. Make the code change.
2. Run the compile command above.
3. If the change affects runtime behavior, validate with:
   - `run.bat`
   - or equivalently `mvn exec:java -Dexec.args="--names ilia reda marwa"`
4. Confirm the main flow still works: it reads `native-list.json`, writes Mystro JSON to `output/mystro/json/`, writes Astro-Seek JSON to `output/astroseek/json/`, and writes one comparison summary to `output/report.json`.
5. Only consider the change complete after compilation succeeds and runtime validation succeeds when applicable.

## Notes
- This rule applies even for small processor, parser, or service changes.
- This also applies to Astro-Seek micro-parser changes.
- Prefer rerunning the multi-case runtime flow over assuming compile success is enough when logic changed.
