package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicAssessmentEntry(
        String topic,
        String primaryDoctrine,
        List<String> supportingDoctrines,
        String methodId,
        List<TopicEvidenceEntry> evidence
) {
    public TopicAssessmentEntry {
        supportingDoctrines = supportingDoctrines == null ? List.of() : List.copyOf(supportingDoctrines);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }
}
