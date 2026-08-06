package app.reading.lifearc.primarydirection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import app.chart.ChartCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.Chart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.valens.calculator.ValensPtolemaicHylegAlcocodenCalculator;

class PrimaryDirectionCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final PrimaryDirectionCalculator calculator = new PrimaryDirectionCalculator();

    @Test
    void calculateTableBuildsHylegAndAnglePrimaryDirections() {
        Subject subject = subject();
        Chart natalChart = natalChart(subject);

        PrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(PrimaryDirectionCalculator.METHOD_ID, table.methodId());
        assertEquals("ptolemaic_normalized", table.primaryDoctrine());
        assertEquals(0, table.ageStartYears());
        assertEquals(100, table.ageEndYearsInclusive());
        assertFalse(table.significators().isEmpty());
        assertFalse(table.events().isEmpty());

        Set<String> roles = table.significators().stream()
                .map(PrimaryDirectionSignificator::role)
                .collect(Collectors.toSet());
        assertTrue(roles.contains("HYLEG"));
        assertTrue(roles.contains("MIDHEAVEN_ANGLE"));
        PrimaryDirectionSignificator hyleg = table.significators().stream()
                .filter(PrimaryDirectionSignificator::selectedHyleg)
                .findFirst()
                .orElseThrow();
        assertEquals(natalChart.getPtolemaicHylegAlcocoden().hyleg().point(), hyleg.point());

        assertTrue(table.events().stream().anyMatch(event -> event.contactType() == PrimaryDirectionContactType.BODY));
        assertTrue(table.events().stream().anyMatch(event -> event.contactType() == PrimaryDirectionContactType.RAY));
        assertTrue(table.events().stream().anyMatch(event -> event.coordinate() == PrimaryDirectionCoordinate.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE));
        assertTrue(table.events().stream().anyMatch(event -> event.coordinate() == PrimaryDirectionCoordinate.RIGHT_ASCENSION));
        assertTrue(table.events().stream().allMatch(event -> event.direction() == PrimaryDirectionPolarity.DIRECT));
        assertTrue(table.events().stream().allMatch(event -> event.ageYears() >= 0.0 && event.ageYears() < 101.0));
        assertEventsSortedAndSequenced(table);
    }

    @Test
    void calculateTableMarksInquiryBirthdayYearWindow() {
        Subject subject = subject();
        Chart natalChart = natalChart(subject);

        PrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(OffsetDateTime.of(2050, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.inquiryYearStartDateTime());
        assertEquals(OffsetDateTime.of(2051, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.inquiryYearEndDateTimeExclusive());
        assertTrue(table.events().stream()
                .filter(PrimaryDirectionEvent::activeForInquiryYear)
                .allMatch(event -> !event.dateTime().isBefore(table.inquiryYearStartDateTime())
                        && event.dateTime().isBefore(table.inquiryYearEndDateTimeExclusive())));
    }

    @Test
    void calculateDirectConverseTableLabelsRowsAndKeepsDirectRowsStable() {
        Subject subject = subject();
        Chart natalChart = natalChart(subject);

        PrimaryDirectionTable direct = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);
        PrimaryDirectionTable expanded = calculator.calculateDirectConverseTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(PrimaryDirectionExpansionDesign.CONVERSE_ZODIACAL_METHOD_ID, expanded.methodId());
        assertTrue(expanded.directionMethod().contains("DIRECT_AND_CONVERSE"));
        assertTrue(expanded.directionMethod().contains("CONVERSE_ARC"));
        assertTrue(expanded.events().stream().anyMatch(event -> event.direction() == PrimaryDirectionPolarity.DIRECT));
        assertTrue(expanded.events().stream().anyMatch(event -> event.direction() == PrimaryDirectionPolarity.CONVERSE));
        assertEquals((long) direct.events().size(), expanded.events().stream()
                .filter(event -> event.direction() == PrimaryDirectionPolarity.DIRECT)
                .count());
        assertEquals(eventKeys(direct.events()), eventKeys(expanded.events().stream()
                .filter(event -> event.direction() == PrimaryDirectionPolarity.DIRECT)
                .toList()));
        assertDirectionArcRules(expanded);
        assertEventsSortedAndSequenced(expanded);
    }

    @Test
    void calculateTableSupportsClippedAgeRanges() {
        Subject subject = subject();
        Chart natalChart = natalChart(subject);

        PrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, null, 50, 50);

        assertTrue(table.events().stream().allMatch(event -> event.ageYears() >= 50.0 - 0.0001 && event.ageYears() < 51.0 + 0.0001));
        assertEventsSortedAndSequenced(table);
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        Chart natalChart = natalChart(subject);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, LocalDate.of(1999, 12, 31), 0, 1));
    }

    private void assertEventsSortedAndSequenced(PrimaryDirectionTable table) {
        double previousAge = -1.0;
        for (int i = 0; i < table.events().size(); i++) {
            PrimaryDirectionEvent event = table.events().get(i);
            assertEquals(i + 1, event.sequenceIndex());
            assertTrue(event.ageYears() >= previousAge - 0.0001);
            previousAge = event.ageYears();
        }
    }

    private void assertDirectionArcRules(PrimaryDirectionTable table) {
        for (PrimaryDirectionEvent event : table.events()) {
            PrimaryDirectionSignificator significator = table.significators().stream()
                    .filter(candidate -> candidate.role().equals(event.significatorRole()))
                    .filter(candidate -> candidate.point().equals(event.significatorPoint()))
                    .filter(candidate -> candidate.coordinate() == event.coordinate())
                    .findFirst()
                    .orElseThrow();
            double expected = switch (event.direction()) {
                case DIRECT -> normalizedArc(event.targetDirectionCoordinateDegrees() - significator.directionCoordinateDegrees());
                case CONVERSE -> normalizedArc(significator.directionCoordinateDegrees() - event.targetDirectionCoordinateDegrees());
            };
            assertEquals(expected, event.arcDegrees(), 1.0e-6);
        }
    }

    private double normalizedArc(double degrees) {
        double value = degrees % 360.0;
        if (value < 0.0) {
            value += 360.0;
        }
        return Math.abs(value) <= 1.0e-9 || Math.abs(value - 360.0) <= 1.0e-9 ? 0.0 : value;
    }

    private List<String> eventKeys(List<PrimaryDirectionEvent> events) {
        return events.stream()
                .map(event -> event.significatorRole()
                        + "|" + event.significatorPoint()
                        + "|" + event.coordinate()
                        + "|" + event.promissorPlanet()
                        + "|" + event.contactType()
                        + "|" + event.aspect()
                        + "|" + event.rayDirection()
                        + "|" + event.arcDegrees()
                        + "|" + event.dateTime())
                .toList();
    }

    private Chart natalChart(Subject subject) {
        CalculationContext ctx = new CalculationContext(subject, CORE);
        Chart chart = new ChartCalculator().calculate(ctx);
        chart.setPtolemaicHylegAlcocoden(new ValensPtolemaicHylegAlcocodenCalculator().calculate(ctx, chart));
        return chart;
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
