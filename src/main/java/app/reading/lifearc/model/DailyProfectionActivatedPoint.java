package app.reading.lifearc.model;

import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

public record DailyProfectionActivatedPoint(
        PointKey point,
        PointType type,
        ZodiacSign sign,
        int house
) {}
