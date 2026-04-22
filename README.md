# mystro

`mystro` now contains a Java port of the traditional astrology report generator, using the bundled pure-Java Swiss Ephemeris port.

The project is a standalone Java astrology application centered on Swiss Ephemeris and Astro-Seek-oriented validation.

## Goals

- provide one project-specific astrology application
- keep all local customizations inside `mystro`
- validate output against Astro-Seek where relevant
- keep the codebase self-contained

## Project layout

```text
mystro/
  input/
    astroseek/
    native-list.json
  output/
  validation/
  pom.xml
  ephe/
  src/main/java/app/
```

## Quick start

Build the Java project:

```bash
mvn compile
```

Run the validation workflow:

```bash
mvn exec:java -Dexec.args="--names ilia marwa reda"
```

If no `--names` are passed, the app processes all entries in `input/native-list.json`.
The runtime writes Mystro JSON to `output/mystro/json/`, Astro-Seek JSON to `output/astroseek/json/`, and one comparison summary JSON to `output/report.json`.
Runtime JSON writing currently works; `JsonFileSupport` registers Jackson Java Time support for `OffsetDateTime`.

## Java project layout

```text
mystro/
  src/main/java/app/
    App.java
    astroseek/
    common/
    mystro/
    swisseph/core/
    swisseph/wrapper/
```

## Current Java scope

The Java app currently focuses on JSON generation and comparison:
- native JSON loading from `input/native-list.json`
- Swiss Ephemeris chart calculation
- whole-sign houses
- planet positions and house positions
- main aspects and other aspects
- Fortune and the 7 Hermetic Lots, with formula strings emitted in textbook arrow notation (`A → B` = forward arc `(B - A)`)
- planetary hour at birth
- Lord of the Orb (`mod84` and `mod12`) in normalized JSON output
- derived chart sections: `dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`
- normalized JSON generation for Mystro and Astro-Seek
- JSON comparison with tolerance-based validation

Mystro JSON assembly is orchestrated by `app.mystro.MystroService` through `NativeReportBuilder` and computation processors.
Astro-Seek parsing is orchestrated directly by `app.astroseek.AstroSeekService` through section parsers.
Astro-Seek birth data is parsed from the saved HTML details panel rather than copied from `native-list.json`.
Shared constants live in `app.common.Config`, and shared runtime error collection lives in `app.common.Logger`.

## Notes

- The authoritative app version is the top-level `<version>` in `pom.xml`.
- Validation reports should include that exact project version and be written to `validation/validation-report-v<version>.md`.
- The `/version` workflow releases the current `pom.xml` version, then bumps the next iteration by incrementing the middle number and resetting the last number to zero (for example `0.2.0 -> 0.3.0`).
- Ephemeris files are expected in `ephe/` under the project root.
- The runtime no longer depends on a native JNI `swisseph` library.
- The app is now English-only and JSON-only; markdown output, i18n resources, and `src/test` were removed.
- Validation is compile-first (`mvn compile`) and runtime-second (`mvn exec:java ...`).
- The validation agent writes versioned reports under `validation/`, for example `validation/validation-report-v0.2.0.md`.
- Chiron is now calculated directly from Swiss Ephemeris using the ephemeris files in `ephe/`; the Astro-Seek HTML fallback is no longer part of active runtime validation.
- Lord of the Orb is now emitted in both Mystro and Astro-Seek normalized JSON so it can be compared on overlapping visible years.

## Next suggested steps

1. validate the expanded normalized sections against more saved Astro-Seek charts
2. refine any astrology rules still inferred from Astro-Seek comparisons
3. keep project memory files in sync with code changes
