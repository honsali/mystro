package app.reading.description.common.model;

import app.chart.data.Planet;
import app.chart.data.AspectType;

public record AspectEntry(
        Planet planetA,
        Planet planetB,
        AspectType type,
        int signDistance,
        double angularSeparation,
        double orbFromExact
) {
}
