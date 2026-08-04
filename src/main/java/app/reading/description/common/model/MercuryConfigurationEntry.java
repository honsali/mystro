package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.MorningEveningStar;
import app.reading.description.common.data.SolarCondition;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MercuryConfigurationEntry(
        ZodiacSign sign,
        int house,
        List<DignityType> dignities,
        List<DignityType> debilities,
        SectCondition sectCondition,
        SolarOrientation solarPhase,
        MorningEveningStar morningOrEveningStar,
        double arcFromSun,
        SolarCondition solarCondition,
        PointMotionEntry motion,
        boolean joinedToSun,
        boolean regardsMoon,
        Boolean aversionToMoon,
        AspectRelationEntry moonRelation,
        List<ConfiguredPlanetEntry> configuredPlanets,
        String method
) {
    public MercuryConfigurationEntry {
        dignities = dignities == null ? List.of() : List.copyOf(dignities);
        debilities = debilities == null ? List.of() : List.copyOf(debilities);
        configuredPlanets = configuredPlanets == null ? List.of() : List.copyOf(configuredPlanets);
    }
}
