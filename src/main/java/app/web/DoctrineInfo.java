package app.web;

import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;
import app.doctrine.Doctrine;

public final class DoctrineInfo {

    private final String id;
    private final String name;
    private final HouseSystem houseSystem;
    private final Zodiac zodiac;
    private final Terms terms;
    private final Triplicity triplicity;
    private final NodeType nodeType;

    public DoctrineInfo(Doctrine doctrine) {
        this.id = doctrine.getId();
        this.name = doctrine.getName();
        this.houseSystem = doctrine.getHouseSystem();
        this.zodiac = doctrine.getZodiac();
        this.terms = doctrine.getTerms();
        this.triplicity = doctrine.getTriplicity();
        this.nodeType = doctrine.getNodeType();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public HouseSystem getHouseSystem() { return houseSystem; }
    public Zodiac getZodiac() { return zodiac; }
    public Terms getTerms() { return terms; }
    public Triplicity getTriplicity() { return triplicity; }
    public NodeType getNodeType() { return nodeType; }
}
