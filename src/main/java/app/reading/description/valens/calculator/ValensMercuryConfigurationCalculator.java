package app.reading.description.valens.calculator;

import java.util.Comparator;
import java.util.List;
import app.chart.AstroMath;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.SolarOrientation;
import app.chart.model.NatalChart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.reading.description.common.data.MorningEveningStar;
import app.reading.description.common.data.PlanetMotionState;
import app.reading.description.common.data.RelativeSpeed;
import app.reading.description.common.model.AspectRelationEntry;
import app.reading.description.common.model.ConfiguredPlanetEntry;
import app.reading.description.common.model.MercuryConfigurationEntry;
import app.reading.description.common.model.PointMotionEntry;

public final class ValensMercuryConfigurationCalculator {
    private static final double STATIONARY_THRESHOLD_DEG_PER_DAY = 0.05;
    private static final List<Planet> CONFIGURED_PLANET_ORDER = List.of(Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.VENUS, Planet.MOON, Planet.SUN);
    private static final String METHOD = "Ptolemaic quality-of-soul: Mercury phase, solar condition, motion, and configurations (Tetrabiblos III.13; Valens Anthology I).";

    public MercuryConfigurationEntry calculate(NatalChart chart) {
        PlanetPointEntry mercury = requirePlanetPoint(chart, Planet.MERCURY);
        PlanetPointEntry sun = requirePlanetPoint(chart, Planet.SUN);
        PlanetPointEntry moon = requirePlanetPoint(chart, Planet.MOON);
        PairwiseRelation moonRelation = relation(chart, Planet.MERCURY, Planet.MOON);
        boolean regardsMoon = moonRelation != null && moonRelation.getAspectBySign() != null;

        return new MercuryConfigurationEntry(mercury.sign(), mercury.house(), mercury.dignities(), mercury.debilities(), mercury.sectCondition(), mercury.solarPhase(), morningOrEveningStar(mercury.solarPhase()), mercuryArcFromSun(mercury, sun), mercury.solarCondition(), motion(mercury),
                mercury.sign() == sun.sign(), regardsMoon, regardsMoon ? null : aversionBySign(mercury, moon), aspectRelation(moonRelation), configuredPlanets(chart), METHOD);
    }

    private PointMotionEntry motion(PlanetPointEntry mercury) {
        PlanetMotionState state;
        if (Math.abs(mercury.speed()) <= STATIONARY_THRESHOLD_DEG_PER_DAY) {
            state = PlanetMotionState.STATIONARY;
        } else if (mercury.retrograde()) {
            state = PlanetMotionState.RETROGRADE;
        } else {
            state = PlanetMotionState.DIRECT;
        }
        return new PointMotionEntry(state, STATIONARY_THRESHOLD_DEG_PER_DAY, mercury.speedRatio(), relativeSpeed(mercury.speedRatio()));
    }

    private RelativeSpeed relativeSpeed(double speedRatio) {
        return speedRatio > 1.0 ? RelativeSpeed.SWIFT : RelativeSpeed.SLOW;
    }

    private MorningEveningStar morningOrEveningStar(SolarOrientation orientation) {
        if (orientation == null || orientation == SolarOrientation.EXACT) {
            return null;
        }
        return orientation == SolarOrientation.ORIENTAL ? MorningEveningStar.MORNING : MorningEveningStar.EVENING;
    }

    private double mercuryArcFromSun(PlanetPointEntry mercury, PlanetPointEntry sun) {
        return AstroMath.rawAngularSeparation(mercury.longitude(), sun.longitude());
    }

    private boolean aversionBySign(PlanetPointEntry mercury, PlanetPointEntry moon) {
        int orientedDistance = Math.floorMod(moon.sign().ordinal() - mercury.sign().ordinal(), 12);
        return orientedDistance == 1 || orientedDistance == 5 || orientedDistance == 7 || orientedDistance == 11;
    }

    private List<ConfiguredPlanetEntry> configuredPlanets(NatalChart chart) {
        return CONFIGURED_PLANET_ORDER.stream().map(planet -> configuredPlanet(chart, planet)).filter(entry -> entry != null)
                .sorted(Comparator.comparingInt((ConfiguredPlanetEntry entry) -> CONFIGURED_PLANET_ORDER.indexOf(entry.planet())).thenComparingDouble(entry -> entry.byDegreeOrb() == null ? Double.POSITIVE_INFINITY : entry.byDegreeOrb())).toList();
    }

    private ConfiguredPlanetEntry configuredPlanet(NatalChart chart, Planet planet) {
        PairwiseRelation relation = relation(chart, Planet.MERCURY, planet);
        if (relation == null || relation.getAspectBySign() == null) {
            return null;
        }
        PairwiseRelation.AspectBySign bySign = relation.getAspectBySign();
        PairwiseRelation.AspectByDegree byDegree = relation.getAspectByDegree();
        return new ConfiguredPlanetEntry(planet, bySign.getAspect(), bySign.getSignDistance(), byDegree == null ? null : byDegree.getOrbFromExact(), byDegree == null ? null : byDegree.getAspectMotion(), relation.getMutualReception());
    }

    private AspectRelationEntry aspectRelation(PairwiseRelation relation) {
        if (relation == null || relation.getAspectBySign() == null) {
            return null;
        }
        PairwiseRelation.AspectBySign bySign = relation.getAspectBySign();
        PairwiseRelation.AspectByDegree byDegree = relation.getAspectByDegree();
        AspectType aspect = bySign.getAspect();
        AspectMotion motion = byDegree == null ? null : byDegree.getAspectMotion();
        return new AspectRelationEntry(aspect, bySign.getSignDistance(), byDegree == null ? null : byDegree.getOrbFromExact(), motion);
    }

    private PairwiseRelation relation(NatalChart chart, Planet first, Planet second) {
        PointKey firstKey = PointKey.of(first);
        PointKey secondKey = PointKey.of(second);
        return chart.getPairwiseRelations().stream().filter(relation -> (relation.getPointAName() == firstKey && relation.getPointBName() == secondKey) || (relation.getPointAName() == secondKey && relation.getPointBName() == firstKey)).findFirst().orElse(null);
    }

    private PlanetPointEntry requirePlanetPoint(NatalChart chart, Planet planet) {
        PointKey pointKey = PointKey.of(planet);
        if (chart.getPoints().get(pointKey) instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        throw new IllegalArgumentException("Missing planet point " + planet);
    }
}
