package app.reading.lifearc.model;

import java.util.List;

public record AnnualProfectionTable(
        String methodId,
        String primaryDoctrine,
        int ageStartYears,
        int ageEndYearsInclusive,
        List<AnnualProfectionReference> referenceOrder,
        List<AnnualProfectionTableRow> rows
) {
    public AnnualProfectionTable {
        referenceOrder = referenceOrder == null ? List.of() : List.copyOf(referenceOrder);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
