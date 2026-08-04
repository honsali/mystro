package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.ZodiacSign;
import app.chart.data.Triplicity;
import app.chart.model.BasicSect;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.data.TriplicityLifePhase;
import app.reading.description.common.data.TriplicityLifeReference;
import app.reading.description.common.data.TriplicityRulerRole;
import app.reading.description.common.data.VitalityYearsTier;
import app.reading.description.common.model.HylegAlcocodenEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.TriplicityLifePhaseEntry;

class ValensTriplicityLifePhaseCalculatorTest {
    private final ValensTriplicityLifePhaseCalculator calculator = new ValensTriplicityLifePhaseCalculator(Triplicity.DOROTHEAN);

    @Test
    void usesReferenceTriplicityRulersForEarlyMiddleAndLateLife() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 100.0, ZodiacSign.CANCER, 4),
                position(Planet.MOON, 190.0, ZodiacSign.LIBRA, 8),
                position(Planet.MERCURY, 100.0, ZodiacSign.CANCER, 5),
                position(Planet.VENUS, 280.0, ZodiacSign.CAPRICORN, 11),
                position(Planet.MARS, 40.0, ZodiacSign.TAURUS, 3),
                position(Planet.JUPITER, 20.0, ZodiacSign.ARIES, 2),
                position(Planet.SATURN, 110.0, ZodiacSign.CANCER, 5)
        ));
        chart.setPoints(Map.of(
                PointKey.of(Planet.MERCURY), point(SolarCondition.FREE_OF_SUN, List.of(), List.of()),
                PointKey.of(Planet.SATURN), point(SolarCondition.UNDER_BEAMS, List.of(), List.of(DignityType.DETRIMENT)),
                PointKey.of(Planet.JUPITER), point(SolarCondition.FREE_OF_SUN, List.of(DignityType.TRIPLICITY), List.of())
        ));
        chart.setLots(List.of(new LotEntry("FORTUNE", "Fortune", "valens", 250.0, ZodiacSign.SAGITTARIUS, 10.0, 10, Planet.JUPITER, "test")));
        chart.setSect(new BasicSect(Sect.NOCTURNAL, Planet.MOON, Planet.SUN, Planet.VENUS, Planet.JUPITER, Planet.MARS, Planet.SATURN, false, true, -1.0, 1.0, Map.of()));

        List<TriplicityLifePhaseEntry> phases = calculator.calculate(chart);

        assertEquals(12, phases.size());
        assertPhase(phases.get(0), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.EARLY_LIFE, null, null, TriplicityRulerRole.PRIMARY_RULER, Planet.MERCURY, SolarCondition.FREE_OF_SUN);
        assertPhase(phases.get(1), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.MIDDLE_LIFE, null, null, TriplicityRulerRole.SECONDARY_RULER, Planet.SATURN, SolarCondition.UNDER_BEAMS);
        assertPhase(phases.get(2), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.LATE_LIFE, null, null, TriplicityRulerRole.PARTICIPATING_RULER, Planet.JUPITER, SolarCondition.FREE_OF_SUN);
        assertEquals(List.of(DignityType.DETRIMENT), phases.get(1).rulerDebilities());
        assertEquals(List.of(DignityType.TRIPLICITY), phases.get(2).rulerDignities());

        assertPhase(phases.get(3), TriplicityLifeReference.SUN, "SUN", ZodiacSign.CANCER, TriplicityLifePhase.EARLY_LIFE, null, null, TriplicityRulerRole.PRIMARY_RULER, Planet.MARS, null);
        assertPhase(phases.get(9), TriplicityLifeReference.LOT_FORTUNE, "FORTUNE", ZodiacSign.SAGITTARIUS, TriplicityLifePhase.EARLY_LIFE, null, null, TriplicityRulerRole.PRIMARY_RULER, Planet.JUPITER, SolarCondition.FREE_OF_SUN);
    }

    @Test
    void partitionsVitalityYearsIntoTriplicityPhaseAgeRanges() {
        NatalChart chart = chartWithVitalityYears(90.0);

        List<TriplicityLifePhaseEntry> phases = calculator.calculate(chart);

        assertPhase(phases.get(0), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.EARLY_LIFE, 0.0, 30.0, TriplicityRulerRole.PRIMARY_RULER, Planet.MERCURY, SolarCondition.FREE_OF_SUN);
        assertPhase(phases.get(1), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.MIDDLE_LIFE, 30.0, 60.0, TriplicityRulerRole.SECONDARY_RULER, Planet.SATURN, SolarCondition.UNDER_BEAMS);
        assertPhase(phases.get(2), TriplicityLifeReference.LIGHT_OF_SECT, "MOON", ZodiacSign.LIBRA, TriplicityLifePhase.LATE_LIFE, 60.0, 90.0, TriplicityRulerRole.PARTICIPATING_RULER, Planet.JUPITER, SolarCondition.FREE_OF_SUN);
    }

    private void assertPhase(TriplicityLifePhaseEntry entry, TriplicityLifeReference reference, String referenceName, ZodiacSign referenceSign, TriplicityLifePhase phase, Double startAgeYears, Double endAgeYears, TriplicityRulerRole role, Planet ruler, SolarCondition solarCondition) {
        assertEquals(reference, entry.reference());
        assertEquals(referenceName, entry.referenceName());
        assertEquals(referenceSign, entry.referenceSign());
        assertEquals(phase, entry.phase());
        if (startAgeYears == null) {
            assertNull(entry.startAgeYears());
            assertNull(entry.endAgeYears());
        } else {
            assertEquals(startAgeYears, entry.startAgeYears());
            assertEquals(endAgeYears, entry.endAgeYears());
        }
        assertEquals(role, entry.role());
        assertEquals(ruler, entry.ruler());
        assertEquals(solarCondition, entry.rulerSolarCondition());
    }

    private NatalChart chartWithVitalityYears(double indicatedYears) {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 100.0, ZodiacSign.CANCER, 4),
                position(Planet.MOON, 190.0, ZodiacSign.LIBRA, 8),
                position(Planet.MERCURY, 100.0, ZodiacSign.CANCER, 5),
                position(Planet.VENUS, 280.0, ZodiacSign.CAPRICORN, 11),
                position(Planet.MARS, 40.0, ZodiacSign.TAURUS, 3),
                position(Planet.JUPITER, 20.0, ZodiacSign.ARIES, 2),
                position(Planet.SATURN, 110.0, ZodiacSign.CANCER, 5)
        ));
        chart.setPoints(Map.of(
                PointKey.of(Planet.MERCURY), point(SolarCondition.FREE_OF_SUN, List.of(), List.of()),
                PointKey.of(Planet.SATURN), point(SolarCondition.UNDER_BEAMS, List.of(), List.of(DignityType.DETRIMENT)),
                PointKey.of(Planet.JUPITER), point(SolarCondition.FREE_OF_SUN, List.of(DignityType.TRIPLICITY), List.of())
        ));
        chart.setLots(List.of(new LotEntry("FORTUNE", "Fortune", "valens", 250.0, ZodiacSign.SAGITTARIUS, 10.0, 10, Planet.JUPITER, "test")));
        chart.setSect(new BasicSect(Sect.NOCTURNAL, Planet.MOON, Planet.SUN, Planet.VENUS, Planet.JUPITER, Planet.MARS, Planet.SATURN, false, true, -1.0, 1.0, Map.of()));
        chart.setPtolemaicHylegAlcocoden(new HylegAlcocodenEntry(
                "ptolemaic",
                "test",
                null,
                null,
                new HylegAlcocodenEntry.VitalityYearsIndicator(Planet.VENUS, Angularity.SUCCEDENT, indicatedYears, VitalityYearsTier.MEAN, List.of(), indicatedYears, "test"),
                List.of()
        ));
        return chart;
    }

    private PlanetPosition position(Planet planet, double longitude, ZodiacSign sign, int wholeSignHouse) {
        return new PlanetPosition(planet, longitude, sign, longitude % 30.0, 0.0, 0.0, 0.0, 0.0, false,
                1.0, 1.0, 1.0, false, wholeSignHouse, wholeSignHouse, wholeSignHouse, Angularity.SUCCEDENT,
                null, 0.0, 0.0, 0.0);
    }

    private PointEntry point(SolarCondition solarCondition, List<DignityType> dignities, List<DignityType> debilities) {
        return new PlanetPointEntry(0.0, ZodiacSign.ARIES, null, 0.0, 0.0, 0.0, 0.0, 0.0, false, 0.0, 0.0, 0.0,
                false, 1, 1, 1, Angularity.SUCCEDENT, 0.0, 0.0, null, null, null, null, null, null, null, null,
                null, dignities, debilities, null, null, solarCondition, List.of(), false, PointType.PLANET);
    }
}
