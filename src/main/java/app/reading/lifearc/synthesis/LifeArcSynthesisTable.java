package app.reading.lifearc.synthesis;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record LifeArcSynthesisTable(
        String methodId,
        String primaryDoctrine,
        String synthesisMethod,
        LocalDate inquiryDate,
        OffsetDateTime inquiryDateTime,
        int completedAgeYears,
        OffsetDateTime activeYearStartDateTime,
        OffsetDateTime activeYearEndDateTimeExclusive,
        List<LifeArcSynthesisEvidence> evidence,
        List<LifeArcSynthesisGroup> groups
) {
    public LifeArcSynthesisTable {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        groups = groups == null ? List.of() : List.copyOf(groups);
    }
}
