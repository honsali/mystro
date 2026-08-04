package app.reading.lifearc.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.HouseSystem;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.ephemeris.SweConst;

class ExactTransitSearchCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final MonthlyTransitCheckpointCalculator checkpointCalculator = new MonthlyTransitCheckpointCalculator();
    private final TransitSearchWindowCalculator windowCalculator = new TransitSearchWindowCalculator();
    private final ExactTransitSearchCalculator exactCalculator = new ExactTransitSearchCalculator();

    @Test
    void calculateHitsFindsFiniteExactHitsInsideGeneratedShortWindows() {
        Subject subject = subject();
        List<TransitSearchWindow> windows = generatedWindows(subject, LocalDate.of(2050, 6, 3), 50, 6);

        List<ExactTransitHit> hits = exactCalculator.calculateHits(subject, windows);

        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(hit -> hit.hitKind() == ExactTransitHitKind.EXACT_ASPECT));
        for (int i = 0; i < hits.size(); i++) {
            ExactTransitHit hit = hits.get(i);
            assertEquals(i + 1, hit.sequence());
            assertTrue(hit.sourceWindowSequence() >= 1);
            assertFalse(hit.exactDateTime().isBefore(hit.searchWindowStartDateTime()));
            assertFalse(hit.exactDateTime().isAfter(hit.searchWindowEndDateTime()));
            assertTrue(Double.isFinite(hit.transitLongitude()));
            assertTrue(Double.isFinite(hit.angularSeparation()));
            assertTrue(Double.isFinite(hit.orbFromExactDegrees()));
            if (hit.hitKind() == ExactTransitHitKind.EXACT_ASPECT) {
                assertEquals(AspectMotion.EXACT, hit.aspectMotion());
                assertTrue(hit.orbFromExactDegrees() <= 0.01, "exact hits should refine to a tight orb");
            } else {
                assertTrue(hit.orbFromExactDegrees() <= ExactTransitSearchCalculator.DEFAULT_STATION_NEAR_TARGET_ORB_DEGREES + 0.0001);
            }
        }
    }

    @Test
    void calculateHitsKeepsRetrogradeRepeatHitsInsideOneWindow() {
        Subject subject = subject();
        RetrogradeLoop loop = mercuryRetrogradeLoop(subject, 2026);
        TransitSearchWindow window = syntheticMercuryConjunctionWindow(loop);

        List<ExactTransitHit> hits = exactCalculator.calculateHits(subject, List.of(window), Duration.ofHours(6), 0.0).stream()
                .filter(hit -> hit.hitKind() == ExactTransitHitKind.EXACT_ASPECT)
                .toList();

        assertTrue(hits.size() >= 3, "Mercury should cross the same synthetic conjunction target repeatedly during the retrograde loop");
        assertTrue(hits.stream().allMatch(hit -> hit.transitPoint() == PointKey.MERCURY));
        assertTrue(hits.stream().allMatch(hit -> hit.aspect() == AspectType.CONJUNCTION));
        for (int i = 1; i < hits.size(); i++) {
            assertTrue(hits.get(i - 1).exactDateTime().isBefore(hits.get(i).exactDateTime()));
        }
    }

    @Test
    void calculateHitsSuppressesDuplicateRootsButKeepsDistinctHits() {
        Subject subject = subject();
        List<TransitSearchWindow> windows = generatedWindows(subject, LocalDate.of(2050, 6, 3), 50, 12);

        List<ExactTransitHit> hits = exactCalculator.calculateHits(subject, windows, Duration.ofHours(6), 0.0);

        assertFalse(hits.isEmpty());
        Set<String> seen = new HashSet<>();
        for (ExactTransitHit hit : hits) {
            String roundedDuplicateKey = hit.sourceWindowSequence()
                    + "|" + hit.hitKind()
                    + "|" + hit.transitPoint()
                    + "|" + hit.natalTargetType()
                    + "|" + hit.natalTargetName()
                    + "|" + hit.aspect()
                    + "|" + hit.exactDateTime().toEpochSecond() / 3600L;
            assertTrue(seen.add(roundedDuplicateKey), "duplicate exact transit hit was not suppressed: " + roundedDuplicateKey);
        }
    }

    @Test
    void calculateHitsRejectsInvalidInputs() {
        Subject subject = subject();
        List<TransitSearchWindow> windows = generatedWindows(subject, LocalDate.of(2050, 6, 3), 50, 1);

        assertThrows(IllegalArgumentException.class, () -> exactCalculator.calculateHits(null, windows));
        assertThrows(IllegalArgumentException.class, () -> exactCalculator.calculateHits(subject, null));
        assertThrows(IllegalArgumentException.class, () -> exactCalculator.calculateHits(subject, windows, Duration.ZERO, 0.25));
        assertThrows(IllegalArgumentException.class, () -> exactCalculator.calculateHits(subject, windows, Duration.ofHours(1), -0.1));
    }

    private List<TransitSearchWindow> generatedWindows(Subject subject, LocalDate inquiryDate, int ageYears, int maxWindows) {
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));
        MonthlyTransitCheckpointTable checkpointTable = checkpointCalculator.calculateTable(subject, natalChart, inquiryDate, ageYears, ageYears);
        return windowCalculator.calculateWindows(checkpointTable, TransitSearchWindowCalculator.DEFAULT_WINDOW_RADIUS, maxWindows);
    }

    private RetrogradeLoop mercuryRetrogradeLoop(Subject subject, int year) {
        CalculationContext ctx = new CalculationContext(subject, CORE);
        OffsetDateTime cursor = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = cursor.plusYears(1);
        OffsetDateTime firstStation = null;
        OffsetDateTime secondStation = null;
        double previousSpeed = mercurySpeed(ctx, cursor);
        cursor = cursor.plusHours(12);
        while (cursor.isBefore(end)) {
            double speed = mercurySpeed(ctx, cursor);
            if (previousSpeed > 0.0 && speed < 0.0) {
                firstStation = cursor;
            } else if (firstStation != null && previousSpeed < 0.0 && speed > 0.0) {
                secondStation = cursor;
                break;
            }
            previousSpeed = speed;
            cursor = cursor.plusHours(12);
        }
        if (firstStation == null || secondStation == null) {
            throw new AssertionError("Expected a Mercury retrograde loop in " + year);
        }
        OffsetDateTime midpoint = firstStation.plus(Duration.between(firstStation, secondStation).dividedBy(2));
        return new RetrogradeLoop(firstStation, secondStation, mercuryLongitude(ctx, midpoint));
    }

    private TransitSearchWindow syntheticMercuryConjunctionWindow(RetrogradeLoop loop) {
        OffsetDateTime start = loop.firstStation().minusDays(30);
        OffsetDateTime end = loop.secondStation().plusDays(30);
        OffsetDateTime checkpoint = start.plus(Duration.between(start, end).dividedBy(2));
        return new TransitSearchWindow(
                1,
                "TEST_RETROGRADE_WINDOW",
                "TEST",
                1,
                checkpoint,
                start,
                end,
                PointKey.MERCURY,
                false,
                false,
                TransitNatalTargetType.POINT,
                "SYNTHETIC_MERCURY_TARGET",
                loop.targetLongitude(),
                AstroMath.signOf(loop.targetLongitude()),
                AstroMath.degreeInSign(loop.targetLongitude()),
                1,
                AspectType.CONJUNCTION,
                0.0,
                0.0,
                List.of(MonthlyTransitActivationReason.TRANSIT_LORD_OF_MONTH),
                1
        );
    }

    private double mercuryLongitude(CalculationContext ctx, OffsetDateTime dateTime) {
        double[] values = mercuryValues(ctx, dateTime);
        return AstroMath.normalize(values[0]);
    }

    private double mercurySpeed(CalculationContext ctx, OffsetDateTime dateTime) {
        double[] values = mercuryValues(ctx, dateTime);
        return values[3];
    }

    private double[] mercuryValues(CalculationContext ctx, OffsetDateTime dateTime) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay(dateTime), SweConst.SE_MERCURY, ctx.planetFlags(), values, error);
        if (result < 0 || !Double.isFinite(values[0]) || !Double.isFinite(values[3])) {
            throw new AssertionError("Swiss Ephemeris failed to calculate Mercury in test: " + error);
        }
        return values;
    }

    private double julianDay(OffsetDateTime dateTime) {
        return 2440587.5 + dateTime.toInstant().getEpochSecond() / 86400.0 + dateTime.toInstant().getNano() / 86_400_000_000_000.0;
    }

    private record RetrogradeLoop(OffsetDateTime firstStation, OffsetDateTime secondStation, double targetLongitude) {}

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
