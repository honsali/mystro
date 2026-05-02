package app.descriptive.ptolemy.calculator;

import app.basic.BasicCalculationContext;
import app.basic.data.Triplicity;
import app.basic.model.BasicChart;
import app.descriptive.common.calculator.SyzygyCalculator;
import app.descriptive.ptolemy.model.PtolemyDescriptiveData;

public final class PtolemyDescriptiveCalculator {
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final PtolemyAspectCalculator aspectCalculator = new PtolemyAspectCalculator();
    private final PtolemyDignityCalculator dignityCalculator;

    public PtolemyDescriptiveCalculator(Triplicity triplicity) {
        this.dignityCalculator = new PtolemyDignityCalculator(triplicity);
    }

    public PtolemyDescriptiveData calculate(BasicCalculationContext ctx, BasicChart chart) {
        return new PtolemyDescriptiveData(
                syzygyCalculator.calculate(ctx),
                aspectCalculator.calculate(chart),
                dignityCalculator.calculate(chart)
        );
    }
}
