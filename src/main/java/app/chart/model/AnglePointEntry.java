package app.chart.model;

import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

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
