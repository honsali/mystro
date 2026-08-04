package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.NatalChart;
import app.chart.model.PointEntry;
import app.reading.description.common.model.DodecatemoriaEntry;
import app.reading.description.common.model.LotEntry;

class ValensDodecatemoriaCalculatorTest {
    private final ValensDodecatemoriaCalculator calculator = new ValensDodecatemoriaCalculator();

    @Test
    void calculatesTwelfthPartForChartPointsAndLots() {
        NatalChart chart = new NatalChart();
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.ASCENDANT, new AnglePointEntry(15.0, ZodiacSign.ARIES, 15.0));
        chart.setPoints(points);
        chart.setLots(List.of(new LotEntry(
                "FORTUNE",
                "Fortune",
                "valens",
                282.5,
                ZodiacSign.CAPRICORN,
                12.5,
                1,
                null,
                "test"
        )));

        List<DodecatemoriaEntry> entries = calculator.calculate(chart);

        assertEquals(2, entries.size());
        DodecatemoriaEntry ascendant = entries.get(0);
        assertEquals("ASCENDANT", ascendant.sourceName());
        assertEquals("POINT", ascendant.sourceType());
        assertNull(ascendant.sourceDoctrine());
        assertEquals(7, ascendant.twelfthPart());
        assertEquals(180.0, ascendant.longitude());
        assertEquals(ZodiacSign.LIBRA, ascendant.sign());
        assertEquals(0.0, ascendant.degreeInSign());

        DodecatemoriaEntry fortune = entries.get(1);
        assertEquals("FORTUNE", fortune.sourceName());
        assertEquals("LOT", fortune.sourceType());
        assertEquals("valens", fortune.sourceDoctrine());
        assertEquals(6, fortune.twelfthPart());
        assertEquals(60.0, fortune.longitude());
        assertEquals(ZodiacSign.GEMINI, fortune.sign());
        assertEquals(0.0, fortune.degreeInSign());
    }
}
