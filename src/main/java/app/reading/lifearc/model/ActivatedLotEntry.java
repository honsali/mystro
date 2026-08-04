package app.reading.lifearc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivatedLotEntry(
        String name,
        String displayName,
        String doctrine,
        ZodiacSign sign,
        int house,
        Planet ruler,
        String lotAssessmentRef
) {
}
