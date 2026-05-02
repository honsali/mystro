package app.basic.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.basic.data.Angularity;
import app.basic.data.Planet;
import app.basic.data.PointType;
import app.basic.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanetPointEntry(
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        double latitude,
        double rightAscension,
        double declination,
        double altitude,
        boolean aboveHorizon,
        double speed,
        double meanDailySpeed,
        double speedRatio,
        boolean retrograde,
        int house,
        int wholeSignHouse,
        Integer quadrantHouse,
        Angularity angularity,
        double antisciaLongitude,
        double contraAntisciaLongitude,
        Planet domicileRuler,
        Planet exaltationRuler,
        TriplicityRulers triplicityRulers,
        Planet termRuler,
        Planet faceRuler,
        Planet detrimentRuler,
        Planet fallRuler
) implements PointEntry {
    @Override
    public PointType getType() {
        return PointType.PLANET;
    }
}
