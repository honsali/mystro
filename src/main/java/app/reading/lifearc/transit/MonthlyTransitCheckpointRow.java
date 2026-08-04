package app.reading.lifearc.transit;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public record MonthlyTransitCheckpointRow(
        int checkpointNumber,
        int ageYears,
        int monthInYear,
        OffsetDateTime checkpointDateTime,
        OffsetDateTime periodEndDateTimeExclusive,
        boolean activeForInquiry,
        int annualProfectedHouse,
        ZodiacSign annualProfectedSign,
        Planet lordOfYear,
        int monthlyProfectedHouse,
        ZodiacSign monthlyProfectedSign,
        Planet lordOfMonth,
        List<MonthlyTransitPointEntry> transitPoints,
        List<PointKey> transitPointsInAnnualProfectedSign,
        List<PointKey> transitPointsInMonthlyProfectedSign,
        List<PointKey> transitPointsOverlayingAnnualProfectedHouse,
        List<PointKey> transitPointsOverlayingMonthlyProfectedHouse,
        List<MonthlyTransitNatalContact> conjunctions,
        List<MonthlyTransitActivationContact> activationContacts
) {
    public MonthlyTransitCheckpointRow {
        transitPoints = transitPoints == null ? List.of() : List.copyOf(transitPoints);
        transitPointsInAnnualProfectedSign = transitPointsInAnnualProfectedSign == null ? List.of() : List.copyOf(transitPointsInAnnualProfectedSign);
        transitPointsInMonthlyProfectedSign = transitPointsInMonthlyProfectedSign == null ? List.of() : List.copyOf(transitPointsInMonthlyProfectedSign);
        transitPointsOverlayingAnnualProfectedHouse = transitPointsOverlayingAnnualProfectedHouse == null ? List.of() : List.copyOf(transitPointsOverlayingAnnualProfectedHouse);
        transitPointsOverlayingMonthlyProfectedHouse = transitPointsOverlayingMonthlyProfectedHouse == null ? List.of() : List.copyOf(transitPointsOverlayingMonthlyProfectedHouse);
        conjunctions = conjunctions == null ? List.of() : List.copyOf(conjunctions);
        activationContacts = activationContacts == null ? List.of() : List.copyOf(activationContacts);
    }
}
