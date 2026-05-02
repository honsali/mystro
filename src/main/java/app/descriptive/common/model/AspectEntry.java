package app.descriptive.common.model;

import app.basic.data.Planet;
import app.descriptive.common.data.AspectType;

public record AspectEntry(
        Planet planetA,
        Planet planetB,
        AspectType type,
        int signDistance,
        double angularSeparation,
        double orbFromExact
) {
}
