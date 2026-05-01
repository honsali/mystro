package app.model.basic;

import java.time.Instant;
import app.model.data.PointType;
import app.model.data.SyzygyType;
import app.model.data.ZodiacSign;

public record SyzygyPointEntry(
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        SyzygyType syzygyType,
        double julianDay,
        Instant approximateUtcInstant,
        double sunLongitudeAtSyzygy,
        double moonLongitudeAtSyzygy,
        double angularSeparation,
        ZodiacSign sunSignAtSyzygy,
        ZodiacSign moonSignAtSyzygy
) implements PointEntry {
    @Override
    public PointType getType() {
        return PointType.SYZYGY_POINT;
    }
}
