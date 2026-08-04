package app.reading.lifearc.transit;

import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public record MonthlyTransitNatalContact(
        PointKey transitPoint,
        TransitNatalTargetType natalTargetType,
        String natalTargetName,
        ZodiacSign natalTargetSign,
        double natalTargetDegreeInSign,
        int natalTargetHouse,
        double orbDegrees
) {}
