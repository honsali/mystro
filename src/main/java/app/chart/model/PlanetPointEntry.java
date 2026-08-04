package app.chart.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.Angularity;
import app.chart.data.Element;
import app.chart.data.Planet;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.data.SolarOrientation;
import app.chart.data.SectCondition;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.BeneficMaleficAssessmentEntry;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanetPointEntry(
        double longitude,
        ZodiacSign sign,
        Element element,
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
        Planet activeMasterTriplicityRuler,
        Planet participatingTriplicityRuler,
        Planet inactiveMasterTriplicityRuler,
        Planet termRuler,
        Planet faceRuler,
        Planet detrimentPlanet,
        Planet fallPlanet,
        List<DignityType> dignities,
        List<DignityType> debilities,
        SolarOrientation solarPhase,
        SectCondition sectCondition,
        SolarCondition solarCondition,
        List<BeneficMaleficAssessmentEntry> beneficMaleficAssessment,
        boolean joy,
        @JsonIgnore PointType pointType
) implements PointEntry {
    @Override
    public PointType getType() {
        return pointType;
    }

    public PlanetPointEntry withDignityAssessment(List<DignityType> dignities, List<DignityType> debilities) {
        return withAssessments(List.copyOf(dignities), List.copyOf(debilities), solarPhase, sectCondition, solarCondition, beneficMaleficAssessment, joy);
    }

    public PlanetPointEntry withSolarPhase(SolarOrientation solarPhase) {
        return withAssessments(dignities, debilities, solarPhase, sectCondition, solarCondition, beneficMaleficAssessment, joy);
    }

    public PlanetPointEntry withSect(PlanetSectInfo sect) {
        return withAssessments(dignities, debilities, solarPhase, sect.getCondition(), solarCondition, beneficMaleficAssessment, joy);
    }

    public PlanetPointEntry withSolarCondition(SolarCondition solarCondition) {
        return withAssessments(dignities, debilities, solarPhase, sectCondition, solarCondition, beneficMaleficAssessment, joy);
    }

    public PlanetPointEntry withBeneficMaleficAssessment(List<BeneficMaleficAssessmentEntry> beneficMaleficAssessment) {
        return withAssessments(dignities, debilities, solarPhase, sectCondition, solarCondition, List.copyOf(beneficMaleficAssessment), joy);
    }

    public PlanetPointEntry withJoy(boolean joy) {
        return withAssessments(dignities, debilities, solarPhase, sectCondition, solarCondition, beneficMaleficAssessment, joy);
    }

    private PlanetPointEntry withAssessments(List<DignityType> dignities, List<DignityType> debilities, SolarOrientation solarPhase, SectCondition sectCondition, SolarCondition solarCondition, List<BeneficMaleficAssessmentEntry> beneficMaleficAssessment, boolean joy) {
        return new PlanetPointEntry(
                longitude,
                sign,
                element,
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
                activeMasterTriplicityRuler,
                participatingTriplicityRuler,
                inactiveMasterTriplicityRuler,
                termRuler,
                faceRuler,
                detrimentPlanet,
                fallPlanet,
                dignities,
                debilities,
                solarPhase,
                sectCondition,
                solarCondition,
                beneficMaleficAssessment,
                joy,
                pointType
        );
    }
}
