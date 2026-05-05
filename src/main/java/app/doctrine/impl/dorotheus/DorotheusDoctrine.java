package app.doctrine.impl.dorotheus;

import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.doctrine.Doctrine;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;

public final class DorotheusDoctrine implements Doctrine {
    @Override
    public String getId() {
        return "dorotheus";
    }

    @Override
    public String getName() {
        return "Dorotheus";
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
    public void describe(CalculationContext ctx, NatalChart chart) {
    }
}
