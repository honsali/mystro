package app.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.model.input.InputListBundle;
import app.model.input.Subject;
import app.output.Logger;

public final class SubjectListParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public void parse(InputListBundle input) throws IOException {
        List<Subject> subjects = new ArrayList<>();
        Path path = Path.of("input", "subject-list.json");
        if (Files.exists(path)) {

            JsonNode root = mapper.readTree(path.toFile());
            JsonNode subjectArray = root.isArray() ? root : root.path("natal");

            if (subjectArray.isArray()) {
                for (JsonNode node : subjectArray) {
                    String inputSubjectId = text(node, "id");
                    if (inputSubjectId != null && input.getSubjectIds().contains(inputSubjectId)) {
                        Subject subject = readSubject(node);
                        if (subject != null) {
                            subjects.add(subject);
                        }
                    }
                }
            }
        }
        if (subjects.isEmpty()) {
            Logger.instance.error("subject", "No subjects requested. Use --subjects <id> [...]");
        }
        input.setSubjects(subjects);
    }

    private Subject readSubject(JsonNode node) {
        String id = text(node, "id");
        LocalDate date = parseDate(node, "birthDate", id);
        LocalTime time = parseTime(node, "birthTime", id);
        ZoneOffset offset = parseOffset(node, "utcOffset", id);
        Double latitude = parseLocation(node, "latitude", id);
        Double longitude = parseLocation(node, "longitude", id);
        if (date == null || time == null || offset == null || latitude == null || longitude == null) {
            return null;
        }
        return new Subject(id, OffsetDateTime.of(date, time, offset), latitude, longitude);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private LocalDate parseDate(JsonNode node, String field, String id) {
        String value = text(node, field);
        if (value == null) {
            Logger.instance.error("input", "Missing Date " + field + " for " + id);
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            Logger.instance.error("input", "Invalid Date " + field + " for " + id + ": " + value);
            return null;
        }
    }

    private LocalTime parseTime(JsonNode node, String field, String id) {
        String value = text(node, field);
        if (value == null) {
            Logger.instance.error("input", "Missing Time " + field + " for " + id);
            return null;
        }
        try {
            if (!value.matches("\\d{2}:\\d{2}:\\d{2}")) {
                throw new DateTimeParseException("Expected HH:mm:ss", value, 0);
            }
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            Logger.instance.error("input", "Invalid Time " + field + " for " + id + ": " + value);
            return null;
        }
    }

    private Double parseLocation(JsonNode node, String field, String id) {
        String value = text(node, field);
        if (value == null) {
            Logger.instance.error("input", "Missing Location " + field + " for " + id);
            return null;
        }
        double location;
        try {
            location = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Logger.instance.error("input", "Invalid Location " + field + " for " + id + ": " + value);
            return null;
        }
        if ("latitude".equals(field) && (location < -90 || location > 90)) {
            Logger.instance.error("input", "Latitude out of range: " + location);
            return null;
        }
        if ("longitude".equals(field) && (location < -180 || location > 180)) {
            Logger.instance.error("input", "Longitude out of range: " + location);
            return null;
        }

        return location;
    }

    private ZoneOffset parseOffset(JsonNode node, String field, String id) {
        String value = text(node, field);
        if (value == null) {
            Logger.instance.error("input", "Missing Offset " + field + " for " + id);
            return null;
        }
        try {
            return ZoneOffset.of(value);
        } catch (DateTimeException e) {
            Logger.instance.error("input", "Invalid Offset " + field + " for " + id + ": " + value);
            return null;
        }
    }
}
