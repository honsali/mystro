package app.input;

import app.chart.model.Subject;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Validates command-line reading input and converts it to domain objects.
 */
public final class ReadingInputMapper {

    public record ResolvedBundle(Subject subject, LocalDate inquiryDate) {}

    public ResolvedBundle resolve(ReadingInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Input JSON is required");
        }
        Subject subject = toSubject(input);
        return new ResolvedBundle(subject, toInquiryDate(input, subject.getUtcBirthDateTime().toLocalDate()));
    }

    private Subject toSubject(ReadingInput input) {
        if (input.getId() == null || input.getId().isBlank()) {
            throw new IllegalArgumentException("Subject id is required");
        }
        if (input.getBirthDate() == null || input.getBirthDate().isBlank()) {
            throw new IllegalArgumentException("birthDate is required (yyyy-MM-dd)");
        }
        if (input.getBirthTime() == null || input.getBirthTime().isBlank()) {
            throw new IllegalArgumentException("birthTime is required (HH:mm:ss)");
        }
        if (input.getUtcOffset() == null || input.getUtcOffset().isBlank()) {
            throw new IllegalArgumentException("utcOffset is required (e.g. +01:00)");
        }
        if (input.getLatitude() == null) {
            throw new IllegalArgumentException("latitude is required");
        }
        if (input.getLongitude() == null) {
            throw new IllegalArgumentException("longitude is required");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(input.getBirthDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birthDate: " + input.getBirthDate() + " (expected yyyy-MM-dd)");
        }

        LocalTime time;
        try {
            if (!input.getBirthTime().matches("\\d{2}:\\d{2}:\\d{2}")) {
                throw new DateTimeParseException("Expected HH:mm:ss", input.getBirthTime(), 0);
            }
            time = LocalTime.parse(input.getBirthTime());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birthTime: " + input.getBirthTime() + " (expected HH:mm:ss)");
        }

        ZoneOffset offset;
        try {
            offset = ZoneOffset.of(input.getUtcOffset());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid utcOffset: " + input.getUtcOffset());
        }

        double lat = input.getLatitude();
        if (!Double.isFinite(lat)) {
            throw new IllegalArgumentException("Latitude must be finite: " + lat);
        }
        if (lat <= -90.0 || lat >= 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be strictly between -90 and 90 degrees: " + lat);
        }

        double lng = input.getLongitude();
        if (!Double.isFinite(lng)) {
            throw new IllegalArgumentException("Longitude must be finite: " + lng);
        }
        if (lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Longitude out of range: " + lng);
        }

        double elevationMeters = input.getElevationMeters() == null ? 0.0 : input.getElevationMeters();
        if (!Double.isFinite(elevationMeters)) {
            throw new IllegalArgumentException("elevationMeters must be finite: " + elevationMeters);
        }

        return new Subject(
                input.getId(),
                OffsetDateTime.of(date, time, offset),
                lat,
                lng,
                elevationMeters);
    }

    private LocalDate toInquiryDate(ReadingInput input, LocalDate birthDate) {
        if (input.getInquiryDate() == null) {
            return null;
        }
        if (input.getInquiryDate().isBlank()) {
            throw new IllegalArgumentException("inquiryDate must not be blank when supplied");
        }
        LocalDate inquiryDate;
        try {
            inquiryDate = LocalDate.parse(input.getInquiryDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid inquiryDate: " + input.getInquiryDate() + " (expected yyyy-MM-dd)");
        }
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }
        return inquiryDate;
    }
}
