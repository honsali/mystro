package app.reading.lifearc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivatedNatalPointEntry(
        PointKey point,
        PointType type,
        ZodiacSign sign,
        Integer house
) {
}
