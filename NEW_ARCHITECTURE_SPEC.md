# Mystro New Architecture Specification

## 1. Project Goal

Mystro is a self-contained Java astrology calculation engine.

Its goal is to produce the most comprehensive possible set of traditional astrological data for each natal chart, with a high level of confidence, clarity, and traceability.

The core purpose is:

```text
Natal data × Doctrine modules → descriptive astrological calculation output
Natal data × Doctrine modules × inquiry periods → predictive astrological output
Natal data × Doctrine modules × comparison inputs → comparative astrological output
```

For every natal input and every requested doctrine, the app should produce one doctrine-specific descriptive astrological report.

When inquiry dates or periods are provided, the app should also produce doctrine-specific predictive reports.

When comparison inputs are provided, the app should also produce doctrine-specific comparative reports.

---

## 2. Core Architectural Principle

The application is divided into eight conceptual stages:

```text
1. Loading data
2. Input validation / normalization
3. Basic calculation
4. Descriptive calculation
5. Predictive calculation
6. Comparative calculation
7. Formatting / printing
8. Reference validation
```

Input validation / normalization happens before calculation.

Reference validation consumes generated output and operates as a separate stage.

The first seven stages form the main calculation and reporting pipeline.

---

## 3. High-Level Pipeline

```text
Input files
  ↓
InputLoader
  ↓
Raw input bundle
  ↓
InputValidator / InputNormalizer
  ↓
NatalInput list
Doctrine list
Inquiry inputs
Comparison inputs
  ↓
For each natal × doctrine:
    Build BasicCalculationContext from engine metadata and doctrine choices
      ↓
    BasicCalculator.calculate(natal, basicCalculationContext)
      ↓
    BasicChart
      ↓
    doctrine.describe(natal, basicChart)
      ↓
    DescriptiveResult
      ↓
    ReportFormatter / JsonWriter
      ↓
    output/descriptive/{subjectId}/{doctrineId}.json

For each natal × doctrine × supported predictive technique × inquiry period:
    technique.predict(natal, basicChart, descriptiveResult, inquiryPeriod)
      ↓
    PredictiveResult
      ↓
    ReportFormatter / JsonWriter
      ↓
    output/predictive/{subjectId}/{doctrineId}/{techniqueId}/{periodId}.json

For each natal × doctrine × supported comparative technique × comparison input:
    technique.compare(natal, basicChart, descriptiveResult, comparisonInput)
      ↓
    ComparativeResult
      ↓
    ReportFormatter / JsonWriter
      ↓
    output/comparative/{subjectId}/{doctrineId}/{techniqueId}/{comparisonId}.json
```

The doctrine contains its own hardcoded basic calculation choices.

The basic calculator remains shared and receives the doctrine's house system, zodiac, and terms explicitly.

Descriptive, predictive, and comparative calculations are separate outputs.

---

## 4. Main Inputs

The app should accept four main input categories.

### 4.1 Natal Data

A natal record contains birth information only.

Example fields:

```json
{
  "id": "ilia",
  "birthDate": "1990-01-01",
  "birthTime": "13:45:00",
  "utcOffset": "+01:00",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "place": "Paris, France"
}
```

Natal input describes the birth event.

Dates and times in input files use ISO 8601-compatible formats:

```text
birthDate: YYYY-MM-DD
birthTime: HH:mm:ss
utcOffset: ±HH:mm
```

Doctrine selection is handled separately from natal input.

### 4.2 Doctrine Requests

A doctrine request identifies which traditional doctrines should be applied.

Example:

```json
{
  "doctrines": [
    "dorotheus",
    "ptolemy",
    "valens",
    "bonatti"
  ]
}
```

A doctrine is not a loose settings file.

A doctrine is an explicit Java rule module.

### 4.3 Inquiry Periods

Inquiry periods are optional inputs used for predictive calculations.

They are separate from natal data.

Inquiry input uses explicit shapes instead of nullable catch-all fields.

Suggested Java model:

```java
public sealed interface InquiryPeriod
    permits InquiryDate, InquiryRange, InquiryYear {
    String id();
}

public record InquiryDate(
    String id,
    LocalDate targetDate,
    Optional<Location> targetLocation
) implements InquiryPeriod {}

public record InquiryRange(
    String id,
    LocalDate startDate,
    LocalDate endDate,
    Optional<Location> targetLocation
) implements InquiryPeriod {}

public record InquiryYear(
    String id,
    int year,
    Optional<Location> targetLocation
) implements InquiryPeriod {}
```

