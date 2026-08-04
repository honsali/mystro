package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record LunarReturnEntry(
        int sequenceIndex,
        int returnNumberFromBirth,
        boolean activeForInquiry,
        OffsetDateTime returnDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        double julianDayUt,
        double ageYears,
        double moonLongitude,
        ZodiacSign moonSign,
        double moonDegreeInSign,
        double moonLatitude,
        int natalHouseOverlay,
        double sunLongitude,
        ZodiacSign sunSign,
        double sunDegreeInSign,
        double lunarElongationFromSun,
        Planet nearestNode,
        double nearestNodeLongitude,
        double nearestNodeOrbDegrees
) {}
