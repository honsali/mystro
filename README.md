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
astro/
  input/
    astroseek/
    native-list.json
  output/
  pom.xml
  ephe/
  src/main/java/app/
```

## Quick start

Build the Java project:

```bash
cd astro
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
astro/
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
- Fortune and the 7 Hermetic Lots
- planetary hour at birth
- derived chart sections: `dodecatemoria`, `novenaria`, `antiscia`, `contraAntiscia`
- Lord of the Orb support logic
- normalized JSON generation for Mystro and Astro-Seek
- JSON comparison with tolerance-based validation

Mystro JSON assembly is orchestrated by `app.mystro.MystroService` through `NativeReportBuilder` and computation processors.
Astro-Seek parsing is orchestrated directly by `app.astroseek.AstroSeekService` through section parsers.
Astro-Seek birth data is parsed from the saved HTML details panel rather than copied from `native-list.json`.
Shared constants live in `app.common.Config`, and shared runtime error collection lives in `app.common.Logger`.

## Notes

- Ephemeris files are expected in `ephe/` under the project root.
- The runtime no longer depends on a native JNI `swisseph` library.
- The app is now English-only and JSON-only; markdown output, i18n resources, and `src/test` were removed.
- Validation is compile-first (`mvn compile`) and runtime-second (`mvn exec:java ...`).
- Current representative validation target is `ilia`; the current comparison run for `ilia` matches.

## Next suggested steps

1. validate the expanded normalized sections against more saved Astro-Seek charts
2. refine any astrology rules still inferred from Astro-Seek comparisons
3. reduce or eliminate the current Chiron fallback by replacing it with direct ephemeris support if feasible
4. keep project memory files in sync with code changes