`targetLocation` is optional and is used for relocations, revolutions, and returns when needed.

Example:

```json
{
  "periods": [
    {
      "type": "range",
      "id": "year-2026",
      "startDate": "2026-01-01",
      "endDate": "2026-12-31"
    },
    {
      "type": "date",
      "id": "age-35",
      "targetDate": "2026-04-15"
    },
    {
      "type": "year",
      "id": "solar-return-2026",
      "year": 2026
    }
  ]
}
```

Predictive techniques declare which inquiry shape they accept and validate it at the start of calculation.

If a technique receives an unsupported inquiry shape, it must fail explicitly as an execution-level error, for example through `IllegalArgumentException` or a run-manifest error entry.

Such errors belong to the application/run layer, not to the doctrine report.

Required inquiry shapes by technique type:

```text
- annual profections: InquiryDate or InquiryYear
- monthly profections: InquiryDate
- zodiacal releasing enumeration: InquiryRange
- firdaria enumeration: InquiryRange
- distributions through the bounds: InquiryDate or InquiryRange
- primary directions: InquiryDate or InquiryRange
```

Returns and revolutions are modeled as comparative calculations.

Predictive techniques use inquiry periods.

Examples:

```text
- annual profections
- monthly profections
- zodiacal releasing
- firdaria
- distributions through the bounds
- primary directions
```

### 4.4 Comparison Inputs

Comparison inputs are optional inputs used for comparative calculations.

Comparative calculations relate a primary natal chart to another chart or derived sky moment.

Suggested Java model:

```java
public sealed interface ComparisonInput
    permits SecondaryNatalInput, ReturnInquiry, TransitInquiry, DerivedChartInquiry {
    String id();
}

public record SecondaryNatalInput(
    String id,
    NatalInput natal
) implements ComparisonInput {}

public record ReturnInquiry(
    String id,
    int targetYear,
    Optional<Location> targetLocation
) implements ComparisonInput {}

public record TransitInquiry(
    String id,
    Instant atMoment,
    Optional<Location> targetLocation
) implements ComparisonInput {}

public record DerivedChartInquiry(
    String id,
    String method
) implements ComparisonInput {}
```

`DerivedChartInquiry` is intentionally underspecified in the first architecture pass.

Composite charts, Davison charts, secondary progressions, tertiary progressions, and related techniques will need different parameters when implemented.

The `method` field is a placeholder until each derived-chart technique defines its own required input shape.

Comparative inputs distinguish two cases:

```text
- secondary natal inputs, such as synastry, where the secondary chart is calculated internally using the same doctrine
- derived secondary charts, such as returns or transits, where the comparative technique computes the secondary chart internally
```

Examples:

```text
- solar returns / revolutions: ReturnInquiry
- lunar returns: ReturnInquiry or TransitInquiry depending on implementation
- synastry: SecondaryNatalInput
- transits: TransitInquiry
- composite / Davison charts: SecondaryNatalInput or DerivedChartInquiry
- progressed-to-natal comparisons: DerivedChartInquiry
```

---

## 5. Input Validation and Normalization

Input validation and normalization is a pre-calculation stage.

It validates and normalizes raw input before any astrological calculation runs.

Responsibilities:

```text
- validate required fields
- validate ISO 8601 date and time formats
- validate UTC offset format
- validate latitude and longitude ranges
- validate doctrine ids against the doctrine registry
- validate inquiry input shape
- validate comparison input shape
- validate inquiry/comparison compatibility with requested techniques when possible
- normalize local birth datetime plus UTC offset into a resolved UTC instant
- normalize names, ids, and output-safe identifiers
```

Input validation errors are input errors, not calculation mismatches.

They should be reported before basic, descriptive, predictive, or comparative calculation begins.

---

## 6. Doctrine Concept

A doctrine represents a historical or traditional astrological method.

Examples:

```text
- Valens
- Ptolemy
- Dorotheus
- Abu Ma'shar / Albumasar
- Masha'allah / Messahala
- Bonatti
- Lilly
```

A doctrine is a hardcoded knowledge module.

It is not a partial implementation of a universal astrology schema.

Each doctrine should be implemented as code, not as arbitrary runtime settings.

Each doctrine defines:

```text
- what exists for that doctrine
- which concepts are meaningful
- which techniques it exposes
- which calculations it performs
- which result shape best represents its own logic
```

