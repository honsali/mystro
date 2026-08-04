package app.reading;

import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;

public final class CoreDoctrineInfo {

    private final String id;
    private final String name;
    private final HouseSystem houseSystem;
    private final Terms terms;
    private final Triplicity triplicity;

    public CoreDoctrineInfo(String id, String name, HouseSystem houseSystem, Terms terms, Triplicity triplicity) {
        this.id = id;
        this.name = name;
        this.houseSystem = houseSystem;
        this.terms = terms;
        this.triplicity = triplicity;
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

    public Terms getTerms() {
        return terms;
    }

    public Triplicity getTriplicity() {
        return triplicity;
    }
}
