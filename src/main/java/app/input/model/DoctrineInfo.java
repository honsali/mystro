package app.input.model;

import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;

public final class DoctrineInfo {

    private final String id;
    private final String name;
    private final HouseSystem houseSystem;
    private final Zodiac zodiac;
    private final Terms terms;
    private final Triplicity triplicity;
    private final NodeType nodeType;



    public DoctrineInfo(String id, String name, HouseSystem houseSystem, Zodiac zodiac, Terms terms, Triplicity triplicity, NodeType nodeType) {
        this.id = id;
        this.name = name;
        this.houseSystem = houseSystem;
        this.zodiac = zodiac;
        this.terms = terms;
        this.triplicity = triplicity;
        this.nodeType = nodeType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public HouseSystem getHouseSystem() {
        return houseSystem;
    }

    public Zodiac getZodiac() {
        return zodiac;
    }

    public Terms getTerms() {
        return terms;
    }

    public Triplicity getTriplicity() {
        return triplicity;
    }

    public NodeType getNodeType() {
        return nodeType;
    }
}
