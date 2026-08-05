# Mystro Technical Specification

> Documentation axis: current executable architecture, data contracts, and calculation
> conventions. Operational commands belong in `README.md` or `LOCAL_MODULE2_WORKFLOW.md`.

## Runtime boundary

Mystro is a Java 25 command-line application. The production entry point is `app.Main`.

The production flow accepts exactly one alias:

```text
java -jar target/mystro-<version>.jar <native-list-alias>
```

The alias selects one entry from the ignored root file `native-list.json`. Production output is
always written to:

```text
output/<alias>/reading_output.json
```

## Production pipeline

```text
app.Main
  -> NativeListInputLoader
  -> ReadingInput
  -> ReadingInputMapper
  -> Subject + optional inquiry date
  -> ReadingBundleCalculator
  -> NatalDescriptionReadingCalculator
  -> ValensNatalDescriptionSpecialist
  -> ReadingBundleReport
  -> MystroObjectMapper
```

The command-line bundle currently contains one `NATAL_DESCRIPTION` reading. Life-arc reports are
research tooling and are specified separately in
[LOCAL_MODULE2_WORKFLOW.md](LOCAL_MODULE2_WORKFLOW.md).

## Input contract

`NativeListInputLoader` maps the external snake-case fields shown in the
[README](../README.md) to `ReadingInput`. Normalization and validation rules:

- dates enter as `dd/MM/yyyy` and are normalized to ISO dates;
- birth time accepts `HH:mm` or `HH:mm:ss` and is normalized to `HH:mm:ss`;
- UTC offset is mandatory user input;
- latitude must be finite and strictly between `-90` and `90` degrees;
- longitude must be finite and between `-180` and `180` degrees inclusive;
- elevation defaults to `0.0` metres and must be finite;
- inquiry date is optional and falls on or after the canonical UTC birth date.

The original offset date-time is retained as input metadata. Once resolved, every downstream
calendar and astronomical calculation uses the same canonical UTC instant.

## Calculation architecture

`CalculationContext` owns the subject, doctrine conventions, Julian day UT, house cusps, `ascmc`,
ARMC, and the adapter to Swiss Ephemeris.

`BasicCalculator` builds the mechanical natal chart in dependency order:

```text
metadata and time scales
  -> planets
  -> houses and angles
  -> sect
  -> calculated points
  -> pairwise relations
  -> solar conditions
  -> planet sect metadata
  -> Moon phase
```

`ValensNatalDescriptionSpecialist` then adds doctrine-owned material, including the prenatal
syzygy, lots, aspects, dignities, topic rulers and assessments, doryphories, dodecatemoria,
triplicity phases, fixed stars, and the Ptolemaic hyleg/alcocoden annex.

Exact new- and full-Moon searches are centralized in `app.chart.search.SyzygyEventSearch` and are
shared by prenatal-syzygy and life-arc lunar calculations.

## Astronomical engine

- Java binding: `org.swisseph:swisseph-java-ffm:0.3.0`.
- Native engine: Swiss Ephemeris C 2.10.03.
- Runtime ephemeris: file-backed Swiss Ephemeris data from `ephe/`.
- Native library: platform file under `dll/` or `libs/`, or an explicitly configured path.
- Native access: `--enable-native-access=ALL-UNNAMED`.

Mystro rejects Moshier fallback for calculations that require file-backed Swiss Ephemeris.

## Time model

- User input: local civil date-time plus explicit UTC offset.
- Canonical application time: UTC.
- Swiss position and event calls: Julian day UT/UT1 as required by the native API.
- Ephemeris time: Julian day TT derived with Swiss Ephemeris delta-T.
- Emitted event date-times: UTC.

UTC, UT1, and TT retain explicit, distinct mappings throughout the calculation pipeline.

## Calculation conventions

- Tropical zodiac.
- Geocentric apparent planetary longitudes and latitudes.
- Observer elevation retained in the subject model.
- Topocentric position used when calculating observer-dependent altitude.
- Mean lunar node.
- File-backed Swiss Ephemeris failures fail fast.
- Failure of the selected house system stops the calculation.
- Basic sect is altitude-based; Sun altitude `>= 0.0` is diurnal.
- Moon illumination fraction comes from `swe_pheno_ut`.
- Calculators keep full `double` precision.
- JSON serialization rounds finite doubles to two decimal places.

## Output contract

The top-level production JSON has this shape:

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

`NatalChart` contains mechanical chart data and Valens-led enrichments. `lots` and
`pairwiseRelations` are filtered arrays. Optional sections follow the available input and selected
doctrine.

## Package responsibilities

```text
app.input          local input loading, normalization, validation
app.io             JSON mapper and serialization
app.ephemeris      Java-to-native Swiss Ephemeris adapter
app.chart          shared chart model, mechanics, and astronomical searches
app.reading        reading bundles and task-specific calculations
app.planetaryhours planetary-day and planetary-hour calculation
```

`src/test/java/app/local` contains the gated local report runners and renderers.
