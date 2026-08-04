package app.reading.lifearc.zodiacalreleasing;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.ZodiacSign;

public record ZodiacalReleasingTimeline(
        String methodId,
        ZodiacSign startSign,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        List<ZodiacalReleasingPeriod> periods
) {
    public ZodiacalReleasingTimeline {
        periods = periods == null ? List.of() : List.copyOf(periods);
    }
}
