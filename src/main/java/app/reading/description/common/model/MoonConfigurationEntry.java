package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.MoonPhaseName;
import app.chart.data.SectCondition;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.LightDisposition;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MoonConfigurationEntry(
        ZodiacSign sign,
        int house,
        List<DignityType> dignities,
        List<DignityType> debilities,
        SectCondition sectCondition,
        MoonPhaseName phase,
        boolean waxing,
        double illumination,
        LightDisposition lightDisposition,
        PointMotionEntry motion,
        LunarAspectEventEntry lastSeparation,
        LunarAspectEventEntry nextApplication,
        boolean voidOfCourse,
        List<ConfiguredPlanetEntry> configuredPlanets,
        String method
) {
    public MoonConfigurationEntry {
        dignities = dignities == null ? List.of() : List.copyOf(dignities);
        debilities = debilities == null ? List.of() : List.copyOf(debilities);
        configuredPlanets = configuredPlanets == null ? List.of() : List.copyOf(configuredPlanets);
    }
}
