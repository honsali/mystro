package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.DoryphoryDirection;
import app.reading.description.common.data.DoryphoryKind;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DoryphoryEntry(
        Planet light,
        boolean lightOfSect,
        Planet spearBearer,
        DoryphoryKind kind,
        List<DoryphoryKind> kinds,
        List<DignityType> qualifyingDignities,
        int strengthScore,
        DoryphoryDirection direction,
        AspectType aspect,
        int signDistance,
        int lightHouse,
        int spearBearerHouse,
        boolean spearBearerOfSect
) {
    public DoryphoryEntry {
        kinds = List.copyOf(kinds);
        if (qualifyingDignities != null && qualifyingDignities.isEmpty()) {
            qualifyingDignities = null;
        } else if (qualifyingDignities != null) {
            qualifyingDignities = List.copyOf(qualifyingDignities);
        }
    }
}
