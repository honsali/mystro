package app.reading.lifearc.primarydirection;

import app.chart.data.ZodiacSign;

public record MundanePrimaryDirectionSignificator(
        String role,
        String point,
        boolean selectedHyleg,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        double eclipticLatitude,
        double rightAscension,
        double declination,
        double diurnalSemiArcDegrees,
        double nocturnalSemiArcDegrees,
        double natalHourAngleDegrees,
        double mundanePositionDegrees,
        String mundanePositionSegment
) {}
