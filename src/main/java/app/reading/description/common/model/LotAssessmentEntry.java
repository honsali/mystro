package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LotAssessmentEntry(
        String lot,
        String displayName,
        String doctrine,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        Planet ruler,
        List<LotConfiguredPlanetEntry> configuredPlanets
) {
    public LotAssessmentEntry {
        configuredPlanets = configuredPlanets == null ? List.of() : List.copyOf(configuredPlanets);
    }
}
