package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import app.chart.AstroMath;
import app.chart.TraditionalTables;
import app.chart.data.Angularity;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.SectCondition;
import app.chart.data.ZodiacSign;
import app.chart.model.Chart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;
import app.reading.description.common.data.BeneficMaleficCondition;
import app.reading.description.common.data.ConditionAssessment;
import app.reading.description.common.data.RulerAffliction;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.BeneficMaleficAssessmentEntry;

public final class ValensBeneficMaleficAssessmentCalculator {
    private record Ray(Planet planet, AspectType aspect, double longitude) {
    }
    private record RayPair(Ray behind, Ray ahead, double backwardArc, double forwardArc, double span) {
    }

    private static final List<Planet> TRADITIONAL_PLANETS = List.of(Planet.SUN, Planet.MOON, Planet.MERCURY, Planet.VENUS, Planet.MARS, Planet.JUPITER, Planet.SATURN);
    private static final List<Planet> BENEFICS = List.of(Planet.VENUS, Planet.JUPITER);
    private static final List<Planet> MALEFICS = List.of(Planet.MARS, Planet.SATURN);
    private static final double ADHERENCE_ORB_DEGREES = 3.0;

    private static final double BESIEGEMENT_ORB_DEGREES = 7.0;

    private static final double EXACT_TOLERANCE_DEGREES = 1.0e-7;

    public Map<Planet, List<BeneficMaleficAssessmentEntry>> calculate(Chart chart) {
        Map<Planet, List<BeneficMaleficAssessmentEntry>> result = new LinkedHashMap<>();
        for (Planet target : TRADITIONAL_PLANETS) {
            List<BeneficMaleficAssessmentEntry> entries = new ArrayList<>();
            for (Planet agent : BENEFICS) {
                if (agent != target) {
                    addBeneficAspect(entries, chart, target, agent);
                    addAdherence(entries, chart, target, agent, ConditionAssessment.BONIFICATION);
                }
            }
            for (Planet agent : MALEFICS) {
                if (agent != target) {
                    addMaleficAspect(entries, chart, target, agent);
                    addAdherence(entries, chart, target, agent, ConditionAssessment.MALTREATMENT);
                }
            }
            addEnclosureBySign(entries, chart, target, ConditionAssessment.BONIFICATION, BENEFICS, MALEFICS);
            addEnclosureBySign(entries, chart, target, ConditionAssessment.MALTREATMENT, MALEFICS, BENEFICS);
            addBesiegement(entries, chart, target, ConditionAssessment.BONIFICATION, BENEFICS, MALEFICS);
            addBesiegement(entries, chart, target, ConditionAssessment.MALTREATMENT, MALEFICS, BENEFICS);
            addMaltreatmentByRulership(entries, chart, target);
            result.put(target, entries.stream().sorted(entryComparator()).toList());
        }
        return result;
    }

    private void addBeneficAspect(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target, Planet agent) {
        PairwiseRelation relation = relation(chart, target, agent);
        if (relation == null || relation.getAspectBySign() == null) {
            return;
        }
        AspectType aspect = relation.getAspectBySign().getAspect();
        if (aspect == AspectType.CONJUNCTION || aspect == AspectType.SEXTILE || aspect == AspectType.TRINE) {
            entries.add(new BeneficMaleficAssessmentEntry(ConditionAssessment.BONIFICATION, BeneficMaleficCondition.BENEFIC_ASPECT, agent, aspect, orientedSignDistance(chart, target, agent), agentOfSect(chart, agent)));
        }
    }

    private void addMaleficAspect(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target, Planet agent) {
        PairwiseRelation relation = relation(chart, target, agent);
        if (relation == null || relation.getAspectBySign() == null) {
            return;
        }
        AspectType aspect = relation.getAspectBySign().getAspect();
        int signDistance = orientedSignDistance(chart, target, agent);
        if (aspect == AspectType.SQUARE || aspect == AspectType.OPPOSITION) {
            entries.add(new BeneficMaleficAssessmentEntry(ConditionAssessment.MALTREATMENT, BeneficMaleficCondition.MALEFIC_ASPECT, agent, aspect, signDistance, agentOfSect(chart, agent)));
        }
        if (signDistance == 9 || aspect == AspectType.OPPOSITION) {
            entries.add(new BeneficMaleficAssessmentEntry(ConditionAssessment.MALTREATMENT, BeneficMaleficCondition.MALEFIC_OVERCOMING, agent, aspect, signDistance, agentOfSect(chart, agent)));
        }
    }

    private void addAdherence(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target, Planet agent, ConditionAssessment assessment) {
        PlanetPosition targetPosition = chart.requirePlanet(target);
        PlanetPosition agentPosition = chart.requirePlanet(agent);
        if (targetPosition.getSign() != agentPosition.getSign()) {
            return;
        }
        double orb = AstroMath.rawAngularSeparation(targetPosition.getLongitude(), agentPosition.getLongitude());
        if (orb > ADHERENCE_ORB_DEGREES) {
            return;
        }
        entries.add(new BeneficMaleficAssessmentEntry(assessment, BeneficMaleficCondition.ADHERENCE, agent, null, AspectType.CONJUNCTION, null, 0, null, orb, null, agentOfSect(chart, agent), null, null));
    }

