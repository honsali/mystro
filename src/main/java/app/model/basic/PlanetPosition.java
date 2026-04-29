package app.model.basic;

import app.model.data.Angularity;
import app.model.data.Planet;
import app.model.data.ZodiacSign;

public final class PlanetPosition {
    private final Planet planet;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;
    private final double latitude;
    private final double declination;
    private final double speed;
    private final double meanDailySpeed;
    private final double speedRatio;
    private final boolean retrograde;
    private final int house;
    private final Angularity angularity;
    private final Planet termRuler;
    private final double angularDistanceFromSun;
    private final double antisciaLongitude;
    private final double contraAntisciaLongitude;

    public PlanetPosition(
        Planet planet,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        double latitude,
        double declination,
        double speed,
        double meanDailySpeed,
        double speedRatio,
        boolean retrograde,
        int house,
        Angularity angularity,
        Planet termRuler,
        double angularDistanceFromSun,
        double antisciaLongitude,
        double contraAntisciaLongitude
    ) {
        this.planet = planet;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.latitude = latitude;
        this.declination = declination;
        this.speed = speed;
        this.meanDailySpeed = meanDailySpeed;
        this.speedRatio = speedRatio;
        this.retrograde = retrograde;
        this.house = house;
        this.angularity = angularity;
        this.termRuler = termRuler;
        this.angularDistanceFromSun = angularDistanceFromSun;
        this.antisciaLongitude = antisciaLongitude;
        this.contraAntisciaLongitude = contraAntisciaLongitude;
    }

    public Planet getPlanet() { return planet; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
    public double getLatitude() { return latitude; }
    public double getDeclination() { return declination; }
    public double getSpeed() { return speed; }
    public double getMeanDailySpeed() { return meanDailySpeed; }
    public double getSpeedRatio() { return speedRatio; }
    public boolean getRetrograde() { return retrograde; }
    public int getHouse() { return house; }
    public Angularity getAngularity() { return angularity; }
    public Planet getTermRuler() { return termRuler; }
    public double getAngularDistanceFromSun() { return angularDistanceFromSun; }
    public double getAntisciaLongitude() { return antisciaLongitude; }
    public double getContraAntisciaLongitude() { return contraAntisciaLongitude; }
}
