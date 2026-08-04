package app.reading.lifearc.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.data.Terms;

class DistributionDirectedPointTest {
    @Test
    void firstExtendedSliceIsHylegMcFortuneSpiritSunAndMoon() {
        assertEquals(List.of(
                DistributionDirectedPoint.SELECTED_HYLEG,
                DistributionDirectedPoint.MIDHEAVEN,
                DistributionDirectedPoint.LOT_FORTUNE,
                DistributionDirectedPoint.LOT_SPIRIT,
                DistributionDirectedPoint.SUN,
                DistributionDirectedPoint.MOON
        ), DistributionDirectedPoint.firstExtendedSlicePoints());
    }

    @Test
    void coordinatePoliciesPreserveAscendantAndDocumentNonAscendantChoices() {
        assertTrue(DistributionDirectedPoint.ASCENDANT.ascendantBaseline());
        assertFalse(DistributionDirectedPoint.ASCENDANT.firstExtendedSlice());
        assertEquals(DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                DistributionDirectedPoint.ASCENDANT.coordinateMethod());

        assertEquals(DistributionCoordinateMethod.RESOLVED_FROM_SELECTED_HYLEG_POINT,
                DistributionDirectedPoint.SELECTED_HYLEG.coordinateMethod());
        assertEquals(DistributionCoordinateMethod.RIGHT_ASCENSION,
                DistributionDirectedPoint.MIDHEAVEN.coordinateMethod());
        assertEquals(DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                DistributionDirectedPoint.LOT_FORTUNE.coordinateMethod());
        assertEquals(DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                DistributionDirectedPoint.LOT_SPIRIT.coordinateMethod());
        assertEquals(DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                DistributionDirectedPoint.SUN.coordinateMethod());
        assertEquals(DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                DistributionDirectedPoint.MOON.coordinateMethod());
    }

    @Test
    void egyptianTermsRemainDefaultAndAlternateTermsAreDeferred() {
        assertEquals(Terms.EGYPTIAN, DistributionDirectedPoint.DEFAULT_TERMS);
        assertTrue(DistributionDirectedPoint.ALTERNATE_TERMS_POLICY.contains("EGYPTIAN_TERMS_DEFAULT"));
        assertTrue(DistributionDirectedPoint.ALTERNATE_TERMS_POLICY.contains("DEFERRED"));
    }
}
