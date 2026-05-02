package app.descriptive.ptolemy.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.TraditionalTables;
import app.basic.model.BasicChart;
import app.basic.model.PlanetPosition;
import app.descriptive.common.data.AspectType;
import app.descriptive.common.model.AspectEntry;

public final class PtolemyAspectCalculator {
    public List<AspectEntry> calculate(BasicChart chart) {
        List<PlanetPosition> planets = chart.getPlanets().stream()
                .filter(planet -> TraditionalTables.isTraditionalPlanet(planet.getPlanet()))
                .toList();
        List<AspectEntry> aspects = new ArrayList<>();
        for (int i = 0; i < planets.size(); i++) {
            PlanetPosition a = planets.get(i);
            for (int j = i + 1; j < planets.size(); j++) {
                PlanetPosition b = planets.get(j);
                int signDistance = signDistance(a, b);
                AspectType type = ptolemaicConfiguration(signDistance);
                if (type != null) {
                    double separation = rawAngularSeparation(a.getLongitude(), b.getLongitude());
                    aspects.add(new AspectEntry(a.getPlanet(), b.getPlanet(), type, signDistance, separation, Math.abs(separation - exactDegrees(type))));
                }
            }
        }
        return aspects;
    }

    private AspectType ptolemaicConfiguration(int signDistance) {
        return switch (signDistance) {
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
        double distance = Math.abs(TraditionalTables.normalize(longitudeA) - TraditionalTables.normalize(longitudeB));
        return distance > 180.0 ? 360.0 - distance : distance;
    }
}
