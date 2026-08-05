package app.reading.lifearc.transit;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.TraditionalTables;
import app.chart.data.AspectType;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.model.LotEntry;

public final class MonthlyTransitCheckpointCalculator {
    public static final String METHOD_ID = "MONTHLY_TRANSIT_CHECKPOINTS_V1";
    private static final String PRIMARY_DOCTRINE = "traditional";
    private static final String CHECKPOINT_METHOD = "BIRTH_LOCAL_DATE_TIME_PLUS_N_MONTHS; SHORT_MONTHS_USE_JAVA_LOCAL_DATE_PLUS_MONTHS_END_OF_MONTH_CONVENTION";
    private static final String CONTACT_METHOD = "TRANSITING_TRADITIONAL_PLANETS_AND_MEAN_NODES_TO_NATAL_POINTS_AND_EMITTED_LOTS_BY_ECLIPTIC_LONGITUDE_CONJUNCTION_WITHIN_3_DEGREES";
    private static final String ACTIVATION_CONTACT_METHOD = "MONTHLY_CHECKPOINT_TRANSITING_TRADITIONAL_PLANETS_AND_MEAN_NODES_TO_PROFECTION_ACTIVATED_NATAL_POINTS_AND_LOTS_BY_PTOLEMAIC_DEGREE_ASPECT_WITHIN_3_DEGREES; RETAIN_CONTACTS_WHEN_TARGET_OR_TRANSITING_POINT_IS_ACTIVATED_BY_ANNUAL_OR_MONTHLY_PROFECTION";
    private static final double CONJUNCTION_ORB_DEGREES = 3.0;
    private static final double ACTIVATION_ASPECT_ORB_DEGREES = 3.0;
    private static final List<PointKey> TRANSIT_POINT_ORDER = List.of(
            PointKey.SUN,
            PointKey.MOON,
            PointKey.MERCURY,
            PointKey.VENUS,
            PointKey.MARS,
            PointKey.JUPITER,
            PointKey.SATURN,
            PointKey.NORTH_NODE,
            PointKey.SOUTH_NODE
    );
    private static final CoreDoctrineInfo TRANSIT_CONVENTIONS = new CoreDoctrineInfo(
            "monthly_transit_checkpoint",
            "Monthly Transit Checkpoint",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final BasicCalculator basicCalculator = new BasicCalculator();

    public MonthlyTransitCheckpointTable calculateTable(Subject subject, NatalChart natalChart, LocalDate inquiryDate,
                                                        int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }

        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        List<NatalTarget> natalTargets = natalTargets(natalChart);
        List<MonthlyTransitCheckpointRow> rows = new ArrayList<>();
        int checkpointNumber = 1;
        for (int ageYears = ageStartYears; ageYears <= ageEndYearsInclusive; ageYears++) {
            for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
                rows.add(row(subject, natalChart, natalTargets, activeDateTime, checkpointNumber, ageYears, monthIndex));
                checkpointNumber++;
            }
        }

        return new MonthlyTransitCheckpointTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                CHECKPOINT_METHOD,
                CONTACT_METHOD,
                ACTIVATION_CONTACT_METHOD,
                CONJUNCTION_ORB_DEGREES,
                ACTIVATION_ASPECT_ORB_DEGREES,
                ageStartYears,
                ageEndYearsInclusive,
                rows
        );
    }

    private MonthlyTransitCheckpointRow row(Subject subject, NatalChart natalChart, List<NatalTarget> natalTargets,
                                            OffsetDateTime activeDateTime, int checkpointNumber,
                                            int ageYears, int monthIndex) {
        long totalMonths = ageYears * 12L + monthIndex;
        OffsetDateTime checkpointDateTime = subject.getLocalBirthDateTime().plusMonths(totalMonths);
        OffsetDateTime periodEndDateTime = subject.getLocalBirthDateTime().plusMonths(totalMonths + 1L);

        int annualProfectedHouse = Math.floorMod(ageYears, 12) + 1;
        ZodiacSign annualProfectedSign = signForHouse(natalChart, annualProfectedHouse);
        Planet lordOfYear = TraditionalTables.domicileRuler(annualProfectedSign);

        ZodiacSign monthlyProfectedSign = advanceSign(signForHouse(natalChart, 1), ageYears + monthIndex);
        int monthlyProfectedHouse = houseForSign(natalChart, monthlyProfectedSign);
        Planet lordOfMonth = TraditionalTables.domicileRuler(monthlyProfectedSign);

        NatalChart transitChart = transitChart(subject, checkpointDateTime, checkpointNumber);
        List<MonthlyTransitPointEntry> transitPoints = transitPoints(natalChart, transitChart);
        List<PointKey> transitPointsInAnnualProfectedSign = transitPoints.stream()
                .filter(point -> point.sign() == annualProfectedSign)
                .map(MonthlyTransitPointEntry::point)
                .toList();
        List<PointKey> transitPointsInMonthlyProfectedSign = transitPoints.stream()
                .filter(point -> point.sign() == monthlyProfectedSign)
                .map(MonthlyTransitPointEntry::point)
                .toList();
        List<PointKey> transitPointsOverlayingAnnualProfectedHouse = transitPoints.stream()
                .filter(point -> point.natalHouseOverlay() == annualProfectedHouse)
                .map(MonthlyTransitPointEntry::point)
                .toList();
        List<PointKey> transitPointsOverlayingMonthlyProfectedHouse = transitPoints.stream()
                .filter(point -> point.natalHouseOverlay() == monthlyProfectedHouse)
                .map(MonthlyTransitPointEntry::point)
                .toList();

        return new MonthlyTransitCheckpointRow(
                checkpointNumber,
                ageYears,
                monthIndex + 1,
                checkpointDateTime,
                periodEndDateTime,
                active(checkpointDateTime, periodEndDateTime, activeDateTime),
                annualProfectedHouse,
                annualProfectedSign,
                lordOfYear,
                monthlyProfectedHouse,
                monthlyProfectedSign,
                lordOfMonth,
                transitPoints,
                transitPointsInAnnualProfectedSign,
                transitPointsInMonthlyProfectedSign,
                transitPointsOverlayingAnnualProfectedHouse,
                transitPointsOverlayingMonthlyProfectedHouse,
                conjunctions(transitPoints, natalTargets),
                activationContacts(
                        transitPoints,
                        natalTargets,
                        annualProfectedHouse,
                        annualProfectedSign,
                        lordOfYear,
                        monthlyProfectedHouse,
                        monthlyProfectedSign,
                        lordOfMonth
                )
        );
    }

    private NatalChart transitChart(Subject subject, OffsetDateTime checkpointDateTime, int checkpointNumber) {
        Subject checkpointSubject = new Subject(
                subject.getId() + "-monthly-transit-checkpoint-" + checkpointNumber,
                checkpointDateTime,
                checkpointDateTime.toInstant(),
                subject.getLatitude(),
                subject.getLongitude(),
                subject.getElevationMeters()
        );
        return basicCalculator.calculate(new CalculationContext(checkpointSubject, TRANSIT_CONVENTIONS));
    }

    private List<MonthlyTransitPointEntry> transitPoints(NatalChart natalChart, NatalChart transitChart) {
        List<MonthlyTransitPointEntry> entries = new ArrayList<>();
        for (PointKey key : TRANSIT_POINT_ORDER) {
            PointEntry point = transitChart.getPoints().get(key);
            if (!(point instanceof PlanetPointEntry planetPoint)) {
                throw new IllegalArgumentException("Missing transit point " + key);
            }
            entries.add(new MonthlyTransitPointEntry(
                    key,
                    planetPoint.getType(),
                    planetPoint.longitude(),
                    planetPoint.sign(),
                    planetPoint.degreeInSign(),
                    planetPoint.house(),
                    houseForSign(natalChart, planetPoint.sign()),
                    planetPoint.retrograde()
            ));
        }
        return List.copyOf(entries);
    }

    private List<MonthlyTransitNatalContact> conjunctions(List<MonthlyTransitPointEntry> transitPoints,
                                                          List<NatalTarget> natalTargets) {
        List<MonthlyTransitNatalContact> entries = new ArrayList<>();
        for (MonthlyTransitPointEntry transitPoint : transitPoints) {
            for (NatalTarget target : natalTargets) {
                double orb = AstroMath.rawAngularSeparation(transitPoint.longitude(), target.longitude());
                if (orb <= CONJUNCTION_ORB_DEGREES) {
                    entries.add(new MonthlyTransitNatalContact(
                            transitPoint.point(),
                            target.type(),
                            target.name(),
                            target.sign(),
                            target.degreeInSign(),
                            target.house(),
                            orb
                    ));
                }
            }
        }
        return List.copyOf(entries);
    }

    private List<MonthlyTransitActivationContact> activationContacts(List<MonthlyTransitPointEntry> transitPoints,
                                                                     List<NatalTarget> natalTargets,
                                                                     int annualProfectedHouse,
                                                                     ZodiacSign annualProfectedSign,
                                                                     Planet lordOfYear,
                                                                     int monthlyProfectedHouse,
                                                                     ZodiacSign monthlyProfectedSign,
                                                                     Planet lordOfMonth) {
        List<MonthlyTransitActivationContact> contacts = new ArrayList<>();
        for (MonthlyTransitPointEntry transitPoint : transitPoints) {
            List<MonthlyTransitActivationReason> transitReasons = transitActivationReasons(
                    transitPoint,
                    annualProfectedHouse,
                    annualProfectedSign,
                    lordOfYear,
                    monthlyProfectedHouse,
                    monthlyProfectedSign,
                    lordOfMonth
            );
            for (NatalTarget target : natalTargets) {
                List<MonthlyTransitActivationReason> targetReasons = targetActivationReasons(
                        target,
                        annualProfectedHouse,
                        annualProfectedSign,
                        lordOfYear,
                        monthlyProfectedHouse,
                        monthlyProfectedSign,
                        lordOfMonth
                );
                if (transitReasons.isEmpty() && targetReasons.isEmpty()) {
                    continue;
                }
                AspectHit aspectHit = nearestActivationAspect(transitPoint.longitude(), target.longitude());
                if (aspectHit == null) {
                    continue;
                }
                List<MonthlyTransitActivationReason> reasons = new ArrayList<>(targetReasons);
                reasons.addAll(transitReasons);
                contacts.add(new MonthlyTransitActivationContact(
                        transitPoint.point(),
                        transitPoint.point() == PointKey.of(lordOfYear),
                        transitPoint.point() == PointKey.of(lordOfMonth),
                        target.type(),
                        target.name(),
                        target.sign(),
                        target.degreeInSign(),
                        target.house(),
                        aspectHit.aspect(),
                        aspectHit.angularSeparation(),
                        aspectHit.orbFromExactDegrees(),
                        reasons,
                        activationWeight(reasons)
                ));
            }
        }
        return contacts.stream()
                .sorted(Comparator
                        .comparingInt(MonthlyTransitActivationContact::activationWeight).reversed()
                        .thenComparing(contact -> contact.transitPoint().ordinal())
                        .thenComparing(contact -> contact.aspect().ordinal())
                        .thenComparingDouble(MonthlyTransitActivationContact::orbFromExactDegrees)
                        .thenComparing(MonthlyTransitActivationContact::natalTargetName))
                .toList();
    }

    private List<MonthlyTransitActivationReason> transitActivationReasons(MonthlyTransitPointEntry transitPoint,
                                                                          int annualProfectedHouse,
                                                                          ZodiacSign annualProfectedSign,
                                                                          Planet lordOfYear,
                                                                          int monthlyProfectedHouse,
                                                                          ZodiacSign monthlyProfectedSign,
                                                                          Planet lordOfMonth) {
        List<MonthlyTransitActivationReason> reasons = new ArrayList<>();
        if (transitPoint.point() == PointKey.of(lordOfYear)) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_LORD_OF_YEAR);
        }
        if (transitPoint.point() == PointKey.of(lordOfMonth)) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_LORD_OF_MONTH);
        }
        if (transitPoint.sign() == annualProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_IN_ANNUAL_PROFECTED_SIGN);
        }
        if (transitPoint.sign() == monthlyProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_IN_MONTHLY_PROFECTED_SIGN);
        }
        if (transitPoint.natalHouseOverlay() == annualProfectedHouse) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_OVERLAYING_ANNUAL_PROFECTED_HOUSE);
        }
        if (transitPoint.natalHouseOverlay() == monthlyProfectedHouse) {
            reasons.add(MonthlyTransitActivationReason.TRANSIT_OVERLAYING_MONTHLY_PROFECTED_HOUSE);
        }
        return List.copyOf(reasons);
    }

    private List<MonthlyTransitActivationReason> targetActivationReasons(NatalTarget target,
                                                                         int annualProfectedHouse,
                                                                         ZodiacSign annualProfectedSign,
                                                                         Planet lordOfYear,
                                                                         int monthlyProfectedHouse,
                                                                         ZodiacSign monthlyProfectedSign,
                                                                         Planet lordOfMonth) {
        List<MonthlyTransitActivationReason> reasons = new ArrayList<>();
        if (target.sign() == annualProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TARGET_ANNUAL_PROFECTED_SIGN);
        }
        if (target.house() == annualProfectedHouse) {
            reasons.add(MonthlyTransitActivationReason.TARGET_ANNUAL_PROFECTED_HOUSE);
        }
        if (target.sign() == monthlyProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TARGET_MONTHLY_PROFECTED_SIGN);
        }
        if (target.house() == monthlyProfectedHouse) {
            reasons.add(MonthlyTransitActivationReason.TARGET_MONTHLY_PROFECTED_HOUSE);
        }
        if (target.pointKey() == PointKey.of(lordOfYear)) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LORD_OF_YEAR_NATAL_POINT);
        }
        if (target.pointKey() == PointKey.of(lordOfMonth)) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LORD_OF_MONTH_NATAL_POINT);
        }
        if (target.type() == TransitNatalTargetType.LOT && target.sign() == annualProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LOT_IN_ANNUAL_PROFECTED_SIGN);
        }
        if (target.type() == TransitNatalTargetType.LOT && target.sign() == monthlyProfectedSign) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LOT_IN_MONTHLY_PROFECTED_SIGN);
        }
        return List.copyOf(reasons);
    }

    private AspectHit nearestActivationAspect(double transitLongitude, double targetLongitude) {
        double angularSeparation = AstroMath.rawAngularSeparation(transitLongitude, targetLongitude);
        AspectType nearest = null;
        double nearestOrb = Double.MAX_VALUE;
        for (AspectType aspect : AspectType.values()) {
            double orb = Math.abs(angularSeparation - aspect.getExactAngle());
            if (orb < nearestOrb) {
                nearest = aspect;
                nearestOrb = orb;
            }
        }
        if (nearest == null || nearestOrb > ACTIVATION_ASPECT_ORB_DEGREES) {
            return null;
        }
        return new AspectHit(nearest, angularSeparation, nearestOrb);
    }

    private int activationWeight(List<MonthlyTransitActivationReason> reasons) {
        return reasons.size();
    }

    private List<NatalTarget> natalTargets(NatalChart natalChart) {
        List<NatalTarget> targets = new ArrayList<>();
        for (var entry : natalChart.getPoints().entrySet()) {
            PointPlacement placement = placement(entry.getValue(), natalChart);
            targets.add(new NatalTarget(
                    TransitNatalTargetType.POINT,
                    entry.getKey().name(),
                    entry.getKey(),
                    placement.longitude(),
                    placement.sign(),
                    placement.degreeInSign(),
                    placement.house()
            ));
        }
        if (natalChart.getLots() != null) {
            for (LotEntry lot : natalChart.getLots()) {
                targets.add(new NatalTarget(
                        TransitNatalTargetType.LOT,
                        "LOT_" + lot.name(),
                        null,
                        lot.longitude(),
                        lot.sign(),
                        lot.degreeInSign(),
                        lot.house()
                ));
            }
        }
        return List.copyOf(targets);
    }

    private PointPlacement placement(PointEntry point, NatalChart natalChart) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return new PointPlacement(
                    planetPoint.longitude(),
                    planetPoint.sign(),
                    planetPoint.degreeInSign(),
                    planetPoint.house()
            );
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return new PointPlacement(
                    anglePoint.longitude(),
                    anglePoint.sign(),
                    anglePoint.degreeInSign(),
                    houseForSign(natalChart, anglePoint.sign())
            );
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
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

    private ZodiacSign signForHouse(NatalChart chart, int house) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getHouse() == house)
                .map(HousePosition::getSign)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house " + house));
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    private ZodiacSign advanceSign(ZodiacSign sign, int signs) {
        ZodiacSign[] values = ZodiacSign.values();
        return values[Math.floorMod(sign.ordinal() + signs, values.length)];
    }

    private record NatalTarget(TransitNatalTargetType type, String name, PointKey pointKey, double longitude,
                               ZodiacSign sign, double degreeInSign, int house) {}

    private record PointPlacement(double longitude, ZodiacSign sign, double degreeInSign, int house) {}

    private record AspectHit(AspectType aspect, double angularSeparation, double orbFromExactDegrees) {}
}
