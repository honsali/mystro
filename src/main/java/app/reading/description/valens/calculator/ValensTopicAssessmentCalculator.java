package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.List;

import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.reading.description.common.model.HouseTopicRulerEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.TopicAssessmentEntry;
import app.reading.description.common.model.TopicEvidenceEntry;

public final class ValensTopicAssessmentCalculator {
    private static final String VALENS = "valens";
    private static final String PTOLEMAIC = "ptolemaic";
    private static final String DOROTHEAN = "dorothean";
    private static final String HELLENISTIC = "hellenistic";
    private static final List<Planet> TRADITIONAL_PLANETS = List.of(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN
    );

    public List<TopicAssessmentEntry> calculate(Chart chart) {
        List<TopicAssessmentEntry> topics = new ArrayList<>();
        topics.add(bodyTemperament(chart));
        topics.add(characterSoulQuality(chart));
        topics.add(mindSpeech(chart));
        topics.add(fortuneMaterialCondition(chart));
        topics.add(vocationAction(chart));
        topics.add(marriageUnions(chart));
        topics.add(children(chart));
        topics.add(father(chart));
        topics.add(mother(chart));
        topics.add(siblings(chart));
        topics.add(eminenceRank(chart));
        topics.add(vulnerabilityIndicators(chart));
        return List.copyOf(topics);
    }

