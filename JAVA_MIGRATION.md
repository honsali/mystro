# Java project status

## What was added
- `pom.xml`
- Java sources under `src/main/java/`
- Java resources under `src/main/resources/`
- a first Java port of the traditional report workflow

## Current status
- `mvn compile` works
- `mvn exec:java ...` works again; `JsonFileSupport` now registers Jackson Java Time support for `OffsetDateTime`

## Runtime status
The app now uses the bundled **pure Java Swiss Ephemeris port** under `src/main/java/app/swisseph/core/` instead of the old JNI-based runtime path.

## Practical next step
Validate changes with compilation first, then validate runtime flow with:

```bash
cd astro
mvn compile
mvn exec:java -Dexec.args="--names ilia marwa reda"
```


## Notes
- The current Java project workflow covers:
  - JSON input
  - JSON output
  - planet positions
  - houses
  - main aspects and other aspects
  - Hermetic lots
  - planetary hours
  - derived chart sections: `dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`
  - Lord of the Orb support logic
- Markdown output, i18n resources, and the old `src/test` suite were removed.
- Mystro report assembly now lives in `app.mystro.MystroService` via `NativeReportBuilder` plus computation processors.
- Astro-Seek parsing orchestration now lives in `app.astroseek.AstroSeekService`.
- Current Mystro runtime includes a pragmatic Chiron fallback from saved Astro-Seek HTML when direct ephemeris output is unavailable.
- Shared constants and common runtime error collection now live in `app.common.Config` and `app.common.Logger`.
- Validation should focus on Astro-Seek behavior and known target charts.
