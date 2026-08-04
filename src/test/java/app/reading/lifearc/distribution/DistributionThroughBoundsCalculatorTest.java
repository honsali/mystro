package app.reading.lifearc.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;

class DistributionThroughBoundsCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final DistributionThroughBoundsCalculator calculator = new DistributionThroughBoundsCalculator();

    @Test
    void calculateTableDistributesAscendantThroughEgyptianBoundsAndMarksActivePeriod() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));
        ChartAngle ascendant = natalChart.requireAngle(AngleType.ASCENDANT);

        DistributionThroughBoundsTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(DistributionThroughBoundsCalculator.METHOD_ID, table.methodId());
        assertEquals(Terms.EGYPTIAN, table.terms());
        assertEquals("ASCENDANT", table.directedPoint());
        assertEquals(ascendant.getLongitude(), table.directedPointLongitude(), 0.0001);
        assertEquals(1, table.directedPointHouse());
        assertFalse(table.periods().isEmpty());

        DistributionThroughBoundsPeriod first = table.periods().get(0);
        assertEquals(1, first.sequenceIndex());
        assertEquals(1, first.cycleNumber());
        assertEquals(ascendant.getSign(), first.sign());
        assertEquals(TraditionalTables.termRuler(ascendant.getLongitude(), Terms.EGYPTIAN), first.boundRuler());
        assertEquals(0.0, first.startAgeYears(), 0.0001);
        assertEquals(subject.getLocalBirthDateTime(), first.startDateTime());
        assertTrue(first.endAgeYearsExclusive() > first.startAgeYears());

        long activeCount = table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .count();
        assertEquals(1, activeCount);
        DistributionThroughBoundsPeriod active = table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElseThrow();
        assertTrue(active.startDateTime().isBefore(active.endDateTimeExclusive()));
        assertTrue(active.contacts().stream().allMatch(contact ->
                !contact.dateTime().isBefore(active.startDateTime())
                        && contact.dateTime().isBefore(active.endDateTimeExclusive())));
    }

    @Test
    void calculateTableIncludesPlanetBodiesAndRaysAsContacts() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        DistributionThroughBoundsTable table = calculator.calculateTable(subject, natalChart, null, 0, 20);

        assertTrue(table.periods().stream().flatMap(period -> period.contacts().stream())
                .anyMatch(contact -> contact.contactType() == DistributionContactType.BODY));
        assertTrue(table.periods().stream().flatMap(period -> period.contacts().stream())
                .anyMatch(contact -> contact.contactType() == DistributionContactType.RAY));
    }

    @Test
    void calculateTableSupportsClippedAgeRanges() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        DistributionThroughBoundsTable table = calculator.calculateTable(subject, natalChart, null, 50, 50);

        assertFalse(table.periods().isEmpty());
        assertTrue(table.periods().get(0).startAgeYears() >= 50.0 - 0.0001);
        assertTrue(table.periods().get(table.periods().size() - 1).endAgeYearsExclusive() <= 51.0 + 0.0001);
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
