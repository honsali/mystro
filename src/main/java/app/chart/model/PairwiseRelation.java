package app.chart.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.PointKey;
import app.reading.description.common.data.DignityType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PairwiseRelation {
    private final PointKey pointAName;
    private final PointKey pointBName;
    private final EquatorialRelation equatorial;
    private final AspectBySign aspectBySign;
    private final AspectByDegree aspectByDegree;
    private final List<DignityType> mutualReception;
    private final AspectRelation aspect;

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EquatorialRelation equatorial) {
        this(pointAName, pointBName, equatorial, null, null, List.of(), null);
    }

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EquatorialRelation equatorial, AspectBySign aspectBySign, AspectByDegree aspectByDegree, List<DignityType> mutualReception) {
        this(pointAName, pointBName, equatorial, aspectBySign, aspectByDegree, mutualReception, null);
    }

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EquatorialRelation equatorial, AspectRelation aspect) {
        this(pointAName, pointBName, equatorial, null, null, List.of(), aspect);
    }

    public PairwiseRelation(PointKey pointAName, PointKey pointBName, EquatorialRelation equatorial, AspectBySign aspectBySign, AspectByDegree aspectByDegree, List<DignityType> mutualReception, AspectRelation aspect) {
        this.pointAName = pointAName;
        this.pointBName = pointBName;
        this.equatorial = equatorial;
        this.aspectBySign = aspectBySign;
        this.aspectByDegree = aspectByDegree;
        this.mutualReception = mutualReception == null ? List.of() : List.copyOf(mutualReception);
        this.aspect = aspect;
    }

    public PointKey getPointAName() { return pointAName; }
    public PointKey getPointBName() { return pointBName; }
    public EquatorialRelation getEquatorial() { return equatorial; }
    public AspectBySign getAspectBySign() { return aspectBySign; }
    public AspectByDegree getAspectByDegree() { return aspectByDegree; }
    public List<DignityType> getMutualReception() { return mutualReception; }
    public AspectRelation getAspect() { return aspect; }

    public PairwiseRelation withAspect(AspectRelation aspect) {
        return new PairwiseRelation(pointAName, pointBName, equatorial, aspectBySign, aspectByDegree, mutualReception, aspect);
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

    public static final class AspectBySign {
        private final AspectType aspect;
        private final int signDistance;

        public AspectBySign(AspectType aspect, int signDistance) {
            this.aspect = aspect;
            this.signDistance = signDistance;
        }

        public AspectType getAspect() { return aspect; }
        public int getSignDistance() { return signDistance; }
    }

    public static final class AspectByDegree {
        private final AspectType nearestAspect;
        private final double exactAngle;
        private final double angularSeparation;
        private final double orbFromExact;
        private final double maxMoietyOrb;
        private final AspectMotion aspectMotion;

        public AspectByDegree(AspectType nearestAspect, double exactAngle, double angularSeparation, double orbFromExact, double maxMoietyOrb) {
            this(nearestAspect, exactAngle, angularSeparation, orbFromExact, maxMoietyOrb, null);
        }

        public AspectByDegree(AspectType nearestAspect, double exactAngle, double angularSeparation, double orbFromExact, double maxMoietyOrb, AspectMotion aspectMotion) {
            this.nearestAspect = nearestAspect;
            this.exactAngle = exactAngle;
            this.angularSeparation = angularSeparation;
            this.orbFromExact = orbFromExact;
            this.maxMoietyOrb = maxMoietyOrb;
            this.aspectMotion = aspectMotion;
        }

        public AspectType getNearestAspect() { return nearestAspect; }
        public double getExactAngle() { return exactAngle; }
        public double getAngularSeparation() { return angularSeparation; }
        public double getOrbFromExact() { return orbFromExact; }
        public double getMaxMoietyOrb() { return maxMoietyOrb; }
        public AspectMotion getAspectMotion() { return aspectMotion; }
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
