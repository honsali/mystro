package app.doctrine.impl.ptolemy;

import app.basic.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;
import app.chart.model.NatalChart;
import app.descriptive.ptolemy.calculator.PtolemyDescriptiveCalculator;
import app.doctrine.Doctrine;
import app.input.model.DoctrineInfo;

public final class PtolemyDoctrine extends Doctrine {

    private final DoctrineInfo doctrineInfo = new DoctrineInfo("ptolemy", "Ptolemy", HouseSystem.WHOLE_SIGN, Zodiac.TROPICAL, Terms.PTOLEMAIC, Triplicity.PTOLEMAIC, NodeType.MEAN);

    private final PtolemyDescriptiveCalculator descriptiveCalculator = new PtolemyDescriptiveCalculator(Triplicity.PTOLEMAIC);

    @Override
    public DoctrineInfo getDoctrineInfo() {
        return doctrineInfo;
    }

    @Override
    public void describe(CalculationContext ctx, NatalChart chart) {
        descriptiveCalculator.calculate(ctx, chart);
    }
}