Doctrine code is responsible for:

```text
- declaring its identity
- declaring its hardcoded house system, zodiac, and terms
- choosing which traditional techniques it supports
- applying its own descriptive calculation rules
- exposing its supported predictive techniques
- exposing its supported comparative techniques
- producing doctrine-specific astrological data in its own result shape
```

Static tables may be stored as data if useful, but the doctrine itself should be explicit code.

Examples of static tables:

```text
- Egyptian terms
- Ptolemaic terms
- Chaldean order
- exaltation degrees
- triplicity ruler tables
- faces/decans
```

Examples of doctrine logic that should live in code:

```text
- how sect is interpreted
- how aspects are considered
- how dignity is judged
- which lots are calculated
- whether prenatal syzygy is used and how it is used
- which fixed stars matter and what counts as conjunction
- thresholds for combustion, cazimi, and under-the-beams
- how almutens are calculated
- how time-lord systems are applied
- which predictive techniques are supported
- which comparative techniques are supported
- what counts as relevant evidence
- which derived calculations belong to the doctrine
```

---

## 7. Basic Calculation (Swiss Ephemeris-backed)

Basic calculation is the shared mechanical calculation layer.

The basic package does not depend on doctrine implementations.

Basic calculation depends on a small explicit calculation context.

The context includes the doctrine's basic choices:

```text
- house_system
- zodiac
- terms
```

These choices are hardcoded in the doctrine implementation.

Example:

```text
DorotheusDoctrine
  → Whole Sign
  → Tropical
  → Egyptian Terms
```

Another doctrine may use:

```text
PtolemyDoctrine
  → Quadrant or other selected house system
  → Tropical
  → Ptolemaic Terms
```

The exact choices are defined by doctrine implementation and copied into a `BasicCalculationContext`.

The basic calculator receives the context, not a doctrine object.

Suggested model:

```java
public record BasicCalculationContext(
    String doctrineId,
    HouseSystem houseSystem,
    Zodiac zodiac,
    Terms terms,
    CalculationPrecision precision
) {}
```

The context is the controlled place for future basic-calculation options, such as:

```text
- apparent vs true positions
- geocentric vs topocentric positions
- tropical vs sidereal zodiac
- ayanamsa if sidereal
- mean node vs true node
- house cusp precision
- orb precision
```

The context stores doctrine identity as data, but it does not store a `Doctrine` object.

This preserves the dependency rule: `basic` does not depend on doctrine implementations.

### 7.1 BasicCalculator Responsibility

The basic calculator should compute chart facts that are mechanical once the natal data and basic calculation context are known.

Examples:

```text
- local birth datetime
- resolved UTC instant
- Julian day
- planetary longitudes
- planetary latitudes
- declinations
- speeds
- retrograde status
- Ascendant
- Midheaven
- house cusps
- house placements
- sign placements
- term/bound placements
- optional fixed star positions from an explicitly selected fixed-star set using Swiss Ephemeris
- antiscia and contra-antiscia positions
- angular distance from the Sun
- visibility geometry needed for solar phase conditions
- basic astronomical points
```

The basic calculator may use the bundled Swiss Ephemeris Java implementation.

Fixed-star calculation is not assumed by default.

If fixed stars are added, the star set must be explicitly parameterized as part of the calculation request or context. The basic layer computes positions for that explicit set when fixed-star calculation is enabled.

Doctrines interpret only the fixed-star positions made available by that explicit calculation context according to their own rules.

This keeps fixed-star output traceable without assuming a doctrine-free canonical star list.

Doctrine-specific judgment belongs to the descriptive or predictive calculation layer.

Questions such as whether a planet is fortunate, afflicted, dominant, maltreated, bonified, or significant according to a historical author are handled by doctrine modules.

### 7.2 BasicChart

`BasicChart` is the output of basic calculation.

It is unified, but parameterized by:

```text
- natal input
- house system
- zodiac
- terms
```

It should contain enough raw and geometrical data for doctrines to perform deductions.

Suggested content:

```text
BasicChart
- subject id
- calculation config
- date/time conversion data
- resolved UTC instant
- location
- planets
- points
- houses
- signs
- terms/bounds
- speeds
- retrograde flags
- angles
- optional fixed star positions from an explicit fixed-star set
- antiscia and contra-antiscia positions
- solar phase geometry
```

BasicChart should be factual and non-interpretive.

---

## 8. Descriptive, Predictive, and Comparative Calculation

