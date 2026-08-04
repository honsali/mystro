package app.reading.lifearc.transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.AspectMotion;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;

class TransitSearchWindowCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final MonthlyTransitCheckpointCalculator checkpointCalculator = new MonthlyTransitCheckpointCalculator();
    private final TransitSearchWindowCalculator windowCalculator = new TransitSearchWindowCalculator();

    @Test
    void calculateWindowsUsesStrongestActiveCheckpointContactsWithDefaultRadius() {
        MonthlyTransitCheckpointTable table = checkpointTable(LocalDate.of(2000, 3, 20));
        MonthlyTransitCheckpointRow active = activeRow(table);
        MonthlyTransitActivationContact strongestContact = active.activationContacts().get(0);

        List<TransitSearchWindow> windows = windowCalculator.calculateWindows(table);

        assertFalse(windows.isEmpty());
        assertEquals(Math.min(TransitSearchWindowCalculator.DEFAULT_MAX_WINDOWS, active.activationContacts().size()), windows.size());
        TransitSearchWindow first = windows.get(0);
        assertEquals(1, first.sequence());
        assertEquals(TransitSearchWindowCalculator.SOURCE_TECHNIQUE, first.sourceTechnique());
        assertEquals(MonthlyTransitCheckpointCalculator.METHOD_ID, first.sourceMethodId());
        assertEquals(active.checkpointNumber(), first.sourceCheckpointNumber());
        assertEquals(active.checkpointDateTime(), first.checkpointDateTime());
        assertEquals(active.checkpointDateTime().minus(TransitSearchWindowCalculator.DEFAULT_WINDOW_RADIUS), first.windowStartDateTime());
        assertEquals(active.checkpointDateTime().plus(TransitSearchWindowCalculator.DEFAULT_WINDOW_RADIUS), first.windowEndDateTime());
        assertWindowMatchesContact(first, strongestContact);

        for (int i = 0; i < windows.size(); i++) {
            assertEquals(i + 1, windows.get(i).sequence());
        }
        for (int i = 1; i < windows.size(); i++) {
            assertTrue(windows.get(i - 1).activationWeight() >= windows.get(i).activationWeight());
        }
    }

    @Test
    void calculateWindowsAllowsCustomRadiusAndMaximumRows() {
        MonthlyTransitCheckpointTable table = checkpointTable(LocalDate.of(2000, 3, 20));
        MonthlyTransitCheckpointRow active = activeRow(table);
        Duration radius = Duration.ofDays(3);

        List<TransitSearchWindow> windows = windowCalculator.calculateWindows(table, radius, 2);

        assertEquals(2, windows.size());
        assertTrue(windows.stream().allMatch(window -> window.windowStartDateTime().equals(active.checkpointDateTime().minus(radius))));
        assertTrue(windows.stream().allMatch(window -> window.windowEndDateTime().equals(active.checkpointDateTime().plus(radius))));
        assertWindowMatchesContact(windows.get(0), active.activationContacts().get(0));
        assertWindowMatchesContact(windows.get(1), active.activationContacts().get(1));
    }

    @Test
    void calculateWindowsReturnsEmptyWithoutActiveCheckpointAndRejectsInvalidArgs() {
        MonthlyTransitCheckpointTable inactiveTable = checkpointTable(null);

        assertTrue(windowCalculator.calculateWindows(inactiveTable).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> windowCalculator.calculateWindows(null));
        assertThrows(IllegalArgumentException.class, () -> windowCalculator.calculateWindows(inactiveTable, Duration.ZERO, 1));
        assertThrows(IllegalArgumentException.class, () -> windowCalculator.calculateWindows(inactiveTable, Duration.ofDays(1), 0));
    }

    @Test
    void exactTransitHitModelStoresAspectMotionWhenAvailable() {
        MonthlyTransitCheckpointTable table = checkpointTable(LocalDate.of(2000, 3, 20));
        TransitSearchWindow window = windowCalculator.calculateWindows(table, Duration.ofDays(3), 1).get(0);

        ExactTransitHit hit = new ExactTransitHit(
                1,
                window.sequence(),
                ExactTransitHitKind.EXACT_ASPECT,
                window.checkpointDateTime(),
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
                721.0,
                window.aspect().getExactAngle(),
                0.0,
                AspectMotion.APPLYING
        );

        assertEquals(AstroMath.normalize(721.0), hit.transitLongitude(), 0.0001);
        assertEquals(AspectMotion.APPLYING, hit.aspectMotion());
        assertThrows(IllegalArgumentException.class, () -> new ExactTransitHit(
                1,
                window.sequence(),
                ExactTransitHitKind.EXACT_ASPECT,
                window.windowStartDateTime().minusSeconds(1),
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
                0.0,
                window.aspect().getExactAngle(),
                0.0,
                null
        ));
    }

    private void assertWindowMatchesContact(TransitSearchWindow window, MonthlyTransitActivationContact contact) {
        assertEquals(contact.transitPoint(), window.transitPoint());
        assertEquals(contact.transitPointIsLordOfYear(), window.transitPointIsLordOfYear());
        assertEquals(contact.transitPointIsLordOfMonth(), window.transitPointIsLordOfMonth());
        assertEquals(contact.natalTargetType(), window.natalTargetType());
        assertEquals(contact.natalTargetName(), window.natalTargetName());
        assertEquals(contact.natalTargetSign(), window.natalTargetSign());
        assertEquals(contact.natalTargetDegreeInSign(), window.natalTargetDegreeInSign(), 0.0001);
        assertEquals(contact.natalTargetHouse(), window.natalTargetHouse());
        assertEquals(AstroMath.normalize(contact.natalTargetSign().ordinal() * 30.0 + contact.natalTargetDegreeInSign()), window.natalTargetLongitude(), 0.0001);
        assertEquals(contact.aspect(), window.aspect());
        assertEquals(contact.angularSeparation(), window.checkpointAngularSeparation(), 0.0001);
        assertEquals(contact.orbFromExactDegrees(), window.checkpointOrbFromExactDegrees(), 0.0001);
        assertEquals(contact.activationReasons(), window.activationReasons());
        assertEquals(contact.activationWeight(), window.activationWeight());
    }

    private MonthlyTransitCheckpointRow activeRow(MonthlyTransitCheckpointTable table) {
        return table.rows().stream()
                .filter(MonthlyTransitCheckpointRow::activeForInquiry)
                .findFirst()
                .orElseThrow();
    }

    private MonthlyTransitCheckpointTable checkpointTable(LocalDate inquiryDate) {
        Subject subject = subject();
        NatalChart natalChart = new BasicCalculator().calculate(new CalculationContext(subject, CORE));
        return checkpointCalculator.calculateTable(subject, natalChart, inquiryDate, 0, 0);
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
