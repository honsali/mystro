package app.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.common.model.ChartPoint;
import app.common.model.NativeAnnualProfections;
import app.common.model.NativeAspect;
import app.common.model.NativeBirth;
import app.common.model.NativeChart;
import app.common.model.NativeHermeticLot;
import app.common.model.NativeLordOfOrb;
import app.common.model.NativePlanetaryHour;
import app.common.model.NativeReport;
import app.common.model.NativeSyzygy;
import app.swisseph.core.SwissEph;

public final class NativeReportBuilder {
    private String name;
    private SwissEph swissEph;
    private NativeBirth birth;
    private NativePlanetaryHour planetaryHour;
    private NativeSyzygy syzygy;
    private NativeLordOfOrb lordOfOrb;
    private NativeAnnualProfections annualProfections;
    private NativeChart nativeChart;
    private final Map<String, NativeHermeticLot> lots = new LinkedHashMap<>();
    private final Map<String, ChartPoint> planets = new LinkedHashMap<>();
    private final Map<String, ChartPoint> houses = new LinkedHashMap<>();
    private final List<NativeAspect> mainAspects = new ArrayList<>();
    private final List<NativeAspect> otherAspects = new ArrayList<>();
    private final Map<String, ChartPoint> dodecatemoria = new LinkedHashMap<>();
    private final Map<String, ChartPoint> novenaria = new LinkedHashMap<>();
    private final Map<String, ChartPoint> antiscia = new LinkedHashMap<>();
    private final Map<String, ChartPoint> contraAntiscia = new LinkedHashMap<>();

    public NativeReportBuilder(String name) {
        this.name = name;
    }

    public NativeReportBuilder(String name, NativeBirth birth, SwissEph swissEph) {
        this.name = name;
        this.birth = birth;
        this.swissEph = swissEph;
    }

    public String name() {
        return name;
    }

    public SwissEph swissEph() {
        return swissEph;
    }

    public void birth(NativeBirth birth) {
        this.birth = birth;
    }

    public NativeBirth birth() {
        return birth;
    }

    public void nativeChart(NativeChart nativeChart) {
        this.nativeChart = nativeChart;
    }

    public NativeChart nativeChart() {
        return nativeChart;
    }

    public void planetaryHour(NativePlanetaryHour planetaryHour) {
        this.planetaryHour = planetaryHour;
    }

    public NativePlanetaryHour planetaryHour() {
        return planetaryHour;
    }

    public void syzygy(NativeSyzygy syzygy) {
        this.syzygy = syzygy;
    }

    public NativeSyzygy syzygy() {
        return syzygy;
    }

    public void lordOfOrb(NativeLordOfOrb lordOfOrb) {
        this.lordOfOrb = lordOfOrb;
    }

    public NativeLordOfOrb lordOfOrb() {
        return lordOfOrb;
    }

    public void annualProfections(NativeAnnualProfections annualProfections) {
        this.annualProfections = annualProfections;
    }

    public NativeAnnualProfections annualProfections() {
        return annualProfections;
    }

    public void lot(NativeHermeticLot lot) {
        this.lots.put(lot.lot(), lot);
    }

    public Map<String, NativeHermeticLot> lots() {
        return lots;
    }

    public NativeHermeticLot lot(String lotName) {
        return lots.get(lotName);
    }

    public void planets(Map<String, ChartPoint> planets) {
        this.planets.clear();
        if (planets != null)
            this.planets.putAll(planets);
    }

    public Map<String, ChartPoint> planets() {
        return planets;
    }

    public void houses(Map<String, ChartPoint> houses) {
        this.houses.clear();
        if (houses != null)
            this.houses.putAll(houses);
    }

    public Map<String, ChartPoint> houses() {
        return houses;
    }

    public void mainAspects(List<NativeAspect> aspects) {
        this.mainAspects.clear();
        if (aspects != null)
            this.mainAspects.addAll(aspects);
    }

    public List<NativeAspect> mainAspects() {
        return mainAspects;
    }

    public void otherAspects(List<NativeAspect> aspects) {
        this.otherAspects.clear();
        if (aspects != null)
            this.otherAspects.addAll(aspects);
    }

    public List<NativeAspect> otherAspects() {
        return otherAspects;
    }

    public void dodecatemoria(Map<String, ChartPoint> points) {
        this.dodecatemoria.clear();
        if (points != null)
            this.dodecatemoria.putAll(points);
    }

    public Map<String, ChartPoint> dodecatemoria() {
        return dodecatemoria;
    }

    public void novenaria(Map<String, ChartPoint> points) {
        this.novenaria.clear();
        if (points != null)
            this.novenaria.putAll(points);
    }

    public Map<String, ChartPoint> novenaria() {
        return novenaria;
    }

    public void antiscia(Map<String, ChartPoint> points) {
        this.antiscia.clear();
        if (points != null)
            this.antiscia.putAll(points);
    }

    public Map<String, ChartPoint> antiscia() {
        return antiscia;
    }

    public void contraAntiscia(Map<String, ChartPoint> points) {
        this.contraAntiscia.clear();
        if (points != null)
            this.contraAntiscia.putAll(points);
    }

    public Map<String, ChartPoint> contraAntiscia() {
        return contraAntiscia;
    }

    public NativeReport build() {
        return new NativeReport(name, birth, planetaryHour, syzygy, lordOfOrb, annualProfections, new LinkedHashMap<>(lots), new LinkedHashMap<>(planets), sortedHouses(), List.copyOf(mainAspects), List.copyOf(otherAspects), new LinkedHashMap<>(dodecatemoria),
                new LinkedHashMap<>(novenaria), new LinkedHashMap<>(antiscia), new LinkedHashMap<>(contraAntiscia));
    }

    private LinkedHashMap<String, ChartPoint> sortedHouses() {
        return houses.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> houseSortOrder(entry.getKey())))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
    }

    private int houseSortOrder(String id) {
        if ("AC".equals(id)) {
            return 0;
        }
        if ("MC".equals(id)) {
            return 1;
        }
        try {
            return Integer.parseInt(id) + 1;
        } catch (NumberFormatException ignored) {
            return Integer.MAX_VALUE;
        }
    }
}
