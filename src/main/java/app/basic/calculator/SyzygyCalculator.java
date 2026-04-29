package app.basic.calculator;

import java.time.Instant;
import app.basic.BaseCalculator;
import app.model.basic.BasicSyzygy;
import app.model.data.Planet;
import app.model.data.SyzygyType;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class SyzygyCalculator extends BaseCalculator {


    private record SyzygyCandidate(SyzygyType type, double julianDay) {
    }

    protected void executeCalculation() {
        SyzygyCandidate newMoon = previousSyzygyCandidate(ctx.getFullJulianDay(), 0.0, SyzygyType.NEW_MOON);
        SyzygyCandidate fullMoon = previousSyzygyCandidate(ctx.getFullJulianDay(), 180.0, SyzygyType.FULL_MOON);
        SyzygyCandidate syzygy = newMoon.julianDay() > fullMoon.julianDay() ? newMoon : fullMoon;
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, syzygy.julianDay());
        double moonLongitude = ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, syzygy.julianDay());
        double syzygyLongitude = syzygy.type() == SyzygyType.FULL_MOON ? moonLongitude : sunLongitude;
        double natalAscendant = ctx.normalize(ctx.getAscmc()[0]);
        BasicSyzygy syzygyResult = new BasicSyzygy(syzygy.type(), ctx.round(syzygy.julianDay()), instantFromJulianDay(syzygy.julianDay()), ctx.round(sunLongitude), ctx.round(moonLongitude), ctx.round(ctx.rawAngularSeparation(sunLongitude, moonLongitude)), ctx.signOf(syzygyLongitude),
                ctx.round(ctx.degreeInSign(syzygyLongitude)), ctx.houseOf(syzygyLongitude, natalAscendant), ctx.signOf(sunLongitude), ctx.signOf(moonLongitude));

        basicChart.setSyzygy(syzygyResult);
    }

    private SyzygyCandidate previousSyzygyCandidate(double birthJulianDay, double targetSeparation, SyzygyType type) {
        double step = 0.25;
        double laterJulianDay = birthJulianDay;
        double laterValue = syzygySignedDelta(laterJulianDay, targetSeparation);
        for (double earlierJulianDay = birthJulianDay - step; earlierJulianDay >= birthJulianDay - 30.0; earlierJulianDay -= step) {
            double earlierValue = syzygySignedDelta(earlierJulianDay, targetSeparation);
            if (earlierValue == 0.0 || (Math.signum(earlierValue) != Math.signum(laterValue) && Math.abs(earlierValue - laterValue) < 90.0)) {
                return new SyzygyCandidate(type, refineSyzygy(earlierJulianDay, laterJulianDay, targetSeparation));
            }
            laterJulianDay = earlierJulianDay;
            laterValue = earlierValue;
        }
        Logger.instance.error(ctx.getInput(), "Could not find previous " + type + " syzygy within 30 days");
        return new SyzygyCandidate(type, birthJulianDay - step);
    }

    private double refineSyzygy(double earlierJulianDay, double laterJulianDay, double targetSeparation) {
        double low = earlierJulianDay;
        double high = laterJulianDay;
        double lowValue = syzygySignedDelta(low, targetSeparation);
        for (int i = 0; i < 50; i++) {
            double middle = (low + high) / 2.0;
            double middleValue = syzygySignedDelta(middle, targetSeparation);
            if (Math.signum(lowValue) == Math.signum(middleValue)) {
                low = middle;
                lowValue = middleValue;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2.0;
    }

    private double syzygySignedDelta(double julianDay, double targetSeparation) {
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, julianDay);
        double moonLongitude = ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, julianDay);
        double value = ctx.normalize(moonLongitude - sunLongitude - targetSeparation);
        return value > 180.0 ? value - 360.0 : value;
    }

    private Instant instantFromJulianDay(double julianDay) {
        long epochSecond = Math.round((julianDay - 2440587.5) * 86400.0);
        return Instant.ofEpochSecond(epochSecond);
    }
}
