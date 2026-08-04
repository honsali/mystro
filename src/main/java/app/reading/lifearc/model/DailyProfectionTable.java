package app.reading.lifearc.model;

import java.time.LocalDate;
import java.util.List;

public record DailyProfectionTable(
        String methodId,
        String primaryDoctrine,
        String dailyStepMethod,
        LocalDate focusDate,
        LocalDate windowStartDate,
        LocalDate windowEndDate,
        List<AnnualProfectionReference> referenceOrder,
        List<DailyProfectionTableRow> rows
) {
    public DailyProfectionTable {
        referenceOrder = referenceOrder == null ? List.of() : List.copyOf(referenceOrder);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
