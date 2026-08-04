package app.planetaryhours;

import java.time.LocalDate;

public final class PlanetaryHoursBirthData {

    private final String id;
    private final LocalDate birthDate;
    private final String utcOffset;
    private final double latitude;
    private final double longitude;

    public PlanetaryHoursBirthData(PlanetaryHoursInput input) {
        this.id = input.id();
        this.birthDate = input.birthDate();
        this.utcOffset = input.utcOffset().toString();
        this.latitude = input.latitude();
        this.longitude = input.longitude();
    }

    public String getId() {
        return id;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getUtcOffset() {
        return utcOffset;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
