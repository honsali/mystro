package app.chart.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class SubjectTest {

    private static final OffsetDateTime DATE_TIME = OffsetDateTime.parse("2000-01-01T12:00:00Z");

    @Test
    void acceptsLatitudesImmediatelyInsideThePoles() {
        Subject north = new Subject("near-north-pole", DATE_TIME, 89.999, 0.0);
        Subject south = new Subject("near-south-pole", DATE_TIME, -89.999, 0.0);

        assertEquals(89.999, north.getLatitude());
        assertEquals(-89.999, south.getLatitude());
    }

    @Test
    void preservesInputDateTimeAndExposesCanonicalUtcDateTime() {
        OffsetDateTime inputDateTime = OffsetDateTime.parse("2000-03-01T00:30:00+02:00");

        Subject subject = new Subject("utc-boundary", inputDateTime, 0.0, 0.0);

        assertEquals(inputDateTime, subject.getLocalBirthDateTime());
        assertEquals(Instant.parse("2000-02-29T22:30:00Z"), subject.getResolvedUtcInstant());
        assertEquals(OffsetDateTime.parse("2000-02-29T22:30:00Z"), subject.getUtcBirthDateTime());
    }

}
