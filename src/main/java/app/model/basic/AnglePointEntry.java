package app.model.basic;

import app.model.data.PointType;
import app.model.data.ZodiacSign;

public record AnglePointEntry(
        double longitude,
        ZodiacSign sign,
        double degreeInSign
) implements PointEntry {
    @Override
    public PointType getType() {
        return PointType.ANGLE;
    }
}
