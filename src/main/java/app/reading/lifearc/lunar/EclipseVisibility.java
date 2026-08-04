package app.reading.lifearc.lunar;

import java.util.List;

/**
 * Visibility summary for an eclipse event. Global truth and local visibility are deliberately
 * separated so true global eclipses can be retained even when subject-location visibility is
 * visible, not visible, or unsafe to calculate.
 */
public record EclipseVisibility(
        boolean globallyOccurs,
        EclipseVisibilityStatus localVisibility,
        boolean maximumVisibleAtLocation,
        List<EclipseContactPhase> visibleContactPhases,
        String reason
) {
    public EclipseVisibility {
        localVisibility = localVisibility == null ? EclipseVisibilityStatus.UNKNOWN : localVisibility;
        visibleContactPhases = visibleContactPhases == null ? List.of() : List.copyOf(visibleContactPhases);
        reason = reason == null ? "" : reason;
        if (!maximumVisibleAtLocation && localVisibility == EclipseVisibilityStatus.VISIBLE && visibleContactPhases.isEmpty()) {
            throw new IllegalArgumentException("visible local eclipse requires a visible maximum or contact phase");
        }
    }

    public static EclipseVisibility visible(boolean maximumVisibleAtLocation, List<EclipseContactPhase> visibleContactPhases, String reason) {
        return new EclipseVisibility(true, EclipseVisibilityStatus.VISIBLE, maximumVisibleAtLocation, visibleContactPhases, reason);
    }

    public static EclipseVisibility notVisible(String reason) {
        return new EclipseVisibility(true, EclipseVisibilityStatus.NOT_VISIBLE, false, List.of(), reason);
    }

    public static EclipseVisibility unknown(boolean globallyOccurs, String reason) {
        return new EclipseVisibility(globallyOccurs, EclipseVisibilityStatus.UNKNOWN, false, List.of(), reason);
    }

    public static EclipseVisibility globalOnly(String reason) {
        return unknown(true, reason);
    }

    public static EclipseVisibility candidateReference(String reason) {
        return unknown(false, reason);
    }
}
