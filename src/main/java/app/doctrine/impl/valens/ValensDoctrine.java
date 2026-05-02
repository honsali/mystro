package app.doctrine.impl.valens;

import java.util.Map;
import app.model.basic.BasicChart;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.doctrine.SimpleDescriptiveResult;
import app.model.data.HouseSystem;
import app.model.data.NodeType;
import app.model.data.Terms;
import app.model.data.Triplicity;
import app.model.data.Zodiac;
import app.model.input.Input;

public final class ValensDoctrine implements Doctrine {
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
    public DescriptiveResult describe(Input input, BasicChart chart) {
        return new SimpleDescriptiveResult(getId(), Map.of());
    }
}
