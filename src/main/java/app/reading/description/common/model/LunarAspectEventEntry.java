package app.reading.description.common.model;

import app.chart.data.AspectType;
import app.chart.data.Planet;

public record LunarAspectEventEntry(
        Planet planet,
        AspectType aspect,
        double arcDeg
) {
}
