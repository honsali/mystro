package app.doctrine;

import app.basic.BasicCalculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.basic.model.CalculationDefinition;
import app.input.model.CalculationSetting;
import app.input.model.Subject;

public interface Doctrine extends CalculationDefinition {

    default NatalChart calculateDescriptive(Subject subject, CalculationSetting calculationSetting, BasicCalculator basicCalculator) {
        CalculationContext ctx = new CalculationContext(subject, this, calculationSetting);
        NatalChart natalChart = calculateNatalChart(ctx, basicCalculator);
        describe(ctx, natalChart);
        return natalChart;
    }

    default NatalChart calculateNatalChart(CalculationContext ctx, BasicCalculator basicCalculator) {
        return basicCalculator.calculate(ctx);
    }

    void describe(CalculationContext ctx, NatalChart chart);
}
