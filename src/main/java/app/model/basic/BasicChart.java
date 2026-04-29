package app.model.basic;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BasicChart {

    private Instant resolvedUtcInstant;
    private double julianDay;
    private double armc;
    private double localSiderealTime;
    private double obliquity;
    private Map<String, PointEntry> points;
    private List<PlanetPosition> planets;
    private List<HousePosition> houses;
    private List<ChartAngle> angles;
    private List<RawAspectMatrixEntry> rawAspectMatrix;
    private List<RawDeclinationMatrixEntry> rawDeclinationMatrix;
    private List<RawSignDistanceMatrixEntry> rawSignDistanceMatrix;
    private List<PairwiseRelation> pairwiseRelations;
    private List<SolarPhaseEntry> solarPhase;
    private MoonPhase moonPhase;
    private BasicSect sect;
    private BasicSyzygy syzygy;
    private List<LotPosition> lots;

    public Instant getResolvedUtcInstant() {
        return resolvedUtcInstant;
    }

    public void setResolvedUtcInstant(Instant resolvedUtcInstant) {
        this.resolvedUtcInstant = resolvedUtcInstant;
    }

    public double getJulianDay() {
        return julianDay;
    }

    public void setJulianDay(double julianDay) {
        this.julianDay = julianDay;
    }

    public double getArmc() {
        return armc;
    }

    public void setArmc(double armc) {
        this.armc = armc;
    }

    public double getLocalSiderealTime() {
        return localSiderealTime;
    }

    public void setLocalSiderealTime(double localSiderealTime) {
        this.localSiderealTime = localSiderealTime;
    }

    public double getObliquity() {
        return obliquity;
    }

    public void setObliquity(double obliquity) {
        this.obliquity = obliquity;
    }

    public Map<String, PointEntry> getPoints() {
        return points;
    }

    public void setPoints(Map<String, PointEntry> points) {
        this.points = points;
    }

    @JsonIgnore
    public List<PlanetPosition> getPlanets() {
        return planets;
    }

    public void setPlanets(List<PlanetPosition> planets) {
        this.planets = planets;
    }

    public List<HousePosition> getHouses() {
        return houses;
    }

    public void setHouses(List<HousePosition> houses) {
        this.houses = houses;
    }

    @JsonIgnore
    public List<ChartAngle> getAngles() {
        return angles;
    }

    public void setAngles(List<ChartAngle> angles) {
        this.angles = angles;
    }

    @JsonIgnore
    public List<RawAspectMatrixEntry> getRawAspectMatrix() {
        return rawAspectMatrix;
    }

    public void setRawAspectMatrix(List<RawAspectMatrixEntry> rawAspectMatrix) {
        this.rawAspectMatrix = rawAspectMatrix;
    }

    @JsonIgnore
    public List<RawDeclinationMatrixEntry> getRawDeclinationMatrix() {
        return rawDeclinationMatrix;
    }

    public void setRawDeclinationMatrix(List<RawDeclinationMatrixEntry> rawDeclinationMatrix) {
        this.rawDeclinationMatrix = rawDeclinationMatrix;
    }

    @JsonIgnore
    public List<RawSignDistanceMatrixEntry> getRawSignDistanceMatrix() {
        return rawSignDistanceMatrix;
    }

    public void setRawSignDistanceMatrix(List<RawSignDistanceMatrixEntry> rawSignDistanceMatrix) {
        this.rawSignDistanceMatrix = rawSignDistanceMatrix;
    }

    public List<PairwiseRelation> getPairwiseRelations() {
        return pairwiseRelations;
    }

    public void setPairwiseRelations(List<PairwiseRelation> pairwiseRelations) {
        this.pairwiseRelations = pairwiseRelations;
    }

    public List<SolarPhaseEntry> getSolarPhase() {
        return solarPhase;
    }

    public void setSolarPhase(List<SolarPhaseEntry> solarPhase) {
        this.solarPhase = solarPhase;
    }

    public MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(MoonPhase moonPhase) {
        this.moonPhase = moonPhase;
    }

    public BasicSect getSect() {
        return sect;
    }

    public void setSect(BasicSect sect) {
        this.sect = sect;
    }

    @JsonIgnore
    public BasicSyzygy getSyzygy() {
        return syzygy;
    }

    public void setSyzygy(BasicSyzygy syzygy) {
        this.syzygy = syzygy;
    }

    @JsonIgnore
    public List<LotPosition> getLots() {
        return lots;
    }

    public void setLots(List<LotPosition> lots) {
        this.lots = lots;
    }
}
