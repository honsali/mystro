# Local Life-Arc Workflow

> Documentation axis: how to generate and navigate the local macro and zoom report packs.

Installation, native-library setup, and `native-list.json` creation are documented in the project
[README](../README.md).

## Two report levels

The local workflow is gated test tooling that produces macro and zoom research reports.

```text
native-list.json alias
  -> macro pack under output/<alias>/
  -> optional zoom pack under output/<alias>/<yyyyMMdd>/
```

Generated output remains ignored by Git.

## Macro pack

Run the broad life-arc calculation:

```bash
./run demo
```

Windows:

```bat
run.bat demo
```

The macro pack covers long-range structural timing and deliberately excludes transit scanning. Its
entry point is `output/<alias>/index.md`.

Main output families:

```text
reading_output.json
index.md
annual_profections.md
monthly_profections.md
zodiacal_releasing_l1_all_lots.md
zodiacal_releasing/
firdaria.md
decennials.md
solar_returns.md
solar_return_natal_comparison.md
distributions_through_bounds.md
distributions_extended.md
primary_directions.md
primary_directions_mundane.md
lunar_timing.md
lunar_timing_eclipses.md
lunar_timing_full.md
life_arc_synthesis.md
life_arc_ai_brief.md
topics/
```

Use `index.md` instead of assuming that every optional report is present.

## Zoom pack

Run a bounded high-resolution calculation around one date:

```bash
./run demo 15/06/2024
```

Windows:

```bat
run.bat demo 15/06/2024
```

The date uses `dd/MM/yyyy`. Output is written below a `yyyyMMdd` directory, for example:

```text
output/demo/20240615/
```

The default zoom window is plus or minus 15 days. Its files are:

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

`zoom_overview.md` is the entry point. The zoom pack contains calculations and evidence references.

## Division of responsibility

Macro reports answer “which long periods and structures are active?”. Zoom reports answer “which
exact contacts occur near this date?”. Transit calculations belong exclusively to zoom output.

## Direct Maven invocation

The scripts dispatch to these gated runners:

```bash
mvn -Dtest=LocalReadingDumpRunner \
  -Dlocal.reading=true \
  -Dlocal.reading.alias=demo test

mvn -Dtest=LocalZoomDumpRunner \
  -Dlocal.zoom=true \
  -Dlocal.reading.alias=demo \
  -Dlocal.zoom.date=15/06/2024 test
```

Native access must be enabled through `MAVEN_OPTS`; the `run` scripts do this automatically.

## Output handling

- Keep `output/` and `native-list.json` local and Git-ignored.
- Keep calculator precision intact; Markdown rounding is presentation only.
- A missing optional section indicates that its optional input or calculation context was absent.
- Use the macro index or zoom overview to choose the files supplied to an external assistant.
