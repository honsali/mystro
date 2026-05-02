package app.doctrine.impl.ptolemy;

import app.basic.BasicCalculationContext;
import app.basic.model.BasicChart;
import app.descriptive.ptolemy.calculator.PtolemyDescriptiveCalculator;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.basic.data.HouseSystem;
import app.basic.data.NodeType;
import app.basic.data.Terms;
import app.basic.data.Triplicity;
import app.basic.data.Zodiac;

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
    public DescriptiveResult describe(BasicCalculationContext ctx, BasicChart chart) {
        return descriptiveCalculator.calculate(ctx, chart);
    }
}
