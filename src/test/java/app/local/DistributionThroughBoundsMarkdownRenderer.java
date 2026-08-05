package app.local;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import app.chart.data.AspectType;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.distribution.DistributionContactType;
import app.reading.lifearc.distribution.DistributionThroughBoundsContact;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;

final class DistributionThroughBoundsMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, DistributionThroughBoundsTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Distributions Through Bounds\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActivePeriod(out, table);
        appendOverview(out, table);
        appendDetailedPeriods(out, table);
        return out.toString();
    }

    String renderExtended(Subject subject, LocalDate inquiryDate, List<DistributionThroughBoundsTable> tables) {
        StringBuilder out = new StringBuilder();
        out.append("# Extended Distributions Through Bounds\n\n");
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        if (tables == null || tables.isEmpty()) {
            out.append("- Extended directed points: `0`\n\n");
            out.append("No extended distribution tables were available. This usually means the natal chart did not contain the required enriched hyleg/lot data.\n\n");
            return out.toString();
        }
        out.append("- Method: `").append(tables.get(0).methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(tables.get(0).primaryDoctrine()).append("`\n");
        out.append("- Terms: `").append(tables.get(0).terms()).append("`\n");
        out.append("- Directed points: `").append(tables.size()).append("` — ");
        for (int i = 0; i < tables.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append("`").append(tables.get(i).directedPoint()).append("`");
        }
        out.append("\n\n");
        out.append("This local research file extends distributions through Egyptian bounds to selected hyleg, Midheaven, Valens Fortune, Valens Spirit, Sun, and Moon where available. ")
                .append("The Ascendant baseline remains in `distributions_through_bounds.md`. Each table keeps its own timing/contact method label; rows are timing evidence, not standalone event claims.\n\n");

        appendExtendedActiveOverview(out, tables);
        for (DistributionThroughBoundsTable table : tables) {
            out.append("## Directed point: `").append(table.directedPoint()).append("`\n\n");
            appendSummary(out, subject, inquiryDate, table);
            appendActivePeriod(out, table);
            appendOverview(out, table);
            appendDetailedPeriods(out, table);
        }
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, DistributionThroughBoundsTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Directed point: `").append(table.directedPoint()).append("` at `")
                .append(placement(table.directedPointSign(), table.directedPointDegreeInSign())).append("`\n");
        out.append("- Terms: `").append(table.terms()).append("`\n");
        out.append("- Birth latitude: `").append(formatDecimal(table.birthLatitude(), 4)).append("°`\n");
        out.append("- Timing method: `").append(table.timingMethod()).append("`\n");
        out.append("- Contact method: `").append(table.contactMethod()).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Coverage: `").append(format(table.coverageStartDateTime())).append("` to `")
                .append(format(table.coverageEndDateTimeExclusive())).append("`\n\n");
        out.append("This local research table directs `").append(table.directedPoint()).append("` through Egyptian bounds using the table's stated coordinate method. ")
                .append("One coordinate degree is converted to one mean tropical year. Planet contacts are exact directed-point hits to natal traditional planet bodies and Ptolemaic rays; they are timing evidence, not standalone event claims.\n\n");
    }

    private void appendExtendedActiveOverview(StringBuilder out, List<DistributionThroughBoundsTable> tables) {
        out.append("## Active directed-point bounds\n\n");
        out.append("| Directed point | Placement | Active bound | Directed point house | Window | Contacts |\n");
        out.append("|---|---|---|---:|---|---:|\n");
        for (DistributionThroughBoundsTable table : tables) {
            DistributionThroughBoundsPeriod active = activePeriod(table);
            if (active == null) {
                out.append("| ").append(table.directedPoint())
                        .append(" | ").append(placement(table.directedPointSign(), table.directedPointDegreeInSign()))
                        .append(" | — | — | — | 0 |\n");
                continue;
            }
            out.append("| ").append(table.directedPoint())
                    .append(" | ").append(placement(table.directedPointSign(), table.directedPointDegreeInSign()))
                    .append(" | ").append(active.sign()).append(" ")
                    .append(formatDecimal(active.boundStartDegreeInSign(), 2)).append("–")
                    .append(formatDecimal(active.boundEndDegreeInSign(), 2)).append("° — ").append(active.boundRuler())
                    .append(" | H").append(table.directedPointHouse())
                    .append(" | ").append(format(active.startDateTime())).append(" → ").append(format(active.endDateTimeExclusive()))
                    .append(" | ").append(active.contacts().size())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActivePeriod(StringBuilder out, DistributionThroughBoundsTable table) {
        DistributionThroughBoundsPeriod active = activePeriod(table);
        if (active == null) {
            return;
        }
        out.append("## Active inquiry bound period\n\n");
        appendOverviewHeader(out, false);
        appendOverviewRow(out, active, false);
        out.append("\n");
        appendContacts(out, table.directedPoint(), active.contacts());
    }

    private DistributionThroughBoundsPeriod activePeriod(DistributionThroughBoundsTable table) {
        return table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
    }

    private void appendOverview(StringBuilder out, DistributionThroughBoundsTable table) {
        out.append("## 0–100 overview\n\n");

        appendOverviewHeader(out, true);
        for (DistributionThroughBoundsPeriod period : table.periods()) {
            appendOverviewRow(out, period, true);
        }
        out.append("\n");
    }

    private void appendOverviewHeader(StringBuilder out, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        out.append(" # | Cycle | Bound | Directed span | Ages | Start | End | Duration | Contacts |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        out.append("---:|---:|---|---|---:|---|---|---:|---:|\n");
    }

    private void appendOverviewRow(StringBuilder out, DistributionThroughBoundsPeriod period, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" ").append(period.activeForInquiry() ? "★" : "").append(" |");
        }
        out.append(" ").append(period.sequenceIndex())
                .append(" | ").append(period.cycleNumber())
                .append(" | ").append(period.sign()).append(" ")
                .append(formatDecimal(period.boundStartDegreeInSign(), 2)).append("–")
                .append(formatDecimal(period.boundEndDegreeInSign(), 2)).append("° — ").append(period.boundRuler())
                .append(" | ").append(placement(period.sign(), period.directedStartDegreeInSign()))
                .append(" → ").append(placement(period.sign(), period.directedEndDegreeInSign()))
                .append(" | ").append(formatDecimal(period.startAgeYears(), 2)).append("–")
                .append(formatDecimal(period.endAgeYearsExclusive(), 2))
                .append(" | ").append(format(period.startDateTime()))
                .append(" | ").append(format(period.endDateTimeExclusive()))
                .append(" | ").append(duration(period.startDateTime(), period.endDateTimeExclusive()))
                .append(" | ").append(period.contacts().size())
                .append(" |\n");
    }

    private void appendDetailedPeriods(StringBuilder out, DistributionThroughBoundsTable table) {
        out.append("## Detailed bound periods and contacts\n\n");
        for (DistributionThroughBoundsPeriod period : table.periods()) {
            out.append("### #").append(period.sequenceIndex())
                    .append(" — ").append(period.sign()).append(" ")
                    .append(formatDecimal(period.boundStartDegreeInSign(), 2)).append("–")
                    .append(formatDecimal(period.boundEndDegreeInSign(), 2)).append("° — ").append(period.boundRuler());
            if (period.activeForInquiry()) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("- Directed span: `").append(placement(period.sign(), period.directedStartDegreeInSign()))
                    .append("` to `").append(placement(period.sign(), period.directedEndDegreeInSign())).append("`\n");
            out.append("- Age span: `").append(formatDecimal(period.startAgeYears(), 4)).append("` to `")
                    .append(formatDecimal(period.endAgeYearsExclusive(), 4)).append("`\n");
            out.append("- Dates: `").append(format(period.startDateTime())).append("` to `")
                    .append(format(period.endDateTimeExclusive())).append("`\n\n");
            appendContacts(out, table.directedPoint(), period.contacts());
        }
    }

    private void appendContacts(StringBuilder out, String directedPoint, List<DistributionThroughBoundsContact> contacts) {
        out.append("Directed ").append(directedPoint).append(" contacts in this bound\n\n");
        if (contacts == null || contacts.isEmpty()) {
            out.append("No exact body/ray contacts in this period.\n\n");
            return;
        }
        out.append("| Date | Age | Source planet | Contact | Directed placement | Bound lord | Source natal placement | Source natal house |\n");
        out.append("|---|---:|---|---|---|---|---|---:|\n");
        for (DistributionThroughBoundsContact contact : contacts) {
            out.append("| ").append(format(contact.dateTime()))
                    .append(" | ").append(formatDecimal(contact.ageYears(), 2))
                    .append(" | ").append(contact.sourcePlanet())
                    .append(" | ").append(contactCell(contact))
                    .append(" | ").append(placement(contact.directedSign(), contact.directedDegreeInSign()))
                    .append(" | ").append(contact.boundRulerAtContact())
                    .append(" | ").append(placement(contact.sourceNatalSign(), contact.sourceNatalDegreeInSign()))
                    .append(" | H").append(contact.sourceNatalHouse())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private String contactCell(DistributionThroughBoundsContact contact) {
        if (contact.contactType() == DistributionContactType.BODY) {
            return "BODY " + aspect(contact.aspect());
        }
        return "RAY " + aspect(contact.aspect()) + " " + contact.rayDirection();
    }

    private String aspect(AspectType aspect) {
        return aspect == null ? "—" : aspect.name();
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }

    private String duration(OffsetDateTime start, OffsetDateTime end) {
        Duration duration = Duration.between(start, end);
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        if (hours == 0) {
            return days + "d";
        }
        return days + "d " + hours + "h";
    }
}
