package app.model.basic;

import app.model.data.ZodiacSign;

public final class RawSignDistanceMatrixEntry {
    private final String pointAName;
    private final ZodiacSign pointASign;
    private final String pointBName;
    private final ZodiacSign pointBSign;
    private final int signDistance;

    public RawSignDistanceMatrixEntry(String pointAName, ZodiacSign pointASign, String pointBName, ZodiacSign pointBSign, int signDistance) {
        this.pointAName = pointAName;
        this.pointASign = pointASign;
        this.pointBName = pointBName;
        this.pointBSign = pointBSign;
        this.signDistance = signDistance;
    }

    public String getPointAName() { return pointAName; }
    public ZodiacSign getPointASign() { return pointASign; }
    public String getPointBName() { return pointBName; }
    public ZodiacSign getPointBSign() { return pointBSign; }
    public int getSignDistance() { return signDistance; }
}
