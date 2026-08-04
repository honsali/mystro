package app.reading.lifearc.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record MonthlyProfectionReferenceEntry(
        AnnualProfectionReference reference,
        ZodiacSign natalSign,
        ZodiacSign annualSign,
        ZodiacSign profectedSign,
        Integer profectedHouse,
        Planet lord
) {}
