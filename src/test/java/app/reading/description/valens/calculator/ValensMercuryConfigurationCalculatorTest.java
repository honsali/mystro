package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.Angularity;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Element;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.Chart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.MorningEveningStar;
import app.reading.description.common.data.PlanetMotionState;
import app.reading.description.common.data.RelativeSpeed;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.MercuryConfigurationEntry;

class ValensMercuryConfigurationCalculatorTest {
    private final ValensMercuryConfigurationCalculator calculator = new ValensMercuryConfigurationCalculator();

    @Test
    void calculatesOrientalMorningMercuryAndMoonRelation() {
        Chart chart = chart(
                point(Planet.SUN, 40.0, ZodiacSign.TAURUS, 1.0, 1.0, false, SolarOrientation.ORIENTAL, null, List.of(), List.of()),
                point(Planet.MOON, 300.0, ZodiacSign.AQUARIUS, 13.0, 1.0, false, SolarOrientation.OCCIDENTAL, null, List.of(), List.of()),
                point(Planet.MERCURY, 70.0, ZodiacSign.GEMINI, 1.2, 1.2, false, SolarOrientation.ORIENTAL, SolarCondition.FREE_OF_SUN, List.of(DignityType.DOMICILE), List.of()),
                List.of(
                        relation(Planet.MERCURY, Planet.MOON, AspectType.TRINE, 4, 2.31, AspectMotion.APPLYING, List.of(DignityType.TERM)),
                        relation(Planet.MERCURY, Planet.SATURN, AspectType.SQUARE, 3, 4.1, AspectMotion.SEPARATING, List.of())
                ));

        MercuryConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(ZodiacSign.GEMINI, entry.sign());
        assertEquals(List.of(DignityType.DOMICILE), entry.dignities());
        assertEquals(SolarOrientation.ORIENTAL, entry.solarPhase());
        assertEquals(MorningEveningStar.MORNING, entry.morningOrEveningStar());
        assertEquals(30.0, entry.arcFromSun());
        assertEquals(SolarCondition.FREE_OF_SUN, entry.solarCondition());
        assertEquals(PlanetMotionState.DIRECT, entry.motion().state());
        assertEquals(RelativeSpeed.SWIFT, entry.motion().relativeSpeed());
        assertFalse(entry.joinedToSun());
        assertTrue(entry.regardsMoon());
        assertNull(entry.aversionToMoon());
        assertEquals(AspectType.TRINE, entry.moonRelation().aspect());
        assertEquals(2.31, entry.moonRelation().byDegreeOrb());
        assertEquals(Planet.SATURN, entry.configuredPlanets().get(0).planet());
        assertEquals(Planet.MOON, entry.configuredPlanets().get(1).planet());
    }

    @Test
    void marksOccidentalEveningAndAversionToMoon() {
        Chart chart = chart(
                point(Planet.SUN, 100.0, ZodiacSign.CANCER, 1.0, 1.0, false, SolarOrientation.ORIENTAL, null, List.of(), List.of()),
                point(Planet.MOON, 30.0, ZodiacSign.TAURUS, 13.0, 1.0, false, SolarOrientation.OCCIDENTAL, null, List.of(), List.of()),
                point(Planet.MERCURY, 0.0, ZodiacSign.ARIES, 0.7, 0.8, false, SolarOrientation.OCCIDENTAL, SolarCondition.COMBUST, List.of(), List.of(DignityType.FALL)),
                List.of());

        MercuryConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(MorningEveningStar.EVENING, entry.morningOrEveningStar());
        assertEquals(SolarCondition.COMBUST, entry.solarCondition());
        assertEquals(RelativeSpeed.SLOW, entry.motion().relativeSpeed());
        assertFalse(entry.regardsMoon());
        assertTrue(entry.aversionToMoon());
        assertNull(entry.moonRelation());
    }

    @Test
    void stationaryCazimiMercuryJoinedToSunTakesStationaryPrecedence() {
        Chart chart = chart(
                point(Planet.SUN, 5.0, ZodiacSign.ARIES, 1.0, 1.0, false, SolarOrientation.ORIENTAL, null, List.of(), List.of()),
                point(Planet.MOON, 185.0, ZodiacSign.LIBRA, 13.0, 1.0, false, SolarOrientation.OCCIDENTAL, null, List.of(), List.of()),
                point(Planet.MERCURY, 5.2, ZodiacSign.ARIES, 0.04, 0.03, true, SolarOrientation.ORIENTAL, SolarCondition.CAZIMI, List.of(), List.of()),
                List.of(relation(Planet.MERCURY, Planet.MOON, AspectType.OPPOSITION, 6, 0.2, AspectMotion.EXACT, List.of())));

        MercuryConfigurationEntry entry = calculator.calculate(chart);

        assertEquals(PlanetMotionState.STATIONARY, entry.motion().state());
        assertEquals(0.05, entry.motion().stationaryThresholdDegPerDay());
        assertEquals(SolarCondition.CAZIMI, entry.solarCondition());
        assertTrue(entry.joinedToSun());
    }

    private Chart chart(PlanetPointEntry sun, PlanetPointEntry moon, PlanetPointEntry mercury, List<PairwiseRelation> relations) {
        Chart chart = new Chart();
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.SUN, sun);
        points.put(PointKey.MOON, moon);
        points.put(PointKey.MERCURY, mercury);
        points.put(PointKey.VENUS, point(Planet.VENUS, 130.0, ZodiacSign.LEO, 1.0, 1.0, false, SolarOrientation.ORIENTAL, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.MARS, point(Planet.MARS, 210.0, ZodiacSign.SCORPIO, 1.0, 1.0, false, SolarOrientation.ORIENTAL, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.JUPITER, point(Planet.JUPITER, 250.0, ZodiacSign.SAGITTARIUS, 1.0, 1.0, false, SolarOrientation.ORIENTAL, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        points.put(PointKey.SATURN, point(Planet.SATURN, 160.0, ZodiacSign.VIRGO, 1.0, 1.0, false, SolarOrientation.ORIENTAL, SolarCondition.FREE_OF_SUN, List.of(), List.of()));
        chart.setPoints(points);
        chart.setPairwiseRelations(relations);
        return chart;
    }

    private PlanetPointEntry point(Planet planet, double longitude, ZodiacSign sign, double speed, double speedRatio, boolean retrograde,
                                   SolarOrientation solarPhase, SolarCondition solarCondition, List<DignityType> dignities, List<DignityType> debilities) {
        return new PlanetPointEntry(
                longitude,
                sign,
                Element.FIRE,
                longitude % 30.0,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                speed,
                1.0,
                speedRatio,
                retrograde,
                3,
                3,
                3,
                Angularity.SUCCEDENT,
                0.0,
                0.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                dignities,
                debilities,
                solarPhase,
                SectCondition.OF_SECT,
                solarCondition,
                List.of(),
                false,
                PointType.PLANET
        );
    }

    private PairwiseRelation relation(Planet a, Planet b, AspectType aspect, int signDistance, double orb, AspectMotion motion, List<DignityType> reception) {
        return new PairwiseRelation(
                PointKey.of(a),
                PointKey.of(b),
                null,
                new PairwiseRelation.AspectBySign(aspect, signDistance),
                new PairwiseRelation.AspectByDegree(aspect, aspect.getExactAngle(), aspect.getExactAngle() + orb, orb, 9.0, motion),
                reception
        );
    }
}
