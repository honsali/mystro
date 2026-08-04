package app.reading.description.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.AspectMotion;
import app.chart.data.AspectType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AspectRelationEntry(
        AspectType aspect,
        Integer signDistance,
        Double byDegreeOrb,
        AspectMotion motion
) {
}
