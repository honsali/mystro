package app.descriptive.valens.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.AstroMath;
import app.basic.TraditionalTables;
import app.basic.data.Planet;
import app.basic.model.NatalChart;
import app.basic.model.PlanetPosition;
import app.descriptive.common.data.AspectType;
import app.descriptive.common.model.AspectEntry;

public final class ValensAspectCalculator {
    public List<AspectEntry> calculate(NatalChart chart) {
        List<PlanetPosition> planets = chart.getPlanets().stream()
                .filter(planet -> TraditionalTables.isTraditionalPlanet(planet.getPlanet()))
                .toList();
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
        return switch (type) {
            case CONJUNCTION -> 0.0;
            case SEXTILE -> 60.0;
            case SQUARE -> 90.0;
            case TRINE -> 120.0;
            case OPPOSITION -> 180.0;
        };
    }

    private int signDistance(PlanetPosition a, PlanetPosition b) {
        int distance = Math.abs(a.getSign().ordinal() - b.getSign().ordinal());
        return Math.min(distance, 12 - distance);
    }

    private double rawAngularSeparation(double longitudeA, double longitudeB) {
        return AstroMath.rawAngularSeparation(longitudeA, longitudeB);
    }
}
