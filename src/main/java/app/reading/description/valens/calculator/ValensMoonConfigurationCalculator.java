package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import app.chart.AstroMath;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.model.MoonPhase;
import app.chart.model.Chart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.LightDisposition;
import app.reading.description.common.data.RelativeSpeed;
import app.reading.description.common.model.ConfiguredPlanetEntry;
import app.reading.description.common.model.LunarAspectEventEntry;
import app.reading.description.common.model.MoonConfigurationEntry;
import app.reading.description.common.model.PointMotionEntry;

public final class ValensMoonConfigurationCalculator {
    private enum EventDirection {
        LAST_SEPARATION, NEXT_APPLICATION
    }
    private record LunarAspectCandidate(Planet planet, AspectType aspect, double forwardArc, double backwardArc) {
    }

    private static final double FULL_ZODIAC_SCAN_DEGREES = 360.0;
    private static final double EXACT_TOLERANCE_DEGREES = 1.0e-7;
    private static final List<Planet> APPLICATION_PLANETS = List.of(Planet.SUN, Planet.MERCURY, Planet.VENUS, Planet.MARS, Planet.JUPITER, Planet.SATURN);

    private static final List<Planet> CONFIGURED_PLANET_ORDER = List.of(Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.VENUS, Planet.MERCURY, Planet.SUN);

    private static final String METHOD = "Hellenistic lunar configuration: last separation, next application, and void-of-course using classical planets held at natal longitudes (Ptolemy III.13; Lilly CA I).";

    public MoonConfigurationEntry calculate(Chart chart) {
        PlanetPointEntry moon = requirePlanetPoint(chart, Planet.MOON);
        MoonPhase moonPhase = chart.getMoonPhase();

        return new MoonConfigurationEntry(moon.sign(), moon.house(), moon.dignities(), moon.debilities(), moon.sectCondition(), moonPhase == null ? null : moonPhase.getPhase(), moonPhase != null && moonPhase.isWaxing(), moonPhase == null ? 0.0 : moonPhase.getIlluminationFraction(),
                moonPhase != null && moonPhase.isWaxing() ? LightDisposition.INCREASING : LightDisposition.DECREASING, motion(moon), lunarEvent(chart, moon, EventDirection.LAST_SEPARATION), lunarEvent(chart, moon, EventDirection.NEXT_APPLICATION), voidOfCourse(chart, moon), configuredPlanets(chart),
                METHOD);
    }

    private PointMotionEntry motion(PlanetPointEntry moon) {
        return new PointMotionEntry(null, null, moon.speedRatio(), relativeSpeed(moon.speedRatio()));
    }

    private RelativeSpeed relativeSpeed(double speedRatio) {
        return speedRatio > 1.0 ? RelativeSpeed.SWIFT : RelativeSpeed.SLOW;
    }

    private LunarAspectEventEntry lunarEvent(Chart chart, PlanetPointEntry moon, EventDirection direction) {
        List<LunarAspectCandidate> candidates =
                lunarAspectCandidates(chart, moon.longitude(), FULL_ZODIAC_SCAN_DEGREES, true).stream().filter(candidate -> direction == EventDirection.NEXT_APPLICATION ? candidate.forwardArc() <= FULL_ZODIAC_SCAN_DEGREES : candidate.backwardArc() <= FULL_ZODIAC_SCAN_DEGREES)
                        .filter(candidate -> direction == EventDirection.NEXT_APPLICATION ? candidate.forwardArc() >= 0.0 : candidate.backwardArc() >= 0.0).toList();
        Comparator<LunarAspectCandidate> comparator = Comparator.comparingDouble((LunarAspectCandidate candidate) -> direction == EventDirection.NEXT_APPLICATION ? candidate.forwardArc() : candidate.backwardArc()).thenComparingInt(candidate -> APPLICATION_PLANETS.indexOf(candidate.planet()))
                .thenComparingInt(candidate -> candidate.aspect().ordinal());
        return candidates.stream().min(comparator).map(candidate -> new LunarAspectEventEntry(candidate.planet(), candidate.aspect(), direction == EventDirection.NEXT_APPLICATION ? candidate.forwardArc() : candidate.backwardArc())).orElse(null);
    }

