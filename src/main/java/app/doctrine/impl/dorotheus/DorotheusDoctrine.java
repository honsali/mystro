package app.doctrine.impl.dorotheus;

import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.doctrine.Doctrine;
import app.basic.data.HouseSystem;
import app.basic.data.NodeType;
import app.basic.data.Terms;
import app.basic.data.Triplicity;
import app.basic.data.Zodiac;

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
