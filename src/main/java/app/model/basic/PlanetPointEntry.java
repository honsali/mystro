package app.model.basic;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.model.data.Angularity;
import app.model.data.Planet;
import app.model.data.PointType;
import app.model.data.ZodiacSign;

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
