package app.reading.description.common.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record LotEntry(
        String name,
        String displayName,
        String doctrine,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        Planet ruler,
        String formula
) {
}
