package app.basic.model;

import app.basic.data.PointKey;
import app.basic.data.PointType;

public final class RawAspectMatrixEntry {
    private final PointType pointAType;
    private final PointKey pointAName;
    private final double pointALongitude;
    private final PointType pointBType;
    private final PointKey pointBName;
    private final double pointBLongitude;
    private final double angularSeparation;

    public RawAspectMatrixEntry(
        PointType pointAType,
        PointKey pointAName,
        double pointALongitude,
        PointType pointBType,
        PointKey pointBName,
        double pointBLongitude,
        double angularSeparation
    ) {
        this.pointAType = pointAType;
        this.pointAName = pointAName;
        this.pointALongitude = pointALongitude;
        this.pointBType = pointBType;
        this.pointBName = pointBName;
        this.pointBLongitude = pointBLongitude;
        this.angularSeparation = angularSeparation;
    }

    public PointType getPointAType() { return pointAType; }
    public PointKey getPointAName() { return pointAName; }
    public double getPointALongitude() { return pointALongitude; }
    public PointType getPointBType() { return pointBType; }
    public PointKey getPointBName() { return pointBName; }
    public double getPointBLongitude() { return pointBLongitude; }
    public double getAngularSeparation() { return angularSeparation; }
}
