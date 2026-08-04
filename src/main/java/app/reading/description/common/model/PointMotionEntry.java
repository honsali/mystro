package app.reading.description.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.reading.description.common.data.PlanetMotionState;
import app.reading.description.common.data.RelativeSpeed;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PointMotionEntry(
        PlanetMotionState state,
        Double stationaryThresholdDegPerDay,
        double speedRatio,
        RelativeSpeed relativeSpeed
) {
}