Descriptive, predictive, and comparative calculations are doctrine-specific.

Descriptive calculation applies traditional rules to the natal chart as a fixed structure.

Predictive calculation unfolds time-dependent sequences from one natal chart.

Comparative calculation relates two or more charts to each other.

### 8.1 Doctrine Interface

The base doctrine interface supports descriptive calculation only.

Predictive and comparative techniques are optional and are exposed as separate modules.

```java
public interface Doctrine {
    String id();
    String name();

    HouseSystem houseSystem();
    Zodiac zodiac();
    Terms terms();

    DescriptiveResult describe(NatalInput natal, BasicChart chart);

    List<PredictiveTechnique> predictiveTechniques();
    List<ComparativeTechnique> comparativeTechniques();
}

public interface PredictiveTechnique<R extends PredictiveResult> {
    String id();
    String name();

    R predict(
        NatalInput natal,
        BasicChart chart,
        DescriptiveResult descriptiveResult,
        InquiryPeriod inquiryPeriod
    );
}

public sealed interface PredictiveResult
    permits ProfectionResult,
            ZodiacalReleasingResult,
            FirdariaResult,
            PrimaryDirectionResult {
}

public interface ComparativeTechnique<R extends ComparativeResult> {
    String id();
    String name();

    R compare(
        NatalInput primary,
        BasicChart primaryChart,
        DescriptiveResult primaryDescription,
        ComparisonInput comparison
    );
}

public sealed interface ComparativeResult
    permits SolarReturnResult,
            LunarReturnResult,
            SynastryResult,
            TransitResult,
            CompositeResult,
            ProgressedComparisonResult {
}
```

`PredictiveResult` may also be a non-sealed marker interface in the first implementation if sealed typing is too restrictive early on.

A doctrine with no predictive support returns an empty predictive technique list.

A doctrine with no comparative support returns an empty comparative technique list.

PredictiveTechnique and ComparativeTechnique implementations live with the doctrine implementation that owns them.

For example, `DorotheusFirdaria` lives next to `DorotheusDoctrine` and may read Dorotheus-specific descriptive data.

Shared predictive or comparative technique abstractions can be introduced later only when two doctrine implementations genuinely share the same rules and input shape.

This avoids empty predictive result stubs and avoids unsupported-operation methods.

Descriptive output describes the natal chart as a fixed structure.

Predictive output describes sequences, activations, directions, and time-lord periods relative to an inquiry date or range.

Comparative output describes relationships between charts, such as return charts, synastry, transits, composite charts, and progressed-to-natal comparisons.

### 8.2 DescriptiveResult

`DescriptiveResult` contains descriptive natal data deduced by the doctrine.

Examples:

```text
- sect
- essential dignities
- accidental dignities
- rulers
- dispositors
- aspects according to doctrine
- lots according to doctrine
- prenatal syzygy if used by the doctrine
- fixed star contacts according to doctrine, filtered from the explicitly calculated fixed-star set
- combustion, cazimi, and under-the-beams according to doctrine
- planetary condition
- almutens
- derived charts if doctrine uses them
- doctrine-specific notes
```

### 8.3 PredictiveResult

`PredictiveResult` is a marker interface or sealed parent type.

Each predictive technique returns its own concrete subtype.

Examples:

```text
- ProfectionResult
- ZodiacalReleasingResult
- FirdariaResult
- PrimaryDirectionResult
```

Predictive result subtypes contain predictive data calculated by a doctrine-supported predictive technique for an inquiry date or period.

Examples of predictive data:

```text
- annual profections
- monthly profections
- zodiacal releasing periods
- firdaria periods
- distributions through the bounds
- primary directions
- activated rulers, lots, houses, and planets
```

Predictive output has a different shape from descriptive output.

It may contain sequences, nested periods, date ranges, active rulers, perfected dates, or directed positions.

### 8.4 ComparativeResult

`ComparativeResult` is a marker interface or sealed parent type.

Each comparative technique returns its own concrete subtype.

Examples:

```text
- SolarReturnResult
- LunarReturnResult
- SynastryResult
- TransitResult
- CompositeResult
- ProgressedComparisonResult
```

Comparative result subtypes contain doctrine-specific comparisons between a primary chart and a secondary chart or derived sky moment.

Examples of comparative data:

```text
- solar returns / revolutions
- lunar returns
- synastry
- transits
- composite charts
- Davison charts
- progressed-to-natal comparisons
```