    private void addEnclosureBySign(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target, ConditionAssessment assessment, List<Planet> enclosingPlanets, List<Planet> blockerPlanets) {
        ZodiacSign targetSign = chart.requirePlanet(target).getSign();
        ZodiacSign previousSign = sign(targetSign.ordinal() - 1);
        ZodiacSign nextSign = sign(targetSign.ordinal() + 1);
        Planet previousAgent = firstPlanetInSign(chart, enclosingPlanets, previousSign);
        Planet nextAgent = firstPlanetInSign(chart, enclosingPlanets, nextSign);
        if (previousAgent == null || nextAgent == null) {
            return;
        }
        if (anyPlanetInSign(chart, blockerPlanets, previousSign) || anyPlanetInSign(chart, blockerPlanets, nextSign)) {
            return;
        }
        entries.add(new BeneficMaleficAssessmentEntry(assessment, BeneficMaleficCondition.ENCLOSURE, previousAgent, nextAgent, null, null, orientedSignDistance(chart, target, previousAgent), orientedSignDistance(chart, target, nextAgent), null, null, agentOfSect(chart, previousAgent),
                agentOfSect(chart, nextAgent), null));
    }

    private void addBesiegement(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target, ConditionAssessment assessment, List<Planet> enclosingPlanets, List<Planet> blockerPlanets) {
        List<Ray> enclosingRays = rays(chart, enclosingPlanets, target);
        List<Ray> blockerRays = rays(chart, blockerPlanets, target);
        double targetLongitude = chart.requirePlanet(target).getLongitude();
        RayPair best = null;
        for (Ray behind : enclosingRays) {
            for (Ray ahead : enclosingRays) {
                if (behind.planet() == ahead.planet()) {
                    continue;
                }
                double backwardArc = forwardArc(behind.longitude(), targetLongitude);
                double forwardArc = forwardArc(targetLongitude, ahead.longitude());
                if (backwardArc <= EXACT_TOLERANCE_DEGREES || forwardArc <= EXACT_TOLERANCE_DEGREES) {
                    continue;
                }
                if (backwardArc > BESIEGEMENT_ORB_DEGREES || forwardArc > BESIEGEMENT_ORB_DEGREES) {
                    continue;
                }
                double span = backwardArc + forwardArc;
                if (hasInterveningRay(blockerRays, behind.longitude(), span)) {
                    continue;
                }
                RayPair candidate = new RayPair(behind, ahead, backwardArc, forwardArc, span);
                if (best == null || rayPairComparator().compare(candidate, best) < 0) {
                    best = candidate;
                }
            }
        }
        if (best == null) {
            return;
        }
        entries.add(new BeneficMaleficAssessmentEntry(assessment, BeneficMaleficCondition.BESIEGEMENT, best.behind().planet(), best.ahead().planet(), best.behind().aspect(), best.ahead().aspect(), orientedSignDistance(chart, target, best.behind().planet()),
                orientedSignDistance(chart, target, best.ahead().planet()), best.backwardArc(), best.forwardArc(), agentOfSect(chart, best.behind().planet()), agentOfSect(chart, best.ahead().planet()), null));
    }

    private void addMaltreatmentByRulership(List<BeneficMaleficAssessmentEntry> entries, Chart chart, Planet target) {
        Planet ruler = TraditionalTables.domicileRuler(chart.requirePlanet(target).getSign());
        if (ruler == target || !MALEFICS.contains(ruler)) {
            return;
        }
        List<RulerAffliction> afflictions = rulerAfflictions(chart, ruler);
        if (afflictions.isEmpty()) {
            return;
        }
        entries.add(new BeneficMaleficAssessmentEntry(ConditionAssessment.MALTREATMENT, BeneficMaleficCondition.BY_RULERSHIP, ruler, null, null, null, orientedSignDistance(chart, target, ruler), null, null, null, agentOfSect(chart, ruler), null, afflictions));
    }

    private List<RulerAffliction> rulerAfflictions(Chart chart, Planet ruler) {
        List<RulerAffliction> afflictions = new ArrayList<>();
        PlanetPosition position = chart.requirePlanet(ruler);
        if (position.getRetrograde()) {
            afflictions.add(RulerAffliction.RETROGRADE);
        }
        if (position.getAngularity() == Angularity.CADENT) {
            afflictions.add(RulerAffliction.CADENT);
        }
        PlanetPointEntry point = planetPoint(chart, ruler);
        if (point != null && point.solarCondition() == SolarCondition.COMBUST) {
            afflictions.add(RulerAffliction.COMBUST);
        }
        return afflictions;
    }

