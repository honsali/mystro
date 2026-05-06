package app.basic.calculator;

import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class SimpleCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(ctx.getFullJulianDay(), SweConst.SE_ECL_NUT, 0, values, error);
        if (result < 0 || Double.isNaN(values[0])) {
            Logger.instance.error(ctx.getSubject().getId(), "Swiss Ephemeris failed to calculate obliquity: " + error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }

        double julianDayUt = ctx.getFullJulianDay();
        double deltaTSeconds = ctx.getSwissEph().swe_deltat(julianDayUt) * 86400.0;

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


}
