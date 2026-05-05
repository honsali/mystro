package app.basic;

import app.basic.calculator.AngleCalculator;
import app.basic.calculator.ChartPointCalculator;
import app.basic.calculator.HouseCalculator;
import app.basic.calculator.MoonPhaseCalculator;
import app.basic.calculator.PlanetCalculator;
import app.basic.calculator.PlanetSectInjectionCalculator;
import app.basic.calculator.PointCalculator;
import app.basic.calculator.SectCalculator;
import app.basic.calculator.SimpleCalculator;
import app.basic.calculator.SolarPhaseCalculator;
import app.chart.model.NatalChart;

public final class BasicCalculator {

    public NatalChart calculate(CalculationContext ctx) {
        NatalChart natalChart = new NatalChart();

        // Ordering is intentional and dependency-bearing:
        // Simple -> chart metadata/JD; Planet -> planet positions; House -> cusps; Angle -> angles.
        // Sect requires planets. Point requires planets + angles + sect for ruler selection.
        // Pairwise and solar phase require populated points/positions. Planet sect injection requires points + sect.
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
