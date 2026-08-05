package app.chart.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class SubjectTest {

    private static final OffsetDateTime DATE_TIME = OffsetDateTime.parse("2000-01-01T12:00:00Z");

    @Test
    void rejectsExactGeographicPoles() {
        for (double latitude : new double[] {-90.0, 90.0}) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Subject("pole", DATE_TIME, latitude, 0.0));

            assertEquals(
                    "Latitude must be strictly between -90 and 90 degrees: " + latitude,
                    ex.getMessage());
        }
    }

    @Test
    void acceptsLatitudesImmediatelyInsideThePoles() {
        Subject north = new Subject("near-north-pole", DATE_TIME, 89.999, 0.0);
        Subject south = new Subject("near-south-pole", DATE_TIME, -89.999, 0.0);

        assertEquals(89.999, north.getLatitude());
        assertEquals(-89.999, south.getLatitude());
    }
}
