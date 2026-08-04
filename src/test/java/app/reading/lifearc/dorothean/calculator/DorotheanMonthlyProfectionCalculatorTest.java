package app.reading.lifearc.dorothean.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;

class DorotheanMonthlyProfectionCalculatorTest {

    private final DorotheanMonthlyProfectionCalculator calculator = new DorotheanMonthlyProfectionCalculator();

    @Test
    void calculateTableAdvancesMonthlyFromAnnualProfectedSign() {
        Subject subject = subject();

        MonthlyProfectionTable table = calculator.calculateTable(subject, chart(), LocalDate.of(2050, 11, 3), 50, 50);

        assertEquals(DorotheanMonthlyProfectionCalculator.METHOD_ID, table.methodId());
        assertEquals(12, table.rows().size());

        MonthlyProfectionTableRow firstMonth = table.rows().get(0);
        assertEquals(50, firstMonth.ageYears());
        assertEquals(3, firstMonth.yearInCycle());
        assertEquals(5, firstMonth.cycleNumber());
        assertEquals(1, firstMonth.monthInYear());
        assertEquals(subject.getLocalBirthDateTime().plusMonths(600), firstMonth.periodStartDateTime());
        assertEquals(subject.getLocalBirthDateTime().plusMonths(601), firstMonth.periodEndDateTimeExclusive());
        assertFalse(firstMonth.activeForInquiry());

        MonthlyProfectionReferenceEntry firstAsc = entry(firstMonth, AnnualProfectionReference.ASCENDANT);
        assertEquals(ZodiacSign.PISCES, firstAsc.natalSign());
        assertEquals(ZodiacSign.TAURUS, firstAsc.annualSign());
        assertEquals(ZodiacSign.TAURUS, firstAsc.profectedSign());
        assertEquals(3, firstAsc.profectedHouse());
        assertEquals(Planet.VENUS, firstAsc.lord());

        MonthlyProfectionTableRow activeMonth = table.rows().get(10);
        assertEquals(11, activeMonth.monthInYear());
        assertTrue(activeMonth.activeForInquiry());
        MonthlyProfectionReferenceEntry activeAsc = entry(activeMonth, AnnualProfectionReference.ASCENDANT);
        assertEquals(ZodiacSign.PISCES, activeAsc.profectedSign());
        assertEquals(1, activeAsc.profectedHouse());
        assertEquals(Planet.JUPITER, activeAsc.lord());

        MonthlyProfectionReferenceEntry finalAsc = entry(table.rows().get(11), AnnualProfectionReference.ASCENDANT);
        assertEquals(ZodiacSign.ARIES, finalAsc.profectedSign());
        assertEquals(2, finalAsc.profectedHouse());
        assertEquals(Planet.MARS, finalAsc.lord());
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart chart = chart();

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, LocalDate.of(1999, 12, 31), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, 2, 1));
    }

    private MonthlyProfectionReferenceEntry entry(MonthlyProfectionTableRow row, AnnualProfectionReference reference) {
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
        points.put(PointKey.ASCENDANT, anglePoint(ZodiacSign.PISCES));
        points.put(PointKey.MIDHEAVEN, anglePoint(ZodiacSign.SAGITTARIUS));
        chart.setPoints(points);

        chart.setLots(List.of(
                new LotEntry("FORTUNE", "Lot of Fortune", "valens", 240.0, ZodiacSign.SAGITTARIUS, 0.0, 10, Planet.JUPITER, "fixture"),
                new LotEntry("SPIRIT", "Lot of Spirit", "valens", 330.0, ZodiacSign.PISCES, 0.0, 1, Planet.JUPITER, "fixture")
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
