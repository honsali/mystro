package app.chart.model;

import app.chart.data.PointType;

public sealed interface PointEntry
        permits PlanetPointEntry, AnglePointEntry {
    PointType getType();
}
