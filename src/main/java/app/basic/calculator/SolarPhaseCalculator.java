package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.AstroMath;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.basic.model.PlanetPosition;
import app.basic.model.SolarPhaseEntry;
import app.basic.data.Planet;

public class SolarPhaseCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        List<SolarPhaseEntry> solarPhase = new ArrayList<>();
        PlanetPosition sun = natalChart.requirePlanet(Planet.SUN);
        for (PlanetPosition planet : natalChart.getPlanets()) {
            if (planet.getPlanet() == Planet.SUN || planet.getPlanet() == Planet.NORTH_NODE || planet.getPlanet() == Planet.SOUTH_NODE) {
                continue;
            }
            solarPhase.add(new SolarPhaseEntry(planet.getPlanet(), AstroMath.orientationToSun(planet.getLongitude(), sun.getLongitude())));
        }
        natalChart.setSolarPhase(solarPhase);
    }
}
