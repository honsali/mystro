package app.reading.lifearc.primarydirection;

import java.time.OffsetDateTime;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record MundanePrimaryDirectionEvent(
        int sequenceIndex,
        boolean activeForInquiryYear,
        String significatorRole,
        String significatorPoint,
        Planet promissorPlanet,
        PrimaryDirectionContactType contactType,
        double targetMundanePositionDegrees,
        String targetMundanePositionSegment,
        double promissorRightAscension,
        double promissorDeclination,
        double promissorDiurnalSemiArcDegrees,
        double promissorNocturnalSemiArcDegrees,
        double directedHourAngleDegrees,
        double directedArmcDegrees,
        double arcDegrees,
        double ageYears,
        OffsetDateTime dateTime,
        double promissorNatalLongitude,
        ZodiacSign promissorNatalSign,
        double promissorNatalDegreeInSign,
        int promissorNatalHouse
) {}
