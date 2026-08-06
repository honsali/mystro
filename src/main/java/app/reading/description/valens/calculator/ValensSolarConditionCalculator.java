package app.reading.description.valens.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.chart.TraditionalTables;
import app.chart.data.Planet;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.SolarConditionEntry;

public final class ValensSolarConditionCalculator {
    private static final double CAZIMI_DEGREES = 17.0 / 60.0;
    private static final double COMBUST_DEGREES = 8.5;
    private static final double UNDER_BEAMS_DEGREES = 15.0;

    public Map<Planet, SolarConditionEntry> calculate(Chart chart) {
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
