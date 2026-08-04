package app.reading.lifearc.decennial;

import java.time.OffsetDateTime;

import app.chart.data.Planet;

public record DecennialSubperiod(
        int sequenceIndex,
        Planet partner,
        DecennialRulerCondition partnerNatalCondition,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        boolean activeForInquiry
) {}
