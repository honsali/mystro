package app.reading.description.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DodecatemoriaEntry(
        String sourceName,
        String sourceType,
        String sourceDoctrine,
        double sourceLongitude,
        ZodiacSign sourceSign,
        double sourceDegreeInSign,
        int twelfthPart,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        Planet ruler,
        String formula
) {
}
