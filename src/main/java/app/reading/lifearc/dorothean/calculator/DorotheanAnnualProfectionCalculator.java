package app.reading.lifearc.dorothean.calculator;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.chart.TraditionalTables;
import app.chart.data.Planet;
import app.chart.data.PointKey;
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
import app.reading.lifearc.model.ActivatedLotEntry;
import app.reading.lifearc.model.ActivatedNatalPointEntry;
import app.reading.lifearc.model.ActivatedTopicAssessmentRef;
import app.reading.lifearc.model.AnnualProfectionEntry;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;

public final class DorotheanAnnualProfectionCalculator {
    public static final String METHOD_ID = "DOROTHEAN_ANNUAL_PROFECTION_V1";
    private static final String PRIMARY_DOCTRINE = "dorothean";
    private static final List<AnnualProfectionReference> TABLE_REFERENCE_ORDER = List.of(
            AnnualProfectionReference.ASCENDANT,
            AnnualProfectionReference.MIDHEAVEN,
            AnnualProfectionReference.SUN,
            AnnualProfectionReference.MOON,
            AnnualProfectionReference.LOT_FORTUNE,
            AnnualProfectionReference.LOT_SPIRIT
    );

    public AnnualProfectionEntry calculate(Subject subject, NatalChart chart, LocalDate inquiryDate) {
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }

        AnnualPeriod period = annualPeriod(birthDate, inquiryDate);
        int yearInCycle = Math.floorMod(period.ageYears(), 12) + 1;
        int profectedHouse = yearInCycle;
        ZodiacSign profectedSign = signForHouse(chart, profectedHouse);
        Planet lordOfYear = TraditionalTables.domicileRuler(profectedSign);

        Set<String> activeConditionRefs = new LinkedHashSet<>();
        activeConditionRefs.add(houseTopicRef(profectedHouse));
        activeConditionRefs.add(planetRef(lordOfYear));

        List<ActivatedNatalPointEntry> activatedPoints = activatedPoints(chart, profectedSign, activeConditionRefs);
        List<ActivatedLotEntry> activatedLots = activatedLots(chart, profectedSign, activeConditionRefs);
        List<ActivatedTopicAssessmentRef> topicRefs = activatedTopicAssessmentRefs(chart, activeConditionRefs);

        long daysElapsed = ChronoUnit.DAYS.between(period.startDate(), inquiryDate);
        long daysRemaining = ChronoUnit.DAYS.between(inquiryDate, period.endDateExclusive());

