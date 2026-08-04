package app.reading.lifearc.solarreturn;

import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public record SolarReturnNatalContact(
        PointKey solarReturnPoint,
        SolarReturnNatalTargetType natalTargetType,
        String natalTargetName,
        ZodiacSign natalTargetSign,
        double natalTargetDegreeInSign,
        int natalTargetHouse,
        double orbDegrees
) {}
