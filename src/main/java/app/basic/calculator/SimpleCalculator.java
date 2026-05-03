package app.basic.calculator;

import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.BasicChart;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class SimpleCalculator implements Calculator {


    public void calculate(BasicChart basicChart, CalculationContext ctx) {

        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(ctx.getFullJulianDay(), SweConst.SE_ECL_NUT, 0, values, error);
        if (result < 0 || Double.isNaN(values[0])) {
            Logger.instance.error(ctx.getSubject().getId(), "Swiss Ephemeris failed to calculate obliquity: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }

        double julianDayUt = ctx.getFullJulianDay();
        double deltaTSeconds = ctx.getSwissEph().swe_deltat(julianDayUt) * 86400.0;

        basicChart.setResolvedUtcInstant(ctx.getSubject().getResolvedUtcInstant());
        basicChart.setJulianDayUt(julianDayUt);
        basicChart.setJulianDayTt(julianDayUt + deltaTSeconds / 86400.0);
        basicChart.setDeltaTSeconds(deltaTSeconds);
        basicChart.setArmc(ctx.getArmc());
        basicChart.setLocalApparentSiderealTimeHours(ctx.getArmc() / 15.0);
        basicChart.setTrueObliquity(values[0]);
        basicChart.setMeanObliquity(values[1]);
        basicChart.setNutationLongitude(values[2]);
        basicChart.setNutationObliquity(values[3]);
    }


}
