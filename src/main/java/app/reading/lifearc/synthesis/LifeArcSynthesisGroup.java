package app.reading.lifearc.synthesis;

import java.util.List;

public record LifeArcSynthesisGroup(
        LifeArcEvidenceKeyType keyType,
        String key,
        int totalWeight,
        int evidenceCount,
        List<Integer> evidenceSequenceIndexes
) {
    public LifeArcSynthesisGroup {
        evidenceSequenceIndexes = evidenceSequenceIndexes == null ? List.of() : List.copyOf(evidenceSequenceIndexes);
    }
}
