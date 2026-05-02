package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.basic.model.BasicChart;
import app.basic.model.PlanetPosition;
import app.basic.model.SolarPhaseEntry;
import app.basic.data.Planet;
import app.basic.data.SolarOrientation;

public class SolarPhaseCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        List<SolarPhaseEntry> solarPhase = new ArrayList<>();
        PlanetPosition sun = ctx.planet(basicChart.getPlanets(), Planet.SUN);
        if (sun == null) {
            return;
        }
        for (PlanetPosition planet : basicChart.getPlanets()) {
            if (planet.getPlanet() == Planet.SUN || planet.getPlanet() == Planet.NORTH_NODE || planet.getPlanet() == Planet.SOUTH_NODE) {
                continue;
            }
            solarPhase.add(new SolarPhaseEntry(planet.getPlanet(), orientationToSun(planet.getLongitude(), sun.getLongitude(), ctx)));
        }
        basicChart.setSolarPhase(solarPhase);
    }


    private SolarOrientation orientationToSun(double planetLongitude, double sunLongitude, BasicCalculationContext ctx) {
        double delta = ctx.normalize(planetLongitude - sunLongitude);
        return delta > 180.0 ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL;
    }
}
