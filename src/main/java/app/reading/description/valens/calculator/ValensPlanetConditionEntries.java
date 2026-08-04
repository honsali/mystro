package app.reading.description.valens.calculator;

import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;

final class ValensPlanetConditionEntries {
    PlanetPointEntry requirePlanetPoint(NatalChart chart, Planet planet) {
        PointEntry point = chart.getPoints().get(PointKey.of(planet));
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        throw new IllegalArgumentException("Missing planet point " + planet);
    }
}
