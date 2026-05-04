package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.basic.model.PlanetPosition;
import app.basic.model.SolarPhaseEntry;
import app.basic.data.Planet;
import app.basic.data.SolarOrientation;

public class SolarPhaseCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        List<SolarPhaseEntry> solarPhase = new ArrayList<>();
        PlanetPosition sun = ctx.planet(natalChart.getPlanets(), Planet.SUN);
        if (sun == null) {
            return;
        }
        for (PlanetPosition planet : natalChart.getPlanets()) {
            if (planet.getPlanet() == Planet.SUN || planet.getPlanet() == Planet.NORTH_NODE || planet.getPlanet() == Planet.SOUTH_NODE) {
                continue;
            }
            solarPhase.add(new SolarPhaseEntry(planet.getPlanet(), orientationToSun(planet.getLongitude(), sun.getLongitude(), ctx)));
        }
        natalChart.setSolarPhase(solarPhase);
    }


    private SolarOrientation orientationToSun(double planetLongitude, double sunLongitude, CalculationContext ctx) {
        double delta = ctx.normalize(planetLongitude - sunLongitude);
        return delta > 180.0 ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL;
    }
}
