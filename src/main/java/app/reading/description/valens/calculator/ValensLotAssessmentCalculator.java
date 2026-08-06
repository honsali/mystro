package app.reading.description.valens.calculator;

import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.reading.description.common.model.LotConfiguredPlanetEntry;
import app.reading.description.common.model.LotEntry;

public final class ValensLotAssessmentCalculator {
    private static final List<Planet> CONFIGURED_PLANET_ORDER = List.of(
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.MOON,
            Planet.SUN
    );

    private final ValensPlanetConditionEntries conditionEntries = new ValensPlanetConditionEntries();

    public List<LotEntry> calculate(Chart chart) {
        if (chart.getLots() == null) {
            return List.of();
        }
        return chart.getLots().stream()
                .map(lot -> new LotEntry(
                        lot.name(),
                        lot.displayName(),
                        lot.doctrine(),
                        lot.longitude(),
                        lot.sign(),
                        lot.degreeInSign(),
                        lot.house(),
                        lot.ruler(),
                        lot.formula(),
                        configuredPlanets(chart, lot)
                ))
                .toList();
    }

    private List<LotConfiguredPlanetEntry> configuredPlanets(Chart chart, LotEntry lot) {
        return CONFIGURED_PLANET_ORDER.stream()
                .map(planet -> configuredPlanet(chart, lot, planet))
                .filter(entry -> entry != null)
                .sorted(Comparator
                        .comparingInt((LotConfiguredPlanetEntry entry) -> CONFIGURED_PLANET_ORDER.indexOf(entry.planet()))
                        .thenComparingDouble(LotConfiguredPlanetEntry::degreeOrbFromExact))
                .toList();
    }

    private LotConfiguredPlanetEntry configuredPlanet(Chart chart, LotEntry lot, Planet planet) {
        PlanetPointEntry point = conditionEntries.requirePlanetPoint(chart, planet);
        int signDistance = signDistance(lot.sign(), point.sign());
        AspectType aspect = signAspect(signDistance);
        if (aspect == null) {
            return null;
        }
        double degreeOrbFromExact = degreeOrbFromExact(lot.longitude(), point.longitude(), aspect);
        return new LotConfiguredPlanetEntry(planet, aspect, signDistance, degreeOrbFromExact);
    }

    private double degreeOrbFromExact(double lotLongitude, double planetLongitude, AspectType aspect) {
        double separation = AstroMath.rawAngularSeparation(lotLongitude, planetLongitude);
        return Math.abs(separation - aspect.getExactAngle());
    }

    private int signDistance(app.chart.data.ZodiacSign signA, app.chart.data.ZodiacSign signB) {
        int distance = Math.abs(signA.ordinal() - signB.ordinal());
        return Math.min(distance, 12 - distance);
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
}
