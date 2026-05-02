package app.doctrine.impl.dorotheus;

import java.util.Map;
import app.basic.model.BasicChart;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.doctrine.SimpleDescriptiveResult;
import app.basic.data.HouseSystem;
import app.basic.data.NodeType;
import app.basic.data.Terms;
import app.basic.data.Triplicity;
import app.basic.data.Zodiac;
import app.input.model.Input;

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
    public DescriptiveResult describe(Input input, BasicChart chart) {
        return new SimpleDescriptiveResult(getId(), Map.of());
    }
}
