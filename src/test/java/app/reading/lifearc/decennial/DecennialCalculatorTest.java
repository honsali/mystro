package app.reading.lifearc.decennial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.TraditionalTables;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.ZodiacSign;
import app.chart.model.BasicSect;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;

class DecennialCalculatorTest {

    private final DecennialCalculator calculator = new DecennialCalculator();

    @Test
    void nocturnalChartStartsWithMoonAndMarksActiveJupiterPeriod() {
        Subject subject = subject();

        DecennialTable table = calculator.calculateTable(subject, chart(Sect.NOCTURNAL), LocalDate.of(2026, 1, 1), 0, 100);

        assertEquals(DecennialCalculator.METHOD_ID, table.methodId());
        assertEquals(Sect.NOCTURNAL, table.natalSect());
        assertEquals(List.of(Planet.MOON, Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN, Planet.VENUS, Planet.MERCURY), table.rulerSequence());

        DecennialPeriod first = table.periods().get(0);
        assertEquals(Planet.MOON, first.ruler());
        assertEquals(subject.getLocalBirthDateTime(), first.startDateTime());
        assertEquals(subject.getLocalBirthDateTime().plusYears(10), first.endDateTimeExclusive());
        assertEquals(List.of(Planet.MOON, Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN, Planet.VENUS, Planet.MERCURY),
                first.subperiods().stream().map(DecennialSubperiod::partner).toList());
        assertEquals(List.of(4), first.rulerNatalCondition().ruledNatalHouses());

        DecennialPeriod active = table.periods().stream()
                .filter(DecennialPeriod::activeForInquiry)
                .findFirst()
                .orElseThrow();
        assertEquals(Planet.JUPITER, active.ruler());
        assertEquals(20, active.startAgeYears());
        assertEquals(30, active.endAgeYearsExclusive());
        assertEquals(1, active.subperiods().stream().filter(DecennialSubperiod::activeForInquiry).count());
        assertEquals(Planet.MERCURY, active.subperiods().stream()
                .filter(DecennialSubperiod::activeForInquiry)
                .findFirst()
                .orElseThrow()
                .partner());
    }

    @Test
    void diurnalChartStartsWithSunAndRepeatsAfterSeventyYears() {
        Subject subject = subject();

        DecennialTable table = calculator.calculateTable(subject, chart(Sect.DIURNAL), null, 0, 100);

        assertEquals(List.of(Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON, Planet.SATURN, Planet.JUPITER, Planet.MARS), table.rulerSequence());
        assertEquals(Planet.SUN, table.periods().get(0).ruler());
        assertEquals(Planet.VENUS, table.periods().get(1).ruler());

        DecennialPeriod secondCycleSun = table.periods().stream()
                .filter(period -> period.cycleNumber() == 2 && period.sequenceIndex() == 1)
                .findFirst()
                .orElseThrow();
        assertEquals(Planet.SUN, secondCycleSun.ruler());
        assertEquals(70, secondCycleSun.startAgeYears());
        assertEquals(80, secondCycleSun.endAgeYearsExclusive());
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart chart = chart(Sect.NOCTURNAL);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, LocalDate.of(1999, 12, 31), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, new NatalChart(), null, 0, 1));
    }

    private Subject subject() {
        return new Subject(
                "test",
                OffsetDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                51.5,
                0.0
        );
    }

    private NatalChart chart(Sect sect) {
        NatalChart chart = new NatalChart();
        chart.setSect(new BasicSect(
                sect,
                sect == Sect.DIURNAL ? Planet.SUN : Planet.MOON,
                sect == Sect.DIURNAL ? Planet.MOON : Planet.SUN,
                sect == Sect.DIURNAL ? Planet.JUPITER : Planet.VENUS,
                sect == Sect.DIURNAL ? Planet.VENUS : Planet.JUPITER,
                sect == Sect.DIURNAL ? Planet.SATURN : Planet.MARS,
                sect == Sect.DIURNAL ? Planet.MARS : Planet.SATURN,
                sect == Sect.DIURNAL,
                sect == Sect.NOCTURNAL,
                1.0,
                -1.0,
                Map.of()
        ));
        chart.setHouses(housesFromAriesAscendant());
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.SUN, planetPoint(Planet.SUN, ZodiacSign.ARIES, 1));
        points.put(PointKey.MOON, planetPoint(Planet.MOON, ZodiacSign.CANCER, 4));
        points.put(PointKey.MERCURY, planetPoint(Planet.MERCURY, ZodiacSign.GEMINI, 3));
        points.put(PointKey.VENUS, planetPoint(Planet.VENUS, ZodiacSign.TAURUS, 2));
        points.put(PointKey.MARS, planetPoint(Planet.MARS, ZodiacSign.SCORPIO, 8));
        points.put(PointKey.JUPITER, planetPoint(Planet.JUPITER, ZodiacSign.SAGITTARIUS, 9));
        points.put(PointKey.SATURN, planetPoint(Planet.SATURN, ZodiacSign.CAPRICORN, 10));
        chart.setPoints(points);
        return chart;
    }

    private List<HousePosition> housesFromAriesAscendant() {
        return List.of(
                house(1, ZodiacSign.ARIES),
                house(2, ZodiacSign.TAURUS),
                house(3, ZodiacSign.GEMINI),
                house(4, ZodiacSign.CANCER),
                house(5, ZodiacSign.LEO),
                house(6, ZodiacSign.VIRGO),
                house(7, ZodiacSign.LIBRA),
                house(8, ZodiacSign.SCORPIO),
                house(9, ZodiacSign.SAGITTARIUS),
                house(10, ZodiacSign.CAPRICORN),
                house(11, ZodiacSign.AQUARIUS),
                house(12, ZodiacSign.PISCES)
        );
    }

    private HousePosition house(int house, ZodiacSign sign) {
        return new HousePosition(house, sign.ordinal() * 30.0, sign);
    }

    private PlanetPointEntry planetPoint(Planet planet, ZodiacSign sign, int house) {
        return new PlanetPointEntry(
                sign.ordinal() * 30.0,
                sign,
                TraditionalTables.element(sign),
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                1.0,
                1.0,
                1.0,
                false,
                house,
                house,
                null,
                Angularity.SUCCEDENT,
                0.0,
                0.0,
                TraditionalTables.domicileRuler(sign),
                TraditionalTables.exaltationRuler(sign),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                List.of(),
                false,
                PointType.PLANET
        );
    }
}
