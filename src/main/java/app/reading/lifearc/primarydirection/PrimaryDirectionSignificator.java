package app.reading.lifearc.primarydirection;

import app.chart.data.ZodiacSign;

public record PrimaryDirectionSignificator(
        String role,
        String point,
        boolean selectedHyleg,
        PrimaryDirectionCoordinate coordinate,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        double eclipticLatitude,
        double rightAscension,
        double declination,
        double directionCoordinateDegrees
) {}
