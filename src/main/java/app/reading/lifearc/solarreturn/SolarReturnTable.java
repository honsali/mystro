package app.reading.lifearc.solarreturn;

import java.util.List;

import app.chart.data.ZodiacSign;

public record SolarReturnTable(
        String methodId,
        String primaryDoctrine,
        String locationMethod,
        int ageStartYears,
        int ageEndYearsInclusive,
        double natalSunLongitude,
        ZodiacSign natalSunSign,
        double natalSunDegreeInSign,
        List<SolarReturnEntry> rows
) {
    public SolarReturnTable {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
