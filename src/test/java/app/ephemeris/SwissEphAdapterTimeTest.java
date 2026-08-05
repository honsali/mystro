package app.ephemeris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class SwissEphAdapterTimeTest {

    private static final double SECONDS_PER_DAY = 86_400.0;

    @Test
    void convertsJ2000UtcToTheUt1JulianDayExpectedBySwissEphemeris() {
        double julianDayUt = SwissEphAdapter.utcToJulianDayUt(
                Instant.parse("2000-01-01T12:00:00Z"));

        assertEquals(2451545.00000411, julianDayUt, 5.0e-10);
        assertEquals(0.355, (julianDayUt - 2451545.0) * SECONDS_PER_DAY, 0.001);
    }

    @Test
    void treatsEquivalentOffsetInputsAsTheSameUtcInstant() {
        Instant fromOffset = OffsetDateTime.parse("2024-06-01T14:00:00+02:00").toInstant();
        Instant explicitUtc = Instant.parse("2024-06-01T12:00:00Z");

        assertEquals(explicitUtc, fromOffset);
        assertEquals(
                SwissEphAdapter.utcToJulianDayUt(explicitUtc),
                SwissEphAdapter.utcToJulianDayUt(fromOffset));
    }

    @Test
    void roundTripsUtcInstantsThroughJulianDayUt() {
        List<Instant> instants = List.of(
                Instant.parse("1900-01-01T00:00:00Z"),
                Instant.parse("1972-07-01T00:00:00Z"),
                Instant.parse("2000-01-01T12:00:00Z"),
                Instant.parse("2024-06-01T12:34:56.123456Z"),
                Instant.parse("2100-12-31T23:59:59Z"));

        for (Instant expected : instants) {
            double julianDayUt = SwissEphAdapter.utcToJulianDayUt(expected);
            Instant actual = SwissEphAdapter.julianDayUtToUtc(julianDayUt);
            Duration difference = Duration.between(expected, actual).abs();

            assertTrue(
                    difference.compareTo(Duration.ofMillis(1)) < 0,
                    () -> expected + " round-trip difference was " + difference);
            assertEquals(
                    julianDayUt,
                    SwissEphAdapter.utcToJulianDayUt(actual),
                    5.0e-10);
        }
    }

    @Test
    void rejectsInvalidJulianDays() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> SwissEphAdapter.julianDayUtToUtc(Double.NaN));
    }
}
