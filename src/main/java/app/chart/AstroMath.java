package app.chart;

import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;

public final class AstroMath {
    private static final double ANGLE_EPSILON = 1e-9;

    public static double normalize(double degrees) {
        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Angle must be finite: " + degrees);
        }
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
        if (isAngleBoundary(delta, 0.0) || isAngleBoundary(delta, 180.0) || isAngleBoundary(delta, 360.0)) {
            return SolarOrientation.EXACT;
        }
        return delta > 180.0 ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL;
    }

    private static boolean isAngleBoundary(double value, double boundary) {
        return Math.abs(value - boundary) <= ANGLE_EPSILON;
    }

    private AstroMath() {}
}
