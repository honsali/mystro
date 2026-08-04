package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;

import app.chart.data.ZodiacSign;

public record LunarSignIngressEntry(
        int sequenceIndex,
        OffsetDateTime dateTime,
        ZodiacSign fromSign,
        ZodiacSign toSign,
        double moonLongitude,
        double moonDegreeInSign,
        int natalHouseOverlay
) {}
