package app.reading.lifearc.transit;

import java.util.List;

public record MonthlyTransitCheckpointTable(
        String methodId,
        String primaryDoctrine,
        String checkpointMethod,
        String contactMethod,
        String activationContactMethod,
        double conjunctionOrbDegrees,
        double activationAspectOrbDegrees,
        int ageStartYears,
        int ageEndYearsInclusive,
        List<MonthlyTransitCheckpointRow> rows
) {
    public MonthlyTransitCheckpointTable {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
