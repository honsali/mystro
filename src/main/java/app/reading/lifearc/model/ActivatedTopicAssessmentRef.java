package app.reading.lifearc.model;

import java.util.List;

public record ActivatedTopicAssessmentRef(
        String topic,
        String methodId,
        List<String> matchedConditionRefs
) {
    public ActivatedTopicAssessmentRef {
        matchedConditionRefs = matchedConditionRefs == null ? List.of() : List.copyOf(matchedConditionRefs);
    }
}
