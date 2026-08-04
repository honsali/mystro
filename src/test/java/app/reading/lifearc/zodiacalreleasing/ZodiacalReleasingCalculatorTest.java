package app.reading.lifearc.zodiacalreleasing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.data.ZodiacSign;

class ZodiacalReleasingCalculatorTest {

    private final ZodiacalReleasingCalculator calculator = new ZodiacalReleasingCalculator();

    @Test
    void sagittariusFortuneMatchesKnownEarlyL4Sequence() {
        OffsetDateTime birth = app.testing.SyntheticTestData.BIRTH_DATE_TIME;
        ZodiacalReleasingTimeline timeline = calculator.calculate(ZodiacSign.SAGITTARIUS, birth, birth.plusYears(1));

        ZodiacalReleasingPeriod l2Sagittarius = timeline.periods().get(0).subPeriods().get(0);
        ZodiacalReleasingPeriod l3Sagittarius = l2Sagittarius.subPeriods().get(0);
        List<ZodiacalReleasingPeriod> l4 = l3Sagittarius.subPeriods();

        assertPeriod(l4.get(0), ZodiacSign.SAGITTARIUS, "2000-01-01T12:00Z", List.of());
        assertPeriod(l4.get(1), ZodiacSign.CAPRICORN, "2000-01-04T00:00Z", List.of());
        assertPeriod(l4.get(2), ZodiacSign.AQUARIUS, "2000-01-09T15:00Z", List.of());
        assertPeriod(l4.get(3), ZodiacSign.PISCES, "2000-01-15T21:00Z", List.of());
        assertPeriod(l4.get(4), ZodiacSign.ARIES, "2000-01-18T09:00Z", List.of());
        assertPeriod(l4.get(5), ZodiacSign.TAURUS, "2000-01-21T12:00Z", List.of());
        assertPeriod(l4.get(6), ZodiacSign.GEMINI, "2000-01-23T04:00Z", List.of(ZodiacalReleasingMarker.PREPARATORY_LOOSING_OF_BOND));
        assertPeriod(l4.get(7), ZodiacSign.CANCER, "2000-01-27T08:00Z", List.of());
    }

    @Test
    void capricornSubperiodEmitsPlooseningLooseningCulminationAndCompletionMarkers() {
        OffsetDateTime birth = app.testing.SyntheticTestData.BIRTH_DATE_TIME;
        ZodiacalReleasingTimeline timeline = calculator.calculate(ZodiacSign.SAGITTARIUS, birth, birth.plusYears(1));

        ZodiacalReleasingPeriod l2Sagittarius = timeline.periods().get(0).subPeriods().get(0);
        ZodiacalReleasingPeriod l3Capricorn = l2Sagittarius.subPeriods().get(1);
        List<ZodiacalReleasingPeriod> l4 = l3Capricorn.subPeriods();

        assertPeriod(l4.get(6), ZodiacSign.CANCER, "2000-02-23T20:00Z", List.of(ZodiacalReleasingMarker.PREPARATORY_LOOSING_OF_BOND));
        assertPeriod(l4.get(9), ZodiacSign.LIBRA, "2000-03-08T04:00Z", List.of(ZodiacalReleasingMarker.CULMINATION));
        assertPeriod(l4.get(12), ZodiacSign.CANCER, "2000-03-15T11:00Z", List.of(ZodiacalReleasingMarker.LOOSING_OF_BOND));
        assertPeriod(l4.get(15), ZodiacSign.LIBRA, "2000-03-28T19:00Z", List.of(ZodiacalReleasingMarker.CULMINATION));
        assertPeriod(l4.get(18), ZodiacSign.CAPRICORN, "2000-04-05T02:00Z", List.of(ZodiacalReleasingMarker.COMPLETION));
        assertTrue(l4.get(18).endDateTimeExclusive().isEqual(OffsetDateTime.parse("2000-04-08T00:00Z")));
    }

    private void assertPeriod(ZodiacalReleasingPeriod period, ZodiacSign sign, String start, List<ZodiacalReleasingMarker> markers) {
        assertEquals(sign, period.sign());
        assertEquals(OffsetDateTime.parse(start), period.startDateTime());
        assertEquals(markers, period.markers());
    }
}
