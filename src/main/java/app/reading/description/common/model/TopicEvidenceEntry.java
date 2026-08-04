package app.reading.description.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicEvidenceEntry(
        String role,
        String sourceDoctrine,
        String targetType,
        String target,
        Integer house,
        ZodiacSign sign,
        Planet ruler,
        String conditionRef
) {
}
