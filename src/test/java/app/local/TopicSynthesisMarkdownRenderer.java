package app.local;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.HouseTopicRulerEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.TopicAssessmentEntry;
import app.reading.description.common.model.TopicEvidenceEntry;
import app.reading.lifearc.synthesis.LifeArcEvidenceKeyType;
import app.reading.lifearc.synthesis.LifeArcSynthesisEvidence;
import app.reading.lifearc.synthesis.LifeArcSynthesisTable;

final class TopicSynthesisMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    private static final List<TopicBucket> BUCKETS = List.of(
            new TopicBucket(
                    "career",
                    "Career",
                    "career.md",
                    "Career, vocation, action, rank, public role, and work duties.",
                    List.of("VOCATION_ACTION", "EMINENCE_RANK"),
                    List.of(10, 6, 11),
                    List.of("SPIRIT", "VICTORY", "BASIS"),
                    List.of(Planet.SUN, Planet.MERCURY, Planet.VENUS, Planet.MARS),
                    List.of("MIDHEAVEN"),
                    List.of()
            ),
            new TopicBucket(
                    "relationships_marriage",
                    "Relationships / marriage",
                    "relationships_marriage.md",
                    "Partnership, unions, desire, marriage lots, and relationship houses.",
                    List.of("MARRIAGE_UNIONS"),
                    List.of(7, 5, 8),
                    List.of("WEDDING", "EROS"),
                    List.of(Planet.VENUS, Planet.MOON),
                    List.of(),
                    List.of("OPPOSITION")
            ),
            new TopicBucket(
                    "family_children",
                    "Family / children",
                    "family_children.md",
                    "Children, parents, siblings, lineage, and family-topic lots.",
                    List.of("CHILDREN", "FATHER", "MOTHER", "SIBLINGS"),
                    List.of(3, 4, 5, 10),
                    List.of("CHILDREN", "FATHER", "MOTHER", "SIBLINGS"),
                    List.of(Planet.SUN, Planet.MOON, Planet.VENUS, Planet.JUPITER, Planet.SATURN),
                    List.of("IMUM_COELI", "MIDHEAVEN"),
                    List.of()
            ),
            new TopicBucket(
                    "money_resources",
                    "Money / resources",
                    "money_resources.md",
                    "Possessions, fortune, resources, shared resources, gains, and support.",
                    List.of("FORTUNE_MATERIAL_CONDITION"),
                    List.of(2, 8, 11),
                    List.of("FORTUNE", "BASIS", "VICTORY"),
                    List.of(Planet.JUPITER, Planet.VENUS),
                    List.of(),
                    List.of()
            ),
            new TopicBucket(
                    "health_vitality",
                    "Health / vitality",
                    "health_vitality.md",
                    "Body, temperament, vitality doctrine, illness/labor places, and vitality lots.",
                    List.of("BODY_TEMPERAMENT", "VULNERABILITY_INDICATORS"),
                    List.of(1, 6, 8),
                    List.of("FORTUNE", "NECESSITY", "NEMESIS"),
                    List.of(Planet.SUN, Planet.MOON, Planet.MARS, Planet.SATURN),
                    List.of("ASCENDANT"),
                    List.of("SQUARE", "OPPOSITION")
            ),
            new TopicBucket(
                    "home_relocation",
                    "Home / relocation",
                    "home_relocation.md",
                    "Home, roots, parents-as-place indicators, journeys/relocation houses, and local foundations.",
                    List.of("FATHER", "MOTHER", "SIBLINGS"),
                    List.of(4, 3, 9, 12),
                    List.of("FATHER", "MOTHER", "BASIS"),
                    List.of(Planet.MOON, Planet.MERCURY, Planet.JUPITER, Planet.SATURN),
                    List.of("IMUM_COELI"),
                    List.of()
            ),
            new TopicBucket(
                    "spirituality_learning",
                    "Spirituality / learning",
                    "spirituality_learning.md",
                    "Mind, speech, learning, belief, contemplation, Spirit, and ninth/third/twelfth-place evidence.",
                    List.of("MIND_SPEECH", "CHARACTER_SOUL_QUALITY"),
                    List.of(9, 3, 12),
                    List.of("SPIRIT", "NECESSITY", "COURAGE"),
                    List.of(Planet.MERCURY, Planet.JUPITER, Planet.SUN, Planet.MOON),
                    List.of(),
                    List.of()
            ),
            new TopicBucket(
                    "crisis_conflict",
                    "Crisis / conflict",
                    "crisis_conflict.md",
                    "Conflict, vulnerability, malefic pressure, crisis houses, and hard activation contacts.",
                    List.of("VULNERABILITY_INDICATORS"),
                    List.of(6, 8, 12),
                    List.of("NECESSITY", "NEMESIS", "COURAGE"),
                    List.of(Planet.MARS, Planet.SATURN),
                    List.of(),
                    List.of("SQUARE", "OPPOSITION")
            )
    );

    List<TopicPacket> packets(Chart chart, LifeArcSynthesisTable synthesis) {
        return BUCKETS.stream()
                .map(bucket -> packet(chart, synthesis, bucket))
                .toList();
    }

    String renderIndex(Subject subject, LocalDate inquiryDate, Path outputDir, List<TopicPacket> packets) {
        StringBuilder out = new StringBuilder();
        out.append("# Topic Synthesis Packets\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Scope: local/research Markdown evidence packets grouped by practical AI analysis topics.\n");
        out.append("- Caveat: packets collect natal promise references and active timing evidence; they are not interpretations, predictions, or standalone event claims.\n\n");

        out.append("## Topic map\n\n");
        out.append("| Topic | File | Mapped natal topics | Houses | Lots | Planets/points | Active timing rows |\n");
        out.append("|---|---|---|---|---|---|---:|\n");
        for (TopicPacket packet : packets) {
            TopicBucket bucket = packet.bucket();
            out.append("| ").append(bucket.displayName())
                    .append(" | [").append(bucket.fileName()).append("](").append(bucket.fileName()).append(")")
                    .append(" | ").append(join(bucket.natalTopics()))
                    .append(" | ").append(bucket.houses().stream().map(house -> "H" + house).collect(Collectors.joining(", ")))
                    .append(" | ").append(join(bucket.lots()))
                    .append(" | ").append(joinPlanetsAndPoints(bucket.planets(), bucket.points()))
                    .append(" | ").append(packet.activeEvidence().size())
                    .append(" |\n");
        }
        out.append("\n");

        out.append("## Reading use\n\n");
        out.append("1. Open the topic file that matches the user's practical question.\n");
        out.append("2. Read the natal promise refs first: topicAssessments, houses/lords, lots, and planet/point refs.\n");
        out.append("3. Then read the active timing rows to see which chronocrator, return, direction, lunar, or transit evidence activates the same topic keys.\n");
        out.append("4. Return to `../life_arc_synthesis.md` and `../reading_output.json` when a row needs full context.\n\n");
        return out.toString();
    }

    String renderTopic(Subject subject, LocalDate inquiryDate, TopicPacket packet) {
        StringBuilder out = new StringBuilder();
        TopicBucket bucket = packet.bucket();
        out.append("# Topic Packet — ").append(bucket.displayName()).append("\n\n");
        out.append("## Scope\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Topic id: `").append(bucket.id()).append("`\n");
        out.append("- Topic scope: ").append(bucket.scope()).append("\n");
        out.append("- Output rule: evidence packet only; no final judgment, event claim, advice, or narrative interpretation is emitted here.\n\n");

        appendTaxonomy(out, packet);
        appendNatalPromise(out, packet);
        appendActiveEvidence(out, packet);
        return out.toString();
    }

    private TopicPacket packet(Chart chart, LifeArcSynthesisTable synthesis, TopicBucket bucket) {
        List<TopicAssessmentEntry> assessments = chart.getTopicAssessments() == null
                ? List.of()
                : chart.getTopicAssessments().stream()
                        .filter(topic -> bucket.natalTopics().contains(topic.topic()))
                        .toList();
        CriteriaIndex index = new CriteriaIndex();
        addBucketCriteria(index, chart, bucket);
        List<FlattenedTopicEvidence> evidenceRows = new ArrayList<>();
        for (TopicAssessmentEntry assessment : assessments) {
            for (TopicEvidenceEntry evidence : assessment.evidence()) {
                evidenceRows.add(new FlattenedTopicEvidence(assessment, evidence));
                addTopicEvidenceCriteria(index, assessment, evidence);
            }
        }

        List<HouseTopicRulerEntry> houses = houseRefs(chart, index.houseKeys());
        List<LotEntry> lots = lotRefs(chart, index.lotKeys());
        List<PlanetRef> planets = planetRefs(chart, index.planetKeys(), index.pointKeys());
        List<MatchedActiveEvidence> activeEvidence = synthesis == null
                ? List.of()
                : synthesis.evidence().stream()
                        .map(row -> match(index, row))
                        .filter(Objects::nonNull)
                        .sorted(Comparator
                                .comparingInt((MatchedActiveEvidence item) -> item.evidence().weight()).reversed()
                                .thenComparingInt(item -> item.evidence().sequenceIndex()))
                        .toList();

        return new TopicPacket(bucket, assessments, evidenceRows, houses, lots, planets, activeEvidence, index.snapshot());
    }

    private void addBucketCriteria(CriteriaIndex index, Chart chart, TopicBucket bucket) {
        for (int house : bucket.houses()) {
            index.addHouse(house, "bucket house H" + house);
            houseRef(chart, house).ifPresent(ref -> {
                index.addSign(ref.sign(), "bucket H" + house + " sign");
                index.addPlanet(ref.ruler(), "bucket H" + house + " domicile ruler");
            });
        }
        for (String lotName : bucket.lots()) {
            String normalized = lotKey(lotName);
            index.addLot(normalized, "bucket lot " + normalized);
            lotRef(chart, lotName).ifPresent(lot -> {
                index.addHouse(lot.house(), "bucket lot " + normalized + " house");
                index.addSign(lot.sign(), "bucket lot " + normalized + " sign");
                index.addPlanet(lot.ruler(), "bucket lot " + normalized + " ruler");
            });
        }
        for (Planet planet : bucket.planets()) {
            index.addPlanet(planet, "bucket planet " + planet);
            index.addPoint(planet.name(), "bucket point " + planet);
            planetPoint(chart, planet).ifPresent(point -> {
                index.addHouse(point.house(), "bucket planet " + planet + " natal house");
                index.addSign(point.sign(), "bucket planet " + planet + " natal sign");
            });
        }
        for (String point : bucket.points()) {
            index.addPoint(point, "bucket point " + point);
        }
        for (String aspect : bucket.aspects()) {
            index.addAspect(aspect, "bucket aspect " + aspect);
        }
    }

    private void addTopicEvidenceCriteria(CriteriaIndex index, TopicAssessmentEntry assessment, TopicEvidenceEntry evidence) {
        String prefix = "topic " + assessment.topic() + " / " + evidence.role();
        if (evidence.house() != null) {
            index.addHouse(evidence.house(), prefix + " house");
        }
        if (evidence.sign() != null) {
            index.addSign(evidence.sign(), prefix + " sign");
        }
        if (evidence.ruler() != null) {
            index.addPlanet(evidence.ruler(), prefix + " ruler");
        }
        if ("PLANET".equals(evidence.targetType())) {
            try {
                Planet planet = Planet.valueOf(evidence.target());
                index.addPlanet(planet, prefix + " planet target");
                index.addPoint(planet.name(), prefix + " point target");
            } catch (IllegalArgumentException ignored) {
                index.addPoint(evidence.target(), prefix + " point-like target");
            }
        }
        if ("ANGLE".equals(evidence.targetType())) {
            index.addPoint(evidence.target(), prefix + " angle target");
        }
        if ("LOT".equals(evidence.targetType())) {
            index.addLot(lotKey(evidence.target()), prefix + " lot target");
        }
    }

    private MatchedActiveEvidence match(CriteriaIndex index, LifeArcSynthesisEvidence row) {
        List<String> reasons = switch (row.keyType()) {
            case SIGN -> index.reasons(LifeArcEvidenceKeyType.SIGN, row.key());
            case HOUSE -> index.reasons(LifeArcEvidenceKeyType.HOUSE, row.key());
            case PLANET -> index.reasons(LifeArcEvidenceKeyType.PLANET, row.key());
            case POINT -> index.reasons(LifeArcEvidenceKeyType.POINT, row.key());
            case LOT -> index.reasons(LifeArcEvidenceKeyType.LOT, row.key());
            case ASPECT -> index.reasons(LifeArcEvidenceKeyType.ASPECT, row.key());
            case TECHNIQUE -> List.of();
        };
        return reasons.isEmpty() ? null : new MatchedActiveEvidence(row, reasons);
    }

    private void appendTaxonomy(StringBuilder out, TopicPacket packet) {
        TopicBucket bucket = packet.bucket();
        out.append("## Topic taxonomy mapping\n\n");
        out.append("| Criteria class | Mapped values |\n");
        out.append("|---|---|\n");
        out.append("| Natal topicAssessments | ").append(join(bucket.natalTopics())).append(" |\n");
        out.append("| Houses | ").append(bucket.houses().stream().map(house -> "H" + house).collect(Collectors.joining(", "))).append(" |\n");
        out.append("| Lots | ").append(join(bucket.lots())).append(" |\n");
        out.append("| Planets | ").append(bucket.planets().stream().map(Planet::name).collect(Collectors.joining(", "))).append(" |\n");
        out.append("| Points/angles | ").append(join(bucket.points())).append(" |\n");
        out.append("| Aspects | ").append(join(bucket.aspects())).append(" |\n");
        out.append("\n");
    }

    private void appendNatalPromise(StringBuilder out, TopicPacket packet) {
        out.append("## Natal promise refs\n\n");
        appendTopicAssessments(out, packet.topicAssessments());
        appendHouses(out, packet.houseRefs());
        appendLots(out, packet.lotRefs());
        appendPlanets(out, packet.planetRefs());
        appendTopicEvidence(out, packet.topicEvidence());
    }

    private void appendTopicAssessments(StringBuilder out, List<TopicAssessmentEntry> assessments) {
        out.append("### Mapped natal topicAssessments\n\n");
        if (assessments.isEmpty()) {
            out.append("No natal topicAssessment objects are directly mapped; this packet uses house/lord/lot criteria.\n\n");
            return;
        }
        out.append("| Topic | Method | Primary doctrine | Supporting doctrines | Evidence rows |\n");
        out.append("|---|---|---|---|---:|\n");
        for (TopicAssessmentEntry assessment : assessments) {
            out.append("| ").append(assessment.topic())
                    .append(" | ").append(assessment.methodId())
                    .append(" | ").append(assessment.primaryDoctrine())
                    .append(" | ").append(join(assessment.supportingDoctrines()))
                    .append(" | ").append(assessment.evidence().size())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendHouses(StringBuilder out, List<HouseTopicRulerEntry> houses) {
        out.append("### House/lord refs\n\n");
        if (houses.isEmpty()) {
            out.append("No house/lord refs matched this packet.\n\n");
            return;
        }
        out.append("| House | Sign | Domicile ruler |\n");
        out.append("|---:|---|---|\n");
        for (HouseTopicRulerEntry house : houses) {
            out.append("| H").append(house.house())
                    .append(" | ").append(house.sign())
                    .append(" | ").append(house.ruler())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendLots(StringBuilder out, List<LotEntry> lots) {
        out.append("### Lot refs\n\n");
        if (lots.isEmpty()) {
            out.append("No emitted lots matched this packet.\n\n");
            return;
        }
        out.append("| Lot | Display name | Doctrine | Placement | House | Ruler | Formula |\n");
        out.append("|---|---|---|---|---:|---|---|\n");
        for (LotEntry lot : lots) {
            out.append("| ").append(lot.name())
                    .append(" | ").append(cell(lot.displayName()))
                    .append(" | ").append(lot.doctrine())
                    .append(" | ").append(placement(lot.sign(), lot.degreeInSign()))
                    .append(" | H").append(lot.house())
                    .append(" | ").append(lot.ruler())
                    .append(" | ").append(cell(lot.formula()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendPlanets(StringBuilder out, List<PlanetRef> planets) {
        out.append("### Planet/point refs\n\n");
        if (planets.isEmpty()) {
            out.append("No planet/point refs matched this packet.\n\n");
            return;
        }
        out.append("| Point | Placement | House | Retrograde |\n");
        out.append("|---|---|---:|---|\n");
        for (PlanetRef planet : planets) {
            out.append("| ").append(planet.planet())
                    .append(" | ").append(placement(planet.sign(), planet.degreeInSign()))
                    .append(" | H").append(planet.house())
                    .append(" | ").append(planet.retrograde() ? "R" : "")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendTopicEvidence(StringBuilder out, List<FlattenedTopicEvidence> evidenceRows) {
        out.append("### TopicAssessment evidence refs\n\n");
        if (evidenceRows.isEmpty()) {
            out.append("No topicAssessment evidence rows are directly mapped.\n\n");
            return;
        }
        out.append("| Topic | Role | Source doctrine | Target type | Target | House | Sign | Ruler | Condition ref |\n");
        out.append("|---|---|---|---|---|---:|---|---|---|\n");
        for (FlattenedTopicEvidence row : evidenceRows) {
            TopicEvidenceEntry evidence = row.evidence();
            out.append("| ").append(row.assessment().topic())
                    .append(" | ").append(evidence.role())
                    .append(" | ").append(evidence.sourceDoctrine())
                    .append(" | ").append(evidence.targetType())
                    .append(" | ").append(evidence.target())
                    .append(" | ").append(house(evidence.house()))
                    .append(" | ").append(value(evidence.sign()))
                    .append(" | ").append(value(evidence.ruler()))
                    .append(" | ").append(value(evidence.conditionRef()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveEvidence(StringBuilder out, TopicPacket packet) {
        out.append("## Active timing evidence matching this topic\n\n");
        out.append("Rows are selected from `life_arc_synthesis.md` when their key matches the packet's natal topic, house/lord, lot, planet/point, sign, or aspect criteria.\n\n");
        if (packet.activeEvidence().isEmpty()) {
            out.append("No active life-arc synthesis rows matched this packet's criteria.\n\n");
            return;
        }
        out.append("| # | Source | Timing label | Window | Key | Weight | Matched refs | Detail |\n");
        out.append("|---:|---|---|---|---|---:|---|---|\n");
        for (MatchedActiveEvidence row : packet.activeEvidence()) {
            LifeArcSynthesisEvidence evidence = row.evidence();
            out.append("| ").append(evidence.sequenceIndex())
                    .append(" | ").append(evidence.sourceTechnique())
                    .append(" | ").append(evidence.timingLabel())
                    .append(" | ").append(window(evidence.startDateTime(), evidence.endDateTimeExclusive()))
                    .append(" | ").append(evidence.keyType()).append(": ").append(evidence.key())
                    .append(" | ").append(evidence.weight())
                    .append(" | ").append(cell(row.reasons().stream().limit(5).collect(Collectors.joining("; "))))
                    .append(" | ").append(cell(evidence.detail()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private List<HouseTopicRulerEntry> houseRefs(Chart chart, Set<String> houseKeys) {
        if (chart.getHouseTopicRulers() == null || houseKeys.isEmpty()) {
            return List.of();
        }
        return chart.getHouseTopicRulers().stream()
                .filter(house -> houseKeys.contains("H" + house.house()))
                .sorted(Comparator.comparingInt(HouseTopicRulerEntry::house))
                .toList();
    }

    private List<LotEntry> lotRefs(Chart chart, Set<String> lotKeys) {
        if (chart.getLots() == null || lotKeys.isEmpty()) {
            return List.of();
        }
        return chart.getLots().stream()
                .filter(lot -> lotKeys.contains(lotKey(lot.name())))
                .sorted(Comparator.comparing(LotEntry::name))
                .toList();
    }

    private List<PlanetRef> planetRefs(Chart chart, Set<String> planetKeys, Set<String> pointKeys) {
        Set<Planet> planets = new LinkedHashSet<>();
        for (String key : planetKeys) {
            try {
                planets.add(Planet.valueOf(key));
            } catch (IllegalArgumentException ignored) {
                // Non-planet synthesis keys are ignored for natal planet refs.
            }
        }
        for (String key : pointKeys) {
            try {
                planets.add(Planet.valueOf(key));
            } catch (IllegalArgumentException ignored) {
                // Angles/nodes are not emitted in this planet-ref table.
            }
        }
        return planets.stream()
                .map(planet -> planetRef(chart, planet))
                .flatMap(java.util.Optional::stream)
                .sorted(Comparator.comparing(ref -> ref.planet().ordinal()))
                .toList();
    }

    private java.util.Optional<HouseTopicRulerEntry> houseRef(Chart chart, int house) {
        if (chart.getHouseTopicRulers() == null) {
            return java.util.Optional.empty();
        }
        return chart.getHouseTopicRulers().stream()
                .filter(ref -> ref.house() == house)
                .findFirst();
    }

    private java.util.Optional<LotEntry> lotRef(Chart chart, String lotName) {
        if (chart.getLots() == null) {
            return java.util.Optional.empty();
        }
        return chart.getLots().stream()
                .filter(lot -> lot.name().equals(lotName))
                .findFirst();
    }

    private java.util.Optional<PlanetPointEntry> planetPoint(Chart chart, Planet planet) {
        if (chart.getPoints() == null) {
            return java.util.Optional.empty();
        }
        PointKey pointKey;
        try {
            pointKey = PointKey.of(planet);
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
        PointEntry point = chart.getPoints().get(pointKey);
        if (point instanceof PlanetPointEntry planetPoint) {
            return java.util.Optional.of(planetPoint);
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<PlanetRef> planetRef(Chart chart, Planet planet) {
        return planetPoint(chart, planet)
                .map(point -> new PlanetRef(planet, point.sign(), point.degreeInSign(), point.house(), point.retrograde()));
    }

    private String lotKey(String lotName) {
        return lotName.startsWith("LOT_") ? lotName : "LOT_" + lotName;
    }

    private String fileLink(Path baseDir, String fileName) {
        Path path = baseDir.resolve(fileName);
        return "[" + fileName + "](" + path.getFileName() + ")";
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "—";
        }
        return String.join(", ", values);
    }

    private String joinPlanetsAndPoints(List<Planet> planets, List<String> points) {
        List<String> values = new ArrayList<>();
        values.addAll(planets.stream().map(Planet::name).toList());
        values.addAll(points);
        return join(values);
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String house(Integer house) {
        return house == null ? "—" : "H" + house;
    }

    private String value(Object value) {
        return value == null ? "—" : value.toString();
    }

    private String window(OffsetDateTime start, OffsetDateTime end) {
        if (start == null) {
            return "—";
        }
        if (end == null || end.equals(start)) {
            return format(start);
        }
        return format(start) + " → " + format(end);
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }

    private String cell(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.replace("|", "\\|").replace("\r", " ").replace("\n", " ");
    }

    record TopicPacket(
            TopicBucket bucket,
            List<TopicAssessmentEntry> topicAssessments,
            List<FlattenedTopicEvidence> topicEvidence,
            List<HouseTopicRulerEntry> houseRefs,
            List<LotEntry> lotRefs,
            List<PlanetRef> planetRefs,
            List<MatchedActiveEvidence> activeEvidence,
            CriteriaSnapshot criteria
    ) {}

    record TopicBucket(
            String id,
            String displayName,
            String fileName,
            String scope,
            List<String> natalTopics,
            List<Integer> houses,
            List<String> lots,
            List<Planet> planets,
            List<String> points,
            List<String> aspects
    ) {}

    record FlattenedTopicEvidence(TopicAssessmentEntry assessment, TopicEvidenceEntry evidence) {}

    record MatchedActiveEvidence(LifeArcSynthesisEvidence evidence, List<String> reasons) {}

    record PlanetRef(Planet planet, ZodiacSign sign, double degreeInSign, int house, boolean retrograde) {}

    record CriteriaSnapshot(
            Set<String> signKeys,
            Set<String> houseKeys,
            Set<String> planetKeys,
            Set<String> pointKeys,
            Set<String> lotKeys,
            Set<String> aspectKeys
    ) {}

    private static final class CriteriaIndex {
        private final Map<LifeArcEvidenceKeyType, Map<String, List<String>>> reasons = new LinkedHashMap<>();

        void addSign(ZodiacSign sign, String reason) {
            if (sign != null) {
                add(LifeArcEvidenceKeyType.SIGN, sign.name(), reason);
            }
        }

        void addHouse(Integer house, String reason) {
            if (house != null) {
                add(LifeArcEvidenceKeyType.HOUSE, "H" + house, reason);
            }
        }

        void addPlanet(Planet planet, String reason) {
            if (planet != null) {
                add(LifeArcEvidenceKeyType.PLANET, planet.name(), reason);
            }
        }

        void addPoint(String point, String reason) {
            if (point != null && !point.isBlank()) {
                add(LifeArcEvidenceKeyType.POINT, point, reason);
            }
        }

        void addLot(String lotName, String reason) {
            if (lotName != null && !lotName.isBlank()) {
                add(LifeArcEvidenceKeyType.LOT, lotName, reason);
            }
        }

        void addAspect(String aspect, String reason) {
            if (aspect != null && !aspect.isBlank()) {
                add(LifeArcEvidenceKeyType.ASPECT, aspect, reason);
            }
        }

        List<String> reasons(LifeArcEvidenceKeyType type, String key) {
            return reasons.getOrDefault(type, Map.of()).getOrDefault(key, List.of());
        }

        Set<String> houseKeys() {
            return keys(LifeArcEvidenceKeyType.HOUSE);
        }

        Set<String> lotKeys() {
            return keys(LifeArcEvidenceKeyType.LOT);
        }

        Set<String> planetKeys() {
            return keys(LifeArcEvidenceKeyType.PLANET);
        }

        Set<String> pointKeys() {
            return keys(LifeArcEvidenceKeyType.POINT);
        }

        CriteriaSnapshot snapshot() {
            return new CriteriaSnapshot(
                    keys(LifeArcEvidenceKeyType.SIGN),
                    keys(LifeArcEvidenceKeyType.HOUSE),
                    keys(LifeArcEvidenceKeyType.PLANET),
                    keys(LifeArcEvidenceKeyType.POINT),
                    keys(LifeArcEvidenceKeyType.LOT),
                    keys(LifeArcEvidenceKeyType.ASPECT)
            );
        }

        private void add(LifeArcEvidenceKeyType type, String key, String reason) {
            reasons.computeIfAbsent(type, ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(key, ignored -> new ArrayList<>());
            List<String> existing = reasons.get(type).get(key);
            if (!existing.contains(reason)) {
                existing.add(reason);
            }
        }

        private Set<String> keys(LifeArcEvidenceKeyType type) {
            return new LinkedHashSet<>(reasons.getOrDefault(type, Map.of()).keySet());
        }
    }
}