A comparative technique frequently needs the secondary chart's basic and descriptive output.

Comparison inputs are declarative and do not carry calculated chart state.

For secondary natal comparisons such as synastry, `SecondaryNatalInput` carries only the secondary `NatalInput`.

The comparative pipeline calculates the secondary `BasicChart` and `DescriptiveResult` internally using the same doctrine as the primary chart.

For derived charts such as returns or transits, the comparative technique computes the secondary chart internally from the comparison input.

Calculated secondary charts may be reused through a cache, but not through the input model.

Doctrine results should be allowed to differ between doctrines.

Not every doctrine must calculate the same sections.

If a doctrine does not expose a technique or does not calculate a concept, that absence is not represented as an astrological result inside the doctrine report.

Execution-level errors, such as requesting a technique not exposed by a doctrine, belong to the run manifest or application layer, not to the doctrine report.

When doctrines calculate the same concept, they must use the same field name.

Examples:

```text
- sect is always written under sect
- lots are always written under lots
- dignities are always written under dignities
- aspects are always written under aspects
- syzygy is always written under syzygy
- fixed star contacts are always written under fixedStars
- antiscia and contra-antiscia are always written under antiscia and contraAntiscia
- solar phase conditions are always written under solarPhase
- almutens are always written under almutens
- profections are always written under profections
```

This is a naming discipline, not a forced universal schema.

---

## 9. Formatting and Printing

Formatting is separate from calculation.

The output layer receives:

```text
NatalInput
BasicChart
DescriptiveResult
```

or:

```text
NatalInput
BasicChart
DescriptiveResult
PredictiveTechnique
InquiryPeriod
PredictiveResult
```

or:

```text
NatalInput
BasicChart
DescriptiveResult
ComparativeTechnique
ComparisonInput
ComparativeResult
```

and writes a report.

Every astrology report output must include reproducibility metadata:

```text
- engine version
- doctrine id
- doctrine house system, zodiac, and terms
- input identifiers
```

This makes generated JSON traceable after bug fixes, doctrine changes, or calculation changes.

The application may also write a run manifest for execution-level status.

The run manifest records application concerns such as:

```text
- requested doctrine id not found
- requested predictive or comparative technique not exposed by a doctrine
- unsupported inquiry or comparison input shape
- skipped outputs
- file write errors
```

These execution-level statuses are not astrological results and do not belong inside doctrine reports.

The first supported output format should be JSON.

Possible descriptive output path:

```text
output/descriptive/{subjectId}/{doctrineId}.json
```

Possible predictive output path:

```text
output/predictive/{subjectId}/{doctrineId}/{techniqueId}/{periodId}.json
```

Possible comparative output path:

```text
output/comparative/{subjectId}/{doctrineId}/{techniqueId}/{comparisonId}.json
```

Recommended descriptive JSON shape:

```json
{
  "reportType": "descriptive",
  "metadata": {
    "engineVersion": "0.1.0",
  },
  "subject": {
    "id": "ilia"
  },
  "doctrine": {
    "id": "dorotheus",
    "name": "Dorotheus"
  },
  "resolvedUtcInstant": "1990-01-01T12:45:00Z",
  "basicCalculationContext": {
    "doctrineId": "dorotheus",
    "houseSystem": "WHOLE_SIGN",
    "zodiac": "TROPICAL",
    "terms": "EGYPTIAN",
    "precision": "STANDARD"
  },
  "basic": {
    "planets": [],
    "houses": [],
    "angles": []
  },
  "descriptive": {
    "sect": {},
    "dignities": {},
    "lots": {},
    "aspects": {},
    "syzygy": {},
    "fixedStars": [],
    "solarPhase": {},
    "almutens": {}
  }
}
```

Recommended predictive JSON shape:

```json
{
  "reportType": "predictive",
  "metadata": {
    "engineVersion": "0.1.0",
  },
  "subject": {
    "id": "ilia"
  },
  "doctrine": {
    "id": "dorotheus",
    "name": "Dorotheus"
  },
  "predictiveTechnique": {
    "id": "annual-profections",
    "name": "Annual Profections"
  },
  "inquiryPeriod": {
    "id": "year-2026",
    "startDate": "2026-01-01",
    "endDate": "2026-12-31"
  },
  "predictive": {
    "profections": {},
    "timeLords": []
  }
}
```

Recommended comparative JSON shape:

```json
{
  "reportType": "comparative",
  "metadata": {
    "engineVersion": "0.1.0",
  },
  "subject": {
    "id": "ilia"
  },
  "doctrine": {
    "id": "dorotheus",
    "name": "Dorotheus"
  },
  "comparativeTechnique": {
    "id": "synastry",
    "name": "Synastry"
  },
  "comparisonInput": {
    "id": "ilia-omar",
    "type": "secondaryNatal"
  },
  "comparative": {
    "contacts": [],
    "overlays": []
  }
}
```

The formatter serializes already-calculated data.

---

## 10. Reference Validation

Reference validation is stage 8.

Reference validation is separate from input validation.

It is also separate from the calculation engine.

It consumes generated output and compares it to references.

Possible validation references:

```text
- manually verified charts
- published examples from traditional texts
- internal golden files
- independent software
- external astrology calculation services
```

Reference validation answers:

```text
- Does our output match a reference?
- If not, why?
- Is the difference caused by doctrine, rounding, input data, or a bug?
```

---

## 11. Suggested Package Structure

Initial clean structure:

```text
src/main/java/app/
  App.java

  input/
    InputLoader.java
    InputValidator.java
    InputNormalizer.java
    model/
      NatalInput.java
      InputBundle.java
      DoctrineRequest.java
      InquiryPeriod.java
      InquiryDate.java
      InquiryRange.java
      InquiryYear.java
      ComparisonInput.java
      SecondaryNatalInput.java
      ReturnInquiry.java
      TransitInquiry.java
      DerivedChartInquiry.java

  doctrine/
    Doctrine.java
    DoctrineRegistry.java
    DescriptiveResult.java
    predictive/
      PredictiveTechnique.java
      PredictiveResult.java
      ProfectionResult.java
      ZodiacalReleasingResult.java
      FirdariaResult.java
      PrimaryDirectionResult.java
    comparative/
      ComparativeTechnique.java
      ComparativeResult.java
      SolarReturnResult.java
      LunarReturnResult.java
      SynastryResult.java
      TransitResult.java
      CompositeResult.java
      ProgressedComparisonResult.java
    impl/
      dorotheus/
        DorotheusDoctrine.java
        DorotheusFirdaria.java
        DorotheusProfections.java
        DorotheusSynastry.java
        DorotheusSolarReturn.java
      ptolemy/
        PtolemyDoctrine.java
      valens/
        ValensDoctrine.java

  basic/
    BasicCalculationContext.java
    BasicCalculator.java
    TraditionalTables.java
    calculator/
      SimpleCalculator.java
      PlanetCalculator.java
      HouseCalculator.java
      AngleCalculator.java
      PointCalculator.java
      ChartPointCalculator.java
      SolarPhaseCalculator.java
      MoonPhaseCalculator.java
      SectCalculator.java
    model/
      BasicChart.java
      PlanetPosition.java
      HousePosition.java
      ChartAngle.java
    data/
      Planet.java
      ZodiacSign.java
      HouseSystem.java
      Zodiac.java
      Terms.java
      CalculationPrecision.java

  descriptive/
    common/
      calculator/
      model/
      data/
    valens/
      calculator/
      model/
      data/
    ptolemy/
      calculator/
      model/
      data/
    dorotheus/
      calculator/
      model/
      data/

  output/
    AstrologyReport.java
    DescriptiveAstrologyReport.java
    PredictiveAstrologyReport.java
    ComparativeAstrologyReport.java
    ReportMetadata.java
    RunManifest.java
    RunManifestWriter.java
    JsonReportWriter.java

  common/
    Location.java
    BirthDateTime.java
```

Input POJOs belong under `app.input.model`. Basic output POJOs belong under `app.basic.model`. Basic/shared astrology enums currently belong under `app.basic.data`. Descriptive POJOs and enums should be introduced under the relevant `app.descriptive...model` and `app.descriptive...data` packages instead of returning to the former catch-all `app.model.*` layout.

Later, validation can live under:

```text
app/validation/
```

or remain as a separate tool.

Former code remains isolated under:

```text
app.old
```

Migration into the fresh architecture is deliberate and feature-based.

---

## 12. First Implementation Milestone

The first milestone should be minimal and compiling.

Goal:

```text
Load natal inputs
Load doctrine ids
Validate and normalize inputs
For each natal × doctrine:
    resolve doctrine
    build BasicCalculationContext from doctrine choices
    calculate placeholder BasicChart
    calculate placeholder DescriptiveResult
    write descriptive JSON output
If inquiry periods exist and the doctrine exposes predictive techniques:
    calculate placeholder PredictiveResult per technique
    write predictive JSON output
If comparison inputs exist and the doctrine exposes comparative techniques:
    calculate placeholder ComparativeResult per technique
    write comparative JSON output
```

This milestone proves the new architecture with the smallest useful implementation.

### 12.1 First Doctrine

Start with one doctrine only, for example:

```text
DorotheusDoctrine
```

The first doctrine can initially return placeholder descriptive data.

Doctrine selection remains explicit; do not introduce a hidden default doctrine.

### 12.2 First Basic Calculation

Start with minimal basic values:

```text
- parsed birth data
- local birth datetime
- resolved UTC instant
- Julian day
- BasicCalculationContext
```

Then add real Swiss Ephemeris-backed planetary positions.

Then houses.

Then signs and terms.

---

## 13. Migration Strategy from Old Code

Migration happens feature by feature.

Recommended order:

```text
1. Input loading
2. Input validation / normalization
3. JSON writing
4. Swiss Ephemeris planetary positions
5. Angles and houses
6. Sign and term placement
7. Geometry outputs: antiscia, contra-antiscia, solar angular distances
8. Optional fixed star positions
9. Sect
10. Prenatal syzygy
11. Lots
12. Aspects
13. Dignities
14. Solar phase conditions
15. Almutens
16. Inquiry periods and predictive output
17. Profections and time-lord systems
18. Advanced predictive calculations
19. Comparison inputs and comparative output
20. Synastry, transits, returns, and other comparative calculations
21. Reference validation tools
```

Each migrated feature should be assigned to the correct layer:

```text
Basic layer:
- planets
- houses
- signs
- terms
- speeds
- retrograde
- angles
- optional fixed star positions
- antiscia and contra-antiscia geometry
- solar angular distances and visibility geometry

Descriptive doctrine layer:
- sect interpretation
- dignities
- aspects by doctrine
- lots by doctrine
- prenatal syzygy
- fixed star contacts by doctrine
- solar phase conditions by doctrine
- almutens
- condition judgments

Predictive doctrine layer:
- profections
- time-lord systems
- primary directions
- distributions
- zodiacal releasing
- firdaria

Comparative doctrine layer:
- solar returns
- lunar returns
- revolutions
- synastry
- transits
- composite charts
- Davison charts
- progressed-to-natal comparisons

Output layer:
- JSON serialization only

Input validation / normalization layer:
- input shape checks
- date/time/offset validation
- coordinate validation
- doctrine id validation
- inquiry and comparison compatibility checks
- UTC instant normalization

Reference validation layer:
- generated output comparisons only
```

---

## 14. Important Design Rules

### 14.1 Treat doctrine as a hardcoded knowledge module

A doctrine is code.

A doctrine is not a settings file and not a partial implementation of a universal astrology schema.

Configuration selects a doctrine. Doctrine behavior is defined by doctrine code.

Each doctrine defines what exists for that doctrine, which concepts are meaningful, which techniques it exposes, which calculations it performs, and which result shape best represents its logic.

The engine does not compare doctrine outputs, reconcile contradictions, or mark absent concepts as unsupported inside doctrine reports.

If a doctrine does not expose a technique or does not calculate a concept, that absence is simply absent from the doctrine result.

Execution-level errors belong to the run manifest or application layer.

### 14.2 Separate input validation from reference validation

Input validation / normalization runs before calculation.

Reference validation consumes generated output after calculation.

Calculation depends on normalized input, doctrine modules, and the basic calculation engine.

### 14.3 Basic calculation is shared and dependency-light

There is one basic calculator.

It receives `BasicCalculationContext`, which contains primitive calculation choices:

```text
house_system, zodiac, terms, precision, rounding policy
```

It does not receive or depend on a doctrine object.

### 14.4 Keep package dependencies one-directional

Preferred dependency direction:

```text
input/common
    ↓
basic
    ↓
doctrine
    ↓
output
```

The `basic` package may use input/common models and Swiss Ephemeris code.

The `basic` package must not depend on concrete doctrine implementations.

Doctrine-aware orchestration code may call the basic calculator by building `BasicCalculationContext` from doctrine choices.

### 14.5 Descriptive, predictive, and comparative calculations are separate

Descriptive, predictive, and comparative calculations use separate result models.

Descriptive calculations describe the natal chart itself.

