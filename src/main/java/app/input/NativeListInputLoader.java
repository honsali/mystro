package app.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads a local native-list.json alias entry and normalizes it to the public ReadingInput DTO.
 */
public final class NativeListInputLoader {

    public static final Path DEFAULT_PATH = Path.of("native-list.json");

    private static final DateTimeFormatter NATIVE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OUTPUT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ReadingInput load(String alias, ObjectMapper objectMapper) throws IOException {
        return load(DEFAULT_PATH, alias, objectMapper);
    }

    public ReadingInput load(Path nativeListPath, String alias, ObjectMapper objectMapper) throws IOException {
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("Native-list alias is required");
        }
        if (nativeListPath == null || !Files.exists(nativeListPath)) {
            Path missingPath = nativeListPath == null ? DEFAULT_PATH : nativeListPath;
            throw new IllegalArgumentException("Native-list file not found: " + missingPath.toAbsolutePath());
        }
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper is required");
        }

        NativeListEntry[] entries = objectMapper.readValue(nativeListPath.toFile(), NativeListEntry[].class);
        NativeListEntry entry = Arrays.stream(entries == null ? new NativeListEntry[0] : entries)
                .filter(candidate -> matchesAlias(candidate, alias))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No native-list.json entry found for alias: " + alias + availableAliases(entries)));
        return toReadingInput(entry);
    }

    private static boolean matchesAlias(NativeListEntry entry, String alias) {
        return entry != null
                && entry.name != null
                && entry.name.trim().equalsIgnoreCase(alias.trim());
    }

    private static ReadingInput toReadingInput(NativeListEntry entry) {
        ReadingInput input = new ReadingInput();
        input.setId(required(entry.name, "name"));
        input.setBirthDate(normalizeDate(required(entry.birthDate, "birth_date"), "birth_date"));
        input.setBirthTime(normalizeTime(required(entry.birthTime, "birth_time")));
        input.setUtcOffset(required(entry.utcOffset, "utc_offset"));
        input.setLatitude(required(entry.latitude, "latitude"));
        input.setLongitude(required(entry.longitude, "longitude"));
        if (entry.inquiryDate != null && !entry.inquiryDate.isBlank()) {
            input.setInquiryDate(normalizeDate(entry.inquiryDate.trim(), "inquiry_date"));
        }
        return input;
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required in native-list.json");
        }
        return value.trim();
    }

    private static Double required(Double value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required in native-list.json");
        }
        return value;
    }

    private static String normalizeDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value, NATIVE_DATE_FORMAT).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + " in native-list.json: " + value + " (expected dd/MM/yyyy)");
        }
    }

    private static String normalizeTime(String value) {
        String trimmed = value.trim();
        String normalized = trimmed.matches("\\d{2}:\\d{2}") ? trimmed + ":00" : trimmed;
        try {
            if (!normalized.matches("\\d{2}:\\d{2}:\\d{2}")) {
                throw new DateTimeParseException("Expected HH:mm or HH:mm:ss", trimmed, 0);
            }
            return LocalTime.parse(normalized).format(OUTPUT_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid birth_time in native-list.json: " + value + " (expected HH:mm or HH:mm:ss)");
        }
    }

    private static String availableAliases(NativeListEntry[] entries) {
        if (entries == null || entries.length == 0) {
            return "";
        }
        String aliases = Arrays.stream(entries)
                .filter(Objects::nonNull)
                .map(entry -> entry.name)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
        return aliases.isBlank() ? "" : " (available aliases: " + aliases + ")";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class NativeListEntry {
        @JsonProperty("name")
        private String name;

        @JsonProperty("birth_date")
        private String birthDate;

        @JsonProperty("birth_time")
        private String birthTime;

        @JsonProperty("inquiry_date")
        private String inquiryDate;

        @JsonProperty("utc_offset")
        private String utcOffset;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;
    }
}
