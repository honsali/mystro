package app.reading.lifearc.decennial;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.SectCondition;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DecennialRulerCondition(
        Planet planet,
        ZodiacSign sign,
        double degreeInSign,
        int house,
        boolean retrograde,
        SectCondition sectCondition,
        SolarCondition solarCondition,
        List<DignityType> dignities,
        List<DignityType> debilities,
        List<Integer> ruledNatalHouses
) {
    public DecennialRulerCondition {
        dignities = dignities == null ? List.of() : List.copyOf(dignities);
        debilities = debilities == null ? List.of() : List.copyOf(debilities);
        ruledNatalHouses = ruledNatalHouses == null ? List.of() : List.copyOf(ruledNatalHouses);
    }
}
