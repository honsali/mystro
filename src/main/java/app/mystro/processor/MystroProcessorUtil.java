package app.mystro.processor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import app.common.Config;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;
import app.swisseph.wrapper.api.ISweJulianDate;

public class MystroProcessorUtil {

    public String ordinalSuffix(int value) {
        return switch (value) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    public String signOf(double lon) {
        return Config.SIGNS.get((int) Math.floor(normalize(lon) / 30.0));
    }

    public double signLon(double lon) {
        return normalize(lon) % 30.0;
    }

    public double normalize(double lon) {
        double value = lon % 360.0;
        return value < 0 ? value + 360.0 : value;
    }

    public OffsetDateTime fromJulian(SwissEph swissEph, double jd, ZoneOffset offset) {
        ISweJulianDate utcDate = swissEph.swe_revjul(jd, SweConst.SE_GREG_CAL);
        double hour = utcDate.utime();
        int hours = (int) Math.floor(hour);
        int minutes = (int) Math.floor((hour - hours) * 60.0);
        int seconds = (int) Math.round((((hour - hours) * 60.0) - minutes) * 60.0);
        if (seconds == 60) {
            seconds = 0;
            minutes += 1;
        }
        if (minutes == 60) {
            minutes = 0;
            hours += 1;
        }
        OffsetDateTime utc = OffsetDateTime.of(utcDate.year(), utcDate.month(), utcDate.day(), hours, minutes, seconds, 0, ZoneOffset.UTC);
        return utc.withOffsetSameInstant(offset);
    }

    public int signHouse(String ascSign, String sign) {
        return Math.floorMod(Config.SIGNS.indexOf(sign) - Config.SIGNS.indexOf(ascSign), 12) + 1;
    }

}
