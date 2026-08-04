package app.reading.lifearc.primarydirection;

import java.time.OffsetDateTime;

import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record PrimaryDirectionEvent(
        int sequenceIndex,
        boolean activeForInquiryYear,
        PrimaryDirectionPolarity direction,
        String significatorRole,
        String significatorPoint,
        PrimaryDirectionCoordinate coordinate,
        Planet promissorPlanet,
        PrimaryDirectionContactType contactType,
        AspectType aspect,
        String rayDirection,
        double targetLongitude,
        ZodiacSign targetSign,
        double targetDegreeInSign,
        double targetLatitude,
        double targetRightAscension,
        double targetDeclination,
        double targetDirectionCoordinateDegrees,
        double arcDegrees,
        double ageYears,
        OffsetDateTime dateTime,
        double promissorNatalLongitude,
        ZodiacSign promissorNatalSign,
        double promissorNatalDegreeInSign,
        int promissorNatalHouse
) {}
