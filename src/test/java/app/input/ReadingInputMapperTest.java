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
        assertEquals(0.0, resolved.subject().getElevationMeters());
        assertEquals("2025-01-15", resolved.inquiryDate().toString());
    }

    @Test
    void propagatesExplicitElevation() {
        ReadingInput input = validInput();
        input.setElevationMeters(2_000.0);

        ReadingInputMapper.ResolvedBundle resolved = mapper.resolve(input);

        assertEquals(2_000.0, resolved.subject().getElevationMeters());
    }

    @Test
    void rejectsNonFiniteElevation() {
        ReadingInput input = validInput();
        input.setElevationMeters(Double.NaN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.resolve(input));

        assertEquals("elevationMeters must be finite: NaN", ex.getMessage());
    }

    @Test
    void rejectsExactGeographicPoles() {
        for (double latitude : new double[] {-90.0, 90.0}) {
            ReadingInput input = validInput();
            input.setLatitude(latitude);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> mapper.resolve(input));

            assertEquals(
                    "Latitude must be strictly between -90 and 90 degrees: " + latitude,
                    ex.getMessage());
        }
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
