package app.basic.model;

import app.basic.data.PointType;
import app.basic.data.ZodiacSign;

public final class ChartPoint {
    private final PointType type;
    private final String name;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;
    private final Integer house;

    public ChartPoint(PointType type, String name, double longitude, ZodiacSign sign, double degreeInSign, Integer house) {
        this.type = type;
        this.name = name;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.house = house;
    }

    public PointType getType() { return type; }
    public String getName() { return name; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
    public Integer getHouse() { return house; }
}
