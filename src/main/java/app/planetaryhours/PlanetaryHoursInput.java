package app.planetaryhours;

import java.time.LocalDate;
import java.time.ZoneOffset;

public record PlanetaryHoursInput(
        String id,
        LocalDate birthDate,
        ZoneOffset utcOffset,
        double latitude,
        double longitude) {
}
