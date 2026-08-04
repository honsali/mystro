package app.reading.lifearc.model;

import java.time.LocalDate;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record AnnualProfectionEntry(
        String methodId,
        String primaryDoctrine,
        LocalDate periodStartDate,
        LocalDate periodEndDateExclusive,
        int ageYears,
        int cycleNumber,
        int yearInCycle,
        int profectedHouse,
        ZodiacSign profectedSign,
        Planet lordOfYear,
        long daysElapsed,
        long daysRemaining,
        List<ActivatedNatalPointEntry> activatedNatalPoints,
        List<ActivatedLotEntry> activatedLots,
        List<ActivatedTopicAssessmentRef> activatedTopicAssessmentRefs
) {
    public AnnualProfectionEntry {
        activatedNatalPoints = activatedNatalPoints == null ? List.of() : List.copyOf(activatedNatalPoints);
        activatedLots = activatedLots == null ? List.of() : List.copyOf(activatedLots);
        activatedTopicAssessmentRefs = activatedTopicAssessmentRefs == null ? List.of() : List.copyOf(activatedTopicAssessmentRefs);
    }
}
