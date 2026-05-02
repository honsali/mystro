package app.descriptive.common.model;

import java.time.Instant;
import app.basic.data.SyzygyType;
import app.basic.data.ZodiacSign;

public record PrenatalSyzygyEntry(
        SyzygyType type,
        double julianDay,
        Instant approximateUtcInstant,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        double sunLongitude,
        double moonLongitude,
        double angularSeparation,
        ZodiacSign sunSign,
        ZodiacSign moonSign
) {
}
