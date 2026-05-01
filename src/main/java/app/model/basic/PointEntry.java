package app.model.basic;

import app.model.data.PointType;

public sealed interface PointEntry
        permits PlanetPointEntry, AnglePointEntry, LotPointEntry, SyzygyPointEntry {
    PointType getType();
}
