package app.reading.lifearc.solarreturn;

import java.util.List;

public record SolarReturnNatalComparisonTable(
        String methodId,
        String primaryDoctrine,
        String sourceSolarReturnMethodId,
        String natalOverlayMethod,
        double conjunctionOrbDegrees,
        int ageStartYears,
        int ageEndYearsInclusive,
        List<SolarReturnNatalComparisonRow> rows
) {
    public SolarReturnNatalComparisonTable {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
