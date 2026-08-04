package app.reading.lifearc.distribution;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record DistributionThroughBoundsPeriod(
        int sequenceIndex,
        int cycleNumber,
        ZodiacSign sign,
        int boundIndexInSign,
        Planet boundRuler,
        double boundStartLongitude,
        double boundEndLongitude,
        double boundStartDegreeInSign,
        double boundEndDegreeInSign,
        double directedStartLongitude,
        double directedEndLongitude,
        double directedStartDegreeInSign,
        double directedEndDegreeInSign,
        double startArcDegrees,
        double endArcDegrees,
        double startAgeYears,
        double endAgeYearsExclusive,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        boolean activeForInquiry,
        List<DistributionThroughBoundsContact> contacts
) {
    public DistributionThroughBoundsPeriod {
        contacts = contacts == null ? List.of() : List.copyOf(contacts);
    }
}
