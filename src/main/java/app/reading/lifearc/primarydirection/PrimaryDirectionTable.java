package app.reading.lifearc.primarydirection;

import java.time.OffsetDateTime;
import java.util.List;

public record PrimaryDirectionTable(
        String methodId,
        String primaryDoctrine,
        String directionMethod,
        String arcConversionMethod,
        String contactMethod,
        double birthLatitude,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        OffsetDateTime inquiryYearStartDateTime,
        OffsetDateTime inquiryYearEndDateTimeExclusive,
        List<PrimaryDirectionSignificator> significators,
        List<PrimaryDirectionEvent> events
) {
    public PrimaryDirectionTable {
        significators = significators == null ? List.of() : List.copyOf(significators);
        events = events == null ? List.of() : List.copyOf(events);
    }
}
