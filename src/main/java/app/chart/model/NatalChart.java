package app.chart.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.descriptive.common.data.LotName;
import app.descriptive.common.model.AspectEntry;
import app.descriptive.common.model.LotEntry;
import app.descriptive.common.model.PlanetDignityEntry;
import app.descriptive.common.model.PrenatalSyzygyEntry;
import app.descriptive.common.model.SolarConditionEntry;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NatalChart {

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
    private List<PairwiseRelation> pairwiseRelations;
    private MoonPhase moonPhase;
    private BasicSect sect;
    private PrenatalSyzygyEntry syzygy;
    private Map<LotName, LotEntry> lots;

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

    public List<PairwiseRelation> getPairwiseRelations() {
        return pairwiseRelations;
    }

    public void setPairwiseRelations(List<PairwiseRelation> pairwiseRelations) {
        this.pairwiseRelations = pairwiseRelations;
    }

    public void setSolarPhase(List<SolarPhaseEntry> solarPhases) {
        for (SolarPhaseEntry solarPhase : solarPhases) {
            PointKey pointKey = PointKey.of(solarPhase.getPlanet());
            PointEntry point = points.get(pointKey);
            if (point instanceof PlanetPointEntry planetPoint) {
                points.put(pointKey, planetPoint.withSolarPhase(solarPhase.getOrientationToSun()));
            }
        }
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

    public void applyPlanetSects() {
        if (sect == null || sect.getPlanetSects() == null) {
            return;
        }
        for (Map.Entry<Planet, PlanetSectInfo> planetSect : sect.getPlanetSects().entrySet()) {
            PointKey pointKey = PointKey.of(planetSect.getKey());
            PointEntry point = points.get(pointKey);
            if (point instanceof PlanetPointEntry planetPoint) {
                points.put(pointKey, planetPoint.withSect(planetSect.getValue()));
            }
        }
    }

    public PrenatalSyzygyEntry getSyzygy() {
        return syzygy;
    }

    public void setSyzygy(PrenatalSyzygyEntry syzygy) {
        this.syzygy = syzygy;
    }

    public Map<LotName, LotEntry> getLots() {
        return lots;
    }

    public void setLots(Map<LotName, LotEntry> lots) {
        this.lots = lots;
    }

    public void applyAspects(List<AspectEntry> aspects) {
        for (AspectEntry aspect : aspects) {
            for (int i = 0; i < pairwiseRelations.size(); i++) {
                PairwiseRelation relation = pairwiseRelations.get(i);
                if (matchesAspect(relation, aspect)) {
                    pairwiseRelations.set(i, relation.withAspect(new PairwiseRelation.AspectRelation(aspect.type().name(), aspect.orbFromExact())));
                    break;
                }
            }
        }
    }

    private boolean matchesAspect(PairwiseRelation relation, AspectEntry aspect) {
        PointKey planetA = PointKey.of(aspect.planetA());
        PointKey planetB = PointKey.of(aspect.planetB());
        return (relation.getPointAName() == planetA && relation.getPointBName() == planetB)
                || (relation.getPointAName() == planetB && relation.getPointBName() == planetA);
    }

    public void applyDignityAssessments(Map<Planet, PlanetDignityEntry> dignityAssessments) {
        for (Map.Entry<Planet, PlanetDignityEntry> assessment : dignityAssessments.entrySet()) {
            PointKey pointKey = PointKey.of(assessment.getKey());
            PointEntry point = points.get(pointKey);
            if (point instanceof PlanetPointEntry planetPoint) {
                PlanetDignityEntry dignity = assessment.getValue();
                points.put(pointKey, planetPoint.withDignityAssessment(dignity.dignities(), dignity.debilities()));
            }
        }
    }

    public void applySolarConditions(Map<Planet, SolarConditionEntry> solarConditions) {
        for (Map.Entry<Planet, SolarConditionEntry> solarCondition : solarConditions.entrySet()) {
            PointKey pointKey = PointKey.of(solarCondition.getKey());
            PointEntry point = points.get(pointKey);
            if (point instanceof PlanetPointEntry planetPoint) {
                points.put(pointKey, planetPoint.withSolarCondition(solarCondition.getValue().condition()));
            }
        }
    }

}
