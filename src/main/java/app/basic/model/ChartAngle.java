package app.basic.model;

import app.basic.data.AngleType;
import app.basic.data.ZodiacSign;

public final class ChartAngle {
    private final AngleType name;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;

    public ChartAngle(AngleType name, double longitude, ZodiacSign sign, double degreeInSign) {
        this.name = name;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
    }

    public AngleType getName() { return name; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
}
