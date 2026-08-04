package app.reading.lifearc.decennial;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;

public record DecennialPeriod(
        int cycleNumber,
        int sequenceIndex,
        Planet ruler,
        DecennialRulerCondition rulerNatalCondition,
        int startAgeYears,
        int endAgeYearsExclusive,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        boolean activeForInquiry,
        List<DecennialSubperiod> subperiods
) {
    public DecennialPeriod {
        subperiods = subperiods == null ? List.of() : List.copyOf(subperiods);
    }
}
