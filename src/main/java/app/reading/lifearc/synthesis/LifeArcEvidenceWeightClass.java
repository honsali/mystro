package app.reading.lifearc.synthesis;

/**
 * Broad source class used to calibrate life-arc synthesis evidence weights.
 *
 * <p>The numeric weight scale is intentionally shared across classes so repeated
 * signs, houses, planets, points, lots, and aspects can be aggregated into one
 * evidence-density table. The class label records where a row's weight policy
 * comes from; it is not an interpretive judgment category.</p>
 */
public enum LifeArcEvidenceWeightClass {
    CHRONOCRATOR,
    RETURN_CHART,
    DIRECTION_CONTACT,
    TRANSIT,
    LUNAR_ECLIPSE
}
