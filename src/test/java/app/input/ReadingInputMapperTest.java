package app.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import app.testing.SyntheticTestData;

class ReadingInputMapperTest {
    private final ReadingInputMapper mapper = new ReadingInputMapper();

    @Test
    void resolvesSubjectAndInquiryDate() {
        ReadingInput input = validInput();
        input.setInquiryDate("2025-01-15");

        ReadingInputMapper.ResolvedBundle resolved = mapper.resolve(input);

        assertEquals(SyntheticTestData.SUBJECT_ID, resolved.subject().getId());
        assertEquals("2000-01-01T12:00Z", resolved.subject().getLocalBirthDateTime().toString());
        assertEquals(SyntheticTestData.LATITUDE, resolved.subject().getLatitude());
        assertEquals(SyntheticTestData.LONGITUDE, resolved.subject().getLongitude());
        assertEquals("2025-01-15", resolved.inquiryDate().toString());
    }

    @Test
    void rejectsInquiryBeforeBirthDate() {
        ReadingInput input = validInput();
        input.setInquiryDate("1999-12-31");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.resolve(input));

        assertEquals("inquiryDate must be on or after birthDate", ex.getMessage());
    }

    private ReadingInput validInput() {
        ReadingInput input = new ReadingInput();
        input.setId(SyntheticTestData.SUBJECT_ID);
        input.setBirthDate("2000-01-01");
        input.setBirthTime("12:00:00");
        input.setUtcOffset("+00:00");
        input.setLatitude(SyntheticTestData.LATITUDE);
        input.setLongitude(SyntheticTestData.LONGITUDE);
        return input;
    }
}
