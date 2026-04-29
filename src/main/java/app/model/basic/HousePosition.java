package app.model.basic;

import app.model.data.ZodiacSign;

public final class HousePosition {
    private final int house;
    private final double cuspLongitude;
    private final ZodiacSign sign;
    private final double degreeInSign;

    public HousePosition(int house, double cuspLongitude, ZodiacSign sign, double degreeInSign) {
        this.house = house;
        this.cuspLongitude = cuspLongitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
    }

    public int getHouse() { return house; }
    public double getCuspLongitude() { return cuspLongitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
}
