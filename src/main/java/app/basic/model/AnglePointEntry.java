package app.basic.model;

import app.basic.data.PointType;
import app.basic.data.ZodiacSign;

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
