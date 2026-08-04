package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.AspectType;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.transit.MonthlyTransitActivationContact;
import app.reading.lifearc.transit.MonthlyTransitActivationReason;
import app.reading.lifearc.transit.MonthlyTransitCheckpointRow;
import app.reading.lifearc.transit.MonthlyTransitCheckpointTable;
import app.reading.lifearc.transit.MonthlyTransitNatalContact;
import app.reading.lifearc.transit.MonthlyTransitPointEntry;

final class MonthlyTransitCheckpointMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, MonthlyTransitCheckpointTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Monthly Transit Checkpoints Overview\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveRow(out, table);
        appendTopActivationContacts(out, table);
        appendOverview(out, table);
        appendFullDetailsNote(out);
        return out.toString();
    }

    String renderFullDetails(Subject subject, LocalDate inquiryDate, MonthlyTransitCheckpointTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Monthly Transit Checkpoints — Full Details\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveRow(out, table);
        appendTopActivationContacts(out, table);
        appendOverview(out, table);
        appendDetailedRows(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, MonthlyTransitCheckpointTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Checkpoint method: `").append(table.checkpointMethod()).append("`\n");
        out.append("- Contact method: `").append(table.contactMethod()).append("`\n");
        out.append("- Activation contact method: `").append(table.activationContactMethod()).append("`\n");
        out.append("- Conjunction orb: `").append(formatDecimal(table.conjunctionOrbDegrees(), 2)).append("°`\n");
        out.append("- Activation aspect orb: `").append(formatDecimal(table.activationAspectOrbDegrees(), 2)).append("°`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n\n");
        out.append("Each checkpoint is the native's local birth date/time advanced by whole months, so the day-of-month follows the birth day and shorter months use the normal local-date plus-months end-of-month convention. ")
                .append("Rows are monthly snapshots, not continuous transit searches. Activated contacts retain Ptolemaic degree aspects when the natal target or transiting point is activated by annual/monthly profection. Use them for compact validation and research before event-date synthesis. ")
                .append("The overview file starts with active/top evidence and keeps the 0–100 overview compact; the generated `monthly_transit_checkpoints_full.md` companion keeps all checkpoint overlay details.\n\n");
    }

    private void appendActiveRow(StringBuilder out, MonthlyTransitCheckpointTable table) {
        MonthlyTransitCheckpointRow active = table.rows().stream()
                .filter(MonthlyTransitCheckpointRow::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return;
        }
        out.append("## Active inquiry checkpoint\n\n");
        appendOverviewHeader(out, false);
        appendOverviewRow(out, active, false);
        out.append("\n");
    }

    private void appendTopActivationContacts(StringBuilder out, MonthlyTransitCheckpointTable table) {
        out.append("## Top activated transit aspects\n\n");
        List<ActivationContactSummary> top = table.rows().stream()
                .flatMap(row -> row.activationContacts().stream().map(contact -> new ActivationContactSummary(row, contact)))
                .sorted(Comparator
                        .comparingInt((ActivationContactSummary summary) -> summary.contact().activationWeight()).reversed()
                        .thenComparingDouble(summary -> summary.contact().orbFromExactDegrees())
                        .thenComparing(summary -> summary.row().checkpointDateTime()))
                .limit(30)
                .toList();
        if (top.isEmpty()) {
            out.append("No activated Ptolemaic degree aspects are present in this age range.\n\n");
            return;
        }
        out.append("Highest-weight profection-filtered checkpoint contacts across the covered age range. Open `monthly_transit_checkpoints_full.md` for each checkpoint's full point table and all contacts.\n\n");
        out.append("| Checkpoint | Age/M | Date | Transit point | Aspect | Natal target | Target placement | House | Orb | Weight | Reasons |\n");
        out.append("|---:|---:|---|---|---|---|---|---:|---:|---:|---|\n");
        for (ActivationContactSummary summary : top) {
            MonthlyTransitCheckpointRow row = summary.row();
            MonthlyTransitActivationContact contact = summary.contact();
            out.append("| ").append(row.checkpointNumber())
                    .append(" | ").append(row.ageYears()).append("/M").append(row.monthInYear())
                    .append(" | ").append(format(row.checkpointDateTime()))
                    .append(" | ").append(contact.transitPoint())
                    .append(" | ").append(aspect(contact.aspect()))
                    .append(" | ").append(contact.natalTargetName())
                    .append(" | ").append(placement(contact.natalTargetSign(), contact.natalTargetDegreeInSign()))
                    .append(" | H").append(contact.natalTargetHouse())
                    .append(" | ").append(formatDecimal(contact.orbFromExactDegrees(), 2)).append("°")
                    .append(" | ").append(contact.activationWeight())
                    .append(" | ").append(reasons(contact.activationReasons()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, MonthlyTransitCheckpointTable table) {
        out.append("## 0–100 overview\n\n");
        appendOverviewHeader(out, true);
        for (MonthlyTransitCheckpointRow row : table.rows()) {
            appendOverviewRow(out, row, true);
        }
        out.append("\n");
    }

    private void appendOverviewHeader(StringBuilder out, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        out.append(" # | Age/M | Checkpoint | Annual profection | Monthly profection | In annual sign | In monthly sign | Conj | Activated aspects | Tight activated ≤1° |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        out.append("---:|---:|---|---|---|---|---|---:|---:|---:|\n");
    }

    private void appendOverviewRow(StringBuilder out, MonthlyTransitCheckpointRow row, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" ").append(row.activeForInquiry() ? "★" : "").append(" |");
        }
        out.append(" ").append(row.checkpointNumber())
                .append(" | ").append(row.ageYears()).append("/M").append(row.monthInYear())
                .append(" | ").append(format(row.checkpointDateTime()))
                .append(" | H").append(row.annualProfectedHouse()).append(" ").append(row.annualProfectedSign()).append(" — ").append(row.lordOfYear())
                .append(" | H").append(row.monthlyProfectedHouse()).append(" ").append(row.monthlyProfectedSign()).append(" — ").append(row.lordOfMonth())
                .append(" | ").append(joinPoints(row.transitPointsInAnnualProfectedSign()))
                .append(" | ").append(joinPoints(row.transitPointsInMonthlyProfectedSign()))
                .append(" | ").append(row.conjunctions().size())
                .append(" | ").append(row.activationContacts().size())
                .append(" | ").append(tightActivationContactCount(row))
                .append(" |\n");
    }

    private void appendFullDetailsNote(StringBuilder out) {
        out.append("## Full checkpoint details\n\n");
        out.append("The complete point overlays, conjunctions, and activated aspect tables for every monthly checkpoint are generated separately as `monthly_transit_checkpoints_full.md`. Use this overview first, then open the full file only to audit a specific checkpoint or contact.\n\n");
    }

    private void appendDetailedRows(StringBuilder out, MonthlyTransitCheckpointTable table) {
        out.append("## Detailed checkpoint overlays and contacts\n\n");
        for (MonthlyTransitCheckpointRow row : table.rows()) {
            out.append("### #").append(row.checkpointNumber())
                    .append(" — age ").append(row.ageYears()).append("/M").append(row.monthInYear());
            if (row.activeForInquiry()) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("- Checkpoint: `").append(format(row.checkpointDateTime())).append("`\n");
            out.append("- Period end: `").append(format(row.periodEndDateTimeExclusive())).append("`\n");
            out.append("- Annual profection: `H").append(row.annualProfectedHouse()).append(" ")
                    .append(row.annualProfectedSign()).append(" — ").append(row.lordOfYear()).append("`\n");
            out.append("- Monthly profection: `H").append(row.monthlyProfectedHouse()).append(" ")
                    .append(row.monthlyProfectedSign()).append(" — ").append(row.lordOfMonth()).append("`\n\n");

            appendTransitPoints(out, row);
            appendContacts(out, row);
            appendActivationContacts(out, row);
        }
    }

    private void appendTransitPoints(StringBuilder out, MonthlyTransitCheckpointRow row) {
        out.append("Transit points over natal houses\n\n");
        out.append("| Point | Type | Transit placement | Transit house | Natal house overlay | Retrograde |\n");
        out.append("|---|---|---|---:|---:|---|\n");
        for (MonthlyTransitPointEntry point : row.transitPoints()) {
            out.append("| ").append(point.point())
                    .append(" | ").append(point.type())
                    .append(" | ").append(placement(point.sign(), point.degreeInSign()))
                    .append(" | H").append(point.transitHouse())
                    .append(" | H").append(point.natalHouseOverlay())
                    .append(" | ").append(point.retrograde() ? "R" : "")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendContacts(StringBuilder out, MonthlyTransitCheckpointRow row) {
        out.append("Transit conjunctions to natal points/lots\n\n");
        if (row.conjunctions().isEmpty()) {
            out.append("No conjunctions within configured orb.\n\n");
            return;
        }
        out.append("| Transit point | Natal target type | Natal target | Natal target placement | Natal house | Orb |\n");
        out.append("|---|---|---|---|---:|---:|\n");
        for (MonthlyTransitNatalContact contact : sortedContacts(row.conjunctions())) {
            out.append("| ").append(contact.transitPoint())
                    .append(" | ").append(contact.natalTargetType())
                    .append(" | ").append(contact.natalTargetName())
                    .append(" | ").append(placement(contact.natalTargetSign(), contact.natalTargetDegreeInSign()))
                    .append(" | H").append(contact.natalTargetHouse())
                    .append(" | ").append(formatDecimal(contact.orbDegrees(), 2)).append("°")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActivationContacts(StringBuilder out, MonthlyTransitCheckpointRow row) {
        out.append("Activated transit aspects to profection-triggered natal targets\n\n");
        if (row.activationContacts().isEmpty()) {
            out.append("No activated Ptolemaic degree aspects within configured orb.\n\n");
            return;
        }
        out.append("| Transit point | Natal target type | Natal target | Aspect | Target placement | Natal house | Orb | Weight | Reasons |\n");
        out.append("|---|---|---|---|---|---:|---:|---:|---|\n");
        for (MonthlyTransitActivationContact contact : sortedActivationContacts(row.activationContacts())) {
            out.append("| ").append(contact.transitPoint())
                    .append(" | ").append(contact.natalTargetType())
                    .append(" | ").append(contact.natalTargetName())
                    .append(" | ").append(aspect(contact.aspect()))
                    .append(" | ").append(placement(contact.natalTargetSign(), contact.natalTargetDegreeInSign()))
                    .append(" | H").append(contact.natalTargetHouse())
                    .append(" | ").append(formatDecimal(contact.orbFromExactDegrees(), 2)).append("°")
                    .append(" | ").append(contact.activationWeight())
                    .append(" | ").append(reasons(contact.activationReasons()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private List<MonthlyTransitNatalContact> sortedContacts(List<MonthlyTransitNatalContact> contacts) {
        return contacts.stream()
                .sorted(Comparator
                        .comparing((MonthlyTransitNatalContact contact) -> contact.transitPoint().ordinal())
                        .thenComparingDouble(MonthlyTransitNatalContact::orbDegrees)
                        .thenComparing(MonthlyTransitNatalContact::natalTargetName))
                .toList();
    }

    private List<MonthlyTransitActivationContact> sortedActivationContacts(List<MonthlyTransitActivationContact> contacts) {
        return contacts.stream()
                .sorted(Comparator
                        .comparingInt(MonthlyTransitActivationContact::activationWeight).reversed()
                        .thenComparing(contact -> contact.transitPoint().ordinal())
                        .thenComparing(contact -> contact.aspect().ordinal())
                        .thenComparingDouble(MonthlyTransitActivationContact::orbFromExactDegrees)
                        .thenComparing(MonthlyTransitActivationContact::natalTargetName))
                .toList();
    }

    private String joinPoints(List<PointKey> points) {
        if (points == null || points.isEmpty()) {
            return "—";
        }
        return points.stream().map(PointKey::name).collect(Collectors.joining(", "));
    }

    private long tightActivationContactCount(MonthlyTransitCheckpointRow row) {
        return row.activationContacts().stream()
                .filter(contact -> contact.orbFromExactDegrees() <= 1.0)
                .count();
    }

    private String aspect(AspectType aspect) {
        return aspect == null ? "—" : aspect.name();
    }

    private String reasons(List<MonthlyTransitActivationReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "—";
        }
        return reasons.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "% ." + places + "f", value).trim();
    }

    private record ActivationContactSummary(MonthlyTransitCheckpointRow row, MonthlyTransitActivationContact contact) {}
}
