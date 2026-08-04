package app.reading.lifearc.model;

import java.time.LocalDate;
import java.util.List;

public record AnnualProfectionTableRow(
        int ageYears,
        LocalDate periodStartDate,
        LocalDate periodEndDateExclusive,
        int cycleNumber,
        int yearInCycle,
        boolean activeForInquiry,
        List<AnnualProfectionReferenceEntry> referenceProfections
) {
    public AnnualProfectionTableRow {
        referenceProfections = referenceProfections == null ? List.of() : List.copyOf(referenceProfections);
    }
}
