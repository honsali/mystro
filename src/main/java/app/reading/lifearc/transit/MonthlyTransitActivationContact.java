package app.reading.lifearc.transit;

import java.util.List;

import app.chart.data.AspectType;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public record MonthlyTransitActivationContact(
        PointKey transitPoint,
        boolean transitPointIsLordOfYear,
        boolean transitPointIsLordOfMonth,
        TransitNatalTargetType natalTargetType,
        String natalTargetName,
        ZodiacSign natalTargetSign,
        double natalTargetDegreeInSign,
        int natalTargetHouse,
        AspectType aspect,
        double angularSeparation,
        double orbFromExactDegrees,
        List<MonthlyTransitActivationReason> activationReasons,
        int activationWeight
) {
    public MonthlyTransitActivationContact {
        activationReasons = activationReasons == null ? List.of() : List.copyOf(activationReasons);
    }
}
