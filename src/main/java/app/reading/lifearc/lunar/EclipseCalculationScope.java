package app.reading.lifearc.lunar;

/**
 * Scope of an eclipse row in the local/research lunar-timing model.
 */
public enum EclipseCalculationScope {
    /** Mean-node proximity only; current fallback/reference candidate rows. */
    MEAN_NODE_CANDIDATE_REFERENCE,
    /** True eclipse exists globally according to Swiss Ephemeris. */
    GLOBAL_ECLIPSE_REALITY,
    /** Location-aware visibility calculation for the subject location. */
    LOCAL_VISIBILITY
}
