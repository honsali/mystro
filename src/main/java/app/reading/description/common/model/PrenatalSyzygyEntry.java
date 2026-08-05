package app.reading.description.common.model;

import java.time.Instant;
import app.chart.data.SyzygyType;
import app.chart.data.ZodiacSign;

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
