package app.reading.lifearc.distribution;

import java.util.Arrays;
import java.util.List;

import app.chart.data.Terms;

/**
 * Session 10 design model for extending local/research distributions through bounds.
 *
 * <p>The existing {@link DistributionThroughBoundsCalculator} remains the Ascendant-only baseline.
 * This enum declares the first non-Ascendant directed points that may be implemented in later
 * sessions without turning distributions into an unbounded universal schema.</p>
 *
 * <p>Scope decisions:</p>
 * <ul>
 *   <li>Egyptian terms remain the default bound table.</li>
 *   <li>Current Ascendant distribution output is preserved unchanged.</li>
 *   <li>The first extended slice is selected hyleg, Midheaven, Valens Fortune, Valens Spirit, Sun, and Moon.</li>
 *   <li>Bound traversal is zodiacal; timing arcs use the declared coordinate policy.</li>
 * </ul>
 */
public enum DistributionDirectedPoint {
    ASCENDANT(
            "ASCENDANT",
            DistributionDirectedPointSource.CURRENT_ASCENDANT_BASELINE,
            DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
            true,
            false,
            "Natal Ascendant angle longitude from natalChart.angles.",
            "Keep the current Ascendant distribution table and method id unchanged."),
    SELECTED_HYLEG(
            "HYLEG",
            DistributionDirectedPointSource.PTOLEMAIC_HYLEG_ALCOCODEN,
            DistributionCoordinateMethod.RESOLVED_FROM_SELECTED_HYLEG_POINT,
            false,
            true,
            "Resolved ptolemaicHylegAlcocoden.hyleg from the already enriched natal chart.",
            "Use the hyleg's own stored longitude and label; if the selected hyleg is MIDHEAVEN use right ascension, otherwise use oblique ascension at birth latitude. If no hyleg exists, omit this directed point rather than creating a placeholder."),
    MIDHEAVEN(
            "MIDHEAVEN",
            DistributionDirectedPointSource.NATAL_ANGLE,
            DistributionCoordinateMethod.RIGHT_ASCENSION,
            false,
            true,
            "Natal Midheaven angle longitude from natalChart.angles.",
            "Use right ascension as the normalized meridian coordinate for MC distributions."),
    LOT_FORTUNE(
            "FORTUNE",
            DistributionDirectedPointSource.VALENS_NATAL_LOT,
            DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
            false,
            true,
            "Emitted natalChart.lots entry named FORTUNE from the Valens natal-description apparatus.",
            "Use the emitted Valens Lot of Fortune, not the separate unreversed PTOLEMAIC_FORTUNE fallback used only inside hyleg selection."),
    LOT_SPIRIT(
            "SPIRIT",
            DistributionDirectedPointSource.VALENS_NATAL_LOT,
            DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
            false,
            true,
            "Emitted natalChart.lots entry named SPIRIT from the Valens natal-description apparatus.",
            "Use the emitted Valens Lot of Spirit as a Spirit/vocation/action timing significator."),
    SUN(
            "SUN",
            DistributionDirectedPointSource.NATAL_TRADITIONAL_PLANET,
            DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
            false,
            true,
            "Natal Sun planet longitude from natalChart.points.",
            "Use zodiacal bound traversal from the natal Sun; ecliptic latitude is treated as zero for bound ingress."),
    MOON(
            "MOON",
            DistributionDirectedPointSource.NATAL_TRADITIONAL_PLANET,
            DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
            false,
            true,
            "Natal Moon planet longitude from natalChart.points.",
            "Use zodiacal bound traversal from the natal Moon; ecliptic latitude is treated as zero for bound ingress and no topocentric lunar parallax correction is introduced.");

    public static final Terms DEFAULT_TERMS = Terms.EGYPTIAN;
    public static final String ALTERNATE_TERMS_POLICY = "EGYPTIAN_TERMS_DEFAULT; ALTERNATE_TERM_TABLES_DEFERRED_FOR_EXTENDED_DISTRIBUTIONS";

    private final String outputLabel;
    private final DistributionDirectedPointSource source;
    private final DistributionCoordinateMethod coordinateMethod;
    private final boolean ascendantBaseline;
    private final boolean firstExtendedSlice;
    private final String longitudeSource;
    private final String methodNote;

    DistributionDirectedPoint(String outputLabel,
                              DistributionDirectedPointSource source,
                              DistributionCoordinateMethod coordinateMethod,
                              boolean ascendantBaseline,
                              boolean firstExtendedSlice,
                              String longitudeSource,
                              String methodNote) {
        this.outputLabel = outputLabel;
        this.source = source;
        this.coordinateMethod = coordinateMethod;
        this.ascendantBaseline = ascendantBaseline;
        this.firstExtendedSlice = firstExtendedSlice;
        this.longitudeSource = longitudeSource;
        this.methodNote = methodNote;
    }

    public String outputLabel() {
        return outputLabel;
    }

    public DistributionDirectedPointSource source() {
        return source;
    }

    public DistributionCoordinateMethod coordinateMethod() {
        return coordinateMethod;
    }

    public boolean ascendantBaseline() {
        return ascendantBaseline;
    }

    public boolean firstExtendedSlice() {
        return firstExtendedSlice;
    }

    public String longitudeSource() {
        return longitudeSource;
    }

    public String methodNote() {
        return methodNote;
    }

    public static List<DistributionDirectedPoint> firstExtendedSlicePoints() {
        return Arrays.stream(values())
                .filter(DistributionDirectedPoint::firstExtendedSlice)
                .toList();
    }
}
