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
import app.basic.model.NatalChart;

public final class BasicCalculator {


    public NatalChart calculate(CalculationContext ctx) {
        NatalChart natalChart = new NatalChart();

        (new SimpleCalculator()).calculate(natalChart, ctx);
        (new PlanetCalculator()).calculate(natalChart, ctx);
        (new HouseCalculator()).calculate(natalChart, ctx);
        (new AngleCalculator()).calculate(natalChart, ctx);
        (new SectCalculator()).calculate(natalChart, ctx);
        (new PointCalculator()).calculate(natalChart, ctx);
        (new ChartPointCalculator()).calculate(natalChart, ctx);
        (new SolarPhaseCalculator()).calculate(natalChart, ctx);
        natalChart.applyPlanetSects();
        (new MoonPhaseCalculator()).calculate(natalChart, ctx);


        return natalChart;
    }



}
