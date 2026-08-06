package app.chart.calculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.ChartAngle;
import app.chart.model.ChartPoint;
import app.chart.model.Chart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.reading.description.common.data.DignityType;

public class ChartPointCalculator implements Calculator {

    public void calculate(Chart chart, CalculationContext ctx) {
        List<ChartPoint> chartPoints = new ArrayList<>();
        for (PlanetPosition planet : chart.getPlanets()) {
            chartPoints.add(new ChartPoint(PointKey.of(planet.getPlanet()), planet.getLongitude(), planet.getSign(), planet.getDegreeInSign(), planet.getHouse()));
        }
        for (ChartAngle angle : chart.getAngles()) {
            chartPoints.add(new ChartPoint(PointKey.of(angle.getName()), angle.getLongitude(), angle.getSign(), angle.getDegreeInSign(), null));
        }

        chart.setPairwiseRelations(calculatePairwiseRelations(chartPoints, chart.getPlanets(), chart.getPoints()));
    }

    boolean isInformativeForTest(PairwiseRelation relation) {
        return isInformative(relation);
    }

    PairwiseRelation.AspectByDegree aspectByDegreeForTest(PointKey pointA, PointKey pointB, PlanetPosition planetA, PlanetPosition planetB, double angularSeparation, double maxMoietyOrb) {
        return aspectByDegree(pointA, pointB, planetA, planetB, angularSeparation, maxMoietyOrb);
    }

    List<DignityType> mutualReceptionForTest(PointKey pointAKey, PointKey pointBKey, PlanetPointEntry pointA, PlanetPointEntry pointB) {
        return mutualReception(pointA, pointB, pointAKey, pointBKey);
    }

    private int signDistance(ZodiacSign signA, ZodiacSign signB) {
        int distance = Math.abs(signA.ordinal() - signB.ordinal());
        return Math.min(distance, 12 - distance);
    }

