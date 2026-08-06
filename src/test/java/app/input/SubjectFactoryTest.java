package app.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import app.chart.model.Subject;
import app.testing.SyntheticTestData;

class SubjectFactoryTest {
    private final SubjectFactory factory = new SubjectFactory();

    @Test
    void createsSubject() {
        Subject subject = factory.create(validInput());

        assertEquals(SyntheticTestData.SUBJECT_ID, subject.id());
        assertEquals("2000-01-01T12:00Z", subject.localBirthDateTime().toString());
        assertEquals("2000-01-01T12:00Z", subject.getUtcBirthDateTime().toString());
        assertEquals(SyntheticTestData.LATITUDE, subject.latitude());
        assertEquals(SyntheticTestData.LONGITUDE, subject.longitude());
        assertEquals(0.0, subject.elevationMeters());
    }

    @Test
    void propagatesExplicitElevation() {
        NatalInput input = validInput();

        Subject subject = factory.create(new NatalInput(
                input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                input.latitude(), input.longitude(), 2_000.0));

        assertEquals(2_000.0, subject.elevationMeters());
    }

    @Test
    void rejectsNonFiniteElevation() {
        NatalInput input = validInput();
        NatalInput invalid = new NatalInput(
                input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                input.latitude(), input.longitude(), Double.NaN);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.create(invalid));

        assertEquals("elevationMeters must be finite: NaN", ex.getMessage());
    }

    @Test
    void rejectsExactGeographicPoles() {
        NatalInput input = validInput();
        for (double latitude : new double[] {-90.0, 90.0}) {
            NatalInput invalid = new NatalInput(
                    input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                    latitude, input.longitude(), input.elevationMeters());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> factory.create(invalid));

            assertEquals(
                    "Latitude must be strictly between -90 and 90 degrees: " + latitude,
                    ex.getMessage());
        }
    }

    @Test
    void resolvesCanonicalUtcInstant() {
        NatalInput input = validInput();
        NatalInput offsetInput = new NatalInput(
                input.id(), "01/03/2000", "00:30:00", input.inquiryDate(), "+02:00",
                input.latitude(), input.longitude(), input.elevationMeters());

        Subject subject = factory.create(offsetInput);

        assertEquals("2000-03-01T00:30+02:00", subject.localBirthDateTime().toString());
        assertEquals("2000-02-29T22:30Z", subject.getUtcBirthDateTime().toString());
    }

    @Test
    void rejectsMissingRequiredFields() {
        NatalInput input = validInput();
        assertRequired(new NatalInput(
                null, input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                input.latitude(), input.longitude(), input.elevationMeters()),
                "name is required in native-list.json");
        assertRequired(new NatalInput(
                input.id(), null, input.birthTime(), input.inquiryDate(), input.utcOffset(),
                input.latitude(), input.longitude(), input.elevationMeters()),
                "birth_date is required in native-list.json");
        assertRequired(new NatalInput(
                input.id(), input.birthDate(), null, input.inquiryDate(), input.utcOffset(),
                input.latitude(), input.longitude(), input.elevationMeters()),
                "birth_time is required in native-list.json");
        assertRequired(new NatalInput(
                input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), null,
                input.latitude(), input.longitude(), input.elevationMeters()),
                "utc_offset is required in native-list.json");
        assertRequired(new NatalInput(
                input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                null, input.longitude(), input.elevationMeters()),
                "latitude is required in native-list.json");
        assertRequired(new NatalInput(
                input.id(), input.birthDate(), input.birthTime(), input.inquiryDate(), input.utcOffset(),
                input.latitude(), null, input.elevationMeters()),
                "longitude is required in native-list.json");
    }

    private void assertRequired(NatalInput input, String expectedMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.create(input));

        assertEquals(expectedMessage, ex.getMessage());
    }

    private NatalInput validInput() {
        return new NatalInput(
                SyntheticTestData.SUBJECT_ID,
                "01/01/2000",
                "12:00",
                null,
                "+00:00",
                SyntheticTestData.LATITUDE,
                SyntheticTestData.LONGITUDE,
                null);
    }
}
