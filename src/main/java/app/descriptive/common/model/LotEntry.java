package app.descriptive.common.model;

import app.basic.data.Planet;
import app.basic.data.ZodiacSign;
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
