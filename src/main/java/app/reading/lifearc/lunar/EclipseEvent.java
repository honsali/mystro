package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.AstroMath;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.SyzygyType;

/**
 * Local/research model for true eclipse rows.
 *
 * <p>The local lunar output still emits mean-node eclipse candidates on {@link LunationEntry}
 * as fallback/reference evidence. These rows are true Swiss Ephemeris eclipse events: global
 * eclipse reality first, with subject-location visibility attached when supported.</p>
 */
public record EclipseEvent(
        int sequenceIndex,
        String methodId,
        EclipseCalculationScope calculationScope,
        EclipseEventKind kind,
        SyzygyType syzygy,
        EclipseEventType eclipseType,
        EclipseCandidateType candidateReference,
        OffsetDateTime maximumDateTime,
        double maximumJulianDayUt,
        double syzygyLongitude,
        ZodiacSign syzygySign,
        double syzygyDegreeInSign,
        int natalHouseOverlay,
        Planet nearestNode,
        double nearestNodeLongitude,
        double nearestNodeOrbDegrees,
        Double magnitude,
        Double obscuration,
        Double penumbralMagnitude,
        Integer sarosSeries,
        Integer sarosMember,
        List<EclipseContact> contacts,
        EclipseVisibility visibility
) {
    public EclipseEvent {
        if (sequenceIndex <= 0) {
            throw new IllegalArgumentException("sequenceIndex must be positive");
        }
        if (methodId == null || methodId.isBlank()) {
            throw new IllegalArgumentException("methodId is required");
        }
        if (calculationScope == null) {
            throw new IllegalArgumentException("calculationScope is required");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind is required");
        }
        if (syzygy == null) {
            throw new IllegalArgumentException("syzygy is required");
        }
        if (kind == EclipseEventKind.SOLAR && syzygy != SyzygyType.NEW_MOON) {
            throw new IllegalArgumentException("solar eclipse events must be new-moon syzygies");
        }
        if (kind == EclipseEventKind.LUNAR && syzygy != SyzygyType.FULL_MOON) {
            throw new IllegalArgumentException("lunar eclipse events must be full-moon syzygies");
        }
        if (eclipseType == null) {
            throw new IllegalArgumentException("eclipseType is required");
        }
        if (candidateReference == null) {
            candidateReference = EclipseCandidateType.NONE;
        }
        if (maximumDateTime == null) {
            throw new IllegalArgumentException("maximumDateTime is required");
        }
        if (!Double.isFinite(maximumJulianDayUt)) {
            throw new IllegalArgumentException("maximumJulianDayUt must be finite");
        }
        syzygyLongitude = AstroMath.normalize(syzygyLongitude);
        if (syzygySign == null) {
            throw new IllegalArgumentException("syzygySign is required");
        }
        if (!Double.isFinite(syzygyDegreeInSign) || syzygyDegreeInSign < 0.0 || syzygyDegreeInSign >= 30.0) {
            throw new IllegalArgumentException("syzygyDegreeInSign must be in [0, 30)");
        }
        if (natalHouseOverlay < 1 || natalHouseOverlay > 12) {
            throw new IllegalArgumentException("natalHouseOverlay must be 1..12");
        }
        if (nearestNode != Planet.NORTH_NODE && nearestNode != Planet.SOUTH_NODE) {
            throw new IllegalArgumentException("nearestNode must be NORTH_NODE or SOUTH_NODE");
        }
        nearestNodeLongitude = AstroMath.normalize(nearestNodeLongitude);
        if (!Double.isFinite(nearestNodeOrbDegrees) || nearestNodeOrbDegrees < 0.0 || nearestNodeOrbDegrees > 180.0) {
            throw new IllegalArgumentException("nearestNodeOrbDegrees must be in [0, 180]");
        }
        requireNonNegativeOrNull("magnitude", magnitude);
        requireNonNegativeOrNull("obscuration", obscuration);
        requireNonNegativeOrNull("penumbralMagnitude", penumbralMagnitude);
        if (sarosSeries != null && sarosSeries < 0) {
            throw new IllegalArgumentException("sarosSeries must be non-negative when present");
        }
        if (sarosMember != null && sarosMember < 0) {
            throw new IllegalArgumentException("sarosMember must be non-negative when present");
        }
        contacts = contacts == null ? List.of() : List.copyOf(contacts);
        visibility = visibility == null
                ? EclipseVisibility.unknown(true, "Visibility not supplied")
                : visibility;
    }

    private static void requireNonNegativeOrNull(String field, Double value) {
        if (value != null && (!Double.isFinite(value) || value < 0.0)) {
            throw new IllegalArgumentException(field + " must be non-negative when present");
        }
    }
}
