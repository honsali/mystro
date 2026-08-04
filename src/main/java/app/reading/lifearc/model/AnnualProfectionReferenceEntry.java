package app.reading.lifearc.model;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;

public record AnnualProfectionReferenceEntry(
        AnnualProfectionReference reference,
        ZodiacSign natalSign,
        ZodiacSign profectedSign,
        Integer profectedHouse,
        Planet lord
) {}
