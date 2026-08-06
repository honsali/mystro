package app.chart.model;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Subject(
        String id,
        OffsetDateTime localBirthDateTime,
        Instant resolvedUtcInstant,
        double latitude,
        double longitude,
        double elevationMeters
) {

    public Subject(String id, OffsetDateTime localBirthDateTime, double latitude, double longitude) {
        this(id, localBirthDateTime, localBirthDateTime.toInstant(), latitude, longitude, 0.0);
    }

    public Subject(String id, OffsetDateTime localBirthDateTime, double latitude, double longitude,
                   double elevationMeters) {
        this(id, localBirthDateTime, localBirthDateTime.toInstant(), latitude, longitude, elevationMeters);
    }

    public Subject(String id, OffsetDateTime localBirthDateTime, Instant resolvedUtcInstant,
                   double latitude, double longitude) {
        this(id, localBirthDateTime, resolvedUtcInstant, latitude, longitude, 0.0);
    }

    public String getId() {
        return id;
    }

    public OffsetDateTime getLocalBirthDateTime() {
        return localBirthDateTime;
    }

    public Instant getResolvedUtcInstant() {
        return resolvedUtcInstant;
    }

    @JsonIgnore
    public OffsetDateTime getUtcBirthDateTime() {
        return resolvedUtcInstant.atOffset(ZoneOffset.UTC);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevationMeters() {
        return elevationMeters;
    }
}
