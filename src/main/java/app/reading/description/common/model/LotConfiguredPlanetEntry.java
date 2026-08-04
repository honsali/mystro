package app.reading.description.common.model;

import app.chart.data.AspectType;
import app.chart.data.Planet;

public record LotConfiguredPlanetEntry(
        Planet planet,
        AspectType aspect,
        int signDistance,
        double degreeOrbFromExact
) {
}
