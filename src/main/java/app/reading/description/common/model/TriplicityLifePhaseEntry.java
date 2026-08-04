package app.reading.description.common.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.data.TriplicityLifePhase;
import app.reading.description.common.data.TriplicityLifeReference;
import app.reading.description.common.data.TriplicityRulerRole;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TriplicityLifePhaseEntry(
        TriplicityLifeReference reference,
        String referenceName,
        ZodiacSign referenceSign,
        Integer referenceHouse,
        TriplicityLifePhase phase,
        Double startAgeYears,
        Double endAgeYears,
        TriplicityRulerRole role,
        Planet ruler,
        int rulerHouse,
        int rulerWholeSignHouse,
        Angularity rulerAngularity,
        boolean rulerRetrograde,
        SolarCondition rulerSolarCondition,
        List<DignityType> rulerDignities,
        List<DignityType> rulerDebilities
) {}
