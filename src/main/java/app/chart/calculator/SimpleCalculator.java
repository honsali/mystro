package app.chart.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.model.NatalChart;
import app.ephemeris.SweConst;

public class SimpleCalculator implements Calculator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCalculator.class);

    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(ctx.getFullJulianDay(), SweConst.SE_ECL_NUT, 0, values, error);
        if (result < 0 || hasInvalidEclipticNutationValues(values)) {
            LOG.error("subject={} Swiss Ephemeris failed to calculate obliquity/nutation: {}", ctx.getSubject().getId(), error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }

        double julianDayUt = ctx.getFullJulianDay();
        double deltaTSeconds = ctx.getSwissEph().swe_deltat(julianDayUt) * 86400.0;
        if (!Double.isFinite(deltaTSeconds)) {
            LOG.error("subject={} Swiss Ephemeris returned invalid delta T", ctx.getSubject().getId());
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }

        natalChart.setResolvedUtcInstant(ctx.getSubject().getResolvedUtcInstant());
        natalChart.setJulianDayUt(julianDayUt);
        natalChart.setJulianDayTt(julianDayUt + deltaTSeconds / 86400.0);
        natalChart.setDeltaTSeconds(deltaTSeconds);
        natalChart.setArmc(ctx.getArmc());
        natalChart.setLocalApparentSiderealTimeHours(ctx.getArmc() / 15.0);
        natalChart.setTrueObliquity(values[0]);
        natalChart.setMeanObliquity(values[1]);
        natalChart.setNutationLongitude(values[2]);
        natalChart.setNutationObliquity(values[3]);
    }

    private boolean hasInvalidEclipticNutationValues(double[] values) {
        return !Double.isFinite(values[0]) || !Double.isFinite(values[1]) || !Double.isFinite(values[2]) || !Double.isFinite(values[3]);
    }
}
