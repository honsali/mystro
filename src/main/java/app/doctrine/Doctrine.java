package app.doctrine;

import app.basic.BasicCalculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.input.model.DoctrineInfo;
import app.input.model.Subject;

public abstract class Doctrine {

    public NatalChart calculateNatalChart(CalculationContext ctx, BasicCalculator basicCalculator) {
        return basicCalculator.calculate(ctx);
    }

    public abstract void describe(CalculationContext ctx, NatalChart chart);

    public abstract DoctrineInfo getDoctrineInfo();


    public NatalChart calculateDescriptive(Subject subject, BasicCalculator basicCalculator) {
        CalculationContext ctx = new CalculationContext(subject, getDoctrineInfo());
        NatalChart natalChart = calculateNatalChart(ctx, basicCalculator);
        describe(ctx, natalChart);
        return natalChart;
    }
}
