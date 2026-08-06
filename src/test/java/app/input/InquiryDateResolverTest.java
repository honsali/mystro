package app.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class InquiryDateResolverTest {
    private final InquiryDateResolver resolver = new InquiryDateResolver();

    @Test
    void resolvesOptionalInquiryDate() {
        assertNull(resolver.resolve(null, LocalDate.of(2000, 1, 1)));
        assertEquals(
                LocalDate.of(2025, 1, 15),
                resolver.resolve("15/01/2025", LocalDate.of(2000, 1, 1)));
    }

    @Test
    void rejectsInquiryBeforeUtcBirthDate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve("31/12/1999", LocalDate.of(2000, 1, 1)));

        assertEquals("inquiry_date must be on or after birth date", ex.getMessage());
    }
}
