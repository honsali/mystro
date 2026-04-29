package app.model.basic;

public final class PairwiseRelation {
    private final String pointAName;
    private final String pointBName;
    private final EclipticRelation ecliptic;
    private final EquatorialRelation equatorial;

    public PairwiseRelation(String pointAName, String pointBName, EclipticRelation ecliptic, EquatorialRelation equatorial) {
        this.pointAName = pointAName;
        this.pointBName = pointBName;
        this.ecliptic = ecliptic;
        this.equatorial = equatorial;
    }

    public String getPointAName() { return pointAName; }
    public String getPointBName() { return pointBName; }
    public EclipticRelation getEcliptic() { return ecliptic; }
    public EquatorialRelation getEquatorial() { return equatorial; }

    public static final class EclipticRelation {
        private final double angularSeparation;
        private final int signDistance;

        public EclipticRelation(double angularSeparation, int signDistance) {
            this.angularSeparation = angularSeparation;
            this.signDistance = signDistance;
        }

        public double getAngularSeparation() { return angularSeparation; }
        public int getSignDistance() { return signDistance; }
    }

    public static final class EquatorialRelation {
        private final double declinationDifference;
        private final boolean sameHemisphere;

        public EquatorialRelation(double declinationDifference, boolean sameHemisphere) {
            this.declinationDifference = declinationDifference;
            this.sameHemisphere = sameHemisphere;
        }

        public double getDeclinationDifference() { return declinationDifference; }
        public boolean isSameHemisphere() { return sameHemisphere; }
    }
}
