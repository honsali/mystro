package app.reading.lifearc.lunar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.chart.data.SyzygyType;

class LunarTimingCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final LunarTimingCalculator calculator = new LunarTimingCalculator();

    @Test
    void calculateTableProducesExactLunarReturnsAndLunations() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        LunarTimingTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2000, 7, 15), 0, 1);

        assertEquals(LunarTimingCalculator.METHOD_ID, table.methodId());
        assertEquals(0, table.ageStartYears());
        assertEquals(1, table.ageEndYearsInclusive());
        assertEquals(natalChart.requirePlanet(Planet.MOON).getLongitude(), table.natalMoonLongitude(), 0.0001);
        assertFalse(table.lunarReturns().isEmpty());
        assertFalse(table.lunations().isEmpty());
        assertEquals(subject.getResolvedUtcInstant(), table.lunarReturns().get(0).returnDateTime().toInstant());
        assertTrue(table.lunarReturns().size() >= 13);
        assertTrue(table.lunations().size() >= 24);

        for (LunarReturnEntry row : table.lunarReturns()) {
            assertTrue(AstroMath.rawAngularSeparation(table.natalMoonLongitude(), row.moonLongitude()) < 0.0001,
                    "Lunar return " + row.returnNumberFromBirth() + " should return to natal Moon longitude");
            assertTrue(row.periodEndDateTimeExclusive().isAfter(row.returnDateTime()));
        }

        for (int i = 0; i < table.lunations().size(); i++) {
            LunationEntry row = table.lunations().get(i);
            assertEquals(i + 1, row.sequenceIndex());
            assertTrue(row.periodEndDateTimeExclusive().isAfter(row.dateTime()));
            if (row.type() == SyzygyType.NEW_MOON) {
                assertTrue(row.angularSeparation() < 0.0001 || Math.abs(row.angularSeparation() - 360.0) < 0.0001);
            } else {
                assertEquals(180.0, row.angularSeparation(), 0.0001);
            }
        }
    }

    @Test
    void calculateTableMarksActiveReturnAndLunationPeriods() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        LunarTimingTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 50, 50);

        assertEquals(1, table.lunarReturns().stream().filter(LunarReturnEntry::activeForInquiry).count());
        assertEquals(1, table.lunations().stream().filter(LunationEntry::activeForInquiry).count());
        assertTrue(table.lunarReturns().stream()
                .filter(LunarReturnEntry::activeForInquiry)
                .allMatch(row -> row.returnDateTime().isBefore(row.periodEndDateTimeExclusive())));
        assertTrue(table.lunations().stream()
                .filter(LunationEntry::activeForInquiry)
                .allMatch(row -> row.dateTime().isBefore(row.periodEndDateTimeExclusive())));
    }

    @Test
    void calculateTableClassifiesEclipseCandidatesBySyzygyTypeAndNodeOrb() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        LunarTimingTable table = calculator.calculateTable(subject, natalChart, null, 0, 5);

        assertTrue(table.lunations().stream().anyMatch(row -> row.eclipseType() != EclipseCandidateType.NONE));
        assertTrue(table.lunations().stream()
                .filter(row -> row.eclipseType() == EclipseCandidateType.SOLAR_ECLIPSE_CANDIDATE)
                .allMatch(row -> row.type() == SyzygyType.NEW_MOON
                        && row.nearestNodeOrbDegrees() <= table.solarEclipseNodeOrbDegrees() + 0.0001));
        assertTrue(table.lunations().stream()
                .filter(row -> row.eclipseType() == EclipseCandidateType.LUNAR_ECLIPSE_CANDIDATE)
                .allMatch(row -> row.type() == SyzygyType.FULL_MOON
                        && row.nearestNodeOrbDegrees() <= table.lunarEclipseNodeOrbDegrees() + 0.0001));
    }

    @Test
    void calculateTableIncludesSwissTrueSolarAndLunarEclipsesWithMagnitudeContactsAndNodeReference() {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));

        LunarTimingTable table = calculator.calculateTable(subject, natalChart, LocalDate.of(2050, 6, 3), 50, 50);

        assertFalse(table.eclipseEvents().isEmpty());
        EclipseEvent solar = table.eclipseEvents().stream()
                .filter(event -> event.kind() == EclipseEventKind.SOLAR)
                .findFirst()
                .orElseThrow();
        EclipseEvent lunar = table.eclipseEvents().stream()
                .filter(event -> event.kind() == EclipseEventKind.LUNAR)
                .findFirst()
                .orElseThrow();

        assertEquals(TrueEclipseCalculationDesign.METHOD_ID, solar.methodId());
        assertEquals(EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY, solar.calculationScope());
        assertFalse(solar.maximumDateTime().isBefore(table.coverageStartDateTime()));
        assertTrue(solar.maximumDateTime().isBefore(table.coverageEndDateTimeExclusive()));
        assertEquals(SyzygyType.NEW_MOON, solar.syzygy());
        assertEquals(EclipseCandidateType.SOLAR_ECLIPSE_CANDIDATE, solar.candidateReference());
        assertTrue(solar.nearestNodeOrbDegrees() <= table.solarEclipseNodeOrbDegrees());
        assertNotNull(solar.magnitude());
        assertTrue(solar.magnitude() > 0.0);
        assertNotNull(solar.obscuration());
        assertTrue(solar.obscuration() >= 0.0);
        assertNotNull(solar.sarosSeries());
        assertTrue(solar.contacts().stream().anyMatch(contact -> contact.phase() == EclipseContactPhase.MAXIMUM));
        assertTrue(solar.contacts().stream().anyMatch(contact -> contact.phase() == EclipseContactPhase.ECLIPSE_BEGIN));
        assertTrue(solar.visibility().globallyOccurs());
        assertTrue(solar.visibility().localVisibility() == EclipseVisibilityStatus.VISIBLE
                || solar.visibility().localVisibility() == EclipseVisibilityStatus.NOT_VISIBLE);
        assertFalse(solar.visibility().reason().isBlank());

        assertEquals(EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY, lunar.calculationScope());
        assertFalse(lunar.maximumDateTime().isBefore(table.coverageStartDateTime()));
        assertTrue(lunar.maximumDateTime().isBefore(table.coverageEndDateTimeExclusive()));
        assertEquals(SyzygyType.FULL_MOON, lunar.syzygy());
        assertEquals(EclipseCandidateType.LUNAR_ECLIPSE_CANDIDATE, lunar.candidateReference());
        assertTrue(lunar.nearestNodeOrbDegrees() <= table.lunarEclipseNodeOrbDegrees());
        assertNotNull(lunar.magnitude());
        assertNotNull(lunar.penumbralMagnitude());
        assertTrue(lunar.penumbralMagnitude() > 0.0);
        assertNotNull(lunar.sarosSeries());
        assertTrue(lunar.contacts().stream().anyMatch(contact -> contact.phase() == EclipseContactPhase.MAXIMUM));
        assertTrue(lunar.contacts().stream().anyMatch(contact -> contact.phase() == EclipseContactPhase.PENUMBRAL_BEGIN
                || contact.phase() == EclipseContactPhase.PARTIAL_BEGIN));
        assertTrue(lunar.visibility().globallyOccurs());
        assertTrue(lunar.visibility().localVisibility() == EclipseVisibilityStatus.VISIBLE
                || lunar.visibility().localVisibility() == EclipseVisibilityStatus.NOT_VISIBLE);
        assertFalse(lunar.visibility().reason().isBlank());
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
