package app.reading.lifearc.solarreturn;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Sect;
import app.chart.data.ZodiacSign;

public record SolarReturnEntry(
        int ageYears,
        OffsetDateTime returnDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        double julianDayUt,
        double sunLongitude,
        ZodiacSign sunSign,
        double sunDegreeInSign,
        double ascendantLongitude,
        ZodiacSign ascendantSign,
        double ascendantDegreeInSign,
        double midheavenLongitude,
        ZodiacSign midheavenSign,
        double midheavenDegreeInSign,
        Sect sect,
        List<SolarReturnPointEntry> points
) {
    public SolarReturnEntry {
        points = points == null ? List.of() : List.copyOf(points);
    }
}
