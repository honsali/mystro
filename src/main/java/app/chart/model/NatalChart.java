package app.chart.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.reading.description.common.model.AspectEntry;
import app.reading.description.common.model.BeneficMaleficAssessmentEntry;
import app.reading.description.common.model.DodecatemoriaEntry;
import app.reading.description.common.model.DoryphoryEntry;
import app.reading.description.common.model.DerivedHouseFramesEntry;
import app.reading.description.common.model.FixedStarEntry;
import app.reading.description.common.model.HouseTopicRulerEntry;
import app.reading.description.common.model.HylegAlcocodenEntry;
import app.reading.description.common.model.LotAssessmentEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.MercuryConfigurationEntry;
import app.reading.description.common.model.MoonConfigurationEntry;
import app.reading.description.common.model.PlanetDignityEntry;
import app.reading.description.common.model.PrenatalSyzygyEntry;
import app.reading.description.common.model.SolarConditionEntry;
import app.reading.description.common.model.TopicAssessmentEntry;
import app.reading.description.common.model.TriplicityLifePhaseEntry;

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
    private MercuryConfigurationEntry mercuryConfiguration;
    private MoonConfigurationEntry moonConfiguration;
    private PrenatalSyzygyEntry syzygy;
    private List<HouseTopicRulerEntry> houseTopicRulers;
    private List<LotEntry> lots;
    private List<LotAssessmentEntry> lotAssessments;
    private DerivedHouseFramesEntry derivedHouseFrames;
    private List<TopicAssessmentEntry> topicAssessments;
    private List<DoryphoryEntry> doryphories;
    private List<DodecatemoriaEntry> dodecatemoria;
    private List<TriplicityLifePhaseEntry> triplicityLifePhases;
    private HylegAlcocodenEntry ptolemaicHylegAlcocoden;
    private List<FixedStarEntry> fixedStars;

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

    public MercuryConfigurationEntry getMercuryConfiguration() {
        return mercuryConfiguration;
    }

    public void setMercuryConfiguration(MercuryConfigurationEntry mercuryConfiguration) {
        this.mercuryConfiguration = mercuryConfiguration;
    }

    public MoonConfigurationEntry getMoonConfiguration() {
        return moonConfiguration;
    }

    public void setMoonConfiguration(MoonConfigurationEntry moonConfiguration) {
        this.moonConfiguration = moonConfiguration;
    }

    public PrenatalSyzygyEntry getSyzygy() {
        return syzygy;
    }

    public void setSyzygy(PrenatalSyzygyEntry syzygy) {
        this.syzygy = syzygy;
    }

    public List<HouseTopicRulerEntry> getHouseTopicRulers() {
        return houseTopicRulers;
    }

    public void setHouseTopicRulers(List<HouseTopicRulerEntry> houseTopicRulers) {
        this.houseTopicRulers = houseTopicRulers;
    }

    public List<LotEntry> getLots() {
        return lots;
    }

    public void setLots(List<LotEntry> lots) {
        this.lots = lots;
    }

    public List<LotAssessmentEntry> getLotAssessments() {
        return lotAssessments;
    }

    public void setLotAssessments(List<LotAssessmentEntry> lotAssessments) {
        this.lotAssessments = lotAssessments;
    }

    public DerivedHouseFramesEntry getDerivedHouseFrames() {
        return derivedHouseFrames;
    }

    public void setDerivedHouseFrames(DerivedHouseFramesEntry derivedHouseFrames) {
        this.derivedHouseFrames = derivedHouseFrames;
    }

    public List<TopicAssessmentEntry> getTopicAssessments() {
        return topicAssessments;
    }

    public void setTopicAssessments(List<TopicAssessmentEntry> topicAssessments) {
        this.topicAssessments = topicAssessments;
    }

    public List<DoryphoryEntry> getDoryphories() {
        return doryphories;
    }

    public void setDoryphories(List<DoryphoryEntry> doryphories) {
        this.doryphories = doryphories;
    }

    public List<DodecatemoriaEntry> getDodecatemoria() {
        return dodecatemoria;
    }

    public void setDodecatemoria(List<DodecatemoriaEntry> dodecatemoria) {
        this.dodecatemoria = dodecatemoria;
    }

    public List<TriplicityLifePhaseEntry> getTriplicityLifePhases() {
        return triplicityLifePhases;
    }

    public void setTriplicityLifePhases(List<TriplicityLifePhaseEntry> triplicityLifePhases) {
        this.triplicityLifePhases = triplicityLifePhases;
    }

    public HylegAlcocodenEntry getPtolemaicHylegAlcocoden() {
        return ptolemaicHylegAlcocoden;
    }

    public void setPtolemaicHylegAlcocoden(HylegAlcocodenEntry ptolemaicHylegAlcocoden) {
        this.ptolemaicHylegAlcocoden = ptolemaicHylegAlcocoden;
    }

    public List<FixedStarEntry> getFixedStars() {
        return fixedStars;
    }

    public void setFixedStars(List<FixedStarEntry> fixedStars) {
        this.fixedStars = fixedStars;
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

    public void applyBeneficMaleficAssessments(Map<Planet, List<BeneficMaleficAssessmentEntry>> assessments) {
        for (Map.Entry<Planet, List<BeneficMaleficAssessmentEntry>> assessment : assessments.entrySet()) {
            PointKey pointKey = PointKey.of(assessment.getKey());
            PointEntry point = points.get(pointKey);
            if (point instanceof PlanetPointEntry planetPoint) {
                points.put(pointKey, planetPoint.withBeneficMaleficAssessment(assessment.getValue()));
            }
        }
    }

}
