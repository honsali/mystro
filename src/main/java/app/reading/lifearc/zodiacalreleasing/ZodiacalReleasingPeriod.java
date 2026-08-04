package app.reading.lifearc.zodiacalreleasing;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.ZodiacSign;

public record ZodiacalReleasingPeriod(
        int level,
        ZodiacSign sign,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        int sequenceIndex,
        List<ZodiacalReleasingMarker> markers,
        List<ZodiacalReleasingPeriod> subPeriods
) {
    public ZodiacalReleasingPeriod {
        markers = markers == null ? List.of() : List.copyOf(markers);
        subPeriods = subPeriods == null ? List.of() : List.copyOf(subPeriods);
    }
}
