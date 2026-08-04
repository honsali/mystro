# Mystro Specification

Foundational documents:

- `docs/PROJECT_VISION.md`
- `docs/READING_TASKS_SPEC.md`
- `docs/LOCAL_MODULE2_WORKFLOW.md` for local Module-2 macro/zoom workflow memory

This file defines the current technical architecture and output contract.

## 1. Project goal

Mystro is a self-contained Java traditional astrology calculation engine with a plain command-line entrypoint.

Current output families:

```text
Natal data → natal-description reading bundle JSON
Natal data × inquiry date → local/research life-arc Markdown dumps under output/<alias>/
```

There is no Spring Boot server, no REST API, no web adapter, and no application resources directory.

## 2. Current pipeline

```text
native-list alias
→ Local birth data loading / normalization
→ Natal description reading calculation
→ JSON written under output/<alias>/
```

Runtime entrypoint:

```text
app.Main
```

The current command-line flow is:

```text
app.Main
  ↓
app.input.NativeListInputLoader loads native-list.json alias
  ↓
app.input.ReadingInput
  ↓
app.input.ReadingInputMapper validates and resolves Subject plus optional inquiryDate
  ↓
app.ReadingBundleCalculator
  ↓
NatalDescriptionReadingCalculator uses Valens as the core doctrine/specialist
  ↓
CalculationContext(subject, Valens calculation choices)
  ↓
Valens specialist calculators pour data into NatalChart
  ↓
ReadingBundleReport
  ↓
app.io.MystroObjectMapper writes JSON
```

The broader local/research life-arc macro dump harness remains a gated JUnit utility, not the main application mode:

```bash
mvn -Dtest=LocalReadingDumpRunner -Dlocal.reading=true -Dlocal.reading.alias=demo test
```

The bounded high-zoom timing pack is a separate gated local utility:

```bash
mvn -Dtest=LocalZoomDumpRunner -Dlocal.zoom=true -Dlocal.reading.alias=demo -Dlocal.zoom.date=15/06/2024 test
```

## 3. Input

`app.Main` accepts one local `native-list.json` alias and no file-name arguments:

```text
java -jar target/mystro-<version>.jar <native-list-alias>
```

For an alias such as `demo`, Mystro loads `native-list.json`, matches the entry `name` case-insensitively, and normalizes `birth_date` (`dd/MM/yyyy`), `birth_time` (`HH:mm` or `HH:mm:ss`), `utc_offset`, `latitude`, and `longitude` into the standard input DTO. `native-list.json` is local ignored input data, not an application resource layer.

Current native-list entry shape, using an explicitly synthetic J2000/Greenwich example:

```json
{
  "name": "demo",
  "birth_date": "01/01/2000",
  "birth_time": "12:00",
  "latitude": 51.4769,
  "longitude": 0.0,
  "utc_offset": "+00:00",
  "inquiry_date": "15/01/2025"
}
```

`inquiry_date` is optional and must be on or after `birth_date` when supplied. It is accepted so the same alias can drive local/research Markdown life-arc dumps; it does not add a second JSON reading item.

Valens is the intentional core doctrine/specialist for the current `NATAL_DESCRIPTION` reading. Users do not select a doctrine in the current input shape.

## 4. Output contract

The command-line output is always written to `output/<alias>/reading_output.json` as one direct reading-bundle JSON object:

```json
{
  "engineVersion": "<version>",
  "subject": {},
  "reading": [
    {
      "id": "NATAL_DESCRIPTION",
      "coreDoctrine": "VALENS",
      "coreConventions": {},
      "natalChart": {}
    }
  ]
}
```

Module-2/life-arc timing data is not emitted in command-line JSON; it belongs in local Markdown outputs.

There is no top-level `basicChart`, `descriptive`, `doctrine`, `natalChart`, `calculationSetting`, or REST warning field. Natal chart data lives inside the `reading[]` item.

JSON output rounds doubles to two decimals at serialization through `app.io.RoundedDoubleSerializer`; calculators keep full internal double precision.

## 5. Core Java contracts

Current reading-specialist contract:

```java
public final class NatalDescriptionReadingCalculator {
    public NatalDescriptionReadingReport calculate(Subject subject);
}

public final class ValensNatalDescriptionSpecialist {
    public CoreDoctrineInfo getCoreDoctrineInfo();
    public NatalChart calculate(Subject subject, BasicCalculator basicCalculator);
    public void enrich(CalculationContext ctx, NatalChart chart);
}

```

`CoreDoctrineInfo` exposes core-specialist calculation choices:

```text
- id
- name
- house system
- terms
- triplicity
```

`CalculationContext` is the per-run internal context. It owns the subject, doctrine-derived calculation choices, the Mystro adapter over `swisseph-java-ffm`, Julian day, house cusps, `ascmc`, ARMC, and shared helpers. Astronomical calls execute in Swiss Ephemeris C 2.10.03 through Java 25 FFM; the removed `app.swisseph` Java 2.01 port is not used.

