package app.chart.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
import org.junit.jupiter.api.Test;
import app.chart.calculator.ChartPointCalculator;
import app.chart.data.Angularity;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.DignityType;

class AspectMotionCalculatorTest {

    private final ChartPointCalculator calculator = new ChartPointCalculator();

    @Test
    void applyingWhenFutureOrbDecreases() {
        PairwiseRelation.AspectByDegree aspect = calculator.aspectByDegreeForTest(PointKey.MERCURY, PointKey.MARS, position(PointKey.MERCURY, 59.8, 1.2), position(PointKey.MARS, 0.0, 0.4), 59.8, 7.25);

        assertEquals(AspectType.SEXTILE, aspect.getNearestAspect());
        assertEquals(AspectMotion.APPLYING, aspect.getAspectMotion());
    }

    @Test
    void separatingWhenFutureOrbIncreases() {
        PairwiseRelation.AspectByDegree aspect = calculator.aspectByDegreeForTest(PointKey.VENUS, PointKey.MARS, position(PointKey.VENUS, 114.1, -0.1), position(PointKey.MARS, 0.0, 0.4), 114.1, 7.25);

        assertEquals(AspectType.TRINE, aspect.getNearestAspect());
        assertEquals(AspectMotion.SEPARATING, aspect.getAspectMotion());
    }

    @Test
    void exactWhenCurrentOrbIsInsideTolerance() {
        PairwiseRelation.AspectByDegree aspect = calculator.aspectByDegreeForTest(PointKey.JUPITER, PointKey.SATURN, position(PointKey.JUPITER, 90.0002, 0.01), position(PointKey.SATURN, 0.0, 0.01), 90.0002, 9.0);

        assertEquals(AspectType.SQUARE, aspect.getNearestAspect());
        assertEquals(AspectMotion.EXACT, aspect.getAspectMotion());
    }

    @Test
    void classifiesAcrossCircularWrap() {
        PairwiseRelation.AspectByDegree aspect = calculator.aspectByDegreeForTest(PointKey.SUN, PointKey.MOON, position(PointKey.SUN, 359.9, 0.96), position(PointKey.MOON, 0.1, 14.0), 0.2, 13.5);

        assertEquals(AspectType.CONJUNCTION, aspect.getNearestAspect());
        assertEquals(AspectMotion.SEPARATING, aspect.getAspectMotion());
    }

    @Test
    void unclassifiableWhenNoPlanetMovementData() {
        PairwiseRelation.AspectByDegree aspect = calculator.aspectByDegreeForTest(PointKey.SUN, PointKey.ASCENDANT, position(PointKey.SUN, 119.9, 0.96), null, 119.9, 7.5);

        assertEquals(AspectType.TRINE, aspect.getNearestAspect());
        assertNull(aspect.getAspectMotion());
    }

    @Test
    void mutualReceptionDetectsDomicileAndFace() {
        List<DignityType> mutualReception = calculator.mutualReceptionForTest(PointKey.MERCURY, PointKey.JUPITER, planetPointEntry(Planet.MERCURY, Planet.JUPITER, null, null, Planet.JUPITER), planetPointEntry(Planet.JUPITER, Planet.MERCURY, null, null, Planet.MERCURY));

        assertEquals(List.of(DignityType.DOMICILE, DignityType.FACE), mutualReception);
    }

    @Test
    void mutualReceptionEmptyForUnsupportedPair() {
        List<DignityType> mutualReception = calculator.mutualReceptionForTest(PointKey.NORTH_NODE, PointKey.MERCURY, planetPointEntry(Planet.NORTH_NODE, Planet.MERCURY, null, null, null), planetPointEntry(Planet.MERCURY, Planet.NORTH_NODE, null, null, null));

        assertEquals(List.of(), mutualReception);
    }

    private PlanetPosition position(PointKey key, double longitude, double speed) {
        Planet planet = toPlanet(key);
        ZodiacSign sign = app.chart.AstroMath.signOf(longitude);
        double degreeInSign = app.chart.AstroMath.degreeInSign(longitude);
        double latitude = 0.0;
        double rightAscension = 0.0;
        double declination = 0.0;
        double altitude = 0.0;
        boolean aboveHorizon = false;
        double meanDailySpeed = Math.abs(speed);
        double speedRatio = 0.0;
        boolean retrograde = speed < 0;
        int house = 1;
        int wholeSignHouse = 1;
        Integer quadrantHouse = null;
        Angularity angularity = null;
        Planet termRuler = null;
        double angularDistanceFromSun = 0.0;
        double antisciaLongitude = 0.0;
        double contraAntisciaLongitude = 0.0;

        return new PlanetPosition(planet, longitude, sign, degreeInSign, latitude, rightAscension, declination, altitude, aboveHorizon, speed, meanDailySpeed, speedRatio, retrograde, house, wholeSignHouse, quadrantHouse, angularity, termRuler, angularDistanceFromSun, antisciaLongitude,
                contraAntisciaLongitude);
    }

    private PlanetPointEntry planetPointEntry(Planet pointPlanet, Planet domicileRuler, Planet exaltationRuler, Planet activeMasterTriplicityRuler, Planet faceRuler) {
        return new PlanetPointEntry(0.0, ZodiacSign.ARIES, app.chart.data.Element.FIRE, 0.0, 0.0, 0.0, 0.0, 0.0, false, 0.0, 0.0, 0.0, false, 1, 1, null, null, 0.0, 0.0, domicileRuler, exaltationRuler, activeMasterTriplicityRuler, null, null, null, faceRuler, null, null, List.of(), List.of(), null,
                null, null, List.of(), false, pointPlanet == Planet.NORTH_NODE || pointPlanet == Planet.SOUTH_NODE ? PointType.NODE : PointType.PLANET);
    }

    private Planet toPlanet(PointKey key) {
        return switch (key) {
            case SUN -> Planet.SUN;
            case MOON -> Planet.MOON;
            case MERCURY -> Planet.MERCURY;
            case VENUS -> Planet.VENUS;
            case MARS -> Planet.MARS;
            case JUPITER -> Planet.JUPITER;
            case SATURN -> Planet.SATURN;
            case NORTH_NODE -> Planet.NORTH_NODE;
            case SOUTH_NODE -> Planet.SOUTH_NODE;
            default -> throw new IllegalArgumentException("Unsupported planet mapping in test: " + key);
        };
    }
}
