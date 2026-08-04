package app.reading.lifearc.primarydirection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.valens.calculator.ValensPtolemaicHylegAlcocodenCalculator;

class MundanePrimaryDirectionCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final MundanePrimaryDirectionCalculator calculator = new MundanePrimaryDirectionCalculator();

    @Test
    void calculateTableBuildsClearlyLabelledPrototype() {
        Subject subject = subject();
        NatalChart natalChart = natalChart(subject);

        MundanePrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(PrimaryDirectionExpansionDesign.MUNDANE_SEMI_ARC_PROTOTYPE_METHOD_ID, table.methodId());
        assertEquals("ptolemaic_normalized_prototype", table.primaryDoctrine());
        assertTrue(table.directionMethod().contains("DIRECT_MUNDANE_SEMI_ARC_BODY_CONTACT_PROTOTYPE"));
        assertTrue(table.contactMethod().contains("BODY_CONTACTS_ONLY"));
        assertTrue(table.prototypeCaveat().contains("LOCAL_RESEARCH_PROTOTYPE"));
        assertFalse(table.significators().isEmpty());
        assertFalse(table.events().isEmpty());

        Set<String> roles = table.significators().stream()
                .map(MundanePrimaryDirectionSignificator::role)
                .collect(Collectors.toSet());
        assertTrue(roles.contains("HYLEG"));
        assertTrue(roles.contains("MIDHEAVEN_ANGLE"));
        assertTrue(table.events().stream().allMatch(event -> event.contactType() == PrimaryDirectionContactType.BODY));
        assertEventsFiniteSortedAndSequenced(table);
    }

    @Test
    void calculateTableMarksInquiryBirthdayYearWindow() {
        Subject subject = subject();
        NatalChart natalChart = natalChart(subject);

        MundanePrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 0, 100);

        assertEquals(OffsetDateTime.of(2050, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.inquiryYearStartDateTime());
        assertEquals(OffsetDateTime.of(2051, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.inquiryYearEndDateTimeExclusive());
        assertTrue(table.events().stream()
                .filter(MundanePrimaryDirectionEvent::activeForInquiryYear)
                .allMatch(event -> !event.dateTime().isBefore(table.inquiryYearStartDateTime())
                        && event.dateTime().isBefore(table.inquiryYearEndDateTimeExclusive())));
    }

    @Test
    void calculateTableSupportsClippedAgeRanges() {
        Subject subject = subject();
        NatalChart natalChart = natalChart(subject);

        MundanePrimaryDirectionTable table = calculator.calculateTable(subject, natalChart, null, 50, 50);

        assertTrue(table.events().stream().allMatch(event -> event.ageYears() >= 50.0 - 0.0001 && event.ageYears() < 51.0 + 0.0001));
        assertEventsFiniteSortedAndSequenced(table);
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart natalChart = natalChart(subject);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, LocalDate.of(1999, 12, 31), 0, 1));
    }

    private void assertEventsFiniteSortedAndSequenced(MundanePrimaryDirectionTable table) {
        double previousAge = -1.0;
        for (int i = 0; i < table.events().size(); i++) {
            MundanePrimaryDirectionEvent event = table.events().get(i);
            assertEquals(i + 1, event.sequenceIndex());
            assertTrue(Double.isFinite(event.targetMundanePositionDegrees()));
            assertTrue(Double.isFinite(event.promissorRightAscension()));
            assertTrue(Double.isFinite(event.promissorDeclination()));
            assertTrue(Double.isFinite(event.promissorDiurnalSemiArcDegrees()));
            assertTrue(Double.isFinite(event.promissorNocturnalSemiArcDegrees()));
            assertTrue(Double.isFinite(event.directedHourAngleDegrees()));
            assertTrue(Double.isFinite(event.directedArmcDegrees()));
            assertTrue(Double.isFinite(event.arcDegrees()));
            assertTrue(event.ageYears() >= previousAge - 0.0001);
            assertEquals(event.arcDegrees(), event.ageYears(), 1.0e-9);
            previousAge = event.ageYears();
        }
        for (MundanePrimaryDirectionSignificator significator : table.significators()) {
            assertTrue(Double.isFinite(significator.rightAscension()));
            assertTrue(Double.isFinite(significator.declination()));
            assertTrue(Double.isFinite(significator.diurnalSemiArcDegrees()));
            assertTrue(Double.isFinite(significator.nocturnalSemiArcDegrees()));
            assertTrue(Double.isFinite(significator.natalHourAngleDegrees()));
            assertTrue(Double.isFinite(significator.mundanePositionDegrees()));
        }
    }

    private NatalChart natalChart(Subject subject) {
        CalculationContext ctx = new CalculationContext(subject, CORE);
        NatalChart chart = new BasicCalculator().calculate(ctx);
        chart.setPtolemaicHylegAlcocoden(new ValensPtolemaicHylegAlcocodenCalculator().calculate(ctx, chart));
        return chart;
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
