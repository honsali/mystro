package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Angularity;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.AphesisBasis;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.VitalityYearsTier;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HylegAlcocodenEntry(
        String doctrine,
        String method,
        HylegPoint hyleg,
        AlcocodenPoint alcocoden,
        VitalityYearsIndicator vitalityYears,
        List<HylegCandidate> candidates
) {
    public HylegAlcocodenEntry {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HylegPoint(
            String point,
            double longitude,
            ZodiacSign sign,
            double degreeInSign,
            int house,
            AphesisBasis aphesisBasis,
            String selectionReason
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AlcocodenPoint(
            Planet planet,
            int dignityScore,
            List<DignityType> dignityClaims,
            boolean configuredToHyleg,
            String selectionReason
    ) {
        public AlcocodenPoint {
            dignityClaims = dignityClaims == null ? List.of() : List.copyOf(dignityClaims);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VitalityYearsIndicator(
            Planet alcocoden,
            Angularity alcocodenAngularity,
            double baseYears,
            VitalityYearsTier baseTier,
            List<VitalityYearsModifier> modifiers,
            double indicatedYears,
            String method
    ) {
        public VitalityYearsIndicator {
            modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VitalityYearsModifier(
            Planet planet,
            AspectType aspect,
            double deltaYears,
            String reason
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HylegCandidate(
            String point,
            double longitude,
            ZodiacSign sign,
            double degreeInSign,
            int house,
            boolean eligible,
            AphesisBasis aphesisBasis,
            String reason
    ) {}
}
