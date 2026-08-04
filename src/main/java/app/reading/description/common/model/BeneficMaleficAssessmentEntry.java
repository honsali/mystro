package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.reading.description.common.data.BeneficMaleficCondition;
import app.reading.description.common.data.ConditionAssessment;
import app.reading.description.common.data.RulerAffliction;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BeneficMaleficAssessmentEntry(
        ConditionAssessment assessment,
        BeneficMaleficCondition condition,
        Planet agent,
        Planet coAgent,
        AspectType aspect,
        AspectType coAspect,
        Integer signDistance,
        Integer coSignDistance,
        Double orbFromExact,
        Double coOrbFromExact,
        Boolean agentOfSect,
        Boolean coAgentOfSect,
        List<RulerAffliction> rulerAfflictions
) {
    public BeneficMaleficAssessmentEntry(
            ConditionAssessment assessment,
            BeneficMaleficCondition condition,
            Planet agent,
            AspectType aspect,
            int signDistance,
            boolean agentOfSect
    ) {
        this(assessment, condition, agent, null, aspect, null, signDistance, null, null, null, agentOfSect, null, null);
    }

    public BeneficMaleficAssessmentEntry {
        if (rulerAfflictions != null && rulerAfflictions.isEmpty()) {
            rulerAfflictions = null;
        } else if (rulerAfflictions != null) {
            rulerAfflictions = List.copyOf(rulerAfflictions);
        }
    }
}
