# astro agent notes

## Project purpose
- `mystro` is a Java astrology app built around Swiss Ephemeris.
- The main comparison target for astrological behavior is Astro-Seek.
- Keep the project self-contained.

## Important runtime rule
- The project builds with Maven/Java.
- `mvn compile` works.
- Runtime JSON serialization currently works; `JsonFileSupport` registers Jackson `JavaTimeModule` for `OffsetDateTime`.
- Runtime uses the bundled pure Java Swiss Ephemeris port under `src/main/java/app/swisseph/core/`.
- Read `JAVA_MIGRATION.md` before changing Java runtime code.

## Common commands
```bash
mvn compile
mvn exec:java -Dexec.args="--names ilia reda marwa"
run.bat
```

## Input conventions
- birth date: `dd/MM/yyyy`
- birth time: `HH24:mm`
- latitude/longitude: decimal degrees
- use `utc_offset` explicitly
- native input list: `input/native-list.json`
- each case includes `house_system`, `zodiac`, and `terms`

## Current project facts
- The project is consolidated around the Java implementation.
- Astro-Seek behavior is the main comparison target for astrology rules.
- The app is now **JSON-only** and **English-only**; markdown report generation, i18n, and language selection were removed from the active runtime.
- Java classes are organized under `src/main/java/app/`.
- Main top-level packages are `app.astroseek`, `app.common`, `app.mystro`, `app.swisseph.core`, `app.swisseph.wrapper`, and `app.validator`.
- Main entry point is `src/main/java/app/App.java`.
- Main orchestration classes are `app.mystro.MystroService`, `app.astroseek.AstroSeekService`, and `app.validator.ValidatorService`.
- Astro-Seek HTML parsing lives under `src/main/java/app/astroseek/`.
- Astro-Seek parsing is modular and currently uses the `app.astroseek.parser.AstroSeekParser` interface plus parser implementations under `app.astroseek.parser.impl`, orchestrated by `app.astroseek.AstroSeekService`.
- Current Astro-Seek parser modules cover birth data, planet positions, houses, main aspects, other aspects, planetary hour, Lord of the Orb, annual profections, syzygy, Hermetic lots, and derived charts (`dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`).
- Annual profections are modeled separately from `lordOfOrb`; `lordOfOrb` remains as its own normalized section while `annualProfections` carries the broader table (`lordOfYear`, `lordOfOrb`, profected MC / Sun / Moon / Fortune signs).
- `app.mystro.MystroService` orchestrates JSON report assembly through computation processors plus `app.common.NativeReportBuilder`.
- Current Mystro processors are `ChartProcessor`, `PlanetaryHourProcessor`, `LordOfOrbProcessor`, `SyzygyProcessor`, `HermeticLotsProcessor`, `AnnualProfectionsProcessor`, `PlanetPositionsProcessor`, `HousesProcessor`, `AspectsProcessor`, and `DerivedChartsProcessor`.
- Shared normalized runtime model is centered on `NativeReport`, `NativeBirth`, `NativePlanetaryHour`, `NativeLordOfOrb`, `NativeAnnualProfections`, `NativeAnnualProfectionEntry`, `NativeSyzygy`, `NativeHermeticLot`, `ChartPoint`, and `NativeAspect`.
- `NativeReport` currently emits `lordOfOrb`, `annualProfections`, `planets`, `houses`, `mainAspects`, `otherAspects`, `dodecatemoria`, `novenaria`, `antiscia`, and `contraAntiscia` in addition to birth / planetary hour / syzygy / lots.
- `HermeticLotsProcessor` now applies explicit sect-conditional formulas for all seven lots, and its emitted `formula` strings use textbook arrow notation where `A → B` means the forward arc `(B - A)` added to the Ascendant.
- `PlanetPositionsProcessor` now resolves the Syzygy point by phase/sect instead of always forcing the Sun.
- `ChartProcessor` now calculates Chiron directly through Swiss Ephemeris using the bundled `ephe/` tables; treat Chiron as normal computed output during validation.
- Shared constants now live in `app.common.Config`.
- Shared runtime error collection now lives in the singleton `app.common.Logger`.
- There is currently no active `src/test` suite. The old tracked `test/` artifact tree was removed from Git and is now ignored locally; `target/` remains generated build output.

## When touching Lord of the Orb
- Base it on the birth **planetary hour ruler** from the input birth data.
- Advance by **Chaldean order**:
  - Saturn → Jupiter → Mars → Sun → Venus → Mercury → Moon
- Support both:
  - Mod 84: continuous 7-planet cycle
  - Mod 12: same progression but reset every 12 years

## Working style
- Prefer reading and editing files over guessing.
- After every code change, run `mvn compile` and verify the project still builds.
- If you change runtime logic or output, also run `run.bat` or the equivalent `mvn exec:java -Dexec.args="--names ilia reda marwa"` when practical, or at least a representative subset such as `--names ilia`.
- Current representative validation targets are `ilia`, `marwa`, and `reda`; use them to check parser/processor alignment section by section.
- The main runtime flow uses `app.App`, reads names from CLI `--names ...` or falls back to all entries in `input/native-list.json`, writes Mystro JSON to `output/mystro/json/`, writes Astro-Seek JSON to `output/astroseek/json/`, and writes one comparison summary to `output/report.json`.
- Both branches normalize into the same shared runtime model before JSON is written.
- Missing native config entries, missing Astro-Seek HTML, and missing generated JSON files are collected through `app.common.Logger` instead of ad-hoc missing-lists.
- Use `app.App` as the only executable entry point.
- The project no longer keeps an active `src/test` suite; validation is compile first and runtime second.
- If something seems wrong, inspect `AGENTS.md`, `SESSION_MEMORY.md`, `ASTRO_RULES.md`, and `JAVA_MIGRATION.md` first.
- If runtime fails, distinguish between Java compilation issues, ephemeris-path issues, Astro-Seek parser issues, validator/output issues, and astrology-logic issues before changing code.

## Important project memory files
- `AGENTS.md`
- `SESSION_MEMORY.md`
- `ASTRO_RULES.md`
- `.pi/skills/astro-chart-workflow/SKILL.md`
- `.pi/skills/astro-run-after-changes/SKILL.md`
