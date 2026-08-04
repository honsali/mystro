package app.chart;

import app.chart.calculator.AngleCalculator;
import app.chart.calculator.ChartPointCalculator;
import app.chart.calculator.HouseCalculator;
import app.chart.calculator.MoonPhaseCalculator;
import app.chart.calculator.PlanetCalculator;
import app.chart.calculator.PlanetSectInjectionCalculator;
import app.chart.calculator.PointCalculator;
import app.chart.calculator.SectCalculator;
import app.chart.calculator.SimpleCalculator;
import app.chart.calculator.SolarPhaseCalculator;
import app.chart.model.NatalChart;

public final class BasicCalculator {

    public NatalChart calculate(CalculationContext ctx) {
        NatalChart natalChart = new NatalChart();

        // Ordering is intentional and dependency-bearing:
        // Simple -> chart metadata/JD; Planet -> planet positions; House -> cusps; Angle -> angles.
        // Sect requires planets. Point requires planets + angles + sect for ruler selection.
        // Pairwise and solar phase require populated points/positions. Planet sect injection requires
        // points + sect.
        // Moon phase requires Sun/Moon positions.
        (new SimpleCalculator()).calculate(natalChart, ctx);
        (new PlanetCalculator()).calculate(natalChart, ctx);
        (new HouseCalculator()).calculate(natalChart, ctx);
        (new AngleCalculator()).calculate(natalChart, ctx);
        (new SectCalculator()).calculate(natalChart, ctx);
        (new PointCalculator()).calculate(natalChart, ctx);
        (new ChartPointCalculator()).calculate(natalChart, ctx);
        (new SolarPhaseCalculator()).calculate(natalChart, ctx);
        (new PlanetSectInjectionCalculator()).calculate(natalChart, ctx);
        (new MoonPhaseCalculator()).calculate(natalChart, ctx);

        return natalChart;
    }
}
