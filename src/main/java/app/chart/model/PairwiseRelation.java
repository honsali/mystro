package app.chart.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.PointKey;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PairwiseRelation {
    private final PointKey pointAName;
    private final PointKey pointBName;
    private final EclipticRelation ecliptic;
    private final EquatorialRelation equatorial;
    private final AspectRelation aspect;

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EclipticRelation ecliptic, EquatorialRelation equatorial) {
        this(pointAName, pointBName, ecliptic, equatorial, null);
    }

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EclipticRelation ecliptic, EquatorialRelation equatorial, AspectRelation aspect) {
        this.pointAName = pointAName;
        this.pointBName = pointBName;
        this.ecliptic = ecliptic;
        this.equatorial = equatorial;
        this.aspect = aspect;
    }

    public PointKey getPointAName() { return pointAName; }
    public PointKey getPointBName() { return pointBName; }
    public EclipticRelation getEcliptic() { return ecliptic; }
    public EquatorialRelation getEquatorial() { return equatorial; }
    public AspectRelation getAspect() { return aspect; }

    public PairwiseRelation withAspect(AspectRelation aspect) {
        return new PairwiseRelation(pointAName, pointBName, ecliptic, equatorial, aspect);
    }

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
        private final double contraParallelSeparation;
        private final boolean sameHemisphere;

        public EquatorialRelation(double declinationDifference, double contraParallelSeparation, boolean sameHemisphere) {
            this.declinationDifference = declinationDifference;
            this.contraParallelSeparation = contraParallelSeparation;
            this.sameHemisphere = sameHemisphere;
        }

        public double getDeclinationDifference() { return declinationDifference; }
        public double getContraParallelSeparation() { return contraParallelSeparation; }
        public boolean isSameHemisphere() { return sameHemisphere; }
    }

    public static final class AspectRelation {
        private final String type;
        private final double orbFromExact;

        public AspectRelation(String type, double orbFromExact) {
            this.type = type;
            this.orbFromExact = orbFromExact;
        }

        public String getType() { return type; }
        public double getOrbFromExact() { return orbFromExact; }
    }
}
