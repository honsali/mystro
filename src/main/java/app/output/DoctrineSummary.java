package app.output;

import app.model.data.HouseSystem;
import app.model.data.Terms;
import app.model.data.Zodiac;

public final class DoctrineSummary {
    private final String id;
    private final HouseSystem houseSystem;
    private final Zodiac zodiac;
    private final Terms terms;

    public DoctrineSummary(String id, HouseSystem houseSystem, Zodiac zodiac, Terms terms) {
        this.id = id;
        this.houseSystem = houseSystem;
        this.zodiac = zodiac;
        this.terms = terms;
    }

    public String getId() { return id; }
    public HouseSystem getHouseSystem() { return houseSystem; }
    public Zodiac getZodiac() { return zodiac; }
    public Terms getTerms() { return terms; }
}
