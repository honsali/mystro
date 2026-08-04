package app.reading.lifearc.transit;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.AstroMath;
import app.chart.data.AspectType;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

/**
 * Short exact-transit search window derived from a strong active monthly transit checkpoint contact.
 *
 * <p>This is a local/research model for later exact root-finding. It deliberately represents a
 * bounded search around a profection-filtered checkpoint contact, not a whole-life daily scan.</p>
 */
public record TransitSearchWindow(
        int sequence,
        String sourceTechnique,
        String sourceMethodId,
        int sourceCheckpointNumber,
        OffsetDateTime checkpointDateTime,
        OffsetDateTime windowStartDateTime,
        OffsetDateTime windowEndDateTime,
        PointKey transitPoint,
        boolean transitPointIsLordOfYear,
        boolean transitPointIsLordOfMonth,
        TransitNatalTargetType natalTargetType,
        String natalTargetName,
        double natalTargetLongitude,
        ZodiacSign natalTargetSign,
        double natalTargetDegreeInSign,
        int natalTargetHouse,
        AspectType aspect,
        double checkpointAngularSeparation,
        double checkpointOrbFromExactDegrees,
        List<MonthlyTransitActivationReason> activationReasons,
        int activationWeight
) {
    public TransitSearchWindow {
        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        if (sourceTechnique == null || sourceTechnique.isBlank()) {
            throw new IllegalArgumentException("sourceTechnique is required");
        }
        if (sourceMethodId == null || sourceMethodId.isBlank()) {
            throw new IllegalArgumentException("sourceMethodId is required");
        }
        if (sourceCheckpointNumber <= 0) {
            throw new IllegalArgumentException("sourceCheckpointNumber must be positive");
        }
        if (checkpointDateTime == null) {
            throw new IllegalArgumentException("checkpointDateTime is required");
        }
        if (windowStartDateTime == null || windowEndDateTime == null || !windowStartDateTime.isBefore(windowEndDateTime)) {
            throw new IllegalArgumentException("windowStartDateTime must be before windowEndDateTime");
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
        if (!Double.isFinite(checkpointAngularSeparation)) {
            throw new IllegalArgumentException("checkpointAngularSeparation must be finite");
        }
        if (!Double.isFinite(checkpointOrbFromExactDegrees) || checkpointOrbFromExactDegrees < 0.0) {
            throw new IllegalArgumentException("checkpointOrbFromExactDegrees must be non-negative");
        }
        if (activationWeight <= 0) {
            throw new IllegalArgumentException("activationWeight must be positive");
        }
        natalTargetLongitude = AstroMath.normalize(natalTargetLongitude);
        activationReasons = activationReasons == null ? List.of() : List.copyOf(activationReasons);
    }
}
