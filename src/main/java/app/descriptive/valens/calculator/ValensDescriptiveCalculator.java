package app.descriptive.valens.calculator;

import app.basic.CalculationContext;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.descriptive.common.calculator.SyzygyCalculator;

public final class ValensDescriptiveCalculator {
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final ValensLotCalculator lotCalculator = new ValensLotCalculator();
    private final ValensAspectCalculator aspectCalculator = new ValensAspectCalculator();
    private final ValensDignityCalculator dignityCalculator;
    private final ValensSolarConditionCalculator solarConditionCalculator = new ValensSolarConditionCalculator();

    public ValensDescriptiveCalculator(Triplicity triplicity) {
        this.dignityCalculator = new ValensDignityCalculator(triplicity);
    }

    public void calculate(CalculationContext ctx, NatalChart chart) {
        chart.setSyzygy(syzygyCalculator.calculate(ctx));
        chart.setLots(lotCalculator.calculate(chart));
        chart.applyAspects(aspectCalculator.calculate(chart));
        chart.applyDignityAssessments(dignityCalculator.calculate(chart));
        chart.applySolarConditions(solarConditionCalculator.calculate(chart));
    }
}
