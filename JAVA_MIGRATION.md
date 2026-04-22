# Java project status

## What was added
- `pom.xml`
- Java sources under `src/main/java/`
- Java resources under `src/main/resources/`
- a first Java port of the traditional report workflow

## Current status
- `mvn compile` works
- `run.bat` / `mvn exec:java ...` work again; `JsonFileSupport` now registers Jackson Java Time support for `OffsetDateTime`

## Runtime status
The app now uses the bundled **pure Java Swiss Ephemeris port** under `src/main/java/app/swisseph/core/` instead of the old JNI-based runtime path.

## Practical next step
Validate changes with compilation first, then validate runtime flow with:

```bash
mvn compile
run.bat
# or equivalently:
mvn exec:java -Dexec.args="--names ilia reda marwa"
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
  - annual profections
  - derived chart sections: `dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`
  - Lord of the Orb support logic
- Markdown output and i18n resources were removed; the old tracked `test/` artifact tree is no longer part of the repository.
- Mystro report assembly now lives in `app.mystro.MystroService` via `NativeReportBuilder` plus computation processors.
- Astro-Seek parsing orchestration now lives in `app.astroseek.AstroSeekService`.
- Current Mystro runtime calculates Chiron directly from Swiss Ephemeris via the project `ephe/` files; the old Astro-Seek HTML fallback is no longer part of the active validation path.
- Shared constants and common runtime error collection now live in `app.common.Config` and `app.common.Logger`.
- Validation should focus on Astro-Seek behavior and known target charts.
- The validation agent now writes versioned reports under `validation/validation-report-v<version>.md`.
