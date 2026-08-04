package app.reading.lifearc.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record DailyProfectionActivatedLot(
        String name,
        String displayName,
        ZodiacSign sign,
        int house,
        Planet ruler
) {}
