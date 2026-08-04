package app.reading.lifearc.dorothean.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.TopicAssessmentEntry;
import app.reading.description.common.model.TopicEvidenceEntry;
import app.reading.lifearc.model.ActivatedTopicAssessmentRef;
import app.reading.lifearc.model.AnnualProfectionEntry;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;

class DorotheanAnnualProfectionCalculatorTest {

    private final DorotheanAnnualProfectionCalculator calculator = new DorotheanAnnualProfectionCalculator();

    @Test
    void calculateDerivesActiveAnnualProfectionAndActivatedEvidence() {
        AnnualProfectionEntry entry = calculator.calculate(subject(), chart(), LocalDate.of(2026, 6, 3));

        assertEquals(DorotheanAnnualProfectionCalculator.METHOD_ID, entry.methodId());
        assertEquals("dorothean", entry.primaryDoctrine());
        assertEquals(LocalDate.of(2026, 1, 15), entry.periodStartDate());
        assertEquals(LocalDate.of(2027, 1, 15), entry.periodEndDateExclusive());
        assertEquals(26, entry.ageYears());
        assertEquals(3, entry.cycleNumber());
        assertEquals(3, entry.yearInCycle());
        assertEquals(3, entry.profectedHouse());
        assertEquals(ZodiacSign.GEMINI, entry.profectedSign());
        assertEquals(Planet.MERCURY, entry.lordOfYear());
        assertEquals(139, entry.daysElapsed());
        assertEquals(226, entry.daysRemaining());

        assertEquals(List.of(PointKey.SUN, PointKey.SOUTH_NODE),
                entry.activatedNatalPoints().stream().map(activated -> activated.point()).toList());
        assertEquals(PointType.NODE, entry.activatedNatalPoints().get(1).type());
        assertEquals(List.of("FORTUNE"), entry.activatedLots().stream().map(activated -> activated.name()).toList());
        assertEquals("lotAssessments.lot=FORTUNE", entry.activatedLots().get(0).lotAssessmentRef());

        ActivatedTopicAssessmentRef topicRef = entry.activatedTopicAssessmentRefs().get(0);
        assertEquals("mind-speech", topicRef.topic());
        assertEquals("TEST_TOPIC_ACTIVE", topicRef.methodId());
        assertIterableEquals(
                List.of("houseTopicRulers.house=3", "MERCURY", "SUN", "lotAssessments.lot=FORTUNE"),
                topicRef.matchedConditionRefs()
        );
        assertEquals(1, entry.activatedTopicAssessmentRefs().size());
    }

    @Test
    void calculateHandlesBirthdayBoundaryAndCycleRollover() {
        AnnualProfectionEntry birthday = calculator.calculate(subject(), chart(), LocalDate.of(2026, 1, 15));

        assertEquals(0, birthday.daysElapsed());
        assertEquals(365, birthday.daysRemaining());
        assertEquals(26, birthday.ageYears());
        assertEquals(3, birthday.yearInCycle());

        AnnualProfectionEntry rollover = calculator.calculate(subject(), chart(), LocalDate.of(2024, 1, 15));

        assertEquals(24, rollover.ageYears());
        assertEquals(3, rollover.cycleNumber());
        assertEquals(1, rollover.yearInCycle());
        assertEquals(1, rollover.profectedHouse());
        assertEquals(ZodiacSign.ARIES, rollover.profectedSign());
        assertEquals(Planet.MARS, rollover.lordOfYear());
    }

    @Test
    void calculateTableKeepsReferenceOrderAndRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart chart = chart();

        AnnualProfectionTable table = calculator.calculateTable(subject, chart, LocalDate.of(2026, 6, 3), 26, 26);
        AnnualProfectionTable inactiveTable = calculator.calculateTable(subject, chart, null, 0, 0);

        assertEquals(1, table.rows().size());
        assertEquals(26, table.rows().get(0).ageYears());
        assertEquals(LocalDate.of(2026, 1, 15), table.rows().get(0).periodStartDate());
        assertEquals(LocalDate.of(2027, 1, 15), table.rows().get(0).periodEndDateExclusive());
        assertEquals(List.of(
                        AnnualProfectionReference.ASCENDANT,
                        AnnualProfectionReference.MIDHEAVEN,
                        AnnualProfectionReference.SUN,
                        AnnualProfectionReference.MOON,
                        AnnualProfectionReference.LOT_FORTUNE,
                        AnnualProfectionReference.LOT_SPIRIT
                ),
                table.rows().get(0).referenceProfections().stream()
                        .map(AnnualProfectionReferenceEntry::reference)
                        .toList());
        assertEquals(false, inactiveTable.rows().get(0).activeForInquiry());

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(subject, chart, LocalDate.of(1999, 1, 14)));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, LocalDate.of(1999, 1, 14), 0, 1));
    }

    private Subject subject() {
        return new Subject(
                "annual-profection-fixture",
                OffsetDateTime.of(2000, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                40.0,
                -3.0
        );
    }

    private NatalChart chart() {
        NatalChart chart = new NatalChart();
        chart.setHouses(housesFromAriesAscendant());

        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.SUN, point(ZodiacSign.GEMINI, 3, PointType.PLANET));
        points.put(PointKey.MOON, point(ZodiacSign.CANCER, 4, PointType.PLANET));
        points.put(PointKey.MERCURY, point(ZodiacSign.VIRGO, 6, PointType.PLANET));
        points.put(PointKey.SOUTH_NODE, point(ZodiacSign.GEMINI, 3, PointType.NODE));
        points.put(PointKey.ASCENDANT, anglePoint(ZodiacSign.ARIES));
        points.put(PointKey.MIDHEAVEN, anglePoint(ZodiacSign.CAPRICORN));
        chart.setPoints(points);

        chart.setLots(List.of(
                new LotEntry("FORTUNE", "Lot of Fortune", "valens", 70.0, ZodiacSign.GEMINI, 10.0, 3, Planet.MERCURY, "fixture"),
                new LotEntry("SPIRIT", "Lot of Spirit", "valens", 120.0, ZodiacSign.LEO, 0.0, 5, Planet.SUN, "fixture")
        ));
        chart.setTopicAssessments(List.of(
                topic("mind-speech", "TEST_TOPIC_ACTIVE", List.of(
                        evidence("houseTopicRulers.house=3"),
                        evidence("MERCURY"),
                        evidence("SUN"),
                        evidence("lotAssessments.lot=FORTUNE"),
                        evidence("lotAssessments.lot=SPIRIT")
                )),
                topic("unactivated-topic", "TEST_TOPIC_INACTIVE", List.of(evidence("JUPITER")))
        ));
        return chart;
    }

    private TopicAssessmentEntry topic(String topic, String methodId, List<TopicEvidenceEntry> evidence) {
        return new TopicAssessmentEntry(topic, "test", List.of(), methodId, evidence);
    }

    private TopicEvidenceEntry evidence(String conditionRef) {
        return new TopicEvidenceEntry("fixture", "test", "fixture", conditionRef, null, null, null, conditionRef);
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

    private AnglePointEntry anglePoint(ZodiacSign sign) {
        return new AnglePointEntry(sign.ordinal() * 30.0, sign, 0.0);
    }

    private PlanetPointEntry point(ZodiacSign sign, int house, PointType type) {
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
                type
        );
    }
}
