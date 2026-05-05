package app.doctrine.impl.ptolemy;

import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.descriptive.ptolemy.calculator.PtolemyDescriptiveCalculator;
import app.doctrine.Doctrine;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;

public final class PtolemyDoctrine implements Doctrine {
    private final PtolemyDescriptiveCalculator descriptiveCalculator = new PtolemyDescriptiveCalculator(Triplicity.PTOLEMAIC);

    @Override
    public String getId() {
        return "ptolemy";
    }

    @Override
    public String getName() {
        return "Ptolemy";
    }

    @Override
    public HouseSystem getHouseSystem() {
        return HouseSystem.WHOLE_SIGN;
    }

    @Override
    public Zodiac getZodiac() {
        return Zodiac.TROPICAL;
    }

    @Override
    public Terms getTerms() {
        return Terms.PTOLEMAIC;
    }

    @Override
    public Triplicity getTriplicity() {
        return Triplicity.PTOLEMAIC;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MEAN;
    }

    @Override
    public void describe(CalculationContext ctx, NatalChart chart) {
        descriptiveCalculator.calculate(ctx, chart);
    }
}
