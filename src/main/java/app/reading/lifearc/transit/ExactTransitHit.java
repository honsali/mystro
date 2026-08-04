package app.reading.lifearc.transit;

import java.time.OffsetDateTime;

import app.chart.AstroMath;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

/**
 * Exact transit-to-natal aspect hit, or a station very near that exact target, found inside a
 * {@link TransitSearchWindow}.
 */
public record ExactTransitHit(
        int sequence,
        int sourceWindowSequence,
        ExactTransitHitKind hitKind,
        OffsetDateTime exactDateTime,
        OffsetDateTime searchWindowStartDateTime,
        OffsetDateTime searchWindowEndDateTime,
        PointKey transitPoint,
        TransitNatalTargetType natalTargetType,
        String natalTargetName,
        double natalTargetLongitude,
        ZodiacSign natalTargetSign,
        double natalTargetDegreeInSign,
        int natalTargetHouse,
        AspectType aspect,
        double transitLongitude,
        double angularSeparation,
        double orbFromExactDegrees,
        AspectMotion aspectMotion
) {
    public ExactTransitHit {
        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        if (sourceWindowSequence <= 0) {
            throw new IllegalArgumentException("sourceWindowSequence must be positive");
        }
        if (hitKind == null) {
            throw new IllegalArgumentException("hitKind is required");
        }
        if (exactDateTime == null) {
            throw new IllegalArgumentException("exactDateTime is required");
        }
        if (searchWindowStartDateTime == null || searchWindowEndDateTime == null || !searchWindowStartDateTime.isBefore(searchWindowEndDateTime)) {
            throw new IllegalArgumentException("searchWindowStartDateTime must be before searchWindowEndDateTime");
        }
        if (exactDateTime.isBefore(searchWindowStartDateTime) || exactDateTime.isAfter(searchWindowEndDateTime)) {
            throw new IllegalArgumentException("exactDateTime must be inside the source search window");
        }
        if (transitPoint == null) {
            throw new IllegalArgumentException("transitPoint is required");
        }
        if (natalTargetType == null) {
            throw new IllegalArgumentException("natalTargetType is required");
        }
        if (natalTargetName == null || natalTargetName.isBlank()) {
            throw new IllegalArgumentException("natalTargetName is required");
        }
        if (natalTargetSign == null) {
            throw new IllegalArgumentException("natalTargetSign is required");
        }
        if (!Double.isFinite(natalTargetDegreeInSign) || natalTargetDegreeInSign < 0.0 || natalTargetDegreeInSign >= 30.0) {
            throw new IllegalArgumentException("natalTargetDegreeInSign must be in [0, 30)");
        }
        if (natalTargetHouse < 1 || natalTargetHouse > 12) {
            throw new IllegalArgumentException("natalTargetHouse must be 1..12");
        }
        if (aspect == null) {
            throw new IllegalArgumentException("aspect is required");
        }
        if (!Double.isFinite(angularSeparation)) {
            throw new IllegalArgumentException("angularSeparation must be finite");
        }
        if (!Double.isFinite(orbFromExactDegrees) || orbFromExactDegrees < 0.0) {
            throw new IllegalArgumentException("orbFromExactDegrees must be non-negative");
        }
        natalTargetLongitude = AstroMath.normalize(natalTargetLongitude);
        transitLongitude = AstroMath.normalize(transitLongitude);
    }
}
