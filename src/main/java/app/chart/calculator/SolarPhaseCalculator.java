package app.chart.calculator;

import java.util.ArrayList;
import java.util.List;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.data.Planet;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.chart.model.SolarPhaseEntry;

public class SolarPhaseCalculator implements Calculator {

    public void calculate(Chart chart, CalculationContext ctx) {
        List<SolarPhaseEntry> solarPhase = new ArrayList<>();
        PlanetPosition sun = chart.requirePlanet(Planet.SUN);
        for (PlanetPosition planet : chart.getPlanets()) {
            if (planet.getPlanet() == Planet.SUN || planet.getPlanet() == Planet.NORTH_NODE || planet.getPlanet() == Planet.SOUTH_NODE) {
                continue;
            }
            solarPhase.add(new SolarPhaseEntry(planet.getPlanet(), AstroMath.orientationToSun(planet.getLongitude(), sun.getLongitude())));
        }
        chart.setSolarPhase(solarPhase);
    }
}
