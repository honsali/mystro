package app.common.model;

import java.util.ArrayList;
import java.util.List;

public final class NativeLordOfOrb {
    private String startingRuler;
    private List<NativeLordOfOrbEntry> years;

    public NativeLordOfOrb() {
        this.years = new ArrayList<>();
    }

    public NativeLordOfOrb(String startingRuler, List<NativeLordOfOrbEntry> years) {
        this.startingRuler = startingRuler;
        this.years = years == null ? new ArrayList<>() : new ArrayList<>(years);
    }

    public String startingRuler() { return startingRuler; }
    public void startingRuler(String startingRuler) { this.startingRuler = startingRuler; }
    public String getStartingRuler() { return startingRuler; }
    public void setStartingRuler(String startingRuler) { this.startingRuler = startingRuler; }

    public List<NativeLordOfOrbEntry> years() {
        if (years == null) {
            years = new ArrayList<>();
        }
        return years;
    }

    public void years(List<NativeLordOfOrbEntry> years) {
        this.years = years == null ? new ArrayList<>() : new ArrayList<>(years);
    }

    public List<NativeLordOfOrbEntry> getYears() { return years(); }
    public void setYears(List<NativeLordOfOrbEntry> years) { years(years); }
}
