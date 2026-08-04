package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.Angularity;
import app.chart.data.AspectType;
import app.chart.data.Element;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.BasicSect;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;
import app.chart.model.PointEntry;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.DoryphoryDirection;
import app.reading.description.common.data.DoryphoryKind;
import app.reading.description.common.model.DoryphoryEntry;

class ValensDoryphoryCalculatorTest {
    private final ValensDoryphoryCalculator calculator = new ValensDoryphoryCalculator();

    @Test
    void findsOfSectPlanetsConfiguredToBothLuminariesByWholeSignAspect() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 130.0, ZodiacSign.LEO, 5),
                position(Planet.MOON, 190.0, ZodiacSign.LIBRA, 8),
                position(Planet.MERCURY, 130.0, ZodiacSign.LEO, 5),
                position(Planet.VENUS, 280.0, ZodiacSign.CAPRICORN, 11),
                position(Planet.MARS, 40.0, ZodiacSign.TAURUS, 3),
                position(Planet.JUPITER, 20.0, ZodiacSign.ARIES, 1),
                position(Planet.SATURN, 140.0, ZodiacSign.LEO, 5)
        ));
        chart.setSect(new BasicSect(
                Sect.NOCTURNAL,
                Planet.MOON,
                Planet.SUN,
                Planet.VENUS,
                Planet.JUPITER,
                Planet.MARS,
                Planet.SATURN,
                false,
                true,
                -1.0,
                1.0,
                Map.of(
                        Planet.MERCURY, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.VENUS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.OF_SECT),
                        Planet.MARS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.OF_SECT),
                        Planet.JUPITER, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.SATURN, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT)
                )
        ));

        List<DoryphoryEntry> doryphories = calculator.calculate(chart);

        assertEquals(2, doryphories.size());
        DoryphoryEntry sectLightEntry = doryphories.get(0);
        assertEquals(Planet.MOON, sectLightEntry.light());
        assertTrue(sectLightEntry.lightOfSect());
        assertEquals(Planet.VENUS, sectLightEntry.spearBearer());
        assertEquals(DoryphoryKind.BY_PHASE, sectLightEntry.kind());
        assertEquals(List.of(DoryphoryKind.BY_PHASE, DoryphoryKind.BY_CONFIGURATION), sectLightEntry.kinds());
        assertEquals(3, sectLightEntry.strengthScore());
        assertEquals(DoryphoryDirection.TRAILING, sectLightEntry.direction());
        assertEquals(AspectType.SQUARE, sectLightEntry.aspect());
        assertEquals(3, sectLightEntry.signDistance());
        assertEquals(8, sectLightEntry.lightHouse());
        assertEquals(11, sectLightEntry.spearBearerHouse());

        DoryphoryEntry contraryLightEntry = doryphories.get(1);
        assertEquals(Planet.SUN, contraryLightEntry.light());
        assertEquals(Planet.MARS, contraryLightEntry.spearBearer());
        assertEquals(DoryphoryKind.BY_OVERCOMING, contraryLightEntry.kind());
        assertEquals(List.of(DoryphoryKind.BY_OVERCOMING, DoryphoryKind.BY_CONFIGURATION), contraryLightEntry.kinds());
        assertEquals(5, contraryLightEntry.strengthScore());
        assertEquals(DoryphoryDirection.LEADING, contraryLightEntry.direction());
        assertEquals(AspectType.SQUARE, contraryLightEntry.aspect());
    }

    @Test
    void includesSameSignBodyguardUnlessTooCloseToSun() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 100.0, ZodiacSign.CANCER, 4),
                position(Planet.MOON, 190.0, ZodiacSign.LIBRA, 8),
                position(Planet.MERCURY, 112.0, ZodiacSign.CANCER, 4),
                position(Planet.VENUS, 280.0, ZodiacSign.CAPRICORN, 11),
                position(Planet.MARS, 106.0, ZodiacSign.CANCER, 4),
                position(Planet.JUPITER, 320.0, ZodiacSign.AQUARIUS, 12),
                position(Planet.SATURN, 110.0, ZodiacSign.CANCER, 4)
        ));
        chart.setSect(new BasicSect(
                Sect.DIURNAL,
                Planet.SUN,
                Planet.MOON,
                Planet.JUPITER,
                Planet.VENUS,
                Planet.SATURN,
                Planet.MARS,
                true,
                false,
                1.0,
                -1.0,
                Map.of(
                        Planet.MERCURY, new PlanetSectInfo(Sect.DIURNAL, SectCondition.OF_SECT),
                        Planet.VENUS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.MARS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.JUPITER, new PlanetSectInfo(Sect.DIURNAL, SectCondition.OF_SECT),
                        Planet.SATURN, new PlanetSectInfo(Sect.DIURNAL, SectCondition.OF_SECT)
                )
        ));

        List<DoryphoryEntry> sunDoryphories = calculator.calculate(chart).stream()
                .filter(entry -> entry.light() == Planet.SUN)
                .toList();

        assertEquals(List.of(Planet.MERCURY, Planet.SATURN), sunDoryphories.stream().map(DoryphoryEntry::spearBearer).toList());
        assertEquals(AspectType.CONJUNCTION, sunDoryphories.get(0).aspect());
        assertEquals(List.of(DoryphoryKind.BY_PHASE, DoryphoryKind.BY_CONFIGURATION), sunDoryphories.get(0).kinds());
        assertEquals(DoryphoryDirection.TRAILING, sunDoryphories.get(0).direction());
    }

    @Test
    void aggregatesOvercomingDignityPhaseAndConfigurationKinds() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 10.0, ZodiacSign.ARIES, 1),
                position(Planet.MOON, 100.0, ZodiacSign.CANCER, 4),
                position(Planet.MERCURY, 70.0, ZodiacSign.GEMINI, 3),
                position(Planet.VENUS, 150.0, ZodiacSign.VIRGO, 6),
                position(Planet.MARS, 280.0, ZodiacSign.CAPRICORN, 10),
                position(Planet.JUPITER, 220.0, ZodiacSign.SCORPIO, 8),
                position(Planet.SATURN, 50.0, ZodiacSign.TAURUS, 2)
        ));
        chart.setSect(new BasicSect(
                Sect.DIURNAL,
                Planet.SUN,
                Planet.MOON,
                Planet.JUPITER,
                Planet.VENUS,
                Planet.SATURN,
                Planet.MARS,
                true,
                false,
                1.0,
                -1.0,
                Map.of(
                        Planet.MERCURY, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.VENUS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.MARS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.OF_SECT),
                        Planet.JUPITER, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT),
                        Planet.SATURN, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT)
                )
        ));
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.MARS, planetPoint(280.0, ZodiacSign.CAPRICORN, SolarOrientation.ORIENTAL, List.of(DignityType.EXALTATION)));
        chart.setPoints(points);

        DoryphoryEntry mars = calculator.calculate(chart).stream()
                .filter(entry -> entry.light() == Planet.SUN && entry.spearBearer() == Planet.MARS)
                .findFirst()
                .orElseThrow();

        assertEquals(DoryphoryKind.BY_OVERCOMING, mars.kind());
        assertEquals(List.of(DoryphoryKind.BY_OVERCOMING, DoryphoryKind.BY_DIGNITY, DoryphoryKind.BY_PHASE, DoryphoryKind.BY_CONFIGURATION), mars.kinds());
        assertEquals(List.of(DignityType.EXALTATION), mars.qualifyingDignities());
        assertEquals(10, mars.strengthScore());
        assertEquals(9, mars.signDistance());
    }

    @Test
    void excludesOutOfSectCandidatesEvenWhenOtherwiseQualified() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 10.0, ZodiacSign.ARIES, 1),
                position(Planet.MOON, 100.0, ZodiacSign.CANCER, 4),
                position(Planet.MERCURY, 70.0, ZodiacSign.GEMINI, 3),
                position(Planet.VENUS, 150.0, ZodiacSign.VIRGO, 6),
                position(Planet.MARS, 280.0, ZodiacSign.CAPRICORN, 10),
                position(Planet.JUPITER, 220.0, ZodiacSign.SCORPIO, 8),
                position(Planet.SATURN, 50.0, ZodiacSign.TAURUS, 2)
        ));
        chart.setSect(new BasicSect(
                Sect.DIURNAL,
                Planet.SUN,
                Planet.MOON,
                Planet.JUPITER,
                Planet.VENUS,
                Planet.SATURN,
                Planet.MARS,
                true,
                false,
                1.0,
                -1.0,
                Map.of(
                        Planet.MARS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.CONTRARY_TO_SECT)
                )
        ));
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.MARS, planetPoint(280.0, ZodiacSign.CAPRICORN, SolarOrientation.ORIENTAL, List.of(DignityType.EXALTATION)));
        chart.setPoints(points);

        assertTrue(calculator.calculate(chart).stream().noneMatch(entry -> entry.spearBearer() == Planet.MARS));
    }

    private PlanetPosition position(Planet planet, double longitude, ZodiacSign sign, int wholeSignHouse) {
        return new PlanetPosition(planet, longitude, sign, longitude % 30.0, 0.0, 0.0, 0.0, 0.0, false,
                1.0, 1.0, 1.0, false, wholeSignHouse, wholeSignHouse, wholeSignHouse, Angularity.SUCCEDENT,
                null, 0.0, 0.0, 0.0);
    }

    private PlanetPointEntry planetPoint(double longitude, ZodiacSign sign, SolarOrientation solarOrientation, List<DignityType> dignities) {
        return new PlanetPointEntry(
                longitude,
                sign,
                Element.EARTH,
                longitude % 30.0,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                1.0,
                1.0,
                1.0,
                false,
                10,
                10,
                10,
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
                List.of(),
                solarOrientation,
                SectCondition.OF_SECT,
                null,
                List.of(),
                false,
                PointType.PLANET
        );
    }
}
