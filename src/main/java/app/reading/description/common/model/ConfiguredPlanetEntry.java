package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.reading.description.common.data.DignityType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfiguredPlanetEntry(
        Planet planet,
        AspectType aspect,
        Integer signDistance,
        Double byDegreeOrb,
        AspectMotion motion,
        List<DignityType> reception
) {
    public ConfiguredPlanetEntry {
        reception = reception == null ? List.of() : List.copyOf(reception);
    }
}
