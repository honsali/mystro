# Local Module 2 Workflow Notes

This file is durable handoff memory for Mystro's local/research life-arc timing workflow. It records the current macro/zoom contract, implementation locations, calculation boundaries, and next safe steps.

## Current posture

Mystro is a CLI/local calculation engine. The public command-line JSON remains the Valens-led `NATAL_DESCRIPTION` reading bundle only. Module-2 / life-arc timing is local/research Markdown, not JSON and not REST.

Current local runtime shape:

```text
native-list.json alias
→ natal chart / Valens natal description calculation
→ macro Markdown under output/<alias>/
→ optional date zoom Markdown under output/<alias>/<yyyyMMdd>/
```

Local/generated files stay ignored and uncommitted:

```text
native-list.json
output/
target/
.settings/
.vscode/
```

## Commands

Command-line natal JSON:

```bash
mvn -q compile exec:java -Dexec.args="demo"
```

Local Module-2 macro pack:

```bash
./run demo
run.bat demo
```

Local Module-2 zoom pack centered on one date:

```bash
./run demo 15/06/2024
run.bat demo 15/06/2024
```

The date argument is parsed as `dd/MM/yyyy`. Zoom output directory names use `yyyyMMdd`.

## Output directories

Macro output:

```text
output/<alias>/
```

Zoom output:

```text
output/<alias>/<yyyyMMdd>/
```

Example:

```text
output/demo/20240615/
```

## Run-script dispatch

The shell and Windows run scripts intentionally dispatch by arity:

```text
./run <alias>                 → LocalReadingDumpRunner macro pack
./run <alias> <dd/MM/yyyy>    → LocalZoomDumpRunner zoom pack
```

No file-path input mode should be reintroduced for this workflow. The alias comes from local ignored `native-list.json`.

## Macro pack contract

The macro pack is the broad 0-100 / long-range Module-2 research layer. It keeps structural timing methods and excludes transit calls.

Current macro families include:

- `reading_output.json` from the CLI natal-description bundle
- `index.md`
- `annual_profections.md`
- `monthly_profections.md`
- `zodiacal_releasing_l1_all_lots.md`
- detailed per-lot Zodiacal Releasing files under `zodiacal_releasing/zr_*.md`
- `firdaria.md`
- `decennials.md`
- `solar_returns.md`
- `solar_return_natal_comparison.md`
- `distributions_through_bounds.md`
- `distributions_extended.md`
- `primary_directions.md`
- `primary_directions_mundane.md`
- `lunar_timing.md`
- split lunar timing/eclipses files for large output

Important macro decisions:

- Transits are excluded from macro output.
- The compact all-lots ZR L1 file is macro-level.
- Detailed `zodiacal_releasing/` files remain next-layer/deep-dive source material.
- Old explanatory sentences about `★` markers were removed from renderers and current generated Markdown.
- `annual_profections.md` is always written; null inquiry dates must not render as `Inquiry date: null`.

## Zoom pack contract

The zoom pack is a bounded high-resolution calculation layer below the macro pack. It is centered on a requested local date/time and uses a ±15-day window unless explicitly changed later.

Current zoom files:

```text
zoom_overview.md
active_periods.md
zodiacal_releasing_active.md
daily_profections.md
planetary_hours.md
lunar_30d.md
solar_return_focus.md
directions_30d.md
transits_30d.md
```

Zoom is calculation-only. Do not add ranking, narrative synthesis, prediction prose, or interpretive summaries unless the user explicitly asks for an analysis layer.

## Zoom file purposes

- `zoom_overview.md`: file index for the requested focus date/window.
- `active_periods.md`: active annual/monthly profections, firdaria, decennials, distributions, solar-return context, and lunar timing focus rows.
- `zodiacal_releasing_active.md`: active Zodiacal Releasing chains and boundaries in the window.
- `daily_profections.md`: daily profection rows, advancing one sign per local day from the active monthly profection at the natal birth time.
- `planetary_hours.md`: full planetary day from sunrise to next sunrise for the focus date/location.
- `lunar_30d.md`: exact Moon sign ingresses, lunations/eclipses in the timing table, and exact Moon hits to daily-activated natal points/lots.
- `solar_return_focus.md`: active solar-return chart, natal overlays, and conjunctions.
- `directions_30d.md`: active distribution bound periods plus exact distribution, normalized zodiacal primary-direction, and mundane/semi-arc prototype contacts in the ±15-day window.
- `transits_30d.md`: exact transit hits inside the bounded window, using activated targets/windows.

## Key implementation locations

Main/runtime:

