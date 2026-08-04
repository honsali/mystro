package app.reading.lifearc.synthesis;

import java.time.OffsetDateTime;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record LifeArcSynthesisEvidence(
        int sequenceIndex,
        String sourceTechnique,
        String sourceMethodId,
        LifeArcEvidenceWeightClass weightClass,
        String timingLabel,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        LifeArcEvidenceKeyType keyType,
        String key,
        ZodiacSign sign,
        Integer house,
        Planet planet,
        String point,
        String lotName,
        String aspect,
        int weight,
        String detail
) {}
