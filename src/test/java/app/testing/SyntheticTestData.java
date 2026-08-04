package app.testing;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import app.chart.model.Subject;

/**
 * Public, non-personal reference data shared by calculation tests.
 *
 * <p>The instant is the conventional J2000 reference date and the location is Greenwich. It does
 * not describe a real Mystro user and must remain the only reusable natal fixture in committed
 * tests and documentation.
 */
public final class SyntheticTestData {

    public static final String SUBJECT_ID = "synthetic-j2000-greenwich";
    public static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
    public static final OffsetDateTime BIRTH_DATE_TIME = OffsetDateTime.of(
            2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    public static final double LATITUDE = 51.4769;
    public static final double LONGITUDE = 0.0;

    private SyntheticTestData() {}

    public static Subject subject() {
        return new Subject(SUBJECT_ID, BIRTH_DATE_TIME, LATITUDE, LONGITUDE);
    }
}
