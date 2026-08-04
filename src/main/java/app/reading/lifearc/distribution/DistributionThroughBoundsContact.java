package app.reading.lifearc.distribution;

import java.time.OffsetDateTime;

import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record DistributionThroughBoundsContact(
        Planet sourcePlanet,
        DistributionContactType contactType,
        AspectType aspect,
        String rayDirection,
        double directedLongitude,
        ZodiacSign directedSign,
        double directedDegreeInSign,
        Planet boundRulerAtContact,
        double arcDegrees,
        double ageYears,
        OffsetDateTime dateTime,
        double sourceNatalLongitude,
        ZodiacSign sourceNatalSign,
        double sourceNatalDegreeInSign,
        int sourceNatalHouse
) {}
