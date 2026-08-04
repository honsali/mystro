package app.reading.lifearc.solarreturn;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SolarReturnPointOverlay(
        PointKey point,
        PointType type,
        double longitude,
        ZodiacSign sign,
        double degreeInSign,
        Integer solarReturnHouse,
        int natalHouseOverlay,
        Boolean retrograde
) {}
