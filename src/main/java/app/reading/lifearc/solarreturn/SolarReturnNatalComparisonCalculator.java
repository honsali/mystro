package app.reading.lifearc.solarreturn;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import app.chart.AstroMath;
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

public final class SolarReturnNatalComparisonCalculator {
    public static final String METHOD_ID = "SOLAR_RETURN_TO_NATAL_COMPARISON_V1";
    private static final String PRIMARY_DOCTRINE = "traditional";
    private static final String NATAL_OVERLAY_METHOD = "SOLAR_RETURN_POINTS_OVER_NATAL_WHOLE_SIGN_HOUSES; ECLIPTIC_LONGITUDE_CONJUNCTIONS_WITHIN_3_DEGREES";
    private static final double CONJUNCTION_ORB_DEGREES = 3.0;

    public SolarReturnNatalComparisonTable calculate(Subject subject, NatalChart natalChart,
                                                     SolarReturnTable solarReturnTable, LocalDate inquiryDate) {
        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        List<NatalTarget> natalTargets = natalTargets(natalChart);
        List<SolarReturnNatalComparisonRow> rows = new ArrayList<>();
        for (SolarReturnEntry solarReturn : solarReturnTable.rows()) {
            rows.add(row(natalChart, solarReturn, natalTargets, activeDateTime));
        }
        return new SolarReturnNatalComparisonTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                solarReturnTable.methodId(),
                NATAL_OVERLAY_METHOD,
                CONJUNCTION_ORB_DEGREES,
                solarReturnTable.ageStartYears(),
                solarReturnTable.ageEndYearsInclusive(),
                rows
        );
    }

    private SolarReturnNatalComparisonRow row(NatalChart natalChart, SolarReturnEntry solarReturn,
                                              List<NatalTarget> natalTargets, OffsetDateTime activeDateTime) {
        int profectedHouse = Math.floorMod(solarReturn.ageYears(), 12) + 1;
        ZodiacSign profectedSign = signForHouse(natalChart, profectedHouse);
        Planet lordOfYear = TraditionalTables.domicileRuler(profectedSign);
        List<SolarReturnPointOverlay> pointOverlays = pointOverlays(natalChart, solarReturn);
        SolarReturnPointOverlay lordOfYearOverlay = pointOverlays.stream()
                .filter(overlay -> overlay.point() == PointKey.of(lordOfYear))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing solar-return Lord of Year point " + lordOfYear));

        return new SolarReturnNatalComparisonRow(
                solarReturn.ageYears(),
                solarReturn.returnDateTime(),
                solarReturn.periodEndDateTimeExclusive(),
                active(solarReturn.returnDateTime(), solarReturn.periodEndDateTimeExclusive(), activeDateTime),
                profectedHouse,
                profectedSign,
                lordOfYear,
                lordOfYearOverlay,
                houseForSign(natalChart, solarReturn.ascendantSign()),
                houseForSign(natalChart, solarReturn.midheavenSign()),
                pointOverlays,
                pointOverlays.stream()
                        .filter(overlay -> overlay.sign() == profectedSign)
                        .map(SolarReturnPointOverlay::point)
                        .toList(),
                pointOverlays.stream()
                        .filter(overlay -> overlay.natalHouseOverlay() == profectedHouse)
                        .map(SolarReturnPointOverlay::point)
                        .toList(),
                conjunctions(solarReturn, natalTargets)
        );
    }

    private List<SolarReturnPointOverlay> pointOverlays(NatalChart natalChart, SolarReturnEntry solarReturn) {
        List<SolarReturnPointOverlay> entries = new ArrayList<>();
        for (SolarReturnPointEntry point : solarReturn.points()) {
            entries.add(new SolarReturnPointOverlay(
                    point.point(),
                    point.type(),
                    point.longitude(),
                    point.sign(),
                    point.degreeInSign(),
                    point.house(),
                    houseForSign(natalChart, point.sign()),
                    point.retrograde()
            ));
        }
        return List.copyOf(entries);
    }

    private List<SolarReturnNatalContact> conjunctions(SolarReturnEntry solarReturn, List<NatalTarget> natalTargets) {
        List<SolarReturnNatalContact> entries = new ArrayList<>();
        for (SolarReturnPointEntry point : solarReturn.points()) {
            for (NatalTarget target : natalTargets) {
                double orb = AstroMath.rawAngularSeparation(point.longitude(), target.longitude());
                if (orb <= CONJUNCTION_ORB_DEGREES) {
                    entries.add(new SolarReturnNatalContact(
                            point.point(),
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

    private List<NatalTarget> natalTargets(NatalChart natalChart) {
        List<NatalTarget> targets = new ArrayList<>();
        for (var entry : natalChart.getPoints().entrySet()) {
            PointPlacement placement = placement(entry.getValue(), natalChart);
            targets.add(new NatalTarget(
                    SolarReturnNatalTargetType.POINT,
                    entry.getKey().name(),
                    placement.longitude(),
                    placement.sign(),
                    placement.degreeInSign(),
                    placement.house()
            ));
        }
        if (natalChart.getLots() != null) {
            for (LotEntry lot : natalChart.getLots()) {
                targets.add(new NatalTarget(
                        SolarReturnNatalTargetType.LOT,
                        "LOT_" + lot.name(),
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
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }
        return OffsetDateTime.of(
                inquiryDate,
                subject.getUtcBirthDateTime().toLocalTime(),
                subject.getUtcBirthDateTime().getOffset()
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

    private record NatalTarget(SolarReturnNatalTargetType type, String name, double longitude, ZodiacSign sign,
                               double degreeInSign, int house) {}

    private record PointPlacement(double longitude, ZodiacSign sign, double degreeInSign, int house) {}
}
