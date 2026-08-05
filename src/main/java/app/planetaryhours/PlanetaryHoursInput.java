package app.planetaryhours;

import java.time.LocalDate;
import java.time.ZoneOffset;

public record PlanetaryHoursInput(
        String id,
        LocalDate birthDate,
        ZoneOffset utcOffset,
        double latitude,
        double longitude,
        double elevationMeters) {

    public PlanetaryHoursInput(String id, LocalDate birthDate, ZoneOffset utcOffset,
                               double latitude, double longitude) {
        this(id, birthDate, utcOffset, latitude, longitude, 0.0);
    }

    public PlanetaryHoursInput {
        if (!Double.isFinite(elevationMeters)) {
            throw new IllegalArgumentException("elevationMeters must be finite: " + elevationMeters);
        }
    }
}
