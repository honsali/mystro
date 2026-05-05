package app.chart.model;

import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

public final class ChartPoint {
    private final PointKey key;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;
    private final Integer house;

    public ChartPoint(PointKey key, double longitude, ZodiacSign sign, double degreeInSign, Integer house) {
        this.key = key;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.house = house;
    }

    public PointType getType() {
        return switch (key) {
            case SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN, NORTH_NODE, SOUTH_NODE -> PointType.PLANET;
            case ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI -> PointType.ANGLE;
        };
    }
    public PointKey getKey() { return key; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
    public Integer getHouse() { return house; }
}
