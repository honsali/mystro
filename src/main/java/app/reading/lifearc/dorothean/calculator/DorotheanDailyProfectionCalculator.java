package app.reading.lifearc.dorothean.calculator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import app.reading.lifearc.model.DailyProfectionActivatedLot;
import app.reading.lifearc.model.DailyProfectionActivatedPoint;
import app.reading.lifearc.model.DailyProfectionReferenceEntry;
import app.reading.lifearc.model.DailyProfectionTable;
import app.reading.lifearc.model.DailyProfectionTableRow;

public final class DorotheanDailyProfectionCalculator {
    public static final String METHOD_ID = "DOROTHEAN_DAILY_PROFECTION_V1";
    private static final String PRIMARY_DOCTRINE = "dorothean";
    private static final String DAILY_STEP_METHOD = "DAILY_SIGN_ADVANCE_FROM_ACTIVE_MONTHLY_PROFECTION; ONE SIGN PER LOCAL DAY AT NATAL BIRTH TIME";
    private static final List<AnnualProfectionReference> TABLE_REFERENCE_ORDER = List.of(
            AnnualProfectionReference.ASCENDANT,
            AnnualProfectionReference.MIDHEAVEN,
            AnnualProfectionReference.SUN,
            AnnualProfectionReference.MOON,
            AnnualProfectionReference.LOT_FORTUNE,
            AnnualProfectionReference.LOT_SPIRIT
    );