    private boolean voidOfCourse(Chart chart, PlanetPointEntry moon) {
        double arcToSignEnd = 30.0 - moon.degreeInSign();
        return lunarAspectCandidates(chart, moon.longitude(), arcToSignEnd, false).stream().noneMatch(candidate -> candidate.forwardArc() > EXACT_TOLERANCE_DEGREES && candidate.forwardArc() < arcToSignEnd);
    }

    private List<LunarAspectCandidate> lunarAspectCandidates(Chart chart, double moonLongitude, double maximumArc, boolean includeExactCurrent) {
        List<LunarAspectCandidate> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Planet planet : APPLICATION_PLANETS) {
            PlanetPosition position = chart.requirePlanet(planet);
            for (AspectType aspect : AspectType.values()) {
                addCandidate(result, seen, moonLongitude, planet, aspect, position.getLongitude() + aspect.getExactAngle(), maximumArc, includeExactCurrent);
                addCandidate(result, seen, moonLongitude, planet, aspect, position.getLongitude() - aspect.getExactAngle(), maximumArc, includeExactCurrent);
            }
        }
        return result;
    }

    private void addCandidate(List<LunarAspectCandidate> result, Set<String> seen, double moonLongitude, Planet planet, AspectType aspect, double exactMoonLongitude, double maximumArc, boolean includeExactCurrent) {
        double exactLongitude = AstroMath.normalize(exactMoonLongitude);
        String key = planet + ":" + aspect + ":" + Math.round(exactLongitude * 1_000_000_000.0);
        if (!seen.add(key)) {
            return;
        }
        double forwardArc = normalizedArc(exactLongitude - moonLongitude);
        double backwardArc = normalizedArc(moonLongitude - exactLongitude);
        boolean exactCurrent = forwardArc <= EXACT_TOLERANCE_DEGREES || backwardArc <= EXACT_TOLERANCE_DEGREES;
        if (exactCurrent) {
            if (!includeExactCurrent) {
                return;
            }
            forwardArc = 0.0;
            backwardArc = 0.0;
        }
        if (forwardArc <= maximumArc || backwardArc <= maximumArc) {
            result.add(new LunarAspectCandidate(planet, aspect, forwardArc, backwardArc));
        }
    }

    private double normalizedArc(double degrees) {
        double arc = AstroMath.normalize(degrees);
        return arc <= EXACT_TOLERANCE_DEGREES ? 0.0 : arc;
    }

    private List<ConfiguredPlanetEntry> configuredPlanets(Chart chart) {
        return CONFIGURED_PLANET_ORDER.stream().map(planet -> configuredPlanet(chart, planet)).filter(entry -> entry != null)
                .sorted(Comparator.comparingInt((ConfiguredPlanetEntry entry) -> CONFIGURED_PLANET_ORDER.indexOf(entry.planet())).thenComparingDouble(entry -> entry.byDegreeOrb() == null ? Double.POSITIVE_INFINITY : entry.byDegreeOrb())).toList();
    }

    private ConfiguredPlanetEntry configuredPlanet(Chart chart, Planet planet) {
        PairwiseRelation relation = relation(chart, Planet.MOON, planet);
        if (relation == null || relation.getAspectBySign() == null) {
            return null;
        }
        PairwiseRelation.AspectBySign bySign = relation.getAspectBySign();
        PairwiseRelation.AspectByDegree byDegree = relation.getAspectByDegree();
        return new ConfiguredPlanetEntry(planet, bySign.getAspect(), bySign.getSignDistance(), byDegree == null ? null : byDegree.getOrbFromExact(), byDegree == null ? null : byDegree.getAspectMotion(), relation.getMutualReception());
    }

    private PairwiseRelation relation(Chart chart, Planet first, Planet second) {
        PointKey firstKey = PointKey.of(first);
        PointKey secondKey = PointKey.of(second);
        return chart.getPairwiseRelations().stream().filter(relation -> (relation.getPointAName() == firstKey && relation.getPointBName() == secondKey) || (relation.getPointAName() == secondKey && relation.getPointBName() == firstKey)).findFirst().orElse(null);
    }

    private PlanetPointEntry requirePlanetPoint(Chart chart, Planet planet) {
        PointKey pointKey = PointKey.of(planet);
        if (chart.getPoints().get(pointKey) instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        throw new IllegalArgumentException("Missing planet point " + planet);
    }
}
