package app.descriptive.common.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.descriptive.common.data.LotName;

public record LotEntry(
        LotName name,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        Planet ruler,
        String formula
) {
}
