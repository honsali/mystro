# Session Memory

Last updated: 2026-04-21
Source: refreshed from current codebase, compile/run validation, and existing project notes

## Durable facts

### Environment
- The project is implemented in **Java**.
- `mvn compile` works.
- `JsonFileSupport` now registers `JavaTimeModule`, so `OffsetDateTime` JSON serialization works.
- Runtime uses the bundled **pure Java Swiss Ephemeris port** in `src/main/java/app/swisseph/core/`.
- There is currently **no active `src/test` suite**; validation is compile plus runtime execution.

### Project scaffold decisions
- `mystro` is a **Java Maven project**.
- The authoritative project version is the top-level `<version>` in `pom.xml`; validation reports should read and print that exact value and write to `validation/validation-report-v<version>.md`.
- The `/version` workflow now treats the current `pom.xml` version as the release version, then bumps the next iteration by incrementing the middle number and resetting the last number to zero (for example `0.2.0 -> 0.3.0`).
- Main entry point: `src/main/java/app/App.java`
- Main orchestration classes are now `app.mystro.MystroService`, `app.astroseek.AstroSeekService`, and `app.validator.ValidatorService`.
- Shared normalized runtime model used by both Mystro and Astro-Seek branches is centered on `NativeReport`, `NativeBirth`, `NativePlanetaryHour`, `NativeLordOfOrb`, `NativeSyzygy`, `NativeHermeticLot`, `ChartPoint`, and `NativeAspect`.
- `NativeChart` still exists as the internal Mystro calculation context, and `NativeReportBuilder.build()` now emits normalized chart-derived sections including `lordOfOrb`, `planets`, `houses`, `mainAspects`, `otherAspects`, `dodecatemoria`, `novenaria`, `antiscia`, and `contraAntiscia`.
- Shared constants live in `src/main/java/app/common/Config.java`.
- Shared runtime error collection lives in `src/main/java/app/common/Logger.java`.
- Mystro now builds directly from `app.mystro.MystroService` through processor modules using `app.common.NativeReportBuilder` and `app.swisseph.core.SwissEph`.
- Main comparison target for astrology behavior: Astro-Seek.
- The app is now **JSON-only** and **English-only**; markdown writers, i18n resources, and language selection were removed.
- `app.mystro.MystroService` assembles Mystro JSON through `NativeReportBuilder` plus processor modules.
- Current Mystro processors are `ChartProcessor`, `PlanetaryHourProcessor`, `LordOfOrbProcessor`, `SyzygyProcessor`, `HermeticLotsProcessor`, `PlanetPositionsProcessor`, `HousesProcessor`, `AspectsProcessor`, and `DerivedChartsProcessor`.
- Astro-Seek parsing still uses the `app.astroseek.parser.AstroSeekParser` interface with parser implementations under `app.astroseek.parser.impl`.
- Current Astro-Seek parser modules are `AstroSeekBirthParser`, `AstroSeekPlanetPositionsParser`, `AstroSeekHousesParser`, `AstroSeekMainAspectsParser`, `AstroSeekOtherAspectsParser`, `AstroSeekDerivedChartsParser`, `AstroSeekPlanetaryHourParser`, `AstroSeekLordOfOrbParser`, `AstroSeekSyzygyParser`, and `AstroSeekHermeticLotParser`.
- `HermeticLotsProcessor` now uses explicit day/night formulas for all seven lots rather than only swapping Fortune and Spirit.
- `PlanetPositionsProcessor` now resolves the Syzygy point by phase and sect instead of always injecting the Sun's longitude.
- `ChartProcessor` attempts direct Swiss Ephemeris Chiron calculation first and logs `CHIRON_HTML_FALLBACK` when it has to reuse the saved Astro-Seek HTML fallback.
- Package layout is organized under `app` with `app.astroseek`, `app.mystro`, `app.common`, `app.swisseph.core`, and `app.swisseph.wrapper`.
- Astro-Seek parsing lives in main code under `src/main/java/app/astroseek/`.
- Astro-Seek parsing uses micro-parsers orchestrated by `app.astroseek.AstroSeekService`.
- Current Astro-Seek parser modules cover birth data, planet positions, houses, main aspects, other aspects, planetary hour, Lord of the Orb, syzygy, Hermetic lots, and derived charts (`dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`).

### CLI and input facts
- The main CLI supports `--names name1 name2 ...`; with no names it processes all entries.
- The old single-chart `--input/--output` CLI flow was removed.
- The native input list is `input/native-list.json`.
- Each native case includes `house_system`, `zodiac`, and `terms`.
- Main runtime loops over the selected natal cases, writes Mystro JSON to `output/mystro/json/<name>.json`, writes Astro-Seek JSON to `output/astroseek/json/<name>.json`, and writes one comparison summary to `output/report.json`.
- Missing native config entries, missing Astro-Seek HTML files, and missing generated JSON files do not stop the run; they are collected in `Logger` and reflected in comparison statuses.

### Current Java commands
```bash
mvn compile
mvn exec:java -Dexec.args="--names ilia marwa reda"
run.bat
```

Current status:
- `mvn compile` succeeds
- `mvn exec:java -Dexec.args="--names ilia marwa reda"` succeeds
- Current normalized outputs cover Lord of the Orb, planets, houses, aspects, Hermetic lots, and derived charts (`dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`).
- Current remaining comparison diffs are concentrated in Courage-vs-Astro-Seek, aspect policy/oracle differences, and station-sensitive retrograde flags.

### Lord of the Orb rule to keep
Use the Astro-Seek-style method:
1. Determine the **planetary hour ruler at birth** from the actual input birth data.
2. That ruler is the Lord of the Orb for the first year of life / age 0 row.
3. Advance year by year in **Chaldean order**:
   - Saturn → Jupiter → Mars → Sun → Venus → Mercury → Moon
4. Support both:
   - **Mod 84**: continuous 7-planet cycle
   - **Mod 12**: progression resets every 12 years
5. Astro-Seek HTML currently exposes only the visible 12-year window in the saved table, so validator comparison must match overlapping ages rather than assuming a full 0..83 dump.
6. Fortune row in Hermetic Lots must keep `L→R`; it was fixed to match Astro-Seek (`5th` for the reference chart).

### Practical warning
If a new session reports Java runtime failure, first distinguish ephemeris path resolution, working-directory issues, Astro-Seek parser issues, validator/output issues, and astrology logic issues before assuming a Swiss Ephemeris code issue.

### Workflow rule to keep
After every code change, rerun:
```bash
mvn compile
```
For runtime validation, use:
```bash
mvn exec:java -Dexec.args="--names ilia marwa reda"
```
or run with no args to process all entries from `input/native-list.json`.
Runtime validation is now possible again.
Do not treat a change as complete based only on code inspection.

### Astro-Seek parser direction to keep
- Keep Astro-Seek extraction modular: if a new normalized report section gets a Mystro calculation processor, prefer adding a matching Astro-Seek parser.
- Astro-Seek birth data must be parsed from the HTML details panel (`ascendent-vypocet-vlevo` / right-side values), not copied from `native-list.json`.
- Keep Astro-Seek parser work modular and validated through the main runtime flow.
- The parser goal is to keep normalized output easy to compare against Mystro JSON and summaries.
- Parser orchestration now belongs in `AstroSeekService`, not in a separate parser wrapper class.

## Recommended files to read first in future sessions
1. `AGENTS.md`
2. `SESSION_MEMORY.md`
3. `ASTRO_RULES.md`
4. `.pi/skills/astro-chart-workflow/SKILL.md`
5. `.pi/skills/astro-run-after-changes/SKILL.md`
