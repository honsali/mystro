package app.reading.lifearc.transit;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.AspectMotion;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.ephemeris.SweConst;

/**
 * Finds exact transit-to-natal aspect hits inside preselected short search windows.
 *
 * <p>The search is intentionally bounded by {@link TransitSearchWindow}; this calculator must not
 * be used as a whole-life daily scanner. It samples each short window, brackets exact contacts by
 * signed longitude difference, then refines each bracket by bisection. Retrograde repeats are kept
 * when they occur as separate roots. Duplicate roots caused by sample boundaries are suppressed.</p>
 */
public final class ExactTransitSearchCalculator {
    public static final String METHOD_ID = "EXACT_TRANSIT_HITS_IN_SHORT_WINDOWS_V1";
    public static final Duration DEFAULT_SAMPLE_STEP = Duration.ofHours(3);
    public static final double DEFAULT_STATION_NEAR_TARGET_ORB_DEGREES = 0.25;

    private static final double ROOT_TOLERANCE_DEGREES = 1.0 / 3600.0;
    private static final double STATION_SPEED_TOLERANCE_DEGREES_PER_DAY = 1.0e-5;
    private static final Duration ROOT_TIME_TOLERANCE = Duration.ofSeconds(1);
    private static final Duration DUPLICATE_SUPPRESSION_WINDOW = Duration.ofHours(1);
    private static final CoreDoctrineInfo SEARCH_CONVENTIONS = new CoreDoctrineInfo(
            "exact_transit_search",
            "Exact Transit Search",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    public List<ExactTransitHit> calculateHits(Subject subject, List<TransitSearchWindow> windows) {
        return calculateHits(subject, windows, DEFAULT_SAMPLE_STEP, DEFAULT_STATION_NEAR_TARGET_ORB_DEGREES);
    }

    public List<ExactTransitHit> calculateHits(Subject subject,
                                               List<TransitSearchWindow> windows,
                                               Duration sampleStep,
                                               double stationNearTargetOrbDegrees) {
        if (subject == null) {
            throw new IllegalArgumentException("subject is required");
        }
        if (windows == null) {
            throw new IllegalArgumentException("windows is required");
        }
        if (sampleStep == null || sampleStep.isZero() || sampleStep.isNegative()) {
            throw new IllegalArgumentException("sampleStep must be positive");
        }
        if (!Double.isFinite(stationNearTargetOrbDegrees) || stationNearTargetOrbDegrees < 0.0) {
            throw new IllegalArgumentException("stationNearTargetOrbDegrees must be non-negative");
        }

        CalculationContext ctx = new CalculationContext(subject, SEARCH_CONVENTIONS);
        List<HitDraft> drafts = new ArrayList<>();
        for (TransitSearchWindow window : windows) {
            addWindowHits(drafts, ctx, window, sampleStep, stationNearTargetOrbDegrees);
        }

        List<HitDraft> sorted = drafts.stream()
                .sorted(Comparator
                        .comparing(HitDraft::exactInstant)
                        .thenComparingInt(draft -> draft.window().sequence())
                        .thenComparing(draft -> draft.window().transitPoint().ordinal())
                        .thenComparing(draft -> draft.window().natalTargetName())
                        .thenComparing(draft -> draft.window().aspect().ordinal())
                        .thenComparing(draft -> draft.hitKind().ordinal()))
                .toList();

        List<ExactTransitHit> hits = new ArrayList<>();
        for (HitDraft draft : sorted) {
            hits.add(hit(hits.size() + 1, draft));
        }
        return List.copyOf(hits);
    }

    private void addWindowHits(List<HitDraft> drafts,
                               CalculationContext ctx,
                               TransitSearchWindow window,
                               Duration sampleStep,
                               double stationNearTargetOrbDegrees) {
        List<Instant> samples = samples(window.windowStartDateTime().toInstant(), window.windowEndDateTime().toInstant(), sampleStep);
        for (double exactLongitude : exactLongitudes(window)) {
            addExactAspectHits(drafts, ctx, window, exactLongitude, samples);
            addStationNearTargetHits(drafts, ctx, window, exactLongitude, samples, stationNearTargetOrbDegrees);
        }
    }

    private void addExactAspectHits(List<HitDraft> drafts,
                                    CalculationContext ctx,
                                    TransitSearchWindow window,
                                    double exactLongitude,
                                    List<Instant> samples) {
        Instant previous = samples.get(0);
        double previousValue = signedDistanceToExact(ctx, window.transitPoint(), previous, exactLongitude);
        if (Math.abs(previousValue) <= ROOT_TOLERANCE_DEGREES) {
            addUnique(drafts, draft(ctx, window, previous, ExactTransitHitKind.EXACT_ASPECT));
        }
        for (int i = 1; i < samples.size(); i++) {
            Instant current = samples.get(i);
            double currentValue = signedDistanceToExact(ctx, window.transitPoint(), current, exactLongitude);
            if (crossesSignedZero(previousValue, currentValue)) {
                Instant root = refineRoot(ctx, window.transitPoint(), exactLongitude, previous, current, previousValue, currentValue);
                addUnique(drafts, draft(ctx, window, root, ExactTransitHitKind.EXACT_ASPECT));
            }
            if (Math.abs(currentValue) <= ROOT_TOLERANCE_DEGREES) {
                addUnique(drafts, draft(ctx, window, current, ExactTransitHitKind.EXACT_ASPECT));
            }
            previous = current;
            previousValue = currentValue;
        }
    }

    private void addStationNearTargetHits(List<HitDraft> drafts,
                                          CalculationContext ctx,
                                          TransitSearchWindow window,
                                          double exactLongitude,
                                          List<Instant> samples,
                                          double stationNearTargetOrbDegrees) {
        if (stationNearTargetOrbDegrees == 0.0) {
            return;
        }
        Instant previous = samples.get(0);
        double previousSpeed = position(ctx, window.transitPoint(), previous).speed();
        for (int i = 1; i < samples.size(); i++) {
            Instant current = samples.get(i);
            double currentSpeed = position(ctx, window.transitPoint(), current).speed();
            if (speedCrossesZero(previousSpeed, currentSpeed)) {
                Instant station = refineStation(ctx, window.transitPoint(), previous, current, previousSpeed, currentSpeed);
                TransitPosition stationPosition = position(ctx, window.transitPoint(), station);
                double orb = Math.abs(signedDistance(stationPosition.longitude(), exactLongitude));
                if (orb <= stationNearTargetOrbDegrees) {
                    addUnique(drafts, draft(ctx, window, station, ExactTransitHitKind.STATION_NEAR_EXACT_TARGET));
                }
            }
            previous = current;
            previousSpeed = currentSpeed;
        }
    }

    private HitDraft draft(CalculationContext ctx, TransitSearchWindow window, Instant instant, ExactTransitHitKind hitKind) {
        TransitPosition position = position(ctx, window.transitPoint(), instant);
        double angularSeparation = AstroMath.rawAngularSeparation(position.longitude(), window.natalTargetLongitude());
        double orb = Math.abs(angularSeparation - window.aspect().getExactAngle());
        return new HitDraft(window, hitKind, instant, position.longitude(), angularSeparation, orb, aspectMotion(hitKind, position.speed(), orb));
    }

    private ExactTransitHit hit(int sequence, HitDraft draft) {
        TransitSearchWindow window = draft.window();
        return new ExactTransitHit(
                sequence,
                window.sequence(),
                draft.hitKind(),
                OffsetDateTime.ofInstant(draft.exactInstant(), offset(window)),
                window.windowStartDateTime(),
                window.windowEndDateTime(),
                window.transitPoint(),
                window.natalTargetType(),
                window.natalTargetName(),
                window.natalTargetLongitude(),
                window.natalTargetSign(),
                window.natalTargetDegreeInSign(),
                window.natalTargetHouse(),
                window.aspect(),
                draft.transitLongitude(),
                draft.angularSeparation(),
                draft.orbFromExactDegrees(),
                draft.aspectMotion()
        );
    }

    private AspectMotion aspectMotion(ExactTransitHitKind hitKind, double speed, double orb) {
        if (hitKind == ExactTransitHitKind.EXACT_ASPECT || orb <= ROOT_TOLERANCE_DEGREES) {
            return AspectMotion.EXACT;
        }
        if (Math.abs(speed) <= STATION_SPEED_TOLERANCE_DEGREES_PER_DAY) {
            return AspectMotion.EXACT;
        }
        return null;
    }

    private void addUnique(List<HitDraft> drafts, HitDraft candidate) {
        for (int i = 0; i < drafts.size(); i++) {
            HitDraft existing = drafts.get(i);
            if (!sameDuplicateKey(existing, candidate)) {
                continue;
            }
            Duration distance = absoluteDuration(Duration.between(existing.exactInstant(), candidate.exactInstant()));
            if (distance.compareTo(DUPLICATE_SUPPRESSION_WINDOW) > 0) {
                continue;
            }
            if (candidate.orbFromExactDegrees() < existing.orbFromExactDegrees()) {
                drafts.set(i, candidate);
            }
            return;
        }
        drafts.add(candidate);
    }

    private boolean sameDuplicateKey(HitDraft first, HitDraft second) {
        return first.window().sequence() == second.window().sequence()
                && first.hitKind() == second.hitKind()
                && first.window().transitPoint() == second.window().transitPoint()
                && first.window().natalTargetType() == second.window().natalTargetType()
                && first.window().natalTargetName().equals(second.window().natalTargetName())
                && first.window().aspect() == second.window().aspect();
    }

    private Duration absoluteDuration(Duration duration) {
        return duration.isNegative() ? duration.negated() : duration;
    }

    private Instant refineRoot(CalculationContext ctx,
                               PointKey transitPoint,
                               double exactLongitude,
                               Instant start,
                               Instant end,
                               double startValue,
                               double endValue) {
        Instant left = start;
        Instant right = end;
        double leftValue = startValue;
        double rightValue = endValue;
        while (Duration.between(left, right).compareTo(ROOT_TIME_TOLERANCE) > 0) {
            Instant mid = midpoint(left, right);
            double midValue = signedDistanceToExact(ctx, transitPoint, mid, exactLongitude);
            if (Math.abs(midValue) <= ROOT_TOLERANCE_DEGREES) {
                return mid;
            }
            if (crossesSignedZero(leftValue, midValue)) {
                right = mid;
                rightValue = midValue;
            } else if (crossesSignedZero(midValue, rightValue)) {
                left = mid;
                leftValue = midValue;
            } else {
                return Math.abs(leftValue) <= Math.abs(rightValue) ? left : right;
            }
        }
        return midpoint(left, right);
    }

    private Instant refineStation(CalculationContext ctx,
                                  PointKey transitPoint,
                                  Instant start,
                                  Instant end,
                                  double startSpeed,
                                  double endSpeed) {
        Instant left = start;
        Instant right = end;
        double leftSpeed = startSpeed;
        double rightSpeed = endSpeed;
        while (Duration.between(left, right).compareTo(ROOT_TIME_TOLERANCE) > 0) {
            Instant mid = midpoint(left, right);
            double midSpeed = position(ctx, transitPoint, mid).speed();
            if (Math.abs(midSpeed) <= STATION_SPEED_TOLERANCE_DEGREES_PER_DAY) {
                return mid;
            }
            if (speedCrossesZero(leftSpeed, midSpeed)) {
                right = mid;
                rightSpeed = midSpeed;
            } else if (speedCrossesZero(midSpeed, rightSpeed)) {
                left = mid;
                leftSpeed = midSpeed;
            } else {
                return Math.abs(leftSpeed) <= Math.abs(rightSpeed) ? left : right;
            }
        }
        return midpoint(left, right);
    }

    private boolean crossesSignedZero(double first, double second) {
        if (Math.abs(first) <= ROOT_TOLERANCE_DEGREES || Math.abs(second) <= ROOT_TOLERANCE_DEGREES) {
            return true;
        }
        if (Math.abs(first - second) > 180.0) {
            return false;
        }
        return (first < 0.0 && second > 0.0) || (first > 0.0 && second < 0.0);
    }

    private boolean speedCrossesZero(double first, double second) {
        if (Math.abs(first) <= STATION_SPEED_TOLERANCE_DEGREES_PER_DAY || Math.abs(second) <= STATION_SPEED_TOLERANCE_DEGREES_PER_DAY) {
            return true;
        }
        return (first < 0.0 && second > 0.0) || (first > 0.0 && second < 0.0);
    }

    private List<Double> exactLongitudes(TransitSearchWindow window) {
        Set<Double> longitudes = new LinkedHashSet<>();
        double aspectAngle = window.aspect().getExactAngle();
        longitudes.add(round(AstroMath.normalize(window.natalTargetLongitude() + aspectAngle)));
        longitudes.add(round(AstroMath.normalize(window.natalTargetLongitude() - aspectAngle)));
        return longitudes.stream().map(Double::doubleValue).toList();
    }

    private double round(double value) {
        return Math.rint(value * 1_000_000_000.0) / 1_000_000_000.0;
    }

    private double signedDistanceToExact(CalculationContext ctx, PointKey transitPoint, Instant instant, double exactLongitude) {
        return signedDistance(position(ctx, transitPoint, instant).longitude(), exactLongitude);
    }

    private double signedDistance(double longitude, double exactLongitude) {
        double value = AstroMath.normalize(longitude - exactLongitude + 180.0) - 180.0;
        return value <= -180.0 ? 180.0 : value;
    }

    private TransitPosition position(CalculationContext ctx, PointKey transitPoint, Instant instant) {
        Planet planet = planet(transitPoint);
        int swissPlanetId = swissPlanetId(transitPoint);
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay(instant), swissPlanetId, ctx.planetFlags(), values, error);
        ctx.requireSwissEphemerisResult(result, planet, "exact transit search position", error);
        if (!Double.isFinite(values[0]) || !Double.isFinite(values[3])) {
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        double longitude = AstroMath.normalize(values[0]);
        if (transitPoint == PointKey.SOUTH_NODE) {
            longitude = AstroMath.normalize(longitude + 180.0);
        }
        return new TransitPosition(longitude, values[3]);
    }

    private Planet planet(PointKey point) {
        return switch (point) {
            case SUN -> Planet.SUN;
            case MOON -> Planet.MOON;
            case MERCURY -> Planet.MERCURY;
            case VENUS -> Planet.VENUS;
            case MARS -> Planet.MARS;
            case JUPITER -> Planet.JUPITER;
            case SATURN -> Planet.SATURN;
            case NORTH_NODE -> Planet.NORTH_NODE;
            case SOUTH_NODE -> Planet.SOUTH_NODE;
            case ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI -> throw new IllegalArgumentException("Transit point must be a planet or node: " + point);
        };
    }

    private int swissPlanetId(PointKey point) {
        return switch (point) {
            case SUN -> SweConst.SE_SUN;
            case MOON -> SweConst.SE_MOON;
            case MERCURY -> SweConst.SE_MERCURY;
            case VENUS -> SweConst.SE_VENUS;
            case MARS -> SweConst.SE_MARS;
            case JUPITER -> SweConst.SE_JUPITER;
            case SATURN -> SweConst.SE_SATURN;
            case NORTH_NODE, SOUTH_NODE -> SweConst.SE_MEAN_NODE;
            case ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI -> throw new IllegalArgumentException("Transit point must be a planet or node: " + point);
        };
    }

    private List<Instant> samples(Instant start, Instant end, Duration sampleStep) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("window start must be before window end");
        }
        List<Instant> samples = new ArrayList<>();
        Instant current = start;
        samples.add(current);
        while (current.plus(sampleStep).isBefore(end)) {
            current = current.plus(sampleStep);
            samples.add(current);
        }
        samples.add(end);
        return samples;
    }

    private Instant midpoint(Instant start, Instant end) {
        return start.plusNanos(Duration.between(start, end).toNanos() / 2L);
    }

    private ZoneOffset offset(TransitSearchWindow window) {
        return window.checkpointDateTime().getOffset();
    }

    private double julianDay(Instant instant) {
        return 2440587.5 + instant.getEpochSecond() / 86400.0 + instant.getNano() / 86_400_000_000_000.0;
    }

    private record TransitPosition(double longitude, double speed) {}

    private record HitDraft(
            TransitSearchWindow window,
            ExactTransitHitKind hitKind,
            Instant exactInstant,
            double transitLongitude,
            double angularSeparation,
            double orbFromExactDegrees,
            AspectMotion aspectMotion
    ) {}
}
