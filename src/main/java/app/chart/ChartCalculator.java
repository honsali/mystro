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
import app.chart.model.Chart;

public final class ChartCalculator {

    public Chart calculate(CalculationContext ctx) {
        Chart chart = new Chart();

        // Ordering is intentional and dependency-bearing:
        // Simple -> chart metadata/JD; Planet -> planet positions; House -> cusps; Angle -> angles.
        // Sect requires planets. Point requires planets + angles + sect for ruler selection.
        // Pairwise and solar phase require populated points/positions. Planet sect injection requires
        // points + sect.
        // Moon phase requires Sun/Moon positions.
        (new SimpleCalculator()).calculate(chart, ctx);
        (new PlanetCalculator()).calculate(chart, ctx);
        (new HouseCalculator()).calculate(chart, ctx);
        (new AngleCalculator()).calculate(chart, ctx);
        (new SectCalculator()).calculate(chart, ctx);
        (new PointCalculator()).calculate(chart, ctx);
        (new ChartPointCalculator()).calculate(chart, ctx);
        (new SolarPhaseCalculator()).calculate(chart, ctx);
        (new PlanetSectInjectionCalculator()).calculate(chart, ctx);
        (new MoonPhaseCalculator()).calculate(chart, ctx);

        return chart;
    }
}
