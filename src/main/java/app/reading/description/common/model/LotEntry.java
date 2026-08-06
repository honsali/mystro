package app.reading.description.common.model;

import java.util.List;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record LotEntry(
        String name,
        String displayName,
        String doctrine,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        Planet ruler,
        String formula,
        List<LotConfiguredPlanetEntry> configuredPlanets
) {
    public LotEntry {
        configuredPlanets = configuredPlanets == null ? List.of() : List.copyOf(configuredPlanets);
    }
}