    public DailyProfectionTable calculateWindow(Subject subject, NatalChart chart, LocalDate focusDate, int radiusDays) {
        if (subject == null) {
            throw new IllegalArgumentException("subject is required");
        }
        if (chart == null) {
            throw new IllegalArgumentException("chart is required");
        }
        if (focusDate == null) {
            throw new IllegalArgumentException("focusDate is required");
        }
        if (radiusDays < 0) {
            throw new IllegalArgumentException("radiusDays must be zero or greater");
        }
        LocalDate birthDate = subject.getLocalBirthDateTime().toLocalDate();
        if (focusDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("focusDate must be on or after birthDate");
        }

        LocalDate windowStart = focusDate.minusDays(radiusDays);
        LocalDate windowEnd = focusDate.plusDays(radiusDays);
        LocalDate firstDate = windowStart.isBefore(birthDate) ? birthDate : windowStart;
        List<DailyProfectionTableRow> rows = new ArrayList<>();
        for (LocalDate date = firstDate; !date.isAfter(windowEnd); date = date.plusDays(1)) {
            rows.add(row(subject, chart, focusDate, date));
        }

        return new DailyProfectionTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                DAILY_STEP_METHOD,
                focusDate,
                firstDate,
                windowEnd,
                TABLE_REFERENCE_ORDER,
                rows
        );
    }

    private DailyProfectionTableRow row(Subject subject, NatalChart chart, LocalDate focusDate, LocalDate date) {
        OffsetDateTime startDateTime = OffsetDateTime.of(
                date,
                subject.getLocalBirthDateTime().toLocalTime(),
                subject.getLocalBirthDateTime().getOffset()
        );
        OffsetDateTime endDateTime = startDateTime.plusDays(1);
        MonthlyPosition monthlyPosition = monthlyPosition(subject.getLocalBirthDateTime(), startDateTime);
        int ageYears = monthlyPosition.ageYears();
        int monthIndex = monthlyPosition.monthIndex();
        int dayIndex = (int) ChronoUnit.DAYS.between(monthlyPosition.monthStartDateTime(), startDateTime);
        int cycleNumber = (ageYears / 12) + 1;
        int yearInCycle = Math.floorMod(ageYears, 12) + 1;
        int signSteps = ageYears + monthIndex + dayIndex;

        List<DailyProfectionReferenceEntry> referenceProfections = new ArrayList<>();
        for (AnnualProfectionReference reference : TABLE_REFERENCE_ORDER) {
            referenceProfections.add(referenceProfection(chart, reference, ageYears, monthIndex, signSteps));
        }

        DailyProfectionReferenceEntry ascendant = referenceProfections.stream()
                .filter(entry -> entry.reference() == AnnualProfectionReference.ASCENDANT)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing Ascendant daily profection"));
        List<DailyProfectionActivatedPoint> activatedPoints = activatedPoints(chart, ascendant.profectedSign(), ascendant.profectedHouse());
        List<DailyProfectionActivatedLot> activatedLots = activatedLots(chart, ascendant.profectedSign(), ascendant.profectedHouse());

        return new DailyProfectionTableRow(
                date,
                startDateTime,
                endDateTime,
                ageYears,
                cycleNumber,
                yearInCycle,
                monthIndex + 1,
                dayIndex + 1,
                date.equals(focusDate),
                referenceProfections,
                activatedPoints,
                activatedLots
        );
    }

    private MonthlyPosition monthlyPosition(OffsetDateTime birthDateTime, OffsetDateTime dateTime) {
        long totalMonths = ChronoUnit.MONTHS.between(birthDateTime, dateTime);
        while (birthDateTime.plusMonths(totalMonths + 1L).compareTo(dateTime) <= 0) {
            totalMonths++;
        }
        while (birthDateTime.plusMonths(totalMonths).isAfter(dateTime)) {
            totalMonths--;
        }
        if (totalMonths < 0) {
            throw new IllegalArgumentException("dateTime must be on or after birthDateTime");
        }
        int ageYears = Math.toIntExact(totalMonths / 12L);
        int monthIndex = Math.toIntExact(Math.floorMod(totalMonths, 12L));
        OffsetDateTime monthStartDateTime = birthDateTime.plusMonths(totalMonths);
        return new MonthlyPosition(ageYears, monthIndex, monthStartDateTime);
    }

    private DailyProfectionReferenceEntry referenceProfection(NatalChart chart,
                                                              AnnualProfectionReference reference,
                                                              int ageYears,
                                                              int monthIndex,
                                                              int signSteps) {
        ZodiacSign natalSign = natalSign(chart, reference);
        ZodiacSign annualSign = advanceSign(natalSign, ageYears);
        ZodiacSign monthlySign = advanceSign(natalSign, ageYears + monthIndex);
        ZodiacSign profectedSign = advanceSign(natalSign, signSteps);
        Planet annualLord = TraditionalTables.domicileRuler(annualSign);
        Planet monthlyLord = TraditionalTables.domicileRuler(monthlySign);
        Planet lord = TraditionalTables.domicileRuler(profectedSign);
        return new DailyProfectionReferenceEntry(
                reference,
                natalSign,
                annualSign,
                houseForSign(chart, annualSign),
                annualLord,
                monthlySign,
                houseForSign(chart, monthlySign),
                monthlyLord,
                profectedSign,
                houseForSign(chart, profectedSign),
                lord
        );
    }

    private List<DailyProfectionActivatedPoint> activatedPoints(NatalChart chart, ZodiacSign sign, Integer house) {
        List<DailyProfectionActivatedPoint> entries = new ArrayList<>();
        for (Map.Entry<PointKey, PointEntry> entry : chart.getPoints().entrySet()) {
            PointPlacement placement = pointPlacement(chart, entry.getValue());
            if (placement.sign() == sign || (house != null && placement.house() == house)) {
                entries.add(new DailyProfectionActivatedPoint(
                        entry.getKey(),
                        entry.getValue().getType(),
                        placement.sign(),
                        placement.house()
                ));
            }
        }
        return List.copyOf(entries);
    }

    private List<DailyProfectionActivatedLot> activatedLots(NatalChart chart, ZodiacSign sign, Integer house) {
        if (chart.getLots() == null) {
            return List.of();
        }
        List<DailyProfectionActivatedLot> entries = new ArrayList<>();
        for (LotEntry lot : chart.getLots()) {
            if (lot.sign() == sign || (house != null && lot.house() == house)) {
                entries.add(new DailyProfectionActivatedLot(
                        lot.name(),
                        lot.displayName(),
                        lot.sign(),
                        lot.house(),
                        lot.ruler()
                ));
            }
        }
        return List.copyOf(entries);
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

    private PointPlacement pointPlacement(NatalChart chart, PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return new PointPlacement(planetPoint.sign(), planetPoint.house());
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return new PointPlacement(anglePoint.sign(), houseForSign(chart, anglePoint.sign()));
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

    private record MonthlyPosition(int ageYears, int monthIndex, OffsetDateTime monthStartDateTime) {}

    private record PointPlacement(ZodiacSign sign, int house) {}
}
