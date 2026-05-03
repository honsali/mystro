package app.basic.model;

import app.basic.data.HouseSystem;
import app.basic.data.NodeType;
import app.basic.data.Terms;
import app.basic.data.Triplicity;
import app.basic.data.Zodiac;

public interface CalculationDefinition {

    String getId();

    String getName();

    HouseSystem getHouseSystem();

    Zodiac getZodiac();

    Terms getTerms();

    Triplicity getTriplicity();

    NodeType getNodeType();
}
