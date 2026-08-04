package app.reading.lifearc.solarreturn;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.AngleType;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.AnglePointEntry;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.ephemeris.SweConst;

public final class SolarReturnCalculator {
    public static final String METHOD_ID = "TRADITIONAL_SOLAR_RETURN_NATAL_LOCATION_V1";
    private static final String PRIMARY_DOCTRINE = "traditional";
    private static final String LOCATION_METHOD = "NATAL_LOCATION";
    private static final double UNIX_EPOCH_JULIAN_DAY = 2440587.5;
    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double ROOT_DEGREE_TOLERANCE = 1.0e-9;
    private static final double SCAN_STEP_DAYS = 0.25;
    private static final int BISECTION_STEPS = 80;
    private static final CoreDoctrineInfo RETURN_CONVENTIONS = new CoreDoctrineInfo(
            "solar_return",
            "Traditional Solar Return",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final BasicCalculator basicCalculator = new BasicCalculator();

    public SolarReturnTable calculateTable(Subject subject, NatalChart natalChart, int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }

        double natalSunLongitude = natalChart.requirePlanet(Planet.SUN).getLongitude();
        ZoneOffset outputOffset = subject.getLocalBirthDateTime().getOffset();
        CalculationContext ephemerisContext = new CalculationContext(subject, RETURN_CONVENTIONS);

        List<ReturnInstant> returns = new ArrayList<>();
        for (int ageYears = ageStartYears; ageYears <= ageEndYearsInclusive + 1; ageYears++) {
            returns.add(returnInstant(subject, outputOffset, ephemerisContext, natalSunLongitude, ageYears));
        }

        List<SolarReturnEntry> rows = new ArrayList<>();
        for (int i = 0; i < returns.size() - 1; i++) {
            int ageYears = ageStartYears + i;
            rows.add(entry(subject, returns.get(i), returns.get(i + 1), ageYears));
        }

        return new SolarReturnTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                LOCATION_METHOD,
                ageStartYears,
                ageEndYearsInclusive,
                natalSunLongitude,
                AstroMath.signOf(natalSunLongitude),
                AstroMath.degreeInSign(natalSunLongitude),
                rows
        );
    }

    private ReturnInstant returnInstant(Subject subject, ZoneOffset outputOffset, CalculationContext ephemerisContext,
                                        double natalSunLongitude, int ageYears) {
        if (ageYears == 0) {
            Instant birthInstant = subject.getResolvedUtcInstant();
            return new ReturnInstant(julianDayFromInstant(birthInstant), birthInstant, birthInstant.atOffset(outputOffset));
        }
        double julianDay = findReturnJulianDay(subject, ephemerisContext, natalSunLongitude, ageYears);
        Instant instant = instantFromJulianDay(julianDay);
        return new ReturnInstant(julianDay, instant, instant.atOffset(outputOffset));
    }

    private double findReturnJulianDay(Subject subject, CalculationContext ephemerisContext, double natalSunLongitude, int ageYears) {
        OffsetDateTime approximate = subject.getLocalBirthDateTime().plusYears(ageYears);
        double approximateJulianDay = julianDayFromInstant(approximate.toInstant());
        for (double radiusDays : List.of(4.0, 8.0, 16.0)) {
            Double root = scanForRoot(ephemerisContext, natalSunLongitude,
                    approximateJulianDay - radiusDays,
                    approximateJulianDay + radiusDays);
            if (root != null) {
                return root;
            }
        }
        throw new IllegalArgumentException("Could not bracket solar return for age " + ageYears);
    }

    private Double scanForRoot(CalculationContext ephemerisContext, double natalSunLongitude, double startJulianDay, double endJulianDay) {
        double leftJulianDay = startJulianDay;
        double leftDifference = solarDifference(ephemerisContext, natalSunLongitude, leftJulianDay);
        if (Math.abs(leftDifference) <= ROOT_DEGREE_TOLERANCE) {
            return leftJulianDay;
        }

        for (double rightJulianDay = startJulianDay + SCAN_STEP_DAYS;
             rightJulianDay <= endJulianDay + 1.0e-9;
             rightJulianDay += SCAN_STEP_DAYS) {
            double rightDifference = solarDifference(ephemerisContext, natalSunLongitude, rightJulianDay);
            if (Math.abs(rightDifference) <= ROOT_DEGREE_TOLERANCE) {
                return rightJulianDay;
            }
            if (bracketsRoot(leftDifference, rightDifference)) {
                return bisectRoot(ephemerisContext, natalSunLongitude, leftJulianDay, rightJulianDay, leftDifference, rightDifference);
            }
            leftJulianDay = rightJulianDay;
            leftDifference = rightDifference;
        }
        return null;
    }

    private double bisectRoot(CalculationContext ephemerisContext, double natalSunLongitude,
                              double leftJulianDay, double rightJulianDay,
                              double leftDifference, double rightDifference) {
        double left = leftJulianDay;
        double right = rightJulianDay;
        double leftDiff = leftDifference;
        double rightDiff = rightDifference;
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double mid = (left + right) / 2.0;
            double midDiff = solarDifference(ephemerisContext, natalSunLongitude, mid);
            if (Math.abs(midDiff) <= ROOT_DEGREE_TOLERANCE) {
                return mid;
            }
            if (bracketsRoot(leftDiff, midDiff)) {
                right = mid;
                rightDiff = midDiff;
            } else if (bracketsRoot(midDiff, rightDiff)) {
                left = mid;
                leftDiff = midDiff;
            } else if (Math.abs(leftDiff) < Math.abs(rightDiff)) {
                right = mid;
                rightDiff = midDiff;
            } else {
                left = mid;
                leftDiff = midDiff;
            }
        }
        return (left + right) / 2.0;
    }

    private boolean bracketsRoot(double leftDifference, double rightDifference) {
        return (leftDifference <= 0.0 && rightDifference >= 0.0)
                || (leftDifference >= 0.0 && rightDifference <= 0.0);
    }

    private double solarDifference(CalculationContext ephemerisContext, double natalSunLongitude, double julianDay) {
        double sunLongitude = ephemerisContext.longitudeFor(Planet.SUN, SweConst.SE_SUN, julianDay);
        double difference = AstroMath.normalize(sunLongitude - natalSunLongitude);
        return difference > 180.0 ? difference - 360.0 : difference;
    }

    private SolarReturnEntry entry(Subject subject, ReturnInstant start, ReturnInstant end, int ageYears) {
        Subject returnSubject = new Subject(
                subject.getId() + "-solar-return-" + ageYears,
                start.dateTime(),
                start.instant(),
                subject.getLatitude(),
                subject.getLongitude()
        );
        NatalChart chart = basicCalculator.calculate(new CalculationContext(returnSubject, RETURN_CONVENTIONS));
        PlanetPosition sun = chart.requirePlanet(Planet.SUN);
        ChartAngle ascendant = chart.requireAngle(AngleType.ASCENDANT);
        ChartAngle midheaven = chart.requireAngle(AngleType.MIDHEAVEN);

        return new SolarReturnEntry(
                ageYears,
                start.dateTime(),
                end.dateTime(),
                start.julianDay(),
                sun.getLongitude(),
                sun.getSign(),
                sun.getDegreeInSign(),
                ascendant.getLongitude(),
                ascendant.getSign(),
                ascendant.getDegreeInSign(),
                midheaven.getLongitude(),
                midheaven.getSign(),
                midheaven.getDegreeInSign(),
                chart.getSect().getSect(),
                pointEntries(chart)
        );
    }

    private List<SolarReturnPointEntry> pointEntries(NatalChart chart) {
        List<SolarReturnPointEntry> entries = new ArrayList<>();
        for (Map.Entry<PointKey, PointEntry> point : chart.getPoints().entrySet()) {
            entries.add(pointEntry(point.getKey(), point.getValue()));
        }
        return List.copyOf(entries);
    }

    private SolarReturnPointEntry pointEntry(PointKey key, PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return new SolarReturnPointEntry(
                    key,
                    planetPoint.getType(),
                    planetPoint.longitude(),
                    planetPoint.sign(),
                    planetPoint.degreeInSign(),
                    planetPoint.house(),
                    planetPoint.retrograde()
            );
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return new SolarReturnPointEntry(
                    key,
                    anglePoint.getType(),
                    anglePoint.longitude(),
                    anglePoint.sign(),
                    anglePoint.degreeInSign(),
                    null,
                    null
            );
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
    }

    private double julianDayFromInstant(Instant instant) {
        return UNIX_EPOCH_JULIAN_DAY
                + instant.getEpochSecond() / SECONDS_PER_DAY
                + instant.getNano() / (SECONDS_PER_DAY * 1_000_000_000.0);
    }

    private Instant instantFromJulianDay(double julianDay) {
        double epochSeconds = (julianDay - UNIX_EPOCH_JULIAN_DAY) * SECONDS_PER_DAY;
        long seconds = (long) Math.floor(epochSeconds);
        long nanos = Math.round((epochSeconds - seconds) * 1_000_000_000.0);
        if (nanos == 1_000_000_000L) {
            seconds += 1;
            nanos = 0;
        }
        return Instant.ofEpochSecond(seconds, nanos);
    }

    private record ReturnInstant(double julianDay, Instant instant, OffsetDateTime dateTime) {}
}