```text
src/main/java/app/Main.java
src/main/java/app/input/NativeListInputLoader.java
src/main/java/app/input/ReadingInput.java
src/main/java/app/input/ReadingInputMapper.java
```

Local macro/zoom harnesses:

```text
src/test/java/app/local/LocalReadingDumpRunner.java
src/test/java/app/local/LocalZoomDumpRunner.java
```

Recent zoom renderers:

```text
src/test/java/app/local/ZoomOverviewMarkdownRenderer.java
src/test/java/app/local/ZoomActivePeriodsMarkdownRenderer.java
src/test/java/app/local/ZodiacalReleasingActiveMarkdownRenderer.java
src/test/java/app/local/DailyProfectionMarkdownRenderer.java
src/test/java/app/local/PlanetaryHoursMarkdownRenderer.java
src/test/java/app/local/LunarZoomMarkdownRenderer.java
src/test/java/app/local/SolarReturnFocusMarkdownRenderer.java
src/test/java/app/local/DirectionsZoomMarkdownRenderer.java
src/test/java/app/local/ZoomTransitMarkdownRenderer.java
```

Recent calculation/model additions:

```text
src/main/java/app/reading/lifearc/dorothean/calculator/DorotheanDailyProfectionCalculator.java
src/main/java/app/reading/lifearc/model/DailyProfectionTable.java
src/main/java/app/reading/lifearc/model/DailyProfectionTableRow.java
src/main/java/app/reading/lifearc/model/DailyProfectionReferenceEntry.java
src/main/java/app/reading/lifearc/model/DailyProfectionActivatedPoint.java
src/main/java/app/reading/lifearc/model/DailyProfectionActivatedLot.java
src/main/java/app/reading/lifearc/lunar/LunarZoomCalculator.java
src/main/java/app/reading/lifearc/lunar/LunarZoomTable.java
src/main/java/app/reading/lifearc/lunar/LunarSignIngressEntry.java
src/main/java/app/planetaryhours/PlanetaryHoursCalculator.java
```

Direction-related source models/calculators used by `directions_30d.md`:

```text
src/main/java/app/reading/lifearc/distribution/DistributionThroughBoundsTable.java
src/main/java/app/reading/lifearc/distribution/DistributionThroughBoundsPeriod.java
src/main/java/app/reading/lifearc/distribution/DistributionThroughBoundsContact.java
src/main/java/app/reading/lifearc/primarydirection/PrimaryDirectionCalculator.java
src/main/java/app/reading/lifearc/primarydirection/PrimaryDirectionTable.java
src/main/java/app/reading/lifearc/primarydirection/PrimaryDirectionEvent.java
src/main/java/app/reading/lifearc/primarydirection/MundanePrimaryDirectionCalculator.java
src/main/java/app/reading/lifearc/primarydirection/MundanePrimaryDirectionTable.java
src/main/java/app/reading/lifearc/primarydirection/MundanePrimaryDirectionEvent.java
```

## Calculation conventions to preserve

- Java 25.
- Swiss Ephemeris file-backed data under `ephe/` is required.
- Tropical zodiac; no sidereal mode.
- Geocentric apparent planet positions.
- Mean lunar node.
- No topocentric lunar parallax correction.
- Placidus failures fail fast.
- Basic sect is altitude-based with Sun altitude `>= 0.0` as diurnal.
- Calculators keep full precision; renderers round only for readable Markdown.
- Planetary hours are calculation code, not a web endpoint.
- Monthly/exact transit calculators remain in source, but macro flow must not call them.

## Verification state

Repository verification commands:

```bash
mvn test
mvn package -DskipTests
```

Local dump commands additionally require a private, ignored `native-list.json`; they are intentionally not part of the default test run.

## Current version/workflow state

The latest pushed stable version is `1.32.0` for the refreshed local output-index guidance. Current development files are bumped to `1.33.0` for the next cycle.

## Safe next improvements

Prefer committing the current post-1.32.0 version bump before adding more techniques, unless the user asks to continue local experimentation.

Potential calculation-only follow-ups:

1. Add a small test around `LocalZoomDumpRunner` output file references.
2. Add nearest-before / nearest-after rows to `directions_30d.md` when no exact direction contact occurs inside ±15 days.
3. Add another bounded zoom technique only if the user names a specific calculation; do not broaden zoom into interpretation.

## Do not regress

- Do not reintroduce REST, Spring Boot, `app.web`, or `src/main/resources/application.yml`.
- Do not make users pass input/output file paths for the current local workflow.
- Do not emit Module-2 timing in the command-line JSON bundle.
- Do not commit `native-list.json`, `output/`, `target/`, IDE settings, or generated local artifacts.
- Do not turn zoom files into narrative analysis without explicit user instruction.
