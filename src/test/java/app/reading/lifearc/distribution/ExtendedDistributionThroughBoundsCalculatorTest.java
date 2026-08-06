package app.reading.lifearc.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import app.chart.ChartCalculator;
import app.chart.data.AngleType;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.Subject;
import app.reading.description.common.model.HylegAlcocodenEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.NatalChartCalculator;

class ExtendedDistributionThroughBoundsCalculatorTest {
    private final DistributionThroughBoundsCalculator calculator = new DistributionThroughBoundsCalculator();

    @Test
    void calculateExtendedTablesBuildsHylegMcFortuneSpiritAndFirstSliceTables() {
        Subject subject = subject();
        Chart natalChart = valensChart(subject);
        HylegAlcocodenEntry.HylegPoint hyleg = natalChart.getPtolemaicHylegAlcocoden().hyleg();

        List<DistributionThroughBoundsTable> tables = calculator.calculateExtendedTables(
                subject,
                natalChart,
                LocalDate.of(2050, 6, 3),
                0,
                100
        );

        Set<String> directedPoints = tables.stream()
                .map(DistributionThroughBoundsTable::directedPoint)
                .collect(Collectors.toSet());
        assertEquals(Set.of("HYLEG:" + hyleg.point(), "MIDHEAVEN", "FORTUNE", "SPIRIT", "SUN", "MOON"), directedPoints);

        DistributionThroughBoundsTable hylegTable = table(tables, "HYLEG:" + hyleg.point());
        assertEquals(DistributionThroughBoundsCalculator.EXTENDED_METHOD_ID, hylegTable.methodId());
        assertEquals(hyleg.longitude(), hylegTable.directedPointLongitude(), 0.0001);
        assertEquals(hyleg.sign(), hylegTable.directedPointSign());
        assertEquals(hyleg.degreeInSign(), hylegTable.directedPointDegreeInSign(), 0.0001);
        assertEquals(hyleg.house(), hylegTable.directedPointHouse());
        assertTrue(hylegTable.timingMethod().contains("SELECTED_HYLEG_POINT=" + hyleg.point()));
        assertActiveOnce(hylegTable);

        ChartAngle midheaven = natalChart.requireAngle(AngleType.MIDHEAVEN);
        DistributionThroughBoundsTable mcTable = table(tables, "MIDHEAVEN");
        assertEquals(midheaven.getLongitude(), mcTable.directedPointLongitude(), 0.0001);
        assertTrue(mcTable.timingMethod().contains("COORDINATE=RIGHT_ASCENSION"));
        assertTrue(mcTable.contactMethod().contains("RIGHT_ASCENSION"));
        assertActiveOnce(mcTable);

        LotEntry fortune = lot(natalChart, "FORTUNE");
        DistributionThroughBoundsTable fortuneTable = table(tables, "FORTUNE");
        assertEquals(fortune.longitude(), fortuneTable.directedPointLongitude(), 0.0001);
        assertEquals(fortune.house(), fortuneTable.directedPointHouse());
        assertTrue(fortuneTable.timingMethod().contains("COORDINATE=OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE"));
        assertActiveOnce(fortuneTable);

        LotEntry spirit = lot(natalChart, "SPIRIT");
        DistributionThroughBoundsTable spiritTable = table(tables, "SPIRIT");
        assertEquals(spirit.longitude(), spiritTable.directedPointLongitude(), 0.0001);
        assertEquals(spirit.house(), spiritTable.directedPointHouse());
        assertTrue(spiritTable.timingMethod().contains("COORDINATE=OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE"));
        assertActiveOnce(spiritTable);
    }

    @Test
    void calculateExtendedTablesIncludesPlanetBodyAndRayContacts() {
        Subject subject = subject();
        Chart natalChart = valensChart(subject);

        List<DistributionThroughBoundsTable> tables = calculator.calculateExtendedTables(subject, natalChart, null, 0, 360);

        for (DistributionThroughBoundsTable table : tables) {
            List<DistributionThroughBoundsContact> contacts = table.periods().stream()
                    .flatMap(period -> period.contacts().stream())
                    .toList();
            assertFalse(contacts.isEmpty(), () -> "Expected contacts for " + table.directedPoint());
            assertTrue(contacts.stream().anyMatch(contact -> contact.contactType() == DistributionContactType.BODY),
                    () -> "Expected body contacts for " + table.directedPoint());
            assertTrue(contacts.stream().anyMatch(contact -> contact.contactType() == DistributionContactType.RAY),
                    () -> "Expected ray contacts for " + table.directedPoint());
            assertTrue(contacts.stream().allMatch(contact -> contact.ageYears() >= 0.0 && contact.ageYears() <= 361.0));
        }
    }

    @Test
    void calculateExtendedTablesRejectsInvalidInputs() {
        Subject subject = subject();
        Chart natalChart = valensChart(subject);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateExtendedTables(subject, natalChart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateExtendedTables(subject, natalChart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateExtendedTables(subject, natalChart, LocalDate.of(1999, 12, 31), 0, 1));
    }

    private void assertActiveOnce(DistributionThroughBoundsTable table) {
        long activeCount = table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .count();
        assertEquals(1, activeCount, () -> "Expected one active period for " + table.directedPoint());
        DistributionThroughBoundsPeriod active = table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElseThrow();
        assertTrue(active.startDateTime().isBefore(active.endDateTimeExclusive()));
    }

    private DistributionThroughBoundsTable table(List<DistributionThroughBoundsTable> tables, String directedPoint) {
        return tables.stream()
                .filter(table -> directedPoint.equals(table.directedPoint()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing table " + directedPoint));
    }

    private LotEntry lot(Chart chart, String name) {
        return chart.getLots().stream()
                .filter(lot -> name.equals(lot.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing lot " + name));
    }

    private Chart valensChart(Subject subject) {
        return new NatalChartCalculator().calculate(subject, new ChartCalculator());
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
