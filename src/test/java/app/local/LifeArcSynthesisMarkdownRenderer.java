package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.synthesis.LifeArcEvidenceKeyType;
import app.reading.lifearc.synthesis.LifeArcSynthesisEvidence;
import app.reading.lifearc.synthesis.LifeArcSynthesisGroup;
import app.reading.lifearc.synthesis.LifeArcSynthesisTable;

final class LifeArcSynthesisMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LifeArcSynthesisTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Life-Arc Evidence Synthesis\n\n");
        appendSummary(out, subject, table);
        appendTopGroups(out, table.groups());
        appendGroupsByType(out, table.groups());
        appendEvidenceBySource(out, table.evidence());
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LifeArcSynthesisTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        out.append("- Inquiry date/time: `").append(format(table.inquiryDateTime())).append("`\n");
        out.append("- Completed age: `").append(table.completedAgeYears()).append("`\n");
        out.append("- Active birthday-year window: `").append(format(table.activeYearStartDateTime()))
                .append("` to `").append(format(table.activeYearEndDateTimeExclusive())).append("`\n");
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Synthesis method: `").append(table.synthesisMethod()).append("`\n");
        out.append("- Evidence rows: `").append(table.evidence().size()).append("`\n");
        out.append("- Evidence groups: `").append(table.groups().size()).append("`\n\n");
        out.append("This local research file groups active life-arc calculation evidence by repeated signs, houses, planets, points, lots, and aspects. ")
                .append("Weights are class-calibrated evidence-density scores for prioritizing review across chronocrator, return-chart, direction/contact, transit, and lunar/eclipse rows; they are not narrative judgments and do not claim events by themselves. ")
                .append("Primary-direction variants such as normalized converse rows and the mundane/semi-arc prototype are separately labelled and intentionally lower-weight until validated.\n\n");
    }

    private void appendTopGroups(StringBuilder out, List<LifeArcSynthesisGroup> groups) {
        out.append("## Top evidence groups\n\n");
        appendGroupTable(out, groups.stream().limit(25).toList());
        out.append("\n");
    }

    private void appendGroupsByType(StringBuilder out, List<LifeArcSynthesisGroup> groups) {
        out.append("## Groups by key type\n\n");
        Map<LifeArcEvidenceKeyType, List<LifeArcSynthesisGroup>> byType = groups.stream()
                .collect(Collectors.groupingBy(LifeArcSynthesisGroup::keyType));
        for (LifeArcEvidenceKeyType type : LifeArcEvidenceKeyType.values()) {
            List<LifeArcSynthesisGroup> typed = byType.get(type);
            if (typed == null || typed.isEmpty()) {
                continue;
            }
            out.append("### ").append(type).append("\n\n");
            appendGroupTable(out, typed.stream().limit(15).toList());
            out.append("\n");
        }
    }

    private void appendGroupTable(StringBuilder out, List<LifeArcSynthesisGroup> groups) {
        out.append("| Key type | Key | Total weight | Evidence count | Evidence rows |\n");
        out.append("|---|---|---:|---:|---|\n");
        for (LifeArcSynthesisGroup group : groups) {
            out.append("| ").append(group.keyType())
                    .append(" | ").append(group.key())
                    .append(" | ").append(group.totalWeight())
                    .append(" | ").append(group.evidenceCount())
                    .append(" | ").append(group.evidenceSequenceIndexes().stream().map(index -> "#" + index).collect(Collectors.joining(", ")))
                    .append(" |\n");
        }
    }

    private void appendEvidenceBySource(StringBuilder out, List<LifeArcSynthesisEvidence> evidence) {
        out.append("## Evidence rows by source\n\n");
        Map<String, List<LifeArcSynthesisEvidence>> bySource = evidence.stream()
                .collect(Collectors.groupingBy(LifeArcSynthesisEvidence::sourceTechnique));
        List<String> sources = bySource.keySet().stream().sorted().toList();
        for (String source : sources) {
            out.append("### ").append(source).append("\n\n");
            appendEvidenceTable(out, bySource.get(source).stream()
                    .sorted(Comparator.comparingInt(LifeArcSynthesisEvidence::sequenceIndex))
                    .toList());
            out.append("\n");
        }
    }

    private void appendEvidenceTable(StringBuilder out, List<LifeArcSynthesisEvidence> evidence) {
        out.append("| # | Timing label | Window | Weight class | Key type | Key | Weight | Detail |\n");
        out.append("|---:|---|---|---|---|---|---:|---|\n");
        for (LifeArcSynthesisEvidence row : evidence) {
            out.append("| ").append(row.sequenceIndex())
                    .append(" | ").append(row.timingLabel())
                    .append(" | ").append(window(row.startDateTime(), row.endDateTimeExclusive()))
                    .append(" | ").append(row.weightClass())
                    .append(" | ").append(row.keyType())
                    .append(" | ").append(displayKey(row))
                    .append(" | ").append(row.weight())
                    .append(" | ").append(row.detail())
                    .append(" |\n");
        }
    }

    private String displayKey(LifeArcSynthesisEvidence row) {
        if (row.sign() != null) {
            return placement(row.sign());
        }
        if (row.house() != null) {
            return "H" + row.house();
        }
        if (row.planet() != null) {
            return planet(row.planet());
        }
        return row.key();
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

    private String placement(ZodiacSign sign) {
        return sign.name();
    }

    private String planet(Planet planet) {
        return planet.name();
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    @SuppressWarnings("unused")
    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }
}
