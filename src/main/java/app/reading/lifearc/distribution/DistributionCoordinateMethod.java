package app.reading.lifearc.distribution;

/**
 * Timing-coordinate policy for local/research distributions through bounds.
 *
 * <p>Distribution bounds themselves remain zodiacal Egyptian term segments. The coordinate method
 * says how the directed point's zodiacal movement through those segments is converted into timing
 * arcs for the local research table.</p>
 */
public enum DistributionCoordinateMethod {
    OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE(
            "Use oblique ascension at the native's birth latitude; ecliptic latitude is treated as zero for bound traversal."),
    RIGHT_ASCENSION(
            "Use right ascension for the meridian-directed point; ecliptic latitude is treated as zero for bound traversal."),
    RESOLVED_FROM_SELECTED_HYLEG_POINT(
            "Resolve the selected Ptolemaic hyleg first, then use MIDHEAVEN=RIGHT_ASCENSION and all other hyleg points=OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE.");

    private final String description;

    DistributionCoordinateMethod(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
