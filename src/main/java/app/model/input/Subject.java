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
        this.id = id;
        this.localBirthDateTime = localBirthDateTime;
        this.resolvedUtcInstant = resolvedUtcInstant;
        this.latitude = latitude;
        this.longitude = longitude;
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
