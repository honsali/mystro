package app.chart.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.data.SolarOrientation;
import app.descriptive.common.data.DignityType;
import app.descriptive.common.data.SolarCondition;

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
        Planet triplicityRuler,
        Planet participatingTriplicityRuler,
        Planet termRuler,
        Planet faceRuler,
        Planet detrimentRuler,
        Planet fallRuler,
        List<DignityType> dignities,
        List<DignityType> debilities,
        SolarOrientation solarPhase,
        PlanetSectInfo sect,
        SolarCondition solarCondition
) implements PointEntry {
    @Override
    public PointType getType() {
        return PointType.PLANET;
    }

    public PlanetPointEntry withDignityAssessment(List<DignityType> dignities, List<DignityType> debilities) {
        return withAssessments(List.copyOf(dignities), List.copyOf(debilities), solarPhase, sect, solarCondition);
    }

    public PlanetPointEntry withSolarPhase(SolarOrientation solarPhase) {
        return withAssessments(dignities, debilities, solarPhase, sect, solarCondition);
    }

    public PlanetPointEntry withSect(PlanetSectInfo sect) {
        return withAssessments(dignities, debilities, solarPhase, sect, solarCondition);
    }

    public PlanetPointEntry withSolarCondition(SolarCondition solarCondition) {
        return withAssessments(dignities, debilities, solarPhase, sect, solarCondition);
    }

    private PlanetPointEntry withAssessments(List<DignityType> dignities, List<DignityType> debilities, SolarOrientation solarPhase, PlanetSectInfo sect, SolarCondition solarCondition) {
        return new PlanetPointEntry(
                longitude,
                sign,
                degreeInSign,
                latitude,
                rightAscension,
                declination,
                altitude,
                aboveHorizon,
                speed,
                meanDailySpeed,
                speedRatio,
                retrograde,
                house,
                wholeSignHouse,
                quadrantHouse,
                angularity,
                antisciaLongitude,
                contraAntisciaLongitude,
                domicileRuler,
                exaltationRuler,
                triplicityRuler,
                participatingTriplicityRuler,
                termRuler,
                faceRuler,
                detrimentRuler,
                fallRuler,
                dignities,
                debilities,
                solarPhase,
                sect,
                solarCondition
        );
    }
}
