package app.chart.model;

import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;

public interface CalculationDefinition {

    String getId();

    String getName();

    HouseSystem getHouseSystem();

    Zodiac getZodiac();

    Terms getTerms();

    Triplicity getTriplicity();

    NodeType getNodeType();
}
