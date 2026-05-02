package app.descriptive.valens.calculator;

import app.basic.BasicCalculationContext;
import app.basic.data.Triplicity;
import app.basic.model.BasicChart;
import app.descriptive.common.calculator.SyzygyCalculator;
import app.descriptive.valens.model.ValensDescriptiveData;

public final class ValensDescriptiveCalculator {
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final ValensLotCalculator lotCalculator = new ValensLotCalculator();
    private final ValensAspectCalculator aspectCalculator = new ValensAspectCalculator();
    private final ValensDignityCalculator dignityCalculator;
    private final ValensSolarConditionCalculator solarConditionCalculator = new ValensSolarConditionCalculator();

    public ValensDescriptiveCalculator(Triplicity triplicity) {
        this.dignityCalculator = new ValensDignityCalculator(triplicity);
    }

    public ValensDescriptiveData calculate(BasicCalculationContext ctx, BasicChart chart) {
        return new ValensDescriptiveData(
                syzygyCalculator.calculate(ctx),
                lotCalculator.calculate(chart),
                aspectCalculator.calculate(chart),
                dignityCalculator.calculate(chart),
                solarConditionCalculator.calculate(chart)
        );
    }
}
