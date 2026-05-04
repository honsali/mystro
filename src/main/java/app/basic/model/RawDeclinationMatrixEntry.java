package app.basic.model;

public final class RawDeclinationMatrixEntry {
    private final String pointAName;
    private final double pointADeclination;
    private final String pointBName;
    private final double pointBDeclination;
    private final double declinationDifference;
    private final double contraParallelSeparation;
    private final boolean sameHemisphere;

    public RawDeclinationMatrixEntry(String pointAName, double pointADeclination, String pointBName, double pointBDeclination, double declinationDifference, double contraParallelSeparation, boolean sameHemisphere) {
        this.pointAName = pointAName;
        this.pointADeclination = pointADeclination;
        this.pointBName = pointBName;
        this.pointBDeclination = pointBDeclination;
        this.declinationDifference = declinationDifference;
        this.contraParallelSeparation = contraParallelSeparation;
        this.sameHemisphere = sameHemisphere;
    }

    public String getPointAName() { return pointAName; }
    public double getPointADeclination() { return pointADeclination; }
    public String getPointBName() { return pointBName; }
    public double getPointBDeclination() { return pointBDeclination; }
    public double getDeclinationDifference() { return declinationDifference; }
    public double getContraParallelSeparation() { return contraParallelSeparation; }
    public boolean isSameHemisphere() { return sameHemisphere; }
}
