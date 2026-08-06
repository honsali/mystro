package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.data.Angularity;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Element;
import app.chart.data.MoonPhaseName;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.MoonPhase;
import app.chart.model.Chart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.LightDisposition;
import app.reading.description.common.data.RelativeSpeed;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.MoonConfigurationEntry;

class ValensMoonConfigurationCalculatorTest {
    private final ValensMoonConfigurationCalculator calculator = new ValensMoonConfigurationCalculator();

    @Test
    void identifiesLastSeparationNextApplicationAndVoidFalse() {
        Chart chart = chart(40.0, List.of(position(Planet.SUN, 180.0), position(Planet.MERCURY, 250.0), position(Planet.VENUS, 337.0), position(Planet.MARS, 120.0), position(Planet.JUPITER, 260.0), position(Planet.SATURN, 315.0)),
                List.of(relation(Planet.MOON, Planet.JUPITER, AspectType.TRINE, 4, 1.4, AspectMotion.APPLYING)));

        MoonConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(ZodiacSign.TAURUS, entry.sign());
        assertEquals(List.of(DignityType.EXALTATION), entry.dignities());
        assertEquals(MoonPhaseName.CRESCENT_TO_FIRST_QUARTER, entry.phase());
        assertTrue(entry.waxing());
        assertEquals(LightDisposition.INCREASING, entry.lightDisposition());
        assertEquals(RelativeSpeed.SWIFT, entry.motion().relativeSpeed());
        assertEquals(Planet.VENUS, entry.lastSeparation().planet());
        assertEquals(AspectType.SEXTILE, entry.lastSeparation().aspect());
        assertEquals(3.0, entry.lastSeparation().arcDeg());
        assertEquals(Planet.SATURN, entry.nextApplication().planet());
        assertEquals(AspectType.SQUARE, entry.nextApplication().aspect());
        assertEquals(5.0, entry.nextApplication().arcDeg());
        assertFalse(entry.voidOfCourse());
        assertEquals(Planet.JUPITER, entry.configuredPlanets().get(0).planet());
    }

    @Test
    void marksVoidOfCourseWhenNextApplicationFallsAfterSignChange() {
        Chart chart = chart(58.0, List.of(position(Planet.SUN, 170.0), position(Planet.MERCURY, 250.0), position(Planet.VENUS, 140.0), position(Planet.MARS, 210.0), position(Planet.JUPITER, 260.0), position(Planet.SATURN, 335.0)), List.of());

        MoonConfigurationEntry entry = calculator.calculate(chart);

        assertTrue(entry.voidOfCourse());
        assertEquals(Planet.SATURN, entry.nextApplication().planet());
        assertEquals(AspectType.SQUARE, entry.nextApplication().aspect());
        assertEquals(7.0, entry.nextApplication().arcDeg());
    }

    @Test
    void lastAndNextApplicationScanFullZodiac() {
        Chart chart = chart(10.0, List.of(position(Planet.SUN, 0.0), position(Planet.MERCURY, 0.0), position(Planet.VENUS, 0.0), position(Planet.MARS, 0.0), position(Planet.JUPITER, 0.0), position(Planet.SATURN, 0.0)), List.of());

        MoonConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(Planet.SUN, entry.lastSeparation().planet());
        assertEquals(AspectType.CONJUNCTION, entry.lastSeparation().aspect());
        assertEquals(10.0, entry.lastSeparation().arcDeg());
        assertEquals(Planet.SUN, entry.nextApplication().planet());
        assertEquals(AspectType.SEXTILE, entry.nextApplication().aspect());
        assertEquals(50.0, entry.nextApplication().arcDeg());
    }

    @Test
    void partileApplicationHasZeroArc() {
        Chart chart = chart(40.0, List.of(position(Planet.SUN, 180.0), position(Planet.MERCURY, 250.0), position(Planet.VENUS, 120.0), position(Planet.MARS, 210.0), position(Planet.JUPITER, 280.0), position(Planet.SATURN, 315.0)), List.of());

        MoonConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(Planet.JUPITER, entry.nextApplication().planet());
        assertEquals(AspectType.TRINE, entry.nextApplication().aspect());
        assertEquals(0.0, entry.nextApplication().arcDeg());
        assertNull(entry.motion().state());
    }

    private Chart chart(double moonLongitude, List<PlanetPosition> applicationPlanets, List<PairwiseRelation> relations) {
        Chart chart = new Chart();
        PlanetPointEntry moon = point(Planet.MOON, moonLongitude, 1.08, SolarCondition.FREE_OF_SUN, List.of(DignityType.EXALTATION), List.of());
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.MOON, moon);
        points.put(PointKey.SUN, point(Planet.SUN, 180.0, 1.0, null, List.of(), List.of()));
        points.put(PointKey.MERCURY, point(Planet.MERCURY, 250.0, 0.9, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.VENUS, point(Planet.VENUS, 120.0, 1.0, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.MARS, point(Planet.MARS, 210.0, 1.0, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.JUPITER, point(Planet.JUPITER, 260.0, 1.0, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.SATURN, point(Planet.SATURN, 315.0, 1.0, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        chart.setPoints(points);
        chart.setPlanets(withMoon(moonLongitude, applicationPlanets));
        chart.setPairwiseRelations(relations);
        chart.setMoonPhase(new MoonPhase(0.42, MoonPhaseName.CRESCENT_TO_FIRST_QUARTER, true));
        return chart;
    }

    private List<PlanetPosition> withMoon(double moonLongitude, List<PlanetPosition> applicationPlanets) {
        java.util.ArrayList<PlanetPosition> positions = new java.util.ArrayList<>();
        positions.add(position(Planet.MOON, moonLongitude));
        positions.addAll(applicationPlanets);
        return positions;
    }

    private PlanetPointEntry point(Planet planet, double longitude, double speedRatio, SolarCondition solarCondition, List<DignityType> dignities, List<DignityType> debilities) {
        return new PlanetPointEntry(longitude, AstroMath.signOf(longitude), Element.EARTH, AstroMath.degreeInSign(longitude), 0.0, 0.0, 0.0, 0.0, false, speedRatio, 1.0, speedRatio, false, 11, 11, 11, Angularity.SUCCEDENT, 0.0, 0.0, null, null, null, null, null, null, null, null, null, dignities,
                debilities, SolarOrientation.OCCIDENTAL, SectCondition.OF_SECT, solarCondition, List.of(), false, PointType.PLANET);
    }

    private PlanetPosition position(Planet planet, double longitude) {
        return new PlanetPosition(planet, AstroMath.normalize(longitude), AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, false, 1, 1, 1, Angularity.SUCCEDENT, null, 0.0, 0.0, 0.0);
    }

    private PairwiseRelation relation(Planet a, Planet b, AspectType aspect, int signDistance, double orb, AspectMotion motion) {
        return new PairwiseRelation(PointKey.of(a), PointKey.of(b), null, new PairwiseRelation.AspectBySign(aspect, signDistance), new PairwiseRelation.AspectByDegree(aspect, aspect.getExactAngle(), aspect.getExactAngle() + orb, orb, 9.0, motion), List.of());
    }
}
