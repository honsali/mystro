package app.reading.lifearc.transit;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;

/**
 * Builds bounded exact-transit search windows from the strongest active monthly checkpoint contacts.
 *
 * <p>The calculator intentionally starts from the already profection-filtered
 * {@link MonthlyTransitActivationContact} rows. It does not scan a lifetime or create daily transit
 * rows; later exact root finding should operate only inside these short windows unless a caller
 * explicitly supplies a different bounded window radius.</p>
 */
public final class TransitSearchWindowCalculator {
    public static final String METHOD_ID = "EXACT_TRANSIT_SEARCH_WINDOWS_FROM_MONTHLY_CHECKPOINT_ACTIVATIONS_V1";
    public static final String SOURCE_TECHNIQUE = "MONTHLY_TRANSIT_CHECKPOINT";
    public static final Duration DEFAULT_WINDOW_RADIUS = Duration.ofDays(15);
    public static final int DEFAULT_MAX_WINDOWS = 12;

    public List<TransitSearchWindow> calculateWindows(MonthlyTransitCheckpointTable checkpointTable) {
        return calculateWindows(checkpointTable, DEFAULT_WINDOW_RADIUS, DEFAULT_MAX_WINDOWS);
    }

    public List<TransitSearchWindow> calculateWindows(MonthlyTransitCheckpointTable checkpointTable,
                                                       Duration windowRadius,
                                                       int maxWindows) {
        if (checkpointTable == null) {
            throw new IllegalArgumentException("checkpointTable is required");
        }
        if (windowRadius == null || windowRadius.isZero() || windowRadius.isNegative()) {
            throw new IllegalArgumentException("windowRadius must be positive");
        }
        if (maxWindows <= 0) {
            throw new IllegalArgumentException("maxWindows must be positive");
        }

        MonthlyTransitCheckpointRow active = checkpointTable.rows().stream()
                .filter(MonthlyTransitCheckpointRow::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return List.of();
        }

        List<MonthlyTransitActivationContact> contacts = active.activationContacts().stream()
                .sorted(strongestContactOrder())
                .limit(maxWindows)
                .toList();

        return java.util.stream.IntStream.range(0, contacts.size())
                .mapToObj(index -> window(index + 1, checkpointTable, active, contacts.get(index), windowRadius))
                .toList();
    }

    private TransitSearchWindow window(int sequence,
                                       MonthlyTransitCheckpointTable checkpointTable,
                                       MonthlyTransitCheckpointRow active,
                                       MonthlyTransitActivationContact contact,
                                       Duration windowRadius) {
        return new TransitSearchWindow(
                sequence,
                SOURCE_TECHNIQUE,
                checkpointTable.methodId(),
                active.checkpointNumber(),
                active.checkpointDateTime(),
                active.checkpointDateTime().minus(windowRadius),
                active.checkpointDateTime().plus(windowRadius),
                contact.transitPoint(),
                contact.transitPointIsLordOfYear(),
                contact.transitPointIsLordOfMonth(),
                contact.natalTargetType(),
                contact.natalTargetName(),
                natalTargetLongitude(contact),
                contact.natalTargetSign(),
                contact.natalTargetDegreeInSign(),
                contact.natalTargetHouse(),
                contact.aspect(),
                contact.angularSeparation(),
                contact.orbFromExactDegrees(),
                contact.activationReasons(),
                contact.activationWeight()
        );
    }

    private Comparator<MonthlyTransitActivationContact> strongestContactOrder() {
        return Comparator
                .comparingInt(MonthlyTransitActivationContact::activationWeight).reversed()
                .thenComparing(contact -> contact.transitPoint().ordinal())
                .thenComparing(contact -> contact.aspect().ordinal())
                .thenComparingDouble(MonthlyTransitActivationContact::orbFromExactDegrees)
                .thenComparing(MonthlyTransitActivationContact::natalTargetName);
    }

    private double natalTargetLongitude(MonthlyTransitActivationContact contact) {
        return AstroMath.normalize(contact.natalTargetSign().ordinal() * 30.0 + contact.natalTargetDegreeInSign());
    }
}
