package app.planetaryhours;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public final class PlanetaryHourPlanetPlacement {

    private final Planet planet;
    private final String glyph;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;

    public PlanetaryHourPlanetPlacement(Planet planet,
                                        String glyph,
                                        double longitude,
                                        ZodiacSign sign,
                                        double degreeInSign) {
        this.planet = planet;
        this.glyph = glyph;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
    }

    public Planet getPlanet() {
        return planet;
    }

    public String getGlyph() {
        return glyph;
    }

    public double getLongitude() {
        return longitude;
    }

    public ZodiacSign getSign() {
        return sign;
    }

    public double getDegreeInSign() {
        return degreeInSign;
    }
}
