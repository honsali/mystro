package app.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;

class AstroMathTest {

    @Test
    void normalizeWrapsFiniteAngles() {
        assertEquals(0.0, AstroMath.normalize(360.0));
        assertEquals(270.0, AstroMath.normalize(-90.0));
        assertEquals(15.5, AstroMath.normalize(735.5));
    }

    @Test
    void normalizeRejectsNonFiniteAngles() {
        assertThrows(IllegalArgumentException.class, () -> AstroMath.normalize(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> AstroMath.normalize(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> AstroMath.normalize(Double.NEGATIVE_INFINITY));
    }

    @Test
    void signOfRejectsNonFiniteLongitudeRatherThanDefaultingToAries() {
        assertThrows(IllegalArgumentException.class, () -> AstroMath.signOf(Double.NaN));
        assertEquals(ZodiacSign.ARIES, AstroMath.signOf(0.0));
    }

    @Test
    void orientationToSunMarksExactConjunctionAndOppositionAsExact() {
        assertEquals(SolarOrientation.EXACT, AstroMath.orientationToSun(10.0, 10.0));
        assertEquals(SolarOrientation.EXACT, AstroMath.orientationToSun(190.0, 10.0));
        assertEquals(SolarOrientation.EXACT, AstroMath.orientationToSun(9.9999999999, 10.0));
    }

    @Test
    void orientationToSunClassifiesNonBoundaryArcs() {
        assertEquals(SolarOrientation.OCCIDENTAL, AstroMath.orientationToSun(189.999, 10.0));
        assertEquals(SolarOrientation.ORIENTAL, AstroMath.orientationToSun(190.001, 10.0));
        assertEquals(SolarOrientation.ORIENTAL, AstroMath.orientationToSun(9.999, 10.0));
    }
}
