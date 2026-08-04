package app.reading.lifearc.model;

import java.time.OffsetDateTime;
import java.util.List;

public record MonthlyProfectionTableRow(
        int ageYears,
        OffsetDateTime periodStartDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        int cycleNumber,
        int yearInCycle,
        int monthInYear,
        boolean activeForInquiry,
        List<MonthlyProfectionReferenceEntry> referenceProfections
) {
    public MonthlyProfectionTableRow {
        referenceProfections = referenceProfections == null ? List.of() : List.copyOf(referenceProfections);
    }
}
