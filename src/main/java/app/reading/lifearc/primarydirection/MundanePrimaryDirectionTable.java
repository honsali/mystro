package app.reading.lifearc.primarydirection;

import java.time.OffsetDateTime;
import java.util.List;

public record MundanePrimaryDirectionTable(
        String methodId,
        String primaryDoctrine,
        String directionMethod,
        String arcConversionMethod,
        String contactMethod,
        String prototypeCaveat,
        double birthLatitude,
        double natalArmcDegrees,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        OffsetDateTime inquiryYearStartDateTime,
        OffsetDateTime inquiryYearEndDateTimeExclusive,
        List<MundanePrimaryDirectionSignificator> significators,
        List<MundanePrimaryDirectionEvent> events
) {
    public MundanePrimaryDirectionTable {
        significators = significators == null ? List.of() : List.copyOf(significators);
        events = events == null ? List.of() : List.copyOf(events);
    }
}
