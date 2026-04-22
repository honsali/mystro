---
name: astro-chart-workflow
description: Use for working on the astro project chart generator, especially when you need to run or debug reports and validate output against Astro-Seek.
compatibility: Requires the astro Java Maven project.
---

# Astro Chart Workflow

## First checks
1. Work from the project root.
2. Confirm the Maven project builds.
3. Confirm ephemeris files are available in `ephe/`.

## Known good commands

### Build
```bash
mvn compile
```

### Run the main workflow
```bash
mvn exec:java -Dexec.args="--names ilia marwa reda"
```

Runtime JSON writing currently works; `JsonFileSupport` registers Jackson Java Time support for `OffsetDateTime`.

## Project expectations
- `astro` is a standalone Java application.
- Chart calculation uses the bundled Java Swiss Ephemeris implementation.
- Astrology behavior should be checked against Astro-Seek where relevant.
- The app is JSON-only and English-only.
- Astro-Seek HTML parsing is part of main code.
- Astro-Seek parsing should evolve via small section parsers orchestrated by `AstroSeekService`.
- Astro-Seek birth data must be parsed from the HTML details panel, not copied from `native-list.json`.
- Mystro calculation is split into focused computation processors orchestrated by `MystroService`.
- Current normalized output sections include Lord of the Orb, planets, houses, aspects, Hermetic lots, and derived charts (`dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`).
- Shared constants and shared runtime error reporting now live in `app.common.Config` and `app.common.Logger`.

## Input format
Expected case structure in `native-list.json`:
```json
{
  "name": "ilia",
  "birth_date": "14/07/1975",
  "birth_time": "22:55",
  "latitude": 50.60600755996812,
  "longitude": 3.0333769552426793,
  "utc_offset": "+01:00",
  "house_system": "Whole Sign",
  "zodiac": "Tropical",
  "terms": "Egyptian"
}
```

## Runtime output conventions
- Mystro runtime output is written to `output/mystro/json/`
- Astro-Seek runtime output is written to `output/astroseek/json/`
- runtime comparison summary is written to `output/report.json`

## If chart execution fails
- check Maven compilation first
- check project working directory
- check ephemeris path resolution
- distinguish runtime setup issues from astrology-logic issues before changing code
- if the issue is on the Mystro side, check whether it belongs in a computation processor, chart support logic, or Astro-Seek-alignment fallback logic before changing shared runtime assumptions

## Related references
- `../AGENTS.md`
- `../SESSION_MEMORY.md`
- `../ASTRO_RULES.md`
