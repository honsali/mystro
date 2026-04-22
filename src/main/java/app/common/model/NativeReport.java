package app.common.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NativeReport {
    private String name;
    private NativeBirth birth;
    private NativePlanetaryHour planetaryHour;
    private NativeSyzygy syzygy;
    private NativeLordOfOrb lordOfOrb;
    private Map<String, NativeHermeticLot> lots;
    private Map<String, ChartPoint> planets;
    private Map<String, ChartPoint> houses;
    private List<NativeAspect> mainAspects;
    private List<NativeAspect> otherAspects;
    private Map<String, ChartPoint> dodecatemoria;
    private Map<String, ChartPoint> novenaria;
    private Map<String, ChartPoint> antiscia;
    private Map<String, ChartPoint> contraAntiscia;

    public NativeReport() {
        this.lots = new LinkedHashMap<>();
        this.planets = new LinkedHashMap<>();
        this.houses = new LinkedHashMap<>();
        this.mainAspects = new ArrayList<>();
        this.otherAspects = new ArrayList<>();
        this.dodecatemoria = new LinkedHashMap<>();
        this.novenaria = new LinkedHashMap<>();
        this.antiscia = new LinkedHashMap<>();
        this.contraAntiscia = new LinkedHashMap<>();
    }

    public NativeReport(String name,
                        NativeBirth birth,
                        NativePlanetaryHour planetaryHour,
                        NativeSyzygy syzygy,
                        NativeLordOfOrb lordOfOrb,
                        Map<String, NativeHermeticLot> lots,
                        Map<String, ChartPoint> planets,
                        Map<String, ChartPoint> houses,
                        List<NativeAspect> mainAspects,
                        List<NativeAspect> otherAspects,
                        Map<String, ChartPoint> dodecatemoria,
                        Map<String, ChartPoint> novenaria,
                        Map<String, ChartPoint> antiscia,
                        Map<String, ChartPoint> contraAntiscia) {
        this.name = name;
        this.birth = birth;
        this.planetaryHour = planetaryHour;
        this.syzygy = syzygy;
        this.lordOfOrb = lordOfOrb;
        this.lots = lots == null ? new LinkedHashMap<>() : new LinkedHashMap<>(lots);
        this.planets = planets == null ? new LinkedHashMap<>() : new LinkedHashMap<>(planets);
        this.houses = houses == null ? new LinkedHashMap<>() : new LinkedHashMap<>(houses);
        this.mainAspects = mainAspects == null ? new ArrayList<>() : new ArrayList<>(mainAspects);
        this.otherAspects = otherAspects == null ? new ArrayList<>() : new ArrayList<>(otherAspects);
        this.dodecatemoria = dodecatemoria == null ? new LinkedHashMap<>() : new LinkedHashMap<>(dodecatemoria);
        this.novenaria = novenaria == null ? new LinkedHashMap<>() : new LinkedHashMap<>(novenaria);
        this.antiscia = antiscia == null ? new LinkedHashMap<>() : new LinkedHashMap<>(antiscia);
        this.contraAntiscia = contraAntiscia == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contraAntiscia);
    }

    public String name() { return name; }
    public NativeBirth birth() { return birth; }
    public NativePlanetaryHour planetaryHour() { return planetaryHour; }
    public NativeSyzygy syzygy() { return syzygy; }
    public NativeLordOfOrb lordOfOrb() { return lordOfOrb; }
    public Map<String, NativeHermeticLot> lots() { if (lots == null) lots = new LinkedHashMap<>(); return lots; }
    public Map<String, ChartPoint> planets() { if (planets == null) planets = new LinkedHashMap<>(); return planets; }
    public Map<String, ChartPoint> houses() { if (houses == null) houses = new LinkedHashMap<>(); return houses; }
    public List<NativeAspect> mainAspects() { if (mainAspects == null) mainAspects = new ArrayList<>(); return mainAspects; }
    public List<NativeAspect> otherAspects() { if (otherAspects == null) otherAspects = new ArrayList<>(); return otherAspects; }
    public Map<String, ChartPoint> dodecatemoria() { if (dodecatemoria == null) dodecatemoria = new LinkedHashMap<>(); return dodecatemoria; }
    public Map<String, ChartPoint> novenaria() { if (novenaria == null) novenaria = new LinkedHashMap<>(); return novenaria; }
    public Map<String, ChartPoint> antiscia() { if (antiscia == null) antiscia = new LinkedHashMap<>(); return antiscia; }
    public Map<String, ChartPoint> contraAntiscia() { if (contraAntiscia == null) contraAntiscia = new LinkedHashMap<>(); return contraAntiscia; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public NativeBirth getBirth() { return birth; }
    public void setBirth(NativeBirth birth) { this.birth = birth; }
    public NativePlanetaryHour getPlanetaryHour() { return planetaryHour; }
    public void setPlanetaryHour(NativePlanetaryHour planetaryHour) { this.planetaryHour = planetaryHour; }
    public NativeSyzygy getSyzygy() { return syzygy; }
    public void setSyzygy(NativeSyzygy syzygy) { this.syzygy = syzygy; }
    public NativeLordOfOrb getLordOfOrb() { return lordOfOrb; }
    public void setLordOfOrb(NativeLordOfOrb lordOfOrb) { this.lordOfOrb = lordOfOrb; }
    public Map<String, NativeHermeticLot> getLots() { return lots(); }
    public void setLots(Map<String, NativeHermeticLot> lots) { this.lots = lots == null ? new LinkedHashMap<>() : new LinkedHashMap<>(lots); }
    public Map<String, ChartPoint> getPlanets() { return planets(); }
    public void setPlanets(Map<String, ChartPoint> planets) { this.planets = planets == null ? new LinkedHashMap<>() : new LinkedHashMap<>(planets); }
    public Map<String, ChartPoint> getHouses() { return houses(); }
    public void setHouses(Map<String, ChartPoint> houses) { this.houses = houses == null ? new LinkedHashMap<>() : new LinkedHashMap<>(houses); }
    public List<NativeAspect> getMainAspects() { return mainAspects(); }
    public void setMainAspects(List<NativeAspect> mainAspects) { this.mainAspects = mainAspects == null ? new ArrayList<>() : new ArrayList<>(mainAspects); }
    public List<NativeAspect> getOtherAspects() { return otherAspects(); }
    public void setOtherAspects(List<NativeAspect> otherAspects) { this.otherAspects = otherAspects == null ? new ArrayList<>() : new ArrayList<>(otherAspects); }
    public Map<String, ChartPoint> getDodecatemoria() { return dodecatemoria(); }
    public void setDodecatemoria(Map<String, ChartPoint> dodecatemoria) { this.dodecatemoria = dodecatemoria == null ? new LinkedHashMap<>() : new LinkedHashMap<>(dodecatemoria); }
    public Map<String, ChartPoint> getNovenaria() { return novenaria(); }
    public void setNovenaria(Map<String, ChartPoint> novenaria) { this.novenaria = novenaria == null ? new LinkedHashMap<>() : new LinkedHashMap<>(novenaria); }
    public Map<String, ChartPoint> getAntiscia() { return antiscia(); }
    public void setAntiscia(Map<String, ChartPoint> antiscia) { this.antiscia = antiscia == null ? new LinkedHashMap<>() : new LinkedHashMap<>(antiscia); }
    public Map<String, ChartPoint> getContraAntiscia() { return contraAntiscia(); }
    public void setContraAntiscia(Map<String, ChartPoint> contraAntiscia) { this.contraAntiscia = contraAntiscia == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contraAntiscia); }
}
