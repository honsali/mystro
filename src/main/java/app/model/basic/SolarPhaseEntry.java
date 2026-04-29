package app.model.basic;

import app.model.data.Planet;
import app.model.data.SolarOrientation;

public final class SolarPhaseEntry {
    private final Planet planet;
    private final SolarOrientation orientationToSun;

    public SolarPhaseEntry(Planet planet, SolarOrientation orientationToSun) {
        this.planet = planet;
        this.orientationToSun = orientationToSun;
    }

    public Planet getPlanet() { return planet; }
    public SolarOrientation getOrientationToSun() { return orientationToSun; }
}
