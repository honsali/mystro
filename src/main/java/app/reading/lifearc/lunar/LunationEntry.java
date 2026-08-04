package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.SyzygyType;

public record LunationEntry(
        int sequenceIndex,
        SyzygyType type,
        boolean activeForInquiry,
        OffsetDateTime dateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        double julianDayUt,
        double ageYears,
        double syzygyLongitude,
        ZodiacSign syzygySign,
        double syzygyDegreeInSign,
        int natalHouseOverlay,
        double sunLongitude,
        ZodiacSign sunSign,
        double sunDegreeInSign,
        double moonLongitude,
        ZodiacSign moonSign,
        double moonDegreeInSign,
        double moonLatitude,
        double angularSeparation,
        Planet nearestNode,
        double nearestNodeLongitude,
        double nearestNodeOrbDegrees,
        EclipseCandidateType eclipseType
) {}
