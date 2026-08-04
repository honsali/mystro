package app.reading.lifearc.solarreturn;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public record SolarReturnNatalComparisonRow(
        int ageYears,
        OffsetDateTime returnDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        boolean activeForInquiry,
        int profectedHouse,
        ZodiacSign profectedSign,
        Planet lordOfYear,
        SolarReturnPointOverlay lordOfYearOverlay,
        int ascendantNatalHouseOverlay,
        int midheavenNatalHouseOverlay,
        List<SolarReturnPointOverlay> pointOverlays,
        List<PointKey> solarReturnPointsInProfectedSign,
        List<PointKey> solarReturnPointsOverlayingProfectedHouse,
        List<SolarReturnNatalContact> conjunctions
) {
    public SolarReturnNatalComparisonRow {
        pointOverlays = pointOverlays == null ? List.of() : List.copyOf(pointOverlays);
        solarReturnPointsInProfectedSign = solarReturnPointsInProfectedSign == null ? List.of() : List.copyOf(solarReturnPointsInProfectedSign);
        solarReturnPointsOverlayingProfectedHouse = solarReturnPointsOverlayingProfectedHouse == null ? List.of() : List.copyOf(solarReturnPointsOverlayingProfectedHouse);
        conjunctions = conjunctions == null ? List.of() : List.copyOf(conjunctions);
    }
}
