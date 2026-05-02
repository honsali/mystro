package app.descriptive.common.model;

import app.basic.data.Planet;
import app.descriptive.common.data.SolarCondition;

public record SolarConditionEntry(
        Planet planet,
        double angularDistanceFromSun,
        SolarCondition condition
) {
}
