package app.input;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import app.chart.model.Subject;

/** Validates one reading input and creates its immutable subject. */
public final class SubjectFactory {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public Subject create(NatalInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Input JSON is required");
        }
        String id = parseId(input.id());
        LocalDate birthDate = parseDate(input.birthDate());
        LocalTime birthTime = parseTime(input.birthTime());
        ZoneOffset utcOffset = parseOffset(input.utcOffset());
        double latitude = parseLatitude(input.latitude());
        double longitude = parseLongitude(input.longitude());
        double elevationMeters = parseElevation(input.elevationMeters());

        return new Subject(id, OffsetDateTime.of(birthDate, birthTime, utcOffset), latitude, longitude, elevationMeters);
    }

    private String parseId(String value) {
        return required(value, "name");
    }

    private LocalDate parseDate(String value) {
        String birthDate = required(value, "birth_date");
        try {
            return LocalDate.parse(birthDate, INPUT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birth_date: " + value + " (expected dd/MM/yyyy)");
        }
    }

    private LocalTime parseTime(String value) {
        String birthTime = required(value, "birth_time");
        try {
            if (!birthTime.matches("\\d{2}:\\d{2}(:\\d{2})?")) {
                throw new DateTimeParseException("Expected HH:mm or HH:mm:ss", value, 0);
            }
            return LocalTime.parse(birthTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birth_time: " + value + " (expected HH:mm or HH:mm:ss)");
        }
    }

    private ZoneOffset parseOffset(String value) {
        String utcOffset = required(value, "utc_offset");
        try {
            return ZoneOffset.of(utcOffset);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid utc_offset: " + value);
        }
    }

    private double parseLatitude(Double value) {
        double latitude = required(value, "latitude");
        if (!Double.isFinite(latitude)) {
            throw new IllegalArgumentException("Latitude must be finite: " + value);
        }
        if (latitude <= -90.0 || latitude >= 90.0) {
            throw new IllegalArgumentException("Latitude must be strictly between -90 and 90 degrees: " + value);
        }
        return latitude;
    }

    private double parseLongitude(Double value) {
        double longitude = required(value, "longitude");
        if (!Double.isFinite(longitude)) {
            throw new IllegalArgumentException("Longitude must be finite: " + value);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude out of range: " + value);
        }
        return longitude;
    }

    private double parseElevation(Double value) {
        double elevationMeters = value == null ? 0.0 : value;
        if (!Double.isFinite(elevationMeters)) {
            throw new IllegalArgumentException("elevationMeters must be finite: " + value);
        }
        return elevationMeters;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required in native-list.json");
        }
        return value.trim();
    }

    private double required(Double value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required in native-list.json");
        }
        return value;
    }
}
