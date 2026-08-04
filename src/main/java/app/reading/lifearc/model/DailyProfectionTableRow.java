package app.reading.lifearc.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DailyProfectionTableRow(
        LocalDate date,
        OffsetDateTime periodStartDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        int ageYears,
        int cycleNumber,
        int yearInCycle,
        int monthInYear,
        int dayInMonth,
        boolean focusDate,
        List<DailyProfectionReferenceEntry> referenceProfections,
        List<DailyProfectionActivatedPoint> activatedNatalPoints,
        List<DailyProfectionActivatedLot> activatedLots
) {
    public DailyProfectionTableRow {
        referenceProfections = referenceProfections == null ? List.of() : List.copyOf(referenceProfections);
        activatedNatalPoints = activatedNatalPoints == null ? List.of() : List.copyOf(activatedNatalPoints);
        activatedLots = activatedLots == null ? List.of() : List.copyOf(activatedLots);
    }
}
