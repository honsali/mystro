package app.reading.lifearc.lunar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.Chart;
import app.chart.model.Subject;

class LunarZoomCalculatorTest {

    private final LunarZoomCalculator calculator = new LunarZoomCalculator();

    @Test
    void calculatesMoonSignIngressesInsideWindow() {
        Subject subject = subject();
        OffsetDateTime start = OffsetDateTime.of(2022, 6, 29, 22, 55, 0, 0, ZoneOffset.ofHours(1));
        OffsetDateTime end = OffsetDateTime.of(2022, 7, 29, 22, 55, 0, 0, ZoneOffset.ofHours(1));

        LunarZoomTable table = calculator.calculate(subject, chart(), start, end);

        assertEquals(LunarZoomCalculator.METHOD_ID, table.methodId());
        assertEquals(start, table.windowStartDateTime());
        assertEquals(end, table.windowEndDateTime());
        assertFalse(table.signIngresses().isEmpty());
        assertTrue(table.signIngresses().size() >= 10);
        for (LunarSignIngressEntry entry : table.signIngresses()) {
            assertTrue(!entry.dateTime().isBefore(start) && !entry.dateTime().isAfter(end));
            assertEquals(entry.toSign(), advance(entry.fromSign(), 1));
            assertTrue(entry.natalHouseOverlay() >= 1 && entry.natalHouseOverlay() <= 12);
            assertTrue(entry.moonDegreeInSign() < 0.01);
        }
    }

    @Test
    void rejectsInvalidInputs() {
        Subject subject = subject();
        Chart chart = chart();
        OffsetDateTime start = OffsetDateTime.of(2022, 6, 29, 22, 55, 0, 0, ZoneOffset.ofHours(1));
        OffsetDateTime end = OffsetDateTime.of(2022, 7, 29, 22, 55, 0, 0, ZoneOffset.ofHours(1));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null, chart, start, end));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(subject, null, start, end));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(subject, chart, end, start));
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }

    private Chart chart() {
        Chart chart = new Chart();
        chart.setHouses(List.of(
                house(1, ZodiacSign.PISCES),
                house(2, ZodiacSign.ARIES),
                house(3, ZodiacSign.TAURUS),
                house(4, ZodiacSign.GEMINI),
                house(5, ZodiacSign.CANCER),
                house(6, ZodiacSign.LEO),
                house(7, ZodiacSign.VIRGO),
                house(8, ZodiacSign.LIBRA),
                house(9, ZodiacSign.SCORPIO),
                house(10, ZodiacSign.SAGITTARIUS),
                house(11, ZodiacSign.CAPRICORN),
                house(12, ZodiacSign.AQUARIUS)
        ));
        return chart;
    }

    private HousePosition house(int house, ZodiacSign sign) {
        return new HousePosition(house, sign.ordinal() * 30.0, sign);
    }

    private ZodiacSign advance(ZodiacSign sign, int signs) {
        ZodiacSign[] values = ZodiacSign.values();
        return values[Math.floorMod(sign.ordinal() + signs, values.length)];
    }
}
