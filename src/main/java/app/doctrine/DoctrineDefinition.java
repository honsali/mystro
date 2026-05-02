package app.doctrine;

import app.model.data.HouseSystem;
import app.model.data.NodeType;
import app.model.data.Terms;
import app.model.data.Triplicity;
import app.model.data.Zodiac;

public interface DoctrineDefinition {

    String getId();

    String getName();

    HouseSystem getHouseSystem();

    Zodiac getZodiac();

    Terms getTerms();

    Triplicity getTriplicity();

    NodeType getNodeType();
}
