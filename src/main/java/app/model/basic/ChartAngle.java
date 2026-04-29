package app.model.basic;

import app.model.data.ZodiacSign;

public final class ChartAngle {
    private final String name;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;

    public ChartAngle(String name, double longitude, ZodiacSign sign, double degreeInSign) {
        this.name = name;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
    }

    public String getName() { return name; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
}
