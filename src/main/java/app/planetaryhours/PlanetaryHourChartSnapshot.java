package app.planetaryhours;

import app.chart.data.HouseSystem;
import app.chart.data.ZodiacSign;

import java.util.List;

public final class PlanetaryHourChartSnapshot {

    private final HouseSystem houseSystem;
    private final double ascendantLongitude;
    private final ZodiacSign ascendantSign;
    private final List<PlanetaryHourHouseSignPlanetRow> houseSignPlanets;

    public PlanetaryHourChartSnapshot(HouseSystem houseSystem,
                                      double ascendantLongitude,
                                      ZodiacSign ascendantSign,
                                      List<PlanetaryHourHouseSignPlanetRow> houseSignPlanets) {
        this.houseSystem = houseSystem;
        this.ascendantLongitude = ascendantLongitude;
        this.ascendantSign = ascendantSign;
        this.houseSignPlanets = List.copyOf(houseSignPlanets);
    }

    public HouseSystem getHouseSystem() {
        return houseSystem;
    }

    public double getAscendantLongitude() {
        return ascendantLongitude;
    }

    public ZodiacSign getAscendantSign() {
        return ascendantSign;
    }

    public List<PlanetaryHourHouseSignPlanetRow> getHouseSignPlanets() {
        return houseSignPlanets;
    }
}
