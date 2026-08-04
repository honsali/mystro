package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;

/**
 * One named eclipse contact instant. Missing contacts are omitted from the list rather than emitted
 * as null placeholders.
 */
public record EclipseContact(
        EclipseContactPhase phase,
        OffsetDateTime dateTime,
        double julianDayUt,
        EclipseVisibilityStatus visibilityStatus,
        String visibilityNote
) {
    public EclipseContact {
        if (phase == null) {
            throw new IllegalArgumentException("phase is required");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime is required");
        }
        if (!Double.isFinite(julianDayUt)) {
            throw new IllegalArgumentException("julianDayUt must be finite");
        }
        visibilityStatus = visibilityStatus == null ? EclipseVisibilityStatus.UNKNOWN : visibilityStatus;
        visibilityNote = visibilityNote == null ? "" : visibilityNote;
    }
}