    private List<Ray> rays(Chart chart, List<Planet> planets, Planet excludedTarget) {
        List<Ray> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Planet planet : planets) {
            if (planet == excludedTarget) {
                continue;
            }
            double longitude = chart.requirePlanet(planet).getLongitude();
            for (AspectType aspect : AspectType.values()) {
                addRay(result, seen, planet, aspect, longitude + aspect.getExactAngle());
                addRay(result, seen, planet, aspect, longitude - aspect.getExactAngle());
            }
        }
        return result;
    }

    private void addRay(List<Ray> result, Set<String> seen, Planet planet, AspectType aspect, double longitude) {
        double normalized = AstroMath.normalize(longitude);
        String key = planet + ":" + aspect + ":" + Math.round(normalized * 1_000_000_000.0);
        if (seen.add(key)) {
            result.add(new Ray(planet, aspect, normalized));
        }
    }

    private boolean hasInterveningRay(List<Ray> blockerRays, double fromLongitude, double span) {
        for (Ray blocker : blockerRays) {
            double arc = forwardArc(fromLongitude, blocker.longitude());
            if (arc > EXACT_TOLERANCE_DEGREES && arc < span - EXACT_TOLERANCE_DEGREES) {
                return true;
            }
        }
        return false;
    }

    private PairwiseRelation relation(Chart chart, Planet target, Planet agent) {
        if (chart.getPairwiseRelations() == null) {
            return null;
        }
        PointKey targetKey = PointKey.of(target);
        PointKey agentKey = PointKey.of(agent);
        return chart.getPairwiseRelations().stream().filter(relation -> (relation.getPointAName() == targetKey && relation.getPointBName() == agentKey) || (relation.getPointAName() == agentKey && relation.getPointBName() == targetKey)).findFirst().orElse(null);
    }

    private int orientedSignDistance(Chart chart, Planet target, Planet agent) {
        int targetSign = chart.requirePlanet(target).getSign().ordinal();
        int agentSign = chart.requirePlanet(agent).getSign().ordinal();
        return Math.floorMod(agentSign - targetSign, 12);
    }

    private boolean agentOfSect(Chart chart, Planet agent) {
        if (chart.getSect() == null || chart.getSect().getPlanetSects() == null || chart.getSect().getPlanetSects().get(agent) == null) {
            return false;
        }
        PlanetSectInfo sect = chart.getSect().getPlanetSects().get(agent);
        return sect.getCondition() == SectCondition.OF_SECT;
    }

    private PlanetPointEntry planetPoint(Chart chart, Planet planet) {
        if (chart.getPoints() == null) {
            return null;
        }
        if (chart.getPoints().get(PointKey.of(planet)) instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        return null;
    }

    private Planet firstPlanetInSign(Chart chart, List<Planet> planets, ZodiacSign sign) {
        for (Planet planet : planets) {
            if (chart.requirePlanet(planet).getSign() == sign) {
                return planet;
            }
        }
        return null;
    }

    private boolean anyPlanetInSign(Chart chart, List<Planet> planets, ZodiacSign sign) {
        return firstPlanetInSign(chart, planets, sign) != null;
    }

    private ZodiacSign sign(int index) {
        return ZodiacSign.values()[Math.floorMod(index, 12)];
    }

    private double forwardArc(double from, double to) {
        return AstroMath.normalize(to - from);
    }

    private Comparator<BeneficMaleficAssessmentEntry> entryComparator() {
        return Comparator.comparingInt((BeneficMaleficAssessmentEntry entry) -> entry.assessment() == ConditionAssessment.BONIFICATION ? 0 : 1).thenComparingInt(entry -> conditionPriority(entry.condition())).thenComparingInt(entry -> planetPriority(entry.agent()))
                .thenComparingInt(entry -> planetPriority(entry.coAgent())).thenComparingDouble(entry -> entry.orbFromExact() == null ? Double.POSITIVE_INFINITY : entry.orbFromExact()).thenComparingDouble(entry -> entry.coOrbFromExact() == null ? Double.POSITIVE_INFINITY : entry.coOrbFromExact());
    }

    private int conditionPriority(BeneficMaleficCondition condition) {
        return switch (condition) {
            case BENEFIC_ASPECT, MALEFIC_ASPECT -> 0;
            case ADHERENCE -> 1;
            case ENCLOSURE -> 2;
            case BESIEGEMENT -> 3;
            case MALEFIC_OVERCOMING -> 4;
            case BY_RULERSHIP -> 5;
        };
    }

    private int planetPriority(Planet planet) {
        return planet == null ? Integer.MAX_VALUE : TRADITIONAL_PLANETS.indexOf(planet);
    }

    private Comparator<RayPair> rayPairComparator() {
        return Comparator.comparingDouble(RayPair::span).thenComparingInt(pair -> planetPriority(pair.behind().planet())).thenComparingInt(pair -> planetPriority(pair.ahead().planet())).thenComparingInt(pair -> pair.behind().aspect().ordinal())
                .thenComparingInt(pair -> pair.ahead().aspect().ordinal());
    }
}
