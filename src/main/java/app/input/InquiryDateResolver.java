package app.input;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Resolves the optional date used by local timing reports. */
public final class InquiryDateResolver {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public LocalDate resolve(String value, LocalDate birthDate) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("inquiry_date must not be blank when supplied");
        }

        LocalDate inquiryDate;
        try {
            inquiryDate = LocalDate.parse(value.trim(), INPUT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid inquiry_date: " + value + " (expected dd/MM/yyyy)");
        }
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiry_date must be on or after birth date");
        }
        return inquiryDate;
    }
}
