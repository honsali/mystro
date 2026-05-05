package app.basic;

import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;

public final class AstroMath {
    public static double normalize(double degrees) {
        double value = degrees % 360.0;
        return value < 0 ? value + 360.0 : value;
    }

    public static ZodiacSign signOf(double longitude) {
        return ZodiacSign.values()[(int) Math.floor(normalize(longitude) / 30.0)];
    }

    public static double degreeInSign(double longitude) {
        return normalize(longitude) % 30.0;
    }

    public static double rawAngularSeparation(double longitudeA, double longitudeB) {
        double distance = Math.abs(normalize(longitudeA) - normalize(longitudeB));
        return distance > 180.0 ? 360.0 - distance : distance;
    }

    public static SolarOrientation orientationToSun(double planetLongitude, double sunLongitude) {
        double delta = normalize(planetLongitude - sunLongitude);
        return delta > 180.0 ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL;
    }

    private AstroMath() {}
}
