package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.FixedStarCatalogue;
import app.reading.description.common.data.FixedStarCatalogue.FixedStarDefinition;
import app.reading.description.common.data.FixedStarTargetType;
import app.reading.description.common.model.FixedStarEntry;
import app.reading.description.common.model.LotEntry;
import app.ephemeris.SweConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValensFixedStarCalculator {
    static final class SwissEphemerisFixedStarPositionResolver implements FixedStarPositionResolver {
        @Override
        public double longitude(CalculationContext ctx, FixedStarDefinition star) {
            double[] values = new double[6];
            StringBuilder starId = new StringBuilder(star.swissEphemerisId());
            StringBuilder error = new StringBuilder();
            int result = ctx.getSwissEph().swe_fixstar_ut(starId, ctx.getFullJulianDay(), SweConst.SEFLG_SWIEPH, values, error);
            // Fixed-star data comes from ephe/sefstars.txt; unlike planets, Swiss Ephemeris has no
            // Moshier-style fixed-star fallback flag to reject here.
            if (result < 0 || !Double.isFinite(values[0])) {
                LOG.error("subject={} Swiss Ephemeris failed for fixed star {} ({}): {}", ctx.getSubject().getId(), star.name(), star.swissEphemerisId(), error);
                throw new IllegalArgumentException("Calculation failed. See application logs.");
            }
            return values[0];
        }
    }
    private record ConjunctionTarget(String name, FixedStarTargetType type, double longitude, int order) {
    }
    private record FixedStarHit(FixedStarEntry entry, int targetOrder, int starOrder) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(ValensFixedStarCalculator.class);
    private static final double BRIGHT_LUMINARY_ANGLE_ORB_DEG = 1.0;
    private static final double MEDIUM_STAR_ORB_DEG = 40.0 / 60.0;

    private static final double FAINT_STAR_ORB_DEG = 30.0 / 60.0;
    private static final double EPSILON = 1.0e-9;

    private static final String METHOD =
            "Fixed-star conjunction by tropical ecliptic longitude using Swiss Ephemeris swe_fixstar_ut; catalogue natures follow Robson's fixed-star planetary equivalents; orb policy: magnitude <= 1.5 to luminaries/angles = 1 degree, magnitude <= 2.5 = 40 arcminutes, otherwise = 30 arcminutes.";
    private static final List<AngleType> ANGLE_ORDER = List.of(AngleType.ASCENDANT, AngleType.MIDHEAVEN, AngleType.DESCENDANT, AngleType.IMUM_COELI);

    private static final List<Planet> PLANET_ORDER = List.of(Planet.SUN, Planet.MOON, Planet.MERCURY, Planet.VENUS, Planet.MARS, Planet.JUPITER, Planet.SATURN, Planet.NORTH_NODE, Planet.SOUTH_NODE);

    private final List<FixedStarDefinition> catalogue;

    private final FixedStarPositionResolver positionResolver;

    public ValensFixedStarCalculator() {
        this(FixedStarCatalogue.brightTraditionalStars(), new SwissEphemerisFixedStarPositionResolver());
    }

    ValensFixedStarCalculator(List<FixedStarDefinition> catalogue, FixedStarPositionResolver positionResolver) {
        this.catalogue = List.copyOf(catalogue);
        this.positionResolver = positionResolver;
    }

    public List<FixedStarEntry> calculate(CalculationContext ctx, Chart chart) {
        List<ConjunctionTarget> targets = conjunctionTargets(chart);
        if (targets.isEmpty()) {
            return List.of();
        }
        List<FixedStarHit> hits = new ArrayList<>();
        for (int starOrder = 0; starOrder < catalogue.size(); starOrder++) {
            FixedStarDefinition star = catalogue.get(starOrder);
            double longitude = AstroMath.normalize(positionResolver.longitude(ctx, star));
            for (ConjunctionTarget target : targets) {
                double maxOrb = maxOrb(star, target);
                double orb = AstroMath.rawAngularSeparation(longitude, target.longitude());
                if (orb <= maxOrb + EPSILON) {
                    hits.add(new FixedStarHit(new FixedStarEntry(star.name(), star.magnitude(), star.traditionalNature(), star.source(), longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), target.name(), target.type(), target.longitude(), orb, maxOrb, METHOD), target.order(),
                            starOrder));
                }
            }
        }
        hits.sort(Comparator.comparingInt(FixedStarHit::targetOrder).thenComparingDouble(hit -> hit.entry().orbDeg()).thenComparingInt(FixedStarHit::starOrder).thenComparing(hit -> hit.entry().star()));
        return hits.stream().map(FixedStarHit::entry).toList();
    }

    private List<ConjunctionTarget> conjunctionTargets(Chart chart) {
        List<ConjunctionTarget> targets = new ArrayList<>();
        addAngles(chart, targets);
        addPlanets(chart, targets);
        addLots(chart, targets);
        return targets;
    }

    private void addAngles(Chart chart, List<ConjunctionTarget> targets) {
        if (chart.getAngles() == null) {
            return;
        }
        int order = 0;
        for (AngleType angleType : ANGLE_ORDER) {
            ChartAngle angle = chart.getAngles().stream().filter(candidate -> candidate.getName() == angleType).findFirst().orElse(null);
            if (angle != null) {
                targets.add(new ConjunctionTarget(angleType.name(), FixedStarTargetType.ANGLE, AstroMath.normalize(angle.getLongitude()), order));
            }
            order++;
        }
    }

    private void addPlanets(Chart chart, List<ConjunctionTarget> targets) {
        if (chart.getPlanets() == null) {
            return;
        }
        int baseOrder = ANGLE_ORDER.size();
        for (int i = 0; i < PLANET_ORDER.size(); i++) {
            Planet planet = PLANET_ORDER.get(i);
            PlanetPosition position = chart.getPlanets().stream().filter(candidate -> candidate.getPlanet() == planet).findFirst().orElse(null);
            if (position != null) {
                FixedStarTargetType type = switch (planet) {
                    case NORTH_NODE, SOUTH_NODE -> FixedStarTargetType.NODE;
                    default -> FixedStarTargetType.PLANET;
                };
                targets.add(new ConjunctionTarget(planet.name(), type, AstroMath.normalize(position.getLongitude()), baseOrder + i));
            }
        }
    }

    private void addLots(Chart chart, List<ConjunctionTarget> targets) {
        if (chart.getLots() == null) {
            return;
        }
        int baseOrder = ANGLE_ORDER.size() + PLANET_ORDER.size();
        for (int i = 0; i < chart.getLots().size(); i++) {
            LotEntry lot = chart.getLots().get(i);
            targets.add(new ConjunctionTarget("LOT_" + lot.name(), FixedStarTargetType.LOT, AstroMath.normalize(lot.longitude()), baseOrder + i));
        }
    }

    private double maxOrb(FixedStarDefinition star, ConjunctionTarget target) {
        if (star.magnitude() <= 1.5 && (target.type() == FixedStarTargetType.ANGLE || isLuminary(target))) {
            return BRIGHT_LUMINARY_ANGLE_ORB_DEG;
        }
        if (star.magnitude() <= 2.5) {
            return MEDIUM_STAR_ORB_DEG;
        }
        return FAINT_STAR_ORB_DEG;
    }

    private boolean isLuminary(ConjunctionTarget target) {
        return target.type() == FixedStarTargetType.PLANET && (Planet.SUN.name().equals(target.name()) || Planet.MOON.name().equals(target.name()));
    }
}


@FunctionalInterface
interface FixedStarPositionResolver {
    double longitude(CalculationContext ctx, FixedStarDefinition star);
}
