package app.planetaryhours;

import app.chart.data.ZodiacSign;

import java.util.List;

public final class PlanetaryHourHouseSignPlanetRow {

    private final int house;
    private final ZodiacSign sign;
    private final List<PlanetaryHourPlanetPlacement> planets;

    public PlanetaryHourHouseSignPlanetRow(int house,
                                           ZodiacSign sign,
                                           List<PlanetaryHourPlanetPlacement> planets) {
        this.house = house;
        this.sign = sign;
        this.planets = List.copyOf(planets);
    }

    public int getHouse() {
        return house;
    }

    public ZodiacSign getSign() {
        return sign;
    }

    public List<PlanetaryHourPlanetPlacement> getPlanets() {
        return planets;
    }
}
