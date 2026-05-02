package app.doctrine.impl.valens;

import app.basic.BasicCalculationContext;
import app.basic.model.BasicChart;
import app.descriptive.valens.calculator.ValensDescriptiveCalculator;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.basic.data.HouseSystem;
import app.basic.data.NodeType;
import app.basic.data.Terms;
import app.basic.data.Triplicity;
import app.basic.data.Zodiac;

public final class ValensDoctrine implements Doctrine {
    private final ValensDescriptiveCalculator descriptiveCalculator = new ValensDescriptiveCalculator(Triplicity.DOROTHEAN);

    @Override
    public String getId() {
        return "valens";
    }

    @Override
    public String getName() {
        return "Valens";
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
        return Terms.EGYPTIAN;
    }

    @Override
    public Triplicity getTriplicity() {
        return Triplicity.DOROTHEAN;
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
