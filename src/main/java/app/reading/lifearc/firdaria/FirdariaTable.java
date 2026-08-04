package app.reading.lifearc.firdaria;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.Sect;

public record FirdariaTable(
        String methodId,
        String primaryDoctrine,
        String subperiodMethod,
        Sect natalSect,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        List<Planet> mainPeriodSequence,
        List<FirdariaPeriod> periods
) {
    public FirdariaTable {
        mainPeriodSequence = mainPeriodSequence == null ? List.of() : List.copyOf(mainPeriodSequence);
        periods = periods == null ? List.of() : List.copyOf(periods);
    }
}
