package app.chart.search;

import java.util.List;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.Planet;
import app.chart.data.SyzygyType;
import app.ephemeris.SweConst;

/**
 * Shared astronomical search for exact Sun-Moon conjunctions and oppositions.
 *
 * <p>The search is independent of astrological doctrine. Callers remain responsible for choosing
 * which longitude represents the syzygy and for assigning it to a natal house.</p>
 */
public final class SyzygyEventSearch {

    private static final double SYNODIC_HALF_MONTH_DAYS = 14.765294;
    private static final double SCAN_STEP_DAYS = 0.25;
    private static final double MAXIMUM_LOOKBACK_DAYS = 35.0;
    private static final double ROOT_DEGREE_TOLERANCE = 1.0e-9;
    private static final int BISECTION_STEPS = 80;
    private static final List<Double> SEARCH_RADII_DAYS = List.of(2.0, 4.0, 8.0);

    public SyzygyEvent previous(double startJulianDay, CalculationContext context) {
        double laterJulianDay = startJulianDay;
        double laterElongation = lunarElongation(laterJulianDay, context);

        for (double earlierJulianDay = startJulianDay - SCAN_STEP_DAYS;
             earlierJulianDay >= startJulianDay - MAXIMUM_LOOKBACK_DAYS;
             earlierJulianDay -= SCAN_STEP_DAYS) {
            double earlierElongation = unwrapBackward(
                    lunarElongation(earlierJulianDay, context),
                    laterElongation);
            Double targetElongation = crossedTarget(
                    earlierElongation,
                    laterElongation,
                    laterJulianDay == startJulianDay);
            if (targetElongation != null) {
                return event(
                        targetElongation,
                        refine(earlierJulianDay, laterJulianDay, targetElongation, context));
            }
            laterJulianDay = earlierJulianDay;
            laterElongation = earlierElongation;
        }
        throw new IllegalArgumentException(
                "Could not find previous syzygy within " + MAXIMUM_LOOKBACK_DAYS + " days");
    }

    public SyzygyEvent next(SyzygyEvent current, CalculationContext context) {
        double targetElongation = current.targetElongation() + 180.0;
        double approximateJulianDay = current.julianDay() + SYNODIC_HALF_MONTH_DAYS;
        for (double radiusDays : SEARCH_RADII_DAYS) {
            double low = approximateJulianDay - radiusDays;
            double high = approximateJulianDay + radiusDays;
            double lowValue = difference(low, targetElongation, context);
            double highValue = difference(high, targetElongation, context);
            if (Math.abs(lowValue) <= ROOT_DEGREE_TOLERANCE) {
                return event(targetElongation, low);
            }
            if (Math.abs(highValue) <= ROOT_DEGREE_TOLERANCE) {
                return event(targetElongation, high);
            }
            if (lowValue <= 0.0 && highValue >= 0.0) {
                return event(targetElongation, refine(low, high, targetElongation, context));
            }
        }
        throw new IllegalArgumentException(
                "Could not bracket syzygy target elongation " + targetElongation);
    }

    private SyzygyEvent event(double targetElongation, double julianDay) {
        return new SyzygyEvent(typeFor(targetElongation), targetElongation, julianDay);
    }

    private Double crossedTarget(double earlierElongation, double laterElongation, boolean initialInterval) {
        double targetElongation = Math.floor(laterElongation / 180.0) * 180.0;
        if (initialInterval && Math.abs(laterElongation - targetElongation) < ROOT_DEGREE_TOLERANCE) {
            targetElongation -= 180.0;
        }
        return targetElongation >= earlierElongation && targetElongation <= laterElongation
                ? targetElongation
                : null;
    }

    private double refine(double lowJulianDay, double highJulianDay, double targetElongation,
                          CalculationContext context) {
        double low = lowJulianDay;
        double high = highJulianDay;
        double lowValue = difference(low, targetElongation, context);
        double highValue = difference(high, targetElongation, context);
        if (Math.abs(lowValue) <= ROOT_DEGREE_TOLERANCE) {
            return low;
        }
        if (Math.abs(highValue) <= ROOT_DEGREE_TOLERANCE) {
            return high;
        }
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double middle = (low + high) / 2.0;
            double middleValue = difference(middle, targetElongation, context);
            if (Math.abs(middleValue) <= ROOT_DEGREE_TOLERANCE) {
                return middle;
            }
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

    private double difference(double julianDay, double targetElongation, CalculationContext context) {
        return unwrappedLunarElongation(julianDay, targetElongation, context) - targetElongation;
    }

    private double unwrappedLunarElongation(double julianDay, double referenceElongation,
                                            CalculationContext context) {
        double elongation = lunarElongation(julianDay, context);
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

    private double lunarElongation(double julianDay, CalculationContext context) {
        double sunLongitude = context.longitudeFor(Planet.SUN, SweConst.SE_SUN, julianDay);
        double moonLongitude = context.longitudeFor(Planet.MOON, SweConst.SE_MOON, julianDay);
        return AstroMath.normalize(moonLongitude - sunLongitude);
    }

    private SyzygyType typeFor(double targetElongation) {
        long halfCycle = Math.round(targetElongation / 180.0);
        return Math.floorMod(halfCycle, 2) == 0 ? SyzygyType.NEW_MOON : SyzygyType.FULL_MOON;
    }

    public record SyzygyEvent(SyzygyType type, double targetElongation, double julianDay) {
    }
}