    private List<PairwiseRelation> calculatePairwiseRelations(List<ChartPoint> points, List<PlanetPosition> planets, Map<PointKey, PointEntry> natalPoints) {
        Map<PointKey, PlanetPosition> planetByKey = new LinkedHashMap<>();
        for (PlanetPosition planet : planets) {
            planetByKey.put(PointKey.of(planet.getPlanet()), planet);
        }
        List<PairwiseRelation> relations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                PairwiseRelation.EquatorialRelation equatorial = null;
                PlanetPosition planetA = planetByKey.get(pointA.getKey());
                PlanetPosition planetB = planetByKey.get(pointB.getKey());
                if (planetA != null && planetB != null) {
                    double declinationDifference = Math.abs(planetA.getDeclination() - planetB.getDeclination());
                    double contraParallelSeparation = Math.abs(planetA.getDeclination() + planetB.getDeclination());
                    boolean sameHemisphere = (planetA.getDeclination() >= 0.0 && planetB.getDeclination() >= 0.0) || (planetA.getDeclination() < 0.0 && planetB.getDeclination() < 0.0);
                    equatorial = new PairwiseRelation.EquatorialRelation(declinationDifference, contraParallelSeparation, sameHemisphere);
                }
                double angularSeparation = AstroMath.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude());
                int signDistance = signDistance(pointA.getSign(), pointB.getSign());
                double maxMoietyOrb = maxMoietyOrb(pointA.getKey(), pointB.getKey());
                PairwiseRelation relation = new PairwiseRelation(pointA.getKey(), pointB.getKey(), equatorial, aspectBySign(signDistance), aspectByDegree(pointA.getKey(), pointB.getKey(), planetA, planetB, angularSeparation, maxMoietyOrb),
                        mutualReception(planetPointEntry(natalPoints, pointA.getKey()), planetPointEntry(natalPoints, pointB.getKey()), pointA.getKey(), pointB.getKey()));
                if (isInformative(relation)) {
                    relations.add(relation);
                }
            }
        }
        return relations;
    }

    private PairwiseRelation.AspectBySign aspectBySign(int signDistance) {
        AspectType aspect = signAspect(signDistance);
        if (aspect == null) {
            return null;
        }
        return new PairwiseRelation.AspectBySign(aspect, signDistance);
    }

    private boolean isInformative(PairwiseRelation relation) {
        PointKey pointA = relation.getPointAName();
        PointKey pointB = relation.getPointBName();
        if (!isTraditionalPlanet(pointA) && !isTraditionalPlanet(pointB)) {
            return false;
        }

        boolean hasDegreeAspect = relation.getAspectByDegree() != null;
        boolean hasMutualReception = !relation.getMutualReception().isEmpty();
        if (isNode(pointA) || isNode(pointB)) {
            return hasDegreeAspect || hasMutualReception;
        }

        return relation.getAspectBySign() != null || hasDegreeAspect || hasMutualReception;
    }

    private boolean isTraditionalPlanet(PointKey key) {
        return switch (key) {
            case SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN -> true;
            default -> false;
        };
    }

    private boolean isNode(PointKey key) {
        return key == PointKey.NORTH_NODE || key == PointKey.SOUTH_NODE;
    }

    private PairwiseRelation.AspectByDegree aspectByDegree(PointKey pointA, PointKey pointB, PlanetPosition planetA, PlanetPosition planetB, double angularSeparation, double maxMoietyOrb) {
        AspectType nearestAspect = nearestDegreeAspect(angularSeparation);
        double orbFromExact = Math.abs(angularSeparation - nearestAspect.getExactAngle());
        if (orbFromExact >= maxMoietyOrb) {
            return null;
        }
        AspectMotion aspectMotion = aspectMotion(pointA, pointB, planetA, planetB, angularSeparation, nearestAspect);
        return new PairwiseRelation.AspectByDegree(nearestAspect, nearestAspect.getExactAngle(), angularSeparation, orbFromExact, maxMoietyOrb, aspectMotion);
    }

    private AspectType signAspect(int signDistance) {
        return switch (signDistance) {
            case 0 -> AspectType.CONJUNCTION;
            case 2 -> AspectType.SEXTILE;
            case 3 -> AspectType.SQUARE;
            case 4 -> AspectType.TRINE;
            case 6 -> AspectType.OPPOSITION;
            default -> null;
        };
    }

    private AspectType nearestDegreeAspect(double angularSeparation) {
        return List.of(AspectType.values()).stream().min(Comparator.comparingDouble(aspect -> Math.abs(angularSeparation - aspect.getExactAngle()))).orElseThrow();
    }

    private double maxMoietyOrb(PointKey pointA, PointKey pointB) {
        return (fullOrb(pointA) + fullOrb(pointB)) / 2.0;
    }

    private double fullOrb(PointKey point) {
        return switch (point) {
            case SUN -> 15.0;
            case MOON -> 12.0;
            case MERCURY, VENUS -> 7.0;
            case MARS -> 7.5;
            case JUPITER, SATURN -> 9.0;
            case NORTH_NODE, SOUTH_NODE, ASCENDANT, MIDHEAVEN, DESCENDANT, IMUM_COELI -> 0.0;
        };
    }

    private AspectMotion aspectMotion(PointKey pointA, PointKey pointB, PlanetPosition planetA, PlanetPosition planetB, double angularSeparation, AspectType nearestAspect) {
        if (planetA == null || planetB == null) {
            return null;
        }
        double speedA = planetA.getSpeed();
        double speedB = planetB.getSpeed();
        if (!Double.isFinite(speedA) || !Double.isFinite(speedB)) {
            return null;
        }
        double currentOrb = Math.abs(angularSeparation - nearestAspect.getExactAngle());
        double futureLongitudeA = AstroMath.normalize(planetA.getLongitude() + speedA / 86400.0);
        double futureLongitudeB = AstroMath.normalize(planetB.getLongitude() + speedB / 86400.0);
        double futureAngularSeparation = AstroMath.rawAngularSeparation(futureLongitudeA, futureLongitudeB);
        double futureOrb = Math.abs(futureAngularSeparation - nearestAspect.getExactAngle());

        double exactToleranceDegrees = 1.0 / 3600.0;
        if (currentOrb <= exactToleranceDegrees) {
            return AspectMotion.EXACT;
        }
        if (futureOrb < currentOrb) {
            return AspectMotion.APPLYING;
        }
        if (futureOrb > currentOrb) {
            return AspectMotion.SEPARATING;
        }
        return AspectMotion.EXACT;
    }

    private PlanetPointEntry planetPointEntry(Map<PointKey, PointEntry> natalPoints, PointKey key) {
        PointEntry point = natalPoints.get(key);
        if (point instanceof PlanetPointEntry planetPointEntry) {
            return planetPointEntry;
        }
        return null;
    }

    private List<DignityType> mutualReception(PlanetPointEntry pointA, PlanetPointEntry pointB, PointKey pointAKey, PointKey pointBKey) {
        Planet planetA = toTraditionalPlanet(pointAKey);
        Planet planetB = toTraditionalPlanet(pointBKey);
        if (pointA == null || pointB == null || planetA == null || planetB == null) {
            return List.of();
        }
        List<DignityType> receptions = new ArrayList<>();
        addMutualReception(receptions, DignityType.DOMICILE, pointA.domicileRuler(), pointB.domicileRuler(), planetA, planetB);
        addMutualReception(receptions, DignityType.EXALTATION, pointA.exaltationRuler(), pointB.exaltationRuler(), planetA, planetB);
        addMutualReception(receptions, DignityType.TRIPLICITY, pointA.activeMasterTriplicityRuler(), pointB.activeMasterTriplicityRuler(), planetA, planetB);
        addMutualReception(receptions, DignityType.TERM, pointA.termRuler(), pointB.termRuler(), planetA, planetB);
        addMutualReception(receptions, DignityType.FACE, pointA.faceRuler(), pointB.faceRuler(), planetA, planetB);
        return List.copyOf(receptions);
    }

    private void addMutualReception(List<DignityType> receptions, DignityType dignityType, Planet rulerA, Planet rulerB, Planet planetA, Planet planetB) {
        if (rulerA == planetB && rulerB == planetA) {
            receptions.add(dignityType);
        }
    }

    private Planet toTraditionalPlanet(PointKey key) {
        return switch (key) {
            case SUN -> Planet.SUN;
            case MOON -> Planet.MOON;
            case MERCURY -> Planet.MERCURY;
            case VENUS -> Planet.VENUS;
            case MARS -> Planet.MARS;
            case JUPITER -> Planet.JUPITER;
            case SATURN -> Planet.SATURN;
            default -> null;
        };
    }
}
