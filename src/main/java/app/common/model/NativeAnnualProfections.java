package app.common.model;

import java.util.ArrayList;
import java.util.List;

public final class NativeAnnualProfections {
    private List<NativeAnnualProfectionEntry> years;

    public NativeAnnualProfections() {
        this.years = new ArrayList<>();
    }

    public NativeAnnualProfections(List<NativeAnnualProfectionEntry> years) {
        this.years = years == null ? new ArrayList<>() : new ArrayList<>(years);
    }

    public List<NativeAnnualProfectionEntry> years() {
        if (years == null) {
            years = new ArrayList<>();
        }
        return years;
    }

    public void years(List<NativeAnnualProfectionEntry> years) {
        this.years = years == null ? new ArrayList<>() : new ArrayList<>(years);
    }

    public List<NativeAnnualProfectionEntry> getYears() { return years(); }
    public void setYears(List<NativeAnnualProfectionEntry> years) { years(years); }
}