        return new AnnualProfectionEntry(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                period.startDate(),
                period.endDateExclusive(),
                period.ageYears(),
                (period.ageYears() / 12) + 1,
                yearInCycle,
                profectedHouse,
                profectedSign,
                lordOfYear,
                daysElapsed,
                daysRemaining,
                activatedPoints,
                activatedLots,
                topicRefs
        );
    }

    public AnnualProfectionTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }

        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        int activeAgeYears = -1;
        if (inquiryDate != null) {
            if (inquiryDate.isBefore(birthDate)) {
                throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
            }
            activeAgeYears = annualPeriod(birthDate, inquiryDate).ageYears();
        }

        List<AnnualProfectionTableRow> rows = new ArrayList<>();
        for (int ageYears = ageStartYears; ageYears <= ageEndYearsInclusive; ageYears++) {
            rows.add(tableRow(chart, birthDate, ageYears, activeAgeYears));
        }

        return new AnnualProfectionTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                ageStartYears,
                ageEndYearsInclusive,
                TABLE_REFERENCE_ORDER,
                rows
        );
    }

    private AnnualProfectionTableRow tableRow(NatalChart chart, LocalDate birthDate, int ageYears, int activeAgeYears) {
        LocalDate startDate = birthAnniversary(birthDate, birthDate.getYear() + ageYears);
        LocalDate endDate = birthAnniversary(birthDate, birthDate.getYear() + ageYears + 1);
        int yearInCycle = Math.floorMod(ageYears, 12) + 1;
        int cycleNumber = (ageYears / 12) + 1;

        List<AnnualProfectionReferenceEntry> referenceProfections = new ArrayList<>();
        for (AnnualProfectionReference reference : TABLE_REFERENCE_ORDER) {
            referenceProfections.add(referenceProfection(chart, reference, ageYears));
        }

        return new AnnualProfectionTableRow(
                ageYears,
                startDate,
                endDate,
                cycleNumber,
                yearInCycle,
                ageYears == activeAgeYears,
                referenceProfections
        );
    }

    private AnnualProfectionReferenceEntry referenceProfection(NatalChart chart, AnnualProfectionReference reference, int ageYears) {
        ZodiacSign natalSign = natalSign(chart, reference);
        ZodiacSign profectedSign = advanceSign(natalSign, ageYears);
        Planet lord = TraditionalTables.domicileRuler(profectedSign);
        return new AnnualProfectionReferenceEntry(
                reference,
                natalSign,
                profectedSign,
                houseForSign(chart, profectedSign),
                lord
        );
    }

    private ZodiacSign natalSign(NatalChart chart, AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> sign(point(chart, PointKey.ASCENDANT));
            case MIDHEAVEN -> sign(point(chart, PointKey.MIDHEAVEN));
            case SUN -> sign(point(chart, PointKey.SUN));
            case MOON -> sign(point(chart, PointKey.MOON));
            case LOT_FORTUNE -> lotSign(chart, "FORTUNE");
            case LOT_SPIRIT -> lotSign(chart, "SPIRIT");
        };
    }

    private PointEntry point(NatalChart chart, PointKey point) {
        PointEntry entry = chart.getPoints().get(point);
        if (entry == null) {
            throw new IllegalArgumentException("Missing natal point " + point);
        }
        return entry;
    }

    private ZodiacSign lotSign(NatalChart chart, String lotName) {
        if (chart.getLots() == null) {
            throw new IllegalArgumentException("Missing natal lots");
        }
        return chart.getLots().stream()
                .filter(lot -> lotName.equals(lot.name()))
                .map(LotEntry::sign)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal lot " + lotName));
    }

    private ZodiacSign advanceSign(ZodiacSign sign, int signs) {
        ZodiacSign[] values = ZodiacSign.values();
        return values[Math.floorMod(sign.ordinal() + signs, values.length)];
    }

    private AnnualPeriod annualPeriod(LocalDate birthDate, LocalDate inquiryDate) {
        LocalDate start = birthAnniversary(birthDate, inquiryDate.getYear());
        if (inquiryDate.isBefore(start)) {
            start = birthAnniversary(birthDate, inquiryDate.getYear() - 1);
        }
        LocalDate end = birthAnniversary(birthDate, start.getYear() + 1);
        int ageYears = start.getYear() - birthDate.getYear();
        return new AnnualPeriod(start, end, ageYears);
    }

    private LocalDate birthAnniversary(LocalDate birthDate, int year) {
        return MonthDay.from(birthDate).atYear(year);
    }

    private ZodiacSign signForHouse(NatalChart chart, int house) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getHouse() == house)
                .map(HousePosition::getSign)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house " + house));
    }

    private Integer houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElse(null);
    }

    private List<ActivatedNatalPointEntry> activatedPoints(NatalChart chart, ZodiacSign activatedSign, Set<String> activeConditionRefs) {
        List<ActivatedNatalPointEntry> entries = new ArrayList<>();
        for (Map.Entry<PointKey, PointEntry> pointEntry : chart.getPoints().entrySet()) {
            PointEntry point = pointEntry.getValue();
            ZodiacSign pointSign = sign(point);
            if (pointSign != activatedSign) {
                continue;
            }
            Integer house = house(point, chart, pointSign);
            String conditionRef = conditionRef(pointEntry.getKey());
            if (conditionRef != null) {
                activeConditionRefs.add(conditionRef);
            }
            entries.add(new ActivatedNatalPointEntry(pointEntry.getKey(), point.getType(), pointSign, house));
        }
        return List.copyOf(entries);
    }

    private List<ActivatedLotEntry> activatedLots(NatalChart chart, ZodiacSign activatedSign, Set<String> activeConditionRefs) {
        if (chart.getLots() == null) {
            return List.of();
        }
        List<ActivatedLotEntry> entries = new ArrayList<>();
        for (LotEntry lot : chart.getLots()) {
            if (lot.sign() != activatedSign) {
                continue;
            }
            String lotAssessmentRef = lotAssessmentRef(lot.name());
            activeConditionRefs.add(lotAssessmentRef);
            activeConditionRefs.add(planetRef(lot.ruler()));
            entries.add(new ActivatedLotEntry(
                    lot.name(),
                    lot.displayName(),
                    lot.doctrine(),
                    lot.sign(),
                    lot.house(),
                    lot.ruler(),
                    lotAssessmentRef
            ));
        }
        return List.copyOf(entries);
    }

    private List<ActivatedTopicAssessmentRef> activatedTopicAssessmentRefs(NatalChart chart, Set<String> activeConditionRefs) {
        if (chart.getTopicAssessments() == null) {
            return List.of();
        }
        List<ActivatedTopicAssessmentRef> entries = new ArrayList<>();
        for (TopicAssessmentEntry topic : chart.getTopicAssessments()) {
            Set<String> matches = new LinkedHashSet<>();
            for (TopicEvidenceEntry evidence : topic.evidence()) {
                String conditionRef = evidence.conditionRef();
                if (conditionRef != null && activeConditionRefs.contains(conditionRef)) {
                    matches.add(conditionRef);
                }
            }
            if (!matches.isEmpty()) {
                entries.add(new ActivatedTopicAssessmentRef(topic.topic(), topic.methodId(), List.copyOf(matches)));
            }
        }
        return List.copyOf(entries);
    }

    private ZodiacSign sign(PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint.sign();
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return anglePoint.sign();
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
    }

    private Integer house(PointEntry point, NatalChart chart, ZodiacSign sign) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint.house();
        }
        return houseForSign(chart, sign);
    }

    private String conditionRef(PointKey point) {
        Planet planet = traditionalPlanet(point);
        if (planet == null) {
            return null;
        }
        return planetRef(planet);
    }

    private Planet traditionalPlanet(PointKey point) {
        return switch (point) {
            case SUN -> Planet.SUN;
            case MOON -> Planet.MOON;
            case MERCURY -> Planet.MERCURY;
            case VENUS -> Planet.VENUS;
            case MARS -> Planet.MARS;
            case JUPITER -> Planet.JUPITER;
            case SATURN -> Planet.SATURN;
            case NORTH_NODE, SOUTH_NODE, ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI -> null;
        };
    }

    private String houseTopicRef(int house) {
        return "houseTopicRulers.house=" + house;
    }

    private String planetRef(Planet planet) {
        return planet.name();
    }

    private String lotAssessmentRef(String lotName) {
        return "lotAssessments.lot=" + lotName;
    }

    private record AnnualPeriod(LocalDate startDate, LocalDate endDateExclusive, int ageYears) {}
}
