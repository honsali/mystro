package app.reading.lifearc.synthesis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.BasicCalculator;
import app.chart.data.Planet;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.description.valens.ValensNatalDescriptionSpecialist;
import app.reading.lifearc.distribution.DistributionThroughBoundsCalculator;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionPolarity;
import app.reading.lifearc.primarydirection.PrimaryDirectionExpansionDesign;

class LifeArcSynthesisCalculatorTest {
    private final LifeArcSynthesisCalculator calculator = new LifeArcSynthesisCalculator();

    @Test
    void calculateGroupsActiveLifeArcEvidenceForInquiryDate() {
        Subject subject = subject();
        NatalChart chart = new ValensNatalDescriptionSpecialist().calculate(subject, new BasicCalculator());

        LifeArcSynthesisTable table = calculator.calculate(subject, chart, LocalDate.of(2050, 6, 3));

        assertEquals(LifeArcSynthesisCalculator.METHOD_ID, table.methodId());
        assertEquals(50, table.completedAgeYears());
        assertEquals(OffsetDateTime.of(2050, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.activeYearStartDateTime());
        assertEquals(OffsetDateTime.of(2051, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), table.activeYearEndDateTimeExclusive());
        assertFalse(table.evidence().isEmpty());
        assertFalse(table.groups().isEmpty());

        for (int i = 0; i < table.evidence().size(); i++) {
            assertEquals(i + 1, table.evidence().get(i).sequenceIndex());
            assertNotNull(table.evidence().get(i).weightClass());
            assertTrue(table.evidence().get(i).weight() > 0);
        }
        assertSortedAndStable(table.groups());
        assertEquals(
                EnumSet.of(
                        LifeArcEvidenceWeightClass.CHRONOCRATOR,
                        LifeArcEvidenceWeightClass.RETURN_CHART,
                        LifeArcEvidenceWeightClass.DIRECTION_CONTACT,
                        LifeArcEvidenceWeightClass.LUNAR_ECLIPSE
                ),
                table.evidence().stream()
                        .map(LifeArcSynthesisEvidence::weightClass)
                        .collect(() -> EnumSet.noneOf(LifeArcEvidenceWeightClass.class), EnumSet::add, EnumSet::addAll)
        );
        assertEquals(
                List.of(
                        "SIGN:SCORPIO:22",
                        "HOUSE:H8:22",
                        "PLANET:SUN:20",
                        "SIGN:GEMINI:19",
                        "HOUSE:H3:19",
                        "PLANET:MARS:16",
                        "PLANET:JUPITER:14",
                        "PLANET:MERCURY:14",
                        "PLANET:MOON:14"
                ),
                table.groups().stream().limit(9).map(this::groupFingerprint).toList()
        );

        assertTrue(table.evidence().stream().anyMatch(item -> "ANNUAL_PROFECTION".equals(item.sourceTechnique())));
        assertTrue(table.evidence().stream().anyMatch(item -> "MONTHLY_PROFECTION".equals(item.sourceTechnique())));
        assertTrue(table.evidence().stream().anyMatch(item -> "DISTRIBUTIONS_EXTENDED".equals(item.sourceTechnique())));
        assertTrue(table.evidence().stream().anyMatch(item -> DistributionThroughBoundsCalculator.EXTENDED_METHOD_ID.equals(item.sourceMethodId())));
        assertTrue(table.evidence().stream().anyMatch(item -> item.detail().contains("Active HYLEG:")));
        assertTrue(table.evidence().stream().anyMatch(item -> item.detail().contains("Active MIDHEAVEN")));
        assertTrue(table.evidence().stream().anyMatch(item -> "TRUE_ECLIPSE".equals(item.sourceTechnique())));
        assertTrue(table.evidence().stream().anyMatch(item -> item.detail().contains("localVisibility=")));
        assertTrue(table.evidence().stream().anyMatch(item -> item.planet() == Planet.VENUS));
        assertTrue(table.groups().stream().anyMatch(group -> group.keyType() == LifeArcEvidenceKeyType.HOUSE && "H3".equals(group.key())));
        assertTrue(table.groups().stream().anyMatch(group -> group.keyType() == LifeArcEvidenceKeyType.PLANET && "VENUS".equals(group.key())));
    }

    private void assertSortedAndStable(List<LifeArcSynthesisGroup> groups) {
        for (int i = 1; i < groups.size(); i++) {
            LifeArcSynthesisGroup previous = groups.get(i - 1);
            LifeArcSynthesisGroup current = groups.get(i);
            assertTrue(compareGroupOrder(previous, current) <= 0, "groups must sort by weight desc, key type, then key");
        }
    }

    private int compareGroupOrder(LifeArcSynthesisGroup first, LifeArcSynthesisGroup second) {
        int weightOrder = Integer.compare(second.totalWeight(), first.totalWeight());
        if (weightOrder != 0) {
            return weightOrder;
        }
        int typeOrder = Integer.compare(first.keyType().ordinal(), second.keyType().ordinal());
        if (typeOrder != 0) {
            return typeOrder;
        }
        return first.key().compareTo(second.key());
    }

    private String groupFingerprint(LifeArcSynthesisGroup group) {
        return group.keyType() + ":" + group.key() + ":" + group.totalWeight();
    }

    @Test
    void calculateIncludesLowerWeightedPrimaryDirectionVariantsWhenActive() {
        Subject subject = subject();
        NatalChart chart = new ValensNatalDescriptionSpecialist().calculate(subject, new BasicCalculator());

        LocalDate converseInquiry = new PrimaryDirectionCalculator()
                .calculateDirectConverseTable(subject, chart, null, 0, 100)
                .events()
                .stream()
                .filter(event -> event.direction() == PrimaryDirectionPolarity.CONVERSE)
                .findFirst()
                .orElseThrow()
                .dateTime()
                .toLocalDate();
        LifeArcSynthesisTable converseTable = calculator.calculate(subject, chart, converseInquiry);
        List<LifeArcSynthesisEvidence> converseRows = converseTable.evidence().stream()
                .filter(item -> "PRIMARY_DIRECTIONS_CONVERSE".equals(item.sourceTechnique()))
                .toList();
        assertFalse(converseRows.isEmpty());
        assertTrue(converseRows.stream().allMatch(item -> PrimaryDirectionExpansionDesign.CONVERSE_ZODIACAL_METHOD_ID.equals(item.sourceMethodId())));
        assertTrue(converseRows.stream().allMatch(item -> item.weight() <= 3));
        assertTrue(converseRows.stream().anyMatch(item -> item.detail().contains("lower-weight variant evidence")));

        LocalDate mundaneInquiry = new MundanePrimaryDirectionCalculator()
                .calculateTable(subject, chart, null, 0, 100)
                .events()
                .stream()
                .findFirst()
                .orElseThrow()
                .dateTime()
                .toLocalDate();
        LifeArcSynthesisTable mundaneTable = calculator.calculate(subject, chart, mundaneInquiry);
        List<LifeArcSynthesisEvidence> mundaneRows = mundaneTable.evidence().stream()
                .filter(item -> "PRIMARY_DIRECTIONS_MUNDANE_PROTOTYPE".equals(item.sourceTechnique()))
                .toList();
        assertFalse(mundaneRows.isEmpty());
        assertTrue(mundaneRows.stream().allMatch(item -> PrimaryDirectionExpansionDesign.MUNDANE_SEMI_ARC_PROTOTYPE_METHOD_ID.equals(item.sourceMethodId())));
        assertTrue(mundaneRows.stream().allMatch(item -> item.weight() <= 2));
        assertTrue(mundaneRows.stream().anyMatch(item -> item.detail().contains("lower-weight prototype evidence")));
    }

    @Test
    void calculateRejectsMissingOrPrebirthInquiryDate() {
        Subject subject = subject();
        NatalChart chart = new ValensNatalDescriptionSpecialist().calculate(subject, new BasicCalculator());

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(subject, chart, null));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(subject, chart, LocalDate.of(1999, 12, 31)));
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