Predictive calculations describe activations, sequences, directions, and time-lord periods for an inquiry date or date range.

Comparative calculations describe relationships between two or more charts.

Predictive and comparative techniques are optional per doctrine.

### 14.6 Descriptive, predictive, and comparative calculation is doctrine-specific

Doctrines may disagree.

Different results are expected and valid if they follow their declared rule systems.

The engine does not reconcile doctrinal contradictions or force outputs into a universal schema.

### 14.7 Output must identify the doctrine and calculation context

Every output must clearly say:

```text
- which engine version generated it
- which natal input was used
- which doctrine was used
- which house system was used
- which zodiac was used
- which terms/bounds were used
- which precision and rounding policy were used
```

Shared concept names include:

```text
sect
lots
dignities
aspects
syzygy
fixedStars
antiscia
contraAntiscia
solarPhase
almutens
profections
synastry
transits
returns
```

### 14.8 Version outputs for reproducibility

Every generated report carries metadata identifying the engine version.

When a bug is fixed or a doctrine implementation changes, old outputs can be identified as stale or produced by an older calculation model.

### 14.9 Use consistent names for shared concepts

Doctrine outputs may differ in content and structure.

However, when two doctrines calculate the same concept, they use the same field name.

For example, if both DorotheusDoctrine and PtolemyDoctrine calculate sect, both write it under:

```text
sect
```

not separate names such as:

```text
sectAnalysis
sectCondition
sectData
```

This keeps outputs comparable without forcing every doctrine into the same schema.

### 14.10 Prefer explicitness over clever abstraction

Traditional astrology contains real doctrinal differences.

The code should make differences visible instead of hiding them behind overly generic abstractions.

---

## 15. Long-Term Direction

Once the clean pipeline exists, Mystro can grow into a comprehensive traditional astrology engine.

Possible future doctrine modules:

```text
- ValensDoctrine
- PtolemyDoctrine
- DorotheusDoctrine
- AbuMasharDoctrine
- MashaAllahDoctrine
- BonattiDoctrine
- LillyDoctrine
```

Possible future calculation areas:

```text
- essential dignities
- accidental dignities
- sect
- bonification and maltreatment
- reception
- enclosure
- aversion
- lots
- prenatal syzygy
- fixed stars
- antiscia and contra-antiscia
- solar phase conditions
- almutens
- profections
- distributions / circumambulations
- zodiacal releasing
- firdaria
- primary directions
- solar returns
- lunar returns
- revolutions
- synastry
- transits
- composite charts
- Davison charts
- progressed-to-natal comparisons
- planetary condition scoring
```

These should be added gradually and assigned clearly to either:

```text
- basic calculation
- descriptive doctrine calculation
- predictive doctrine calculation
- comparative doctrine calculation
- output formatting
- validation
```

Placement of notable concepts:

```text
- Prenatal syzygy: descriptive doctrine calculation
- Fixed star positions: optional basic calculation from an explicitly selected fixed-star set
- Fixed star relevance and contacts: descriptive doctrine calculation by filtering the explicitly calculated fixed-star set
- Antiscia / contra-antiscia: basic geometrical calculation
- Solar angular distance and visibility geometry: basic geometrical calculation
- Combustion, cazimi, under-the-beams: descriptive doctrine calculation
- Almutens: descriptive doctrine calculation
- Profections, releasing, firdaria, distributions, primary directions: predictive doctrine calculation
- Solar returns, lunar returns, synastry, transits, composites, progressed-to-natal: comparative doctrine calculation
```

---

## 16. Summary

The new architecture should be:

```text
Natal inputs × Doctrine code modules → Basic chart → Descriptive calculation → Descriptive JSON output
Natal inputs × Doctrine code modules × Predictive techniques × Inquiry periods → Predictive JSON output
Natal inputs × Doctrine code modules × Comparative techniques × Comparison inputs → Comparative JSON output
```

The doctrine hardcodes the basic calculation choices:

```text
house_system + zodiac + terms
```

The orchestration layer copies those choices into `BasicCalculationContext` together with precision and rounding policy.

The basic calculator performs reusable Swiss Ephemeris-backed calculations from `NatalInput` and `BasicCalculationContext`.

The doctrine performs descriptive calculations and exposes optional predictive and comparative techniques.

The formatter writes descriptive, predictive, and comparative outputs with reproducibility metadata.

Input validation / normalization happens before calculation.

Reference validation remains external to the calculation engine.
