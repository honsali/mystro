package app.reading.lifearc.dorothean.calculator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;

public final class DorotheanMonthlyProfectionCalculator {
    public static final String METHOD_ID = "DOROTHEAN_MONTHLY_PROFECTION_V1";
    private static final String PRIMARY_DOCTRINE = "dorothean";
    private static final List<AnnualProfectionReference> TABLE_REFERENCE_ORDER = List.of(
            AnnualProfectionReference.ASCENDANT,
            AnnualProfectionReference.MIDHEAVEN,
            AnnualProfectionReference.SUN,
            AnnualProfectionReference.MOON,
            AnnualProfectionReference.LOT_FORTUNE,
            AnnualProfectionReference.LOT_SPIRIT
    );

    public MonthlyProfectionTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                 int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }

        OffsetDateTime birthDateTime = subject.getLocalBirthDateTime();
        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);

        List<MonthlyProfectionTableRow> rows = new ArrayList<>();
        for (int ageYears = ageStartYears; ageYears <= ageEndYearsInclusive; ageYears++) {
            for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
                rows.add(tableRow(chart, birthDateTime, activeDateTime, ageYears, monthIndex));
            }
        }

        return new MonthlyProfectionTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                ageStartYears,
                ageEndYearsInclusive,
                TABLE_REFERENCE_ORDER,
                rows
        );
    }

    private MonthlyProfectionTableRow tableRow(NatalChart chart, OffsetDateTime birthDateTime,
                                               OffsetDateTime activeDateTime, int ageYears, int monthIndex) {
        long totalMonths = ageYears * 12L + monthIndex;
        OffsetDateTime startDateTime = birthDateTime.plusMonths(totalMonths);
        OffsetDateTime endDateTime = birthDateTime.plusMonths(totalMonths + 1L);
        int yearInCycle = Math.floorMod(ageYears, 12) + 1;
        int cycleNumber = (ageYears / 12) + 1;
        int signSteps = ageYears + monthIndex;

        List<MonthlyProfectionReferenceEntry> referenceProfections = new ArrayList<>();
        for (AnnualProfectionReference reference : TABLE_REFERENCE_ORDER) {
            referenceProfections.add(referenceProfection(chart, reference, ageYears, signSteps));
        }

        return new MonthlyProfectionTableRow(
                ageYears,
                startDateTime,
                endDateTime,
                cycleNumber,
                yearInCycle,
                monthIndex + 1,
                active(startDateTime, endDateTime, activeDateTime),
                referenceProfections
        );
    }

    private MonthlyProfectionReferenceEntry referenceProfection(NatalChart chart, AnnualProfectionReference reference,
                                                               int ageYears, int signSteps) {
        ZodiacSign natalSign = natalSign(chart, reference);
        ZodiacSign annualSign = advanceSign(natalSign, ageYears);
        ZodiacSign profectedSign = advanceSign(natalSign, signSteps);
        Planet lord = TraditionalTables.domicileRuler(profectedSign);
        return new MonthlyProfectionReferenceEntry(
                reference,
                natalSign,
                annualSign,
                profectedSign,
                houseForSign(chart, profectedSign),
                lord
        );
    }

    private OffsetDateTime activeDateTime(Subject subject, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            return null;
        }
        LocalDate birthDate = subject.getLocalBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }
        return OffsetDateTime.of(
                inquiryDate,
                subject.getLocalBirthDateTime().toLocalTime(),
                subject.getLocalBirthDateTime().getOffset()
        );
    }

    private boolean active(OffsetDateTime startDateTime, OffsetDateTime endDateTime, OffsetDateTime activeDateTime) {
        return activeDateTime != null
                && !activeDateTime.isBefore(startDateTime)
                && activeDateTime.isBefore(endDateTime);
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

    private ZodiacSign sign(PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint.sign();
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return anglePoint.sign();
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
    }

    private Integer houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElse(null);
    }

    private ZodiacSign advanceSign(ZodiacSign sign, int signs) {
        ZodiacSign[] values = ZodiacSign.values();
        return values[Math.floorMod(sign.ordinal() + signs, values.length)];
    }
}
