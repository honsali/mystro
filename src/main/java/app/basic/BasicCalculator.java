package app.basic;

import app.basic.calculator.AngleCalculator;
import app.basic.calculator.ChartPointCalculator;
import app.basic.calculator.HouseCalculator;
import app.basic.calculator.MoonPhaseCalculator;
import app.basic.calculator.PlanetCalculator;
import app.basic.calculator.PointCalculator;
import app.basic.calculator.SectCalculator;
import app.basic.calculator.SimpleCalculator;
import app.basic.calculator.SolarPhaseCalculator;
import app.basic.model.BasicChart;
import app.input.model.Input;

public final class BasicCalculator {



    public BasicChart calculate(Input input) {
        return calculate(new BasicCalculationContext(input));
    }

    public BasicChart calculate(BasicCalculationContext ctx) {
        BasicChart basicChart = new BasicChart();

        (new SimpleCalculator()).calculate(basicChart, ctx);
        (new PlanetCalculator()).calculate(basicChart, ctx);
        (new HouseCalculator()).calculate(basicChart, ctx);
        (new AngleCalculator()).calculate(basicChart, ctx);
        (new PointCalculator()).calculate(basicChart, ctx);
        (new ChartPointCalculator()).calculate(basicChart, ctx);
        (new SolarPhaseCalculator()).calculate(basicChart, ctx);
        (new MoonPhaseCalculator()).calculate(basicChart, ctx);
        (new SectCalculator()).calculate(basicChart, ctx);


        return basicChart;
    }



}
