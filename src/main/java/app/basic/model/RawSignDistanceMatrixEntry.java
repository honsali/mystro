package app.basic.model;

import app.basic.data.PointKey;
import app.basic.data.ZodiacSign;

public final class RawSignDistanceMatrixEntry {
    private final PointKey pointAName;
    private final ZodiacSign pointASign;
    private final PointKey pointBName;
    private final ZodiacSign pointBSign;
    private final int signDistance;

    public RawSignDistanceMatrixEntry(PointKey pointAName, ZodiacSign pointASign, PointKey pointBName, ZodiacSign pointBSign, int signDistance) {
        this.pointAName = pointAName;
        this.pointASign = pointASign;
        this.pointBName = pointBName;
        this.pointBSign = pointBSign;
        this.signDistance = signDistance;
    }

    public PointKey getPointAName() { return pointAName; }
    public ZodiacSign getPointASign() { return pointASign; }
    public PointKey getPointBName() { return pointBName; }
    public ZodiacSign getPointBSign() { return pointBSign; }
    public int getSignDistance() { return signDistance; }
}
