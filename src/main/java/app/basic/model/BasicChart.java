package app.basic.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import app.basic.data.AngleType;
import app.basic.data.Planet;
import app.basic.data.PointKey;

public class BasicChart {

    private Instant resolvedUtcInstant;
    private double julianDayUt;
    private double julianDayTt;
    private double deltaTSeconds;
    private double armc;
    private double localApparentSiderealTimeHours;
    private double trueObliquity;
    private double meanObliquity;
    private double nutationLongitude;
    private double nutationObliquity;
    private Map<PointKey, PointEntry> points;
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

    public Instant getResolvedUtcInstant() {
        return resolvedUtcInstant;
    }

    public void setResolvedUtcInstant(Instant resolvedUtcInstant) {
        this.resolvedUtcInstant = resolvedUtcInstant;
    }

    public double getJulianDayUt() {
        return julianDayUt;
    }

    public void setJulianDayUt(double julianDayUt) {
        this.julianDayUt = julianDayUt;
    }

    public double getJulianDayTt() {
        return julianDayTt;
    }

    public void setJulianDayTt(double julianDayTt) {
        this.julianDayTt = julianDayTt;
    }

    public double getDeltaTSeconds() {
        return deltaTSeconds;
    }

    public void setDeltaTSeconds(double deltaTSeconds) {
        this.deltaTSeconds = deltaTSeconds;
    }

    public double getArmc() {
        return armc;
    }

    public void setArmc(double armc) {
        this.armc = armc;
    }

    public double getLocalApparentSiderealTimeHours() {
        return localApparentSiderealTimeHours;
    }

    public void setLocalApparentSiderealTimeHours(double localApparentSiderealTimeHours) {
        this.localApparentSiderealTimeHours = localApparentSiderealTimeHours;
    }

    public double getTrueObliquity() {
        return trueObliquity;
    }

    public void setTrueObliquity(double trueObliquity) {
        this.trueObliquity = trueObliquity;
    }

    public double getMeanObliquity() {
        return meanObliquity;
    }

    public void setMeanObliquity(double meanObliquity) {
        this.meanObliquity = meanObliquity;
    }

    public double getNutationLongitude() {
        return nutationLongitude;
    }

    public void setNutationLongitude(double nutationLongitude) {
        this.nutationLongitude = nutationLongitude;
    }

    public double getNutationObliquity() {
        return nutationObliquity;
    }

    public void setNutationObliquity(double nutationObliquity) {
        this.nutationObliquity = nutationObliquity;
    }

    public Map<PointKey, PointEntry> getPoints() {
        return points;
    }

    public void setPoints(Map<PointKey, PointEntry> points) {
        this.points = points;
    }

    @JsonIgnore
    public List<PlanetPosition> getPlanets() {
        return planets;
    }

    public void setPlanets(List<PlanetPosition> planets) {
        this.planets = planets;
    }

    public PlanetPosition requirePlanet(Planet planet) {
        return planets.stream()
                .filter(candidate -> candidate.getPlanet() == planet)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing planet " + planet));
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

    public ChartAngle requireAngle(AngleType angle) {
        return angles.stream()
                .filter(candidate -> candidate.getName() == angle)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing angle " + angle));
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


}
