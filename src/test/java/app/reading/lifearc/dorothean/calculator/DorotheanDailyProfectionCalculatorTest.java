package app.reading.lifearc.dorothean.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.TraditionalTables;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.DailyProfectionReferenceEntry;
import app.reading.lifearc.model.DailyProfectionTable;
import app.reading.lifearc.model.DailyProfectionTableRow;

class DorotheanDailyProfectionCalculatorTest {

    private final DorotheanDailyProfectionCalculator calculator = new DorotheanDailyProfectionCalculator();

    @Test
    void calculateWindowAdvancesDailyFromMonthlyProfectedSign() {
        DailyProfectionTable table = calculator.calculateWindow(subject(), chart(), LocalDate.of(2047, 1, 1), 1);

        assertEquals(DorotheanDailyProfectionCalculator.METHOD_ID, table.methodId());
        assertEquals(LocalDate.of(2046, 12, 31), table.windowStartDate());
        assertEquals(LocalDate.of(2047, 1, 2), table.windowEndDate());
        assertEquals(3, table.rows().size());

        DailyProfectionTableRow focus = table.rows().get(1);
        assertEquals(LocalDate.of(2047, 1, 1), focus.date());
        assertTrue(focus.focusDate());
        assertEquals(47, focus.ageYears());
        assertEquals(4, focus.cycleNumber());
        assertEquals(12, focus.yearInCycle());
        assertEquals(1, focus.monthInYear());
        assertEquals(1, focus.dayInMonth());
        assertEquals(subject().getUtcBirthDateTime().plusYears(47), focus.periodStartDateTime());
        assertEquals(subject().getUtcBirthDateTime().plusYears(47).plusDays(1), focus.periodEndDateTimeExclusive());

        DailyProfectionReferenceEntry asc = entry(focus, AnnualProfectionReference.ASCENDANT);
        assertEquals(ZodiacSign.PISCES, asc.natalSign());
        assertEquals(ZodiacSign.AQUARIUS, asc.annualSign());
        assertEquals(12, asc.annualHouse());
        assertEquals(Planet.SATURN, asc.annualLord());
        assertEquals(ZodiacSign.AQUARIUS, asc.monthlySign());
        assertEquals(12, asc.monthlyHouse());
        assertEquals(Planet.SATURN, asc.monthlyLord());
        assertEquals(ZodiacSign.AQUARIUS, asc.profectedSign());
        assertEquals(12, asc.profectedHouse());
        assertEquals(Planet.SATURN, asc.lord());

        assertEquals(List.of(PointKey.SATURN), focus.activatedNatalPoints().stream().map(point -> point.point()).toList());
        assertEquals(List.of("WEDDING"), focus.activatedLots().stream().map(lot -> lot.name()).toList());

        DailyProfectionReferenceEntry nextAsc = entry(table.rows().get(2), AnnualProfectionReference.ASCENDANT);
        assertEquals(ZodiacSign.PISCES, nextAsc.profectedSign());
        assertEquals(1, nextAsc.profectedHouse());
        assertEquals(Planet.JUPITER, nextAsc.lord());
    }

    @Test
    void calculateWindowRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart chart = chart();

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWindow(subject, chart, LocalDate.of(1999, 12, 31), 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWindow(subject, chart, LocalDate.of(2047, 1, 1), -1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWindow(null, chart, LocalDate.of(2047, 1, 1), 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWindow(subject, null, LocalDate.of(2047, 1, 1), 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateWindow(subject, chart, null, 1));
    }

    private DailyProfectionReferenceEntry entry(DailyProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(candidate -> candidate.reference() == reference)
                .findFirst()
                .orElseThrow();
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }

    private NatalChart chart() {
        NatalChart chart = new NatalChart();
        chart.setHouses(housesFromPiscesAscendant());

        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.SUN, planetPoint(ZodiacSign.CANCER, 5));
        points.put(PointKey.MOON, planetPoint(ZodiacSign.LIBRA, 8));
        points.put(PointKey.SATURN, planetPoint(ZodiacSign.AQUARIUS, 12));
        points.put(PointKey.ASCENDANT, anglePoint(ZodiacSign.PISCES));
        points.put(PointKey.MIDHEAVEN, anglePoint(ZodiacSign.SAGITTARIUS));
        chart.setPoints(points);

        chart.setLots(List.of(
                new LotEntry("FORTUNE", "Lot of Fortune", "valens", 240.0, ZodiacSign.SAGITTARIUS, 0.0, 10, Planet.JUPITER, "fixture"),
                new LotEntry("SPIRIT", "Lot of Spirit", "valens", 60.0, ZodiacSign.GEMINI, 0.0, 4, Planet.MERCURY, "fixture"),
                new LotEntry("WEDDING", "Lot of Wedding", "valens", 300.0, ZodiacSign.AQUARIUS, 0.0, 12, Planet.SATURN, "fixture")
        ));
        return chart;
    }

    private List<HousePosition> housesFromPiscesAscendant() {
        return List.of(
                house(1, ZodiacSign.PISCES),
                house(2, ZodiacSign.ARIES),
                house(3, ZodiacSign.TAURUS),
                house(4, ZodiacSign.GEMINI),
                house(5, ZodiacSign.CANCER),
                house(6, ZodiacSign.LEO),
                house(7, ZodiacSign.VIRGO),
                house(8, ZodiacSign.LIBRA),
                house(9, ZodiacSign.SCORPIO),
                house(10, ZodiacSign.SAGITTARIUS),
                house(11, ZodiacSign.CAPRICORN),
                house(12, ZodiacSign.AQUARIUS)
        );
    }

    private HousePosition house(int house, ZodiacSign sign) {
        return new HousePosition(house, sign.ordinal() * 30.0, sign);
    }

    private AnglePointEntry anglePoint(ZodiacSign sign) {
        return new AnglePointEntry(sign.ordinal() * 30.0, sign, 0.0);
    }

    private PlanetPointEntry planetPoint(ZodiacSign sign, int house) {
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
