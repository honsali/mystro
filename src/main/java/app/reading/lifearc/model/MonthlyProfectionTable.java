package app.reading.lifearc.model;

import java.util.List;

public record MonthlyProfectionTable(
        String methodId,
        String primaryDoctrine,
        int ageStartYears,
        int ageEndYearsInclusive,
        List<AnnualProfectionReference> referenceOrder,
        List<MonthlyProfectionTableRow> rows
) {
    public MonthlyProfectionTable {
        referenceOrder = referenceOrder == null ? List.of() : List.copyOf(referenceOrder);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
