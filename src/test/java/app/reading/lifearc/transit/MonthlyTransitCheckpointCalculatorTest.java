package app.reading.lifearc.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.data.AspectType;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;

class MonthlyTransitCheckpointCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final MonthlyTransitCheckpointCalculator calculator = new MonthlyTransitCheckpointCalculator();

    @Test
    void calculateTableUsesMonthlyBirthDateTimeCheckpointsAndMarksActivePeriod() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        MonthlyTransitCheckpointTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2000, 3, 20), 0, 0);

        assertEquals(MonthlyTransitCheckpointCalculator.METHOD_ID, table.methodId());
        assertEquals(12, table.rows().size());
        assertEquals(3.0, table.activationAspectOrbDegrees(), 0.0001);

        MonthlyTransitCheckpointRow first = table.rows().get(0);
        assertEquals(1, first.checkpointNumber());
        assertEquals(0, first.ageYears());
        assertEquals(1, first.monthInYear());
        assertEquals(subject.getLocalBirthDateTime(), first.checkpointDateTime());
        assertEquals(subject.getLocalBirthDateTime().plusMonths(1), first.periodEndDateTimeExclusive());
        assertEquals(1, first.annualProfectedHouse());
        assertEquals(natalChart.getHouses().get(0).getSign(), first.annualProfectedSign());
        assertEquals(1, first.monthlyProfectedHouse());
        assertEquals(first.annualProfectedSign(), first.monthlyProfectedSign());
        assertFalse(first.activeForInquiry());
        assertEquals(9, first.transitPoints().size());
        assertTrue(first.conjunctions().stream().anyMatch(contact ->
                contact.transitPoint() == PointKey.SUN
                        && contact.natalTargetType() == TransitNatalTargetType.POINT
                        && "SUN".equals(contact.natalTargetName())
                        && contact.orbDegrees() < 0.0001));
        assertFalse(first.activationContacts().isEmpty());
        assertTrue(first.activationContacts().stream().allMatch(contact ->
                contact.orbFromExactDegrees() <= table.activationAspectOrbDegrees() + 0.0001
                        && !contact.activationReasons().isEmpty()));
        assertTrue(first.activationContacts().stream().anyMatch(contact -> contact.aspect() != AspectType.CONJUNCTION));

        MonthlyTransitCheckpointRow active = table.rows().get(2);
        assertEquals(3, active.monthInYear());
        assertTrue(active.activeForInquiry());
        assertEquals(subject.getLocalBirthDateTime().plusMonths(2), active.checkpointDateTime());
        assertEquals(subject.getLocalBirthDateTime().plusMonths(3), active.periodEndDateTimeExclusive());
    }

    @Test
    void calculateTableKeepsBirthDayPlusMonthsEndOfMonthConvention() {
        Subject subject = new Subject(
                "end-of-month",
                OffsetDateTime.of(2000, 1, 31, 12, 0, 0, 0, ZoneOffset.UTC),
                51.5,
                0.0
        );
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        MonthlyTransitCheckpointTable table = calculator.calculateTable(subject, natalChart, null, 0, 0);

        assertEquals(OffsetDateTime.of(2000, 2, 29, 12, 0, 0, 0, ZoneOffset.UTC), table.rows().get(1).checkpointDateTime());
        assertEquals(OffsetDateTime.of(2000, 3, 31, 12, 0, 0, 0, ZoneOffset.UTC), table.rows().get(2).checkpointDateTime());
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, LocalDate.of(1999, 12, 31), 0, 1));
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
