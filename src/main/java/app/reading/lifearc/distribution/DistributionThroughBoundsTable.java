package app.reading.lifearc.distribution;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Terms;
import app.chart.data.ZodiacSign;

public record DistributionThroughBoundsTable(
        String methodId,
        String primaryDoctrine,
        Terms terms,
        String directedPoint,
        double directedPointLongitude,
        ZodiacSign directedPointSign,
        double directedPointDegreeInSign,
        int directedPointHouse,
        String timingMethod,
        String contactMethod,
        double birthLatitude,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        List<DistributionThroughBoundsPeriod> periods
) {
    public DistributionThroughBoundsTable {
        periods = periods == null ? List.of() : List.copyOf(periods);
    }
}
