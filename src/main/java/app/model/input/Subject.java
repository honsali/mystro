package app.model.input;

import java.time.Instant;
import java.time.OffsetDateTime;

public final class Subject {
    private final String id;
    private final OffsetDateTime localBirthDateTime;
    private final Instant resolvedUtcInstant;
    private final double latitude;
    private final double longitude;

    public Subject(String id, OffsetDateTime localBirthDateTime, double latitude, double longitude) {
        this(id, localBirthDateTime, localBirthDateTime.toInstant(), latitude, longitude);
    }

    public Subject(String id, OffsetDateTime localBirthDateTime, Instant resolvedUtcInstant, double latitude, double longitude) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Subject id is required");
        }
        if (localBirthDateTime == null) {
            throw new IllegalArgumentException("Subject local birth date/time is required");
        }
        if (resolvedUtcInstant == null) {
            throw new IllegalArgumentException("Subject resolved UTC instant is required");
        }
        requireFinite("latitude", latitude);
        requireFinite("longitude", longitude);
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }
        this.id = id;
        this.localBirthDateTime = localBirthDateTime;
        this.resolvedUtcInstant = resolvedUtcInstant;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static void requireFinite(String field, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Subject " + field + " must be finite: " + value);
        }
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
