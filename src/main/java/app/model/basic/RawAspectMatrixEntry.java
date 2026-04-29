package app.model.basic;

import app.model.data.PointType;

public final class RawAspectMatrixEntry {
    private final PointType pointAType;
    private final String pointAName;
    private final double pointALongitude;
    private final PointType pointBType;
    private final String pointBName;
    private final double pointBLongitude;
    private final double angularSeparation;

    public RawAspectMatrixEntry(
        PointType pointAType,
        String pointAName,
        double pointALongitude,
        PointType pointBType,
        String pointBName,
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
    public String getPointAName() { return pointAName; }
    public double getPointALongitude() { return pointALongitude; }
    public PointType getPointBType() { return pointBType; }
    public String getPointBName() { return pointBName; }
    public double getPointBLongitude() { return pointBLongitude; }
    public double getAngularSeparation() { return angularSeparation; }
}
