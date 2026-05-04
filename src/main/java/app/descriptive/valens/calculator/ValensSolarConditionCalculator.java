package app.descriptive.valens.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.TraditionalTables;
import app.basic.data.Planet;
import app.basic.model.NatalChart;
import app.basic.model.PlanetPosition;
import app.descriptive.common.data.SolarCondition;
import app.descriptive.common.model.SolarConditionEntry;

public final class ValensSolarConditionCalculator {
    private static final double CAZIMI_DEGREES = 1.0 / 6.0;
    private static final double COMBUST_DEGREES = 8.5;
    private static final double UNDER_BEAMS_DEGREES = 15.0;

    public Map<Planet, SolarConditionEntry> calculate(NatalChart chart) {
        Map<Planet, SolarConditionEntry> result = new LinkedHashMap<>();
        for (PlanetPosition position : chart.getPlanets()) {
            Planet planet = position.getPlanet();
            if (planet == Planet.SUN || !TraditionalTables.isTraditionalPlanet(planet)) {
                continue;
            }
            double distance = position.getAngularDistanceFromSun();
            result.put(planet, new SolarConditionEntry(planet, distance, condition(distance)));
        }
        return result;
    }

    private SolarCondition condition(double distance) {
        if (distance <= CAZIMI_DEGREES) {
            return SolarCondition.CAZIMI;
        }
        if (distance <= COMBUST_DEGREES) {
            return SolarCondition.COMBUST;
        }
        if (distance <= UNDER_BEAMS_DEGREES) {
            return SolarCondition.UNDER_BEAMS;
        }
        return SolarCondition.FREE_OF_SUN;
    }
}