## 6. NatalChart

`NatalChart` is the shared chart object embedded in the `NATAL_DESCRIPTION` reading.

Basic chart calculation pours mechanical facts such as:

```text
- resolvedUtcInstant
- julianDayUt / julianDayTt / deltaTSeconds
- armc
- localApparentSiderealTimeHours
- trueObliquity / meanObliquity
- nutationLongitude / nutationObliquity
- points
- houses
- pairwiseRelations
- moonPhase
- sect
```

Valens natal-description calculators enrich the same chart with doctrine-owned data such as:

```text
- syzygy
- lots
- houseTopicRulers
- lotAssessments
- derivedHouseFrames
- topicAssessments
- point dignity/debility assessments
- point solar conditions
- beneficMaleficAssessment
- mercuryConfiguration
- moonConfiguration
- doryphories
- triplicityLifePhases
- ptolemaicHylegAlcocoden
- dodecatemoria
- fixedStars
```

`natalChart.lots` is a doctrine-owned JSON array, not a map. Current natal-description lot coverage is Valens-led: Fortune, Spirit, Eros, Necessity, Basis, Courage, Victory, Nemesis, Wedding, Children, Father, Mother, and Siblings.

`natalChart.pairwiseRelations` is a filtered retained-relation list, not a dense all-pairs matrix. A pair is emitted only when at least one point is a traditional planet and it carries a retained relation under the current inclusion predicate.

## 7. Local/research life-arc Markdown

Module-2/life-arc timing is no longer part of the command-line reading-bundle JSON. It is generated only by local/research calculators and Markdown renderers.

The local macro dump harness writes Markdown reports under `output/<alias>/` for broader timing work such as annual/monthly profections, compact all-lots L1 Zodiacal Releasing, per-lot Zodiacal Releasing, firdaria, decennials, solar returns, distributions, primary-direction variants, lunar timing, and the output index. Transit calculations are kept out of the current 0-100 macro dump.

The local zoom harness accepts an explicit date (`dd/MM/yyyy`), writes under `output/<alias>/<yyyyMMdd>/`, and emits active-period context, active Zodiacal Releasing chains/boundaries, daily profections, planetary hours, lunar 30-day timing, solar-return focus, directions, and exact transit hits inside the requested ±15-day window.

## 8. Intentional calculation conventions

- Planet positions are geocentric apparent Swiss Ephemeris positions.
- The astronomical engine is Swiss Ephemeris C 2.10.03, called through `org.swisseph:swisseph-java-ffm:0.2.0` and Java 25 FFM.
- A platform-native Swiss Ephemeris shared library is required under `dll/` (with `libs/` retained as a compatibility search path) or through `swisseph.library.path` / `SWISSEPH_LIBRARY`.
- Swiss Ephemeris file-backed data under `ephe/` is required; do not delete it.
- Tropical zodiac positions are used; sidereal modes are out of scope.
- Lunar nodes use the Swiss Ephemeris mean node.
- Lunar parallax is not corrected by converting the Moon to topocentric position.
- Placidus failures fail fast; there is no silent fallback house system.
- A Moon exactly 180° ahead of the Sun is treated as waxing by convention.
- Basic chart sect is altitude-based: Sun altitude `>= 0.0` is diurnal.

## 9. Package layout

Current app code lives under:

```text
src/main/java/app/
  AppVersion.java
  Main.java
  ReadingBundleCalculator.java
  input/
  io/
  chart/
  planetaryhours/
  reading/
  ephemeris/
```

Important current classes:

```text
app.AppVersion
app.Main
app.ReadingBundleCalculator
app.input.ReadingInput
app.input.ReadingInputMapper
app.io.MystroObjectMapper
app.io.RoundedDoubleSerializer
app.chart.BasicCalculator
app.chart.CalculationContext
app.chart.model.NatalChart
app.chart.model.Subject
app.planetaryhours.PlanetaryHoursCalculator
app.reading.ReadingBundleReport
app.reading.description.NatalDescriptionReadingCalculator
app.reading.description.valens.ValensNatalDescriptionSpecialist
```

There is no `app.web` package.

## 10. Development commands

After Java changes:

```bash
mvn compile
```

After behavior, serialization, or packaging-relevant changes:

```bash
mvn test
mvn package -DskipTests
```

To run the command-line natal JSON locally:

```bash
mvn -q compile exec:java -Dexec.args="demo"
```

To run the local life-arc macro dump:

```bash
./run demo
```

To run a local high-zoom timing pack centered on a date with a ±15-day window:

```bash
./run demo 15/06/2024
```

Zoom output is written under `output/<alias>/<yyyyMMdd>/`, for example `output/demo/20240615/`.