    private TopicAssessmentEntry bodyTemperament(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 1, "ASCENDANT_PLACE", PTOLEMAIC));
        evidence.add(angle(chart, AngleType.ASCENDANT, "HOROSKOPOS", PTOLEMAIC));
        evidence.add(planet(chart, Planet.MOON, "BODY_AND_HABIT_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, chart.getSect().getLightOfSect(), "LIGHT_OF_SECT", VALENS));
        addLot(evidence, chart, "FORTUNE", "BODY_AND_MATERIAL_LOT", VALENS);
        addSection(evidence, chart.getPtolemaicHylegAlcocoden() != null, "ptolemaicHylegAlcocoden", "VITALITY_DOCTRINE", PTOLEMAIC);
        addPlanetsInHouse(evidence, chart, 1, "PLANET_IN_ASCENDANT_PLACE", HELLENISTIC);
        return topic("BODY_TEMPERAMENT", PTOLEMAIC, List.of(VALENS), "PTOLEMAIC_BODY_V1", evidence);
    }

    private TopicAssessmentEntry characterSoulQuality(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(planet(chart, Planet.MOON, "SOUL_HABIT_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, Planet.MERCURY, "RATIONAL_SOUL_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, chart.getSect().getLightOfSect(), "LIGHT_OF_SECT", VALENS));
        addSection(evidence, chart.getMoonConfiguration() != null, "moonConfiguration", "MOON_CONFIGURATION", PTOLEMAIC);
        addSection(evidence, chart.getMercuryConfiguration() != null, "mercuryConfiguration", "MERCURY_CONFIGURATION", PTOLEMAIC);
        return topic("CHARACTER_SOUL_QUALITY", PTOLEMAIC, List.of(VALENS), "PTOLEMAIC_SOUL_QUALITY_V1", evidence);
    }

    private TopicAssessmentEntry mindSpeech(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(planet(chart, Planet.MERCURY, "INTELLECT_SPEECH_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, Planet.MOON, "MOON_RELATION_TO_MERCURY", PTOLEMAIC));
        addSection(evidence, chart.getMercuryConfiguration() != null, "mercuryConfiguration", "MERCURY_CONFIGURATION", PTOLEMAIC);
        addSection(evidence, chart.getMoonConfiguration() != null, "moonConfiguration", "MOON_CONFIGURATION", PTOLEMAIC);
        return topic("MIND_SPEECH", PTOLEMAIC, List.of(VALENS), "PTOLEMAIC_MIND_SPEECH_V1", evidence);
    }

    private TopicAssessmentEntry fortuneMaterialCondition(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 2, "POSSESSIONS_PLACE", VALENS));
        addLot(evidence, chart, "FORTUNE", "FORTUNE_LOT", VALENS);
        evidence.add(planet(chart, Planet.JUPITER, "NATURAL_WEALTH_SIGNIFICATOR", PTOLEMAIC));
        addDerivedFrame(evidence, chart.getDerivedHouseFrames() != null && chart.getDerivedHouseFrames().fromFortune() != null, "derivedHouseFrames.fromFortune", "FORTUNE_DERIVED_FRAME", VALENS);
        addPlanetsInHouse(evidence, chart, 2, "PLANET_IN_POSSESSIONS_PLACE", HELLENISTIC);
        return topic("FORTUNE_MATERIAL_CONDITION", VALENS, List.of(PTOLEMAIC), "VALENS_FORTUNE_MATERIAL_V1", evidence);
    }

    private TopicAssessmentEntry vocationAction(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 10, "ACTION_RANK_PLACE", PTOLEMAIC));
        evidence.add(angle(chart, AngleType.MIDHEAVEN, "MIDHEAVEN", PTOLEMAIC));
        addLot(evidence, chart, "SPIRIT", "ACTION_INTENTION_LOT", VALENS);
        evidence.add(planet(chart, Planet.MERCURY, "PROFESSIONAL_CANDIDATE", PTOLEMAIC));
        evidence.add(planet(chart, Planet.VENUS, "PROFESSIONAL_CANDIDATE", PTOLEMAIC));
        evidence.add(planet(chart, Planet.MARS, "PROFESSIONAL_CANDIDATE", PTOLEMAIC));
        evidence.add(planet(chart, Planet.SUN, "VISIBILITY_RANK_SIGNIFICATOR", PTOLEMAIC));
        addDerivedFrame(evidence, chart.getDerivedHouseFrames() != null && chart.getDerivedHouseFrames().fromSpirit() != null, "derivedHouseFrames.fromSpirit", "SPIRIT_DERIVED_FRAME", VALENS);
        addPlanetsInHouse(evidence, chart, 10, "PLANET_IN_ACTION_RANK_PLACE", HELLENISTIC);
        return topic("VOCATION_ACTION", PTOLEMAIC, List.of(VALENS, DOROTHEAN), "PTOLEMAIC_PROFESSION_V1", evidence);
    }

    private TopicAssessmentEntry marriageUnions(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 7, "UNIONS_PLACE", DOROTHEAN));
        evidence.add(planet(chart, Planet.VENUS, "NATURAL_UNIONS_SIGNIFICATOR", PTOLEMAIC));
        addLot(evidence, chart, "WEDDING", "DOROTHEAN_WEDDING_LOT", DOROTHEAN);
        addPlanetsInHouse(evidence, chart, 7, "PLANET_IN_UNIONS_PLACE", HELLENISTIC);
        return topic("MARRIAGE_UNIONS", DOROTHEAN, List.of(PTOLEMAIC, VALENS), "DOROTHEAN_MARRIAGE_V1", evidence);
    }

    private TopicAssessmentEntry children(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 5, "CHILDREN_PLACE", DOROTHEAN));
        evidence.add(planet(chart, Planet.JUPITER, "NATURAL_CHILDREN_SIGNIFICATOR", PTOLEMAIC));
        addLot(evidence, chart, "CHILDREN", "DOROTHEAN_CHILDREN_LOT", DOROTHEAN);
        addPlanetsInHouse(evidence, chart, 5, "PLANET_IN_CHILDREN_PLACE", HELLENISTIC);
        return topic("CHILDREN", DOROTHEAN, List.of(PTOLEMAIC), "DOROTHEAN_CHILDREN_V1", evidence);
    }

    private TopicAssessmentEntry father(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 4, "PARENTS_FATHER_PLACE", DOROTHEAN));
        evidence.add(planet(chart, Planet.SUN, "FATHER_LUMINARY_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, Planet.SATURN, "FATHER_PLANETARY_SIGNIFICATOR", PTOLEMAIC));
        addLot(evidence, chart, "FATHER", "DOROTHEAN_FATHER_LOT", DOROTHEAN);
        addPlanetsInHouse(evidence, chart, 4, "PLANET_IN_FATHER_PLACE", HELLENISTIC);
        return topic("FATHER", DOROTHEAN, List.of(PTOLEMAIC), "DOROTHEAN_FATHER_V1", evidence);
    }

    private TopicAssessmentEntry mother(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 10, "PARENTS_MOTHER_PLACE", DOROTHEAN));
        evidence.add(planet(chart, Planet.MOON, "MOTHER_LUMINARY_SIGNIFICATOR", PTOLEMAIC));
        evidence.add(planet(chart, Planet.VENUS, "MOTHER_PLANETARY_SIGNIFICATOR", PTOLEMAIC));
        addLot(evidence, chart, "MOTHER", "DOROTHEAN_MOTHER_LOT", DOROTHEAN);
        addPlanetsInHouse(evidence, chart, 10, "PLANET_IN_MOTHER_PLACE", HELLENISTIC);
        return topic("MOTHER", DOROTHEAN, List.of(PTOLEMAIC), "DOROTHEAN_MOTHER_V1", evidence);
    }

    private TopicAssessmentEntry siblings(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 3, "SIBLINGS_PLACE", DOROTHEAN));
        addLot(evidence, chart, "SIBLINGS", "DOROTHEAN_SIBLINGS_LOT", DOROTHEAN);
        addPlanetsInHouse(evidence, chart, 3, "PLANET_IN_SIBLINGS_PLACE", HELLENISTIC);
        return topic("SIBLINGS", DOROTHEAN, List.of(VALENS), "DOROTHEAN_SIBLINGS_V1", evidence);
    }

    private TopicAssessmentEntry eminenceRank(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 10, "RANK_ACTION_PLACE", VALENS));
        evidence.add(angle(chart, AngleType.MIDHEAVEN, "MIDHEAVEN", PTOLEMAIC));
        evidence.add(planet(chart, Planet.SUN, "SOLAR_VISIBILITY_SIGNIFICATOR", VALENS));
        evidence.add(planet(chart, Planet.MOON, "LUNAR_VISIBILITY_SIGNIFICATOR", VALENS));
        addLot(evidence, chart, "FORTUNE", "FORTUNE_CAPACITY_LOT", VALENS);
        addLot(evidence, chart, "SPIRIT", "ACTION_INTENTION_LOT", VALENS);
        addSection(evidence, chart.getDoryphories() != null && !chart.getDoryphories().isEmpty(), "doryphories", "DORYPHORY_SECTION", VALENS);
        addSection(evidence, chart.getFixedStars() != null && !chart.getFixedStars().isEmpty(), "fixedStars", "FIXED_STAR_TESTIMONY", PTOLEMAIC);
        addPlanetsInHouse(evidence, chart, 10, "PLANET_IN_RANK_PLACE", HELLENISTIC);
        return topic("EMINENCE_RANK", VALENS, List.of(PTOLEMAIC), "VALENS_EMINENCE_RANK_V1", evidence);
    }

    private TopicAssessmentEntry vulnerabilityIndicators(Chart chart) {
        List<TopicEvidenceEntry> evidence = new ArrayList<>();
        evidence.add(house(chart, 6, "ILLNESS_LABOR_PLACE", PTOLEMAIC));
        evidence.add(house(chart, 8, "MORTALITY_ANXIETY_PLACE", HELLENISTIC));
        evidence.add(house(chart, 12, "CONFINEMENT_HIDDEN_TROUBLE_PLACE", HELLENISTIC));
        evidence.add(planet(chart, Planet.MARS, "MALEFIC_STRESS_SIGNIFICATOR", VALENS));
        evidence.add(planet(chart, Planet.SATURN, "MALEFIC_STRESS_SIGNIFICATOR", VALENS));
        addLot(evidence, chart, "NECESSITY", "NECESSITY_LOT", VALENS);
        addLot(evidence, chart, "NEMESIS", "NEMESIS_LOT", "hermetic");
        addSection(evidence, chart.getPtolemaicHylegAlcocoden() != null, "ptolemaicHylegAlcocoden", "VITALITY_DOCTRINE", PTOLEMAIC);
        addPlanetsInHouse(evidence, chart, 6, "PLANET_IN_ILLNESS_LABOR_PLACE", HELLENISTIC);
        addPlanetsInHouse(evidence, chart, 8, "PLANET_IN_MORTALITY_ANXIETY_PLACE", HELLENISTIC);
        addPlanetsInHouse(evidence, chart, 12, "PLANET_IN_HIDDEN_TROUBLE_PLACE", HELLENISTIC);
        return topic("VULNERABILITY_INDICATORS", PTOLEMAIC, List.of(VALENS), "PTOLEMAIC_VULNERABILITY_INDICATORS_V1", evidence);
    }

    private TopicAssessmentEntry topic(String topic, String primaryDoctrine, List<String> supportingDoctrines, String methodId, List<TopicEvidenceEntry> evidence) {
        return new TopicAssessmentEntry(topic, primaryDoctrine, supportingDoctrines, methodId, evidence);
    }

    private TopicEvidenceEntry house(Chart chart, int house, String role, String sourceDoctrine) {
        HouseTopicRulerEntry entry = chart.getHouseTopicRulers().stream()
                .filter(candidate -> candidate.house() == house)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing house topic ruler " + house));
        return new TopicEvidenceEntry(role, sourceDoctrine, "HOUSE", Integer.toString(house), house, entry.sign(), entry.ruler(), "houseTopicRulers.house=" + house);
    }

    private TopicEvidenceEntry angle(Chart chart, AngleType angle, String role, String sourceDoctrine) {
        ChartAngle entry = chart.requireAngle(angle);
        return new TopicEvidenceEntry(role, sourceDoctrine, "ANGLE", angle.name(), null, entry.getSign(), TraditionalTables.domicileRuler(entry.getSign()), null);
    }

    private TopicEvidenceEntry planet(Chart chart, Planet planet, String role, String sourceDoctrine) {
        PlanetPointEntry point = requirePlanetPoint(chart, planet);
        return new TopicEvidenceEntry(role, sourceDoctrine, "PLANET", planet.name(), point.house(), point.sign(), null, planet.name());
    }

    private void addLot(List<TopicEvidenceEntry> evidence, Chart chart, String lotName, String role, String sourceDoctrine) {
        LotEntry lot = lot(chart, lotName);
        if (lot != null) {
            evidence.add(new TopicEvidenceEntry(role, sourceDoctrine, "LOT", lot.name(), lot.house(), lot.sign(), lot.ruler(), "lots.name=" + lot.name()));
        }
    }

    private void addDerivedFrame(List<TopicEvidenceEntry> evidence, boolean present, String target, String role, String sourceDoctrine) {
        if (present) {
            evidence.add(new TopicEvidenceEntry(role, sourceDoctrine, "DERIVED_FRAME", target, null, null, null, target));
        }
    }

    private void addSection(List<TopicEvidenceEntry> evidence, boolean present, String target, String role, String sourceDoctrine) {
        if (present) {
            evidence.add(new TopicEvidenceEntry(role, sourceDoctrine, "SECTION", target, null, null, null, target));
        }
    }

    private void addPlanetsInHouse(List<TopicEvidenceEntry> evidence, Chart chart, int house, String role, String sourceDoctrine) {
        for (Planet planet : TRADITIONAL_PLANETS) {
            PlanetPointEntry point = requirePlanetPoint(chart, planet);
            if (point.house() == house) {
                evidence.add(new TopicEvidenceEntry(role, sourceDoctrine, "PLANET", planet.name(), point.house(), point.sign(), null, planet.name()));
            }
        }
    }

    private LotEntry lot(Chart chart, String name) {
        if (chart.getLots() == null) {
            return null;
        }
        return chart.getLots().stream()
                .filter(candidate -> name.equals(candidate.name()))
                .findFirst()
                .orElse(null);
    }

    private PlanetPointEntry requirePlanetPoint(Chart chart, Planet planet) {
        PointEntry point = chart.getPoints().get(app.chart.data.PointKey.of(planet));
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        throw new IllegalArgumentException("Missing planet point " + planet);
    }
}
