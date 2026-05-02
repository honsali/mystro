# Astro accuracy notes

Stage 1/basic calculation is currently considered accurate enough to serve as the shared non-interpretive layer.

## Current status

- The six previously identified term-table errors in `TraditionalTables` have been fixed:
  - Egyptian Aries
  - Egyptian Virgo
  - Ptolemaic Gemini
  - Ptolemaic Leo
  - Ptolemaic Libra
  - Ptolemaic Scorpio
- `PlanetCalculator` now preserves equatorial right ascension together with declination.
- South node mirroring includes longitude + 180°, right ascension + 180°, inverted latitude, and inverted declination.
- `SimpleCalculator` exposes both UT and TT Julian days, delta T, true/mean obliquity, and nutation in longitude/obliquity.
- `PlanetPosition` exposes altitude and an `aboveHorizon` boolean for every planet/node, so whole-sign doctrines do not need to infer horizon status from house numbers.
- Basic calculators retain full internal double precision; rounding is performed during JSON serialization.

## Stage 1 scope boundary

Do not add interpretive or doctrine-specific concepts to basic calculation by default. Keep the following outside stage 1 unless a concrete architecture change says otherwise:

- fixed star judgments or default star lists
- lots
- prenatal syzygy interpretation
- combustion/cazimi/under-the-beams
- exact exaltation-degree interpretation
- primary directions as a predictive technique

Fixed-star positions may be added later only with an explicitly selected star set.
