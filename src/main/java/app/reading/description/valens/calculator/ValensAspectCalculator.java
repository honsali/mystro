package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.List;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.chart.AstroMath;
import app.chart.TraditionalTables;
import app.chart.data.AspectType;
import app.reading.description.common.model.AspectEntry;

public final class ValensAspectCalculator {
    public List<AspectEntry> calculate(Chart chart) {
        List<PlanetPosition> planets = chart.getPlanets().stream().filter(planet -> TraditionalTables.isTraditionalPlanet(planet.getPlanet())).toList();
        List<AspectEntry> aspects = new ArrayList<>();
        for (int i = 0; i < planets.size(); i++) {
            PlanetPosition a = planets.get(i);
            for (int j = i + 1; j < planets.size(); j++) {
                PlanetPosition b = planets.get(j);
                int signDistance = signDistance(a, b);
                AspectType type = aspectType(signDistance);
                if (type != null) {
                    double separation = rawAngularSeparation(a.getLongitude(), b.getLongitude());
                    aspects.add(new AspectEntry(a.getPlanet(), b.getPlanet(), type, signDistance, separation, Math.abs(separation - exactDegrees(type))));
                }
            }
        }
        return aspects;
    }

    private AspectType aspectType(int signDistance) {
        return switch (signDistance) {
            case 0 -> AspectType.CONJUNCTION;
            case 2 -> AspectType.SEXTILE;
            case 3 -> AspectType.SQUARE;
            case 4 -> AspectType.TRINE;
            case 6 -> AspectType.OPPOSITION;
            default -> null;
        };
    }

    private double exactDegrees(AspectType type) {
        return type.getExactAngle();
    }

    private int signDistance(PlanetPosition a, PlanetPosition b) {
        int distance = Math.abs(a.getSign().ordinal() - b.getSign().ordinal());
        return Math.min(distance, 12 - distance);
    }

    private double rawAngularSeparation(double longitudeA, double longitudeB) {
        return AstroMath.rawAngularSeparation(longitudeA, longitudeB);
    }
}
