package app.reading.lifearc.firdaria;

import java.time.OffsetDateTime;

import app.chart.data.Planet;

public record FirdariaSubperiod(
        int sequenceIndex,
        Planet partner,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTimeExclusive,
        boolean activeForInquiry
) {}
