package app.model.basic;

import app.model.data.PointType;
import app.model.data.ZodiacSign;

public record LotPointEntry(
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        double antisciaLongitude,
        double contraAntisciaLongitude
) implements PointEntry {
    @Override
    public PointType getType() {
        return PointType.LOT;
    }
}
