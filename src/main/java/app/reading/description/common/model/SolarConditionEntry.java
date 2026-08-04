package app.reading.description.common.model;

import app.chart.data.Planet;
import app.reading.description.common.data.SolarCondition;

public record SolarConditionEntry(
        Planet planet,
        double angularDistanceFromSun,
        SolarCondition condition
) {
}
