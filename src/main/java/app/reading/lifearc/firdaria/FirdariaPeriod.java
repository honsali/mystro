package app.reading.lifearc.firdaria;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;

public record FirdariaPeriod(
        int cycleNumber,
        int sequenceIndex,
        Planet ruler,
        int nominalYears,
        int startAgeYears,
        int endAgeYearsExclusive,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        boolean activeForInquiry,
        List<FirdariaSubperiod> subperiods
) {
    public FirdariaPeriod {
        subperiods = subperiods == null ? List.of() : List.copyOf(subperiods);
    }
}
