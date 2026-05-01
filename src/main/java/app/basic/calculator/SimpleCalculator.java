package app.basic.calculator;

import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class SimpleCalculator implements Calculator {


    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {

        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(ctx.getFullJulianDay(), SweConst.SE_ECL_NUT, 0, values, error);
        if (result < 0 || Double.isNaN(values[0])) {
            Logger.instance.error(ctx.getInput(), "Swiss Ephemeris failed to calculate obliquity: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }

        basicChart.setResolvedUtcInstant(ctx.getInput().getSubject().getResolvedUtcInstant());
        basicChart.setJulianDay(ctx.round(ctx.getFullJulianDay()));
        basicChart.setArmc(ctx.round(ctx.getArmc()));
        basicChart.setLocalSiderealTime(ctx.round(ctx.getArmc() / 15.0));
        basicChart.setObliquity(ctx.round(values[0]));
    }


}
