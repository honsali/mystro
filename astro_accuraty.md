# Astro Accuracy Notes

This file records basic-calculation accuracy review points and their current status.

## Current accepted baseline

- Positions are currently tropical only. Sidereal calculation is intentionally absent until an explicit ayanamsa model and doctrine requirement are introduced.
- Swiss Ephemeris apparent positions with `SEFLG_SPEED` are used. This is intentional and matches common astrology software behavior.
- Julian day is derived from the subject's resolved UTC instant, so reported instant and calculation instant have one source of truth.
- Basic chart sect is a mechanical altitude baseline: Sun above horizon is diurnal, Sun below horizon is nocturnal, using `altitude >= 0.0`.
- Doctrine modules may refine sect for twilight/refraction/author-specific rules.
- The Sun's `angularDistanceFromSun` is stored as `0.0`; doctrine solar-condition calculators must special-case the Sun.
- Lunar nodes use positive `meanDailySpeed` magnitudes; `speed` remains signed and `speedRatio` uses absolute speed magnitude.
- Lunar nodes are positional points and intentionally do not receive point-level sect or dignity/debility assessments.

## Resolved review points

- Removed duplicate zodiac math from `TraditionalTables`; generic zodiac math now lives in `AstroMath`.
- Removed dead nullable planet guards after `calculatePlanet(...)` failure was confirmed to throw.
- Removed unreachable Moon phase `>= 360` branch and asymmetric 180° epsilon.
- Shared Mercury/solar orientation logic through `AstroMath.orientationToSun(...)`.
- Added `contraParallelSeparation` alongside `declinationDifference` for equatorial pair relations.
- Collapsed internal pairwise point identity to `PointKey` instead of string names.
- Reduced `PlanetPointEntry` `with*` duplication through a private updater.
- Removed unsupported `SIDEREAL` enum value and sidereal Swiss Ephemeris flag branches.

## Deferred precision questions

- Topocentric Moon/parallax is not enabled. Current basic positions remain geocentric. Switching the Moon to topocentric is a larger calculation-convention decision and should be explicit.
- Moon altitude currently follows the same ecliptic-to-horizontal baseline used by the rest of the chart. If topocentric Moon is adopted, lunar altitude/parallax should be reviewed together.
- No automatic fallback is provided for quadrant-house failure at extreme latitudes. Current behavior is fail-fast.
- Exact full Moon currently counts as waxing because `waxing = directedElongation <= 180.0`. This convention can be revisited if a doctrine needs a different boundary rule.
- Mercury sect at exact conjunction uses longitude orientation only and does not distinguish superior from inferior conjunction. This is rare and can be refined later if needed.
- Declination `sameHemisphere` treats exact zero declination as north by convention.

## Verified clean points

- Antiscia and contra-antiscia formulas are correct.
- Whole-sign and quadrant house assignment conventions are currently consistent.
- Egyptian/Ptolemaic term tables, Chaldean faces, domicile/exaltation signs, and Dorothean/Ptolemaic triplicities were reviewed as correct for current scope.
- South Node mirroring is geometric: longitude + 180°, RA + 180°, latitude/declination negated, altitude recomputed.
