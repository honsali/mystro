package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.FixedStarTargetType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FixedStarEntry(
        String star,
        double magnitude,
        List<Planet> nature,
        String source,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        String conjoinedPoint,
        FixedStarTargetType conjoinedPointType,
        double conjoinedPointLongitude,
        double orbDeg,
        double maxOrbDeg,
        String method
) {
    public FixedStarEntry {
        nature = List.copyOf(nature);
    }
}
