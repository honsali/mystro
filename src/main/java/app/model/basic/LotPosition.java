package app.model.basic;

import app.model.data.ZodiacSign;

public final class LotPosition {
    private final String name;
    private final double longitude;
    private final ZodiacSign sign;
    private final double degreeInSign;
    private final int house;
    private final double antisciaLongitude;
    private final double contraAntisciaLongitude;

    public LotPosition(String name, double longitude, ZodiacSign sign, double degreeInSign, int house, double antisciaLongitude, double contraAntisciaLongitude) {
        this.name = name;
        this.longitude = longitude;
        this.sign = sign;
        this.degreeInSign = degreeInSign;
        this.house = house;
        this.antisciaLongitude = antisciaLongitude;
        this.contraAntisciaLongitude = contraAntisciaLongitude;
    }

    public String getName() { return name; }
    public double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public double getDegreeInSign() { return degreeInSign; }
    public int getHouse() { return house; }
    public double getAntisciaLongitude() { return antisciaLongitude; }
    public double getContraAntisciaLongitude() { return contraAntisciaLongitude; }
}
