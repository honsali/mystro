package app.reading.lifearc.transit;

import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

public record MonthlyTransitPointEntry(
        PointKey point,
        PointType type,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int transitHouse,
        int natalHouseOverlay,
        boolean retrograde
) {}
