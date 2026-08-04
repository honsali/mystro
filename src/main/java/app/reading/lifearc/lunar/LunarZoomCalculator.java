package app.reading.lifearc.lunar;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.ephemeris.SweConst;

public final class LunarZoomCalculator {
    public static final String METHOD_ID = "LUNAR_30_DAY_ZOOM_V1";
    private static final String PRIMARY_DOCTRINE = "traditional_normalized";
    private static final String SIGN_INGRESS_METHOD = "EXACT_TRANSITING_MOON_TROPICAL_SIGN_BOUNDARY_INGRESSES_BY_SWISS_EPHEMERIS_ROOT_REFINEMENT";
    private static final Duration SCAN_STEP = Duration.ofHours(6);
    private static final Duration ROOT_TIME_TOLERANCE = Duration.ofSeconds(1);
    private static final CoreDoctrineInfo LUNAR_ZOOM_CONVENTIONS = new CoreDoctrineInfo(
            "lunar_zoom",
            "Lunar Zoom",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    public LunarZoomTable calculate(Subject subject, NatalChart natalChart, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        if (subject == null) {
            throw new IllegalArgumentException("subject is required");
        }
        if (natalChart == null) {
            throw new IllegalArgumentException("natalChart is required");
        }
        if (windowStart == null || windowEnd == null || !windowStart.isBefore(windowEnd)) {
            throw new IllegalArgumentException("windowStart must be before windowEnd");
        }
        CalculationContext ctx = new CalculationContext(subject, LUNAR_ZOOM_CONVENTIONS);
        List<LunarSignIngressEntry> signIngresses = signIngresses(ctx, natalChart, windowStart, windowEnd);
        return new LunarZoomTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                SIGN_INGRESS_METHOD,
                windowStart,
                windowEnd,
                signIngresses
        );
    }

    private List<LunarSignIngressEntry> signIngresses(CalculationContext ctx, NatalChart natalChart,
                                                       OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        List<LunarSignIngressEntry> entries = new ArrayList<>();
        Instant previousInstant = windowStart.toInstant();
        double previousLongitude = moonLongitude(ctx, previousInstant);
        ZodiacSign previousSign = AstroMath.signOf(previousLongitude);
        Instant endInstant = windowEnd.toInstant();

        while (previousInstant.isBefore(endInstant)) {
            Instant currentInstant = previousInstant.plus(SCAN_STEP);
            if (currentInstant.isAfter(endInstant)) {
                currentInstant = endInstant;
            }
            double currentLongitude = moonLongitude(ctx, currentInstant);
            ZodiacSign currentSign = AstroMath.signOf(currentLongitude);
            if (currentSign != previousSign) {
                Instant ingressInstant = refineIngress(ctx, previousInstant, currentInstant, currentSign);
                double ingressLongitude = moonLongitude(ctx, ingressInstant);
                entries.add(new LunarSignIngressEntry(
                        entries.size() + 1,
                        OffsetDateTime.ofInstant(ingressInstant, windowStart.getOffset()),
                        previousSign,
                        currentSign,
                        ingressLongitude,
                        ingressDegreeInSign(ingressLongitude, currentSign),
                        houseForSign(natalChart, currentSign)
                ));
            }
            previousInstant = currentInstant;
            previousLongitude = currentLongitude;
            previousSign = currentSign;
        }
        return List.copyOf(entries);
    }

    private Instant refineIngress(CalculationContext ctx, Instant start, Instant end, ZodiacSign toSign) {
        double targetLongitude = toSign.ordinal() * 30.0;
        Instant left = start;
        Instant right = end;
        double leftValue = signedDistanceToTarget(ctx, left, targetLongitude);
        while (Duration.between(left, right).compareTo(ROOT_TIME_TOLERANCE) > 0) {
            Instant mid = midpoint(left, right);
            double midValue = signedDistanceToTarget(ctx, mid, targetLongitude);
            if (crossesSignedZero(leftValue, midValue)) {
                right = mid;
            } else {
                left = mid;
                leftValue = midValue;
            }
        }
        return midpoint(left, right);
    }

    private boolean crossesSignedZero(double first, double second) {
        if (Math.abs(first - second) > 180.0) {
            return false;
        }
        return first == 0.0 || second == 0.0 || (first < 0.0 && second > 0.0) || (first > 0.0 && second < 0.0);
    }

    private double signedDistanceToTarget(CalculationContext ctx, Instant instant, double targetLongitude) {
        return signedDistance(moonLongitude(ctx, instant), targetLongitude);
    }

    private double signedDistance(double longitude, double targetLongitude) {
        double value = AstroMath.normalize(longitude - targetLongitude + 180.0) - 180.0;
        return value <= -180.0 ? 180.0 : value;
    }

    private double moonLongitude(CalculationContext ctx, Instant instant) {
        return ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, julianDay(instant));
    }

    private double ingressDegreeInSign(double longitude, ZodiacSign toSign) {
        double value = AstroMath.normalize(longitude - toSign.ordinal() * 30.0);
        return value > 29.999 ? 0.0 : value;
    }

    private double julianDay(Instant instant) {
        return 2440587.5 + instant.getEpochSecond() / 86400.0 + instant.getNano() / 86_400_000_000_000.0;
    }

    private Instant midpoint(Instant start, Instant end) {
        return start.plusNanos(Duration.between(start, end).toNanos() / 2L);
    }

    private int houseForSign(NatalChart natalChart, ZodiacSign sign) {
        return natalChart.getHouses().stream()
                .filter(house -> house.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house overlay for sign " + sign));
    }
}
