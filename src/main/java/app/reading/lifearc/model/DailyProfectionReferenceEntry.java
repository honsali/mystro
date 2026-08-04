package app.reading.lifearc.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record DailyProfectionReferenceEntry(
        AnnualProfectionReference reference,
        ZodiacSign natalSign,
        ZodiacSign annualSign,
        Integer annualHouse,
        Planet annualLord,
        ZodiacSign monthlySign,
        Integer monthlyHouse,
        Planet monthlyLord,
        ZodiacSign profectedSign,
        Integer profectedHouse,
        Planet lord
) {}
