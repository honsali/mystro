package app.reading.lifearc.decennial;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.Sect;

public record DecennialTable(
        String methodId,
        String primaryDoctrine,
        String sequenceMethod,
        String subperiodMethod,
        Sect natalSect,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        List<Planet> rulerSequence,
        List<DecennialPeriod> periods
) {
    public DecennialTable {
        rulerSequence = rulerSequence == null ? List.of() : List.copyOf(rulerSequence);
        periods = periods == null ? List.of() : List.copyOf(periods);
    }
}
