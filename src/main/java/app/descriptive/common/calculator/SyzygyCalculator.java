package app.descriptive.common.calculator;

import java.time.Instant;
import app.basic.BasicCalculationContext;
import app.basic.data.Planet;
import app.basic.data.SyzygyType;
import app.descriptive.common.model.PrenatalSyzygyEntry;
import app.output.Logger;
import app.swisseph.core.SweConst;

/**
 * Shared doctrine-layer helper for finding the prenatal lunation.
 *
 * <p>The event search itself is astronomical geometry. The selected point of the
 * syzygy is doctrine-sensitive, so doctrines can subclass and override
 * {@link #syzygyLongitude(SyzygyType, double, double, double, BasicCalculationContext)}.
 */
public class SyzygyCalculator {

    private record SyzygyCandidate(SyzygyType type, double julianDay) {
    }

    public PrenatalSyzygyEntry calculate(BasicCalculationContext ctx) {
        SyzygyCandidate syzygy = previousSyzygyCandidate(ctx.getFullJulianDay(), ctx);
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, syzygy.julianDay());
        double moonLongitude = ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, syzygy.julianDay());
        double syzygyLongitude = syzygyLongitude(syzygy.type(), sunLongitude, moonLongitude, syzygy.julianDay(), ctx);
        double natalAscendant = ctx.normalize(ctx.getAscmc()[0]);
        return new PrenatalSyzygyEntry(syzygy.type(), syzygy.julianDay(), instantFromJulianDay(syzygy.julianDay()), syzygyLongitude, ctx.signOf(syzygyLongitude),
                ctx.degreeInSign(syzygyLongitude), ctx.houseOf(syzygyLongitude, natalAscendant), sunLongitude, moonLongitude, ctx.rawAngularSeparation(sunLongitude, moonLongitude), ctx.signOf(sunLongitude),
                ctx.signOf(moonLongitude));
    }

    /**
     * Default Hellenistic selection: conjunction uses the shared Sun/Moon
     * longitude; opposition uses the Moon's longitude.
     */
    protected double syzygyLongitude(SyzygyType type, double sunLongitude, double moonLongitude, double julianDay, BasicCalculationContext ctx) {
        return type == SyzygyType.FULL_MOON ? moonLongitude : sunLongitude;
    }

    private SyzygyCandidate previousSyzygyCandidate(double birthJulianDay, BasicCalculationContext ctx) {
        double step = 0.25;
        double maximumLookbackDays = 35.0;
        double laterJulianDay = birthJulianDay;
        double laterElongation = lunarElongation(laterJulianDay, ctx);

        for (double earlierJulianDay = birthJulianDay - step; earlierJulianDay >= birthJulianDay - maximumLookbackDays; earlierJulianDay -= step) {
            double earlierElongation = unwrapBackward(lunarElongation(earlierJulianDay, ctx), laterElongation);
            Double targetElongation = crossedSyzygyElongation(earlierElongation, laterElongation, laterJulianDay == birthJulianDay);
            if (targetElongation != null) {
                return new SyzygyCandidate(syzygyTypeFor(targetElongation), refineSyzygy(earlierJulianDay, laterJulianDay, targetElongation, ctx));
            }
            laterJulianDay = earlierJulianDay;
            laterElongation = earlierElongation;
        }
        Logger.instance.error(ctx.getInput(), "Could not find previous syzygy within " + maximumLookbackDays + " days");
        throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
    }

    private Double crossedSyzygyElongation(double earlierElongation, double laterElongation, boolean initialInterval) {
        double targetElongation = Math.floor(laterElongation / 180.0) * 180.0;
        if (initialInterval && Math.abs(laterElongation - targetElongation) < 1e-9) {
            targetElongation -= 180.0;
        }
        return targetElongation >= earlierElongation && targetElongation <= laterElongation ? targetElongation : null;
    }

    private double refineSyzygy(double earlierJulianDay, double laterJulianDay, double targetElongation, BasicCalculationContext ctx) {
        double low = earlierJulianDay;
        double high = laterJulianDay;
        double lowValue = unwrappedLunarElongation(low, targetElongation, ctx) - targetElongation;
        double highValue = unwrappedLunarElongation(high, targetElongation, ctx) - targetElongation;
        if (Math.abs(lowValue) < 1e-12) {
            return low;
        }
        if (Math.abs(highValue) < 1e-12) {
            return high;
        }
        for (int i = 0; i < 60; i++) {
            double middle = (low + high) / 2.0;
            double middleValue = unwrappedLunarElongation(middle, targetElongation, ctx) - targetElongation;
            if (middleValue < 0.0) {
                low = middle;
                lowValue = middleValue;
            } else {
                high = middle;
                highValue = middleValue;
            }
        }
        return Math.abs(lowValue) < Math.abs(highValue) ? low : high;
    }

    private SyzygyType syzygyTypeFor(double targetElongation) {
        long halfCycle = Math.round(targetElongation / 180.0);
        return Math.floorMod(halfCycle, 2) == 0 ? SyzygyType.NEW_MOON : SyzygyType.FULL_MOON;
    }

    private double unwrappedLunarElongation(double julianDay, double referenceElongation, BasicCalculationContext ctx) {
        double elongation = lunarElongation(julianDay, ctx);
        while (elongation - referenceElongation > 180.0) {
            elongation -= 360.0;
        }
        while (elongation - referenceElongation <= -180.0) {
            elongation += 360.0;
        }
        return elongation;
    }

    private double unwrapBackward(double earlierElongation, double laterElongation) {
        while (earlierElongation > laterElongation) {
            earlierElongation -= 360.0;
        }
        return earlierElongation;
    }

    private double lunarElongation(double julianDay, BasicCalculationContext ctx) {
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, julianDay);
        double moonLongitude = ctx.longitudeFor(Planet.MOON, SweConst.SE_MOON, julianDay);
        return ctx.normalize(moonLongitude - sunLongitude);
    }

    private Instant instantFromJulianDay(double julianDay) {
        long epochSecond = Math.round((julianDay - 2440587.5) * 86400.0);
        return Instant.ofEpochSecond(epochSecond);
    }
}
