package app.basic.model;

import app.basic.data.PointType;

public sealed interface PointEntry
        permits PlanetPointEntry, AnglePointEntry {
    PointType getType();
}
