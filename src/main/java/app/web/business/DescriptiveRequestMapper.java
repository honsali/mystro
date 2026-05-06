package app.web.business;

import app.doctrine.Doctrine;
import app.input.DoctrineLoader;
import app.input.model.Subject;

import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Validates a {@link DescriptiveRequest} and converts it to domain objects.
 */
@Component
public final class DescriptiveRequestMapper {

    private final DoctrineLoader doctrineLoader;

    public DescriptiveRequestMapper(DoctrineLoader doctrineLoader) {
        this.doctrineLoader = doctrineLoader;
    }

    /**
     * Result of a successful request mapping.
     */
    public record ResolvedBundle(Subject subject, Doctrine doctrine) {}

    /**
     * Validate and convert the request. Throws {@link IllegalArgumentException} with a
     * user-facing message on validation failure.
     */
    public ResolvedBundle resolve(DescriptiveRequest req) {
        Subject subject = toSubject(req);

        String doctrineId = req.getDoctrine();
        if (doctrineId == null || doctrineId.isBlank()) {
            throw new IllegalArgumentException("Doctrine id is required");
        }

        Doctrine doctrine = doctrineLoader.find(doctrineId.trim()).orElse(null);
        if (doctrine == null) {
            throw new IllegalArgumentException("Unknown doctrine: " + doctrineId.trim());
        }

        return new ResolvedBundle(subject, doctrine);
    }

    private Subject toSubject(DescriptiveRequest req) {
        if (req.getId() == null || req.getId().isBlank()) {
            throw new IllegalArgumentException("Subject id is required");
        }
        if (req.getBirthDate() == null || req.getBirthDate().isBlank()) {
            throw new IllegalArgumentException("birthDate is required (yyyy-MM-dd)");
        }
        if (req.getBirthTime() == null || req.getBirthTime().isBlank()) {
            throw new IllegalArgumentException("birthTime is required (HH:mm:ss)");
        }
        if (req.getUtcOffset() == null || req.getUtcOffset().isBlank()) {
            throw new IllegalArgumentException("utcOffset is required (e.g. +01:00)");
        }
        if (req.getLatitude() == null) {
            throw new IllegalArgumentException("latitude is required");
        }
        if (req.getLongitude() == null) {
            throw new IllegalArgumentException("longitude is required");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(req.getBirthDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birthDate: " + req.getBirthDate() + " (expected yyyy-MM-dd)");
        }

        LocalTime time;
        try {
            if (!req.getBirthTime().matches("\\d{2}:\\d{2}:\\d{2}")) {
                throw new DateTimeParseException("Expected HH:mm:ss", req.getBirthTime(), 0);
            }
            time = LocalTime.parse(req.getBirthTime());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birthTime: " + req.getBirthTime() + " (expected HH:mm:ss)");
        }

        ZoneOffset offset;
        try {
            offset = ZoneOffset.of(req.getUtcOffset());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid utcOffset: " + req.getUtcOffset());
        }

        double lat = req.getLatitude();
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude out of range: " + lat);
        }

        double lng = req.getLongitude();
        if (lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Longitude out of range: " + lng);
        }

        return new Subject(req.getId(), OffsetDateTime.of(date, time, offset), lat, lng);
    }
}
