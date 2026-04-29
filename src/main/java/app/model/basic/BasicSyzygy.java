package app.model.basic;

import java.time.Instant;
import app.model.data.SyzygyType;
import app.model.data.ZodiacSign;

public final class BasicSyzygy {
    private final SyzygyType type;
    private final double julianDay;
    private final Instant approximateUtcInstant;
    private final double sunLongitude;
    private final double moonLongitude;
    private final double angularSeparation;
    private final ZodiacSign sign;
    private final double degreeInSign;
    private final int house;
    private final ZodiacSign sunSign;
    private final ZodiacSign moonSign;

    public BasicSyzygy(SyzygyType type, double julianDay, Instant approximateUtcInstant, double sunLongitude, double moonLongitude, double angularSeparation, ZodiacSign sign, double degreeInSign, int house, ZodiacSign sunSign, ZodiacSign moonSign) {
        this.type = type;
        this.julianDay = julianDay;
        this.approximateUtcInstant = approximateUtcInstant;
        this.sunLongitude = sunLongitude;
        this.moonLongitude = moonLongitude;
        this.angularSeparation = angularSeparation;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.house = house;
        this.sunSign = sunSign;
        this.moonSign = moonSign;
    }

    public SyzygyType getType() { return type; }
    public double getLongitude() { return type == SyzygyType.FULL_MOON ? moonLongitude : sunLongitude; }
    public double getJulianDay() { return julianDay; }
    public Instant getApproximateUtcInstant() { return approximateUtcInstant; }
    public double getSunLongitude() { return sunLongitude; }
    public double getMoonLongitude() { return moonLongitude; }
    public double getAngularSeparation() { return angularSeparation; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
    public int getHouse() { return house; }
    public ZodiacSign getSunSign() { return sunSign; }
    public ZodiacSign getMoonSign() { return moonSign; }
}
