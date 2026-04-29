package app.doctrine.impl.ptolemy;

import java.util.Map;
import app.model.basic.BasicChart;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.doctrine.SimpleDescriptiveResult;
import app.model.data.HouseSystem;
import app.model.data.Terms;
import app.model.data.Zodiac;
import app.model.input.Input;

public final class PtolemyDoctrine implements Doctrine {
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
    public DescriptiveResult describe(Input input, BasicChart chart) {
        return new SimpleDescriptiveResult(getId(), Map.of());
    }
}
