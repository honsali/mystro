package app.descriptive.ptolemy.calculator;

import app.basic.CalculationContext;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.descriptive.common.calculator.SyzygyCalculator;

public final class PtolemyDescriptiveCalculator {
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final PtolemyAspectCalculator aspectCalculator = new PtolemyAspectCalculator();
    private final PtolemyDignityCalculator dignityCalculator;

    public PtolemyDescriptiveCalculator(Triplicity triplicity) {
        this.dignityCalculator = new PtolemyDignityCalculator(triplicity);
    }

    public void calculate(CalculationContext ctx, NatalChart chart) {
        chart.setSyzygy(syzygyCalculator.calculate(ctx));
        chart.applyAspects(aspectCalculator.calculate(chart));
        chart.applyDignityAssessments(dignityCalculator.calculate(chart));
    }
}
