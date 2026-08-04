package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonRow;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnNatalContact;
import app.reading.lifearc.solarreturn.SolarReturnPointOverlay;

final class SolarReturnNatalComparisonMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, SolarReturnNatalComparisonTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Solar Return to Natal Comparison\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveRow(out, table);
        appendOverview(out, table);
        appendDetailedRows(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, SolarReturnNatalComparisonTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Source solar-return method: `").append(table.sourceSolarReturnMethodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Natal overlay method: `").append(table.natalOverlayMethod()).append("`\n");
        out.append("- Conjunction orb: `").append(formatDecimal(table.conjunctionOrbDegrees(), 2)).append("°`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n\n");
        out.append("This file compares each exact solar-return chart to the natal chart. ")
                .append("Solar-return points are overlaid onto natal whole-sign houses, annual profection context is repeated for each age, ")
                .append("and ecliptic-longitude conjunctions from solar-return points to natal points/lots are listed. ")
                .append("Use it as timing evidence for backward validation and forward research, not as a standalone event judgment.\n\n");
    }

    private void appendActiveRow(StringBuilder out, SolarReturnNatalComparisonTable table) {
        SolarReturnNatalComparisonRow active = table.rows().stream()
                .filter(SolarReturnNatalComparisonRow::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return;
        }
        out.append("## Active inquiry solar return\n\n");
        out.append("| Age | Period | Profection | Lord of Year in SR | SR Asc over natal | SR MC over natal | SR points in profected sign | SR points overlaying profected house |\n");
        out.append("|---:|---|---|---|---:|---:|---|---|\n");
        appendOverviewRow(out, active, false);
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, SolarReturnNatalComparisonTable table) {
        out.append("## 0–100 overview\n\n");
        out.append("| Active | Age | Period | Profection | Lord of Year in SR | SR Asc over natal | SR MC over natal | SR points in profected sign | SR points overlaying profected house | Contacts | Tight contacts ≤1° |\n");
        out.append("|---|---:|---|---|---|---:|---:|---|---|---:|---:|\n");
        for (SolarReturnNatalComparisonRow row : table.rows()) {
            appendOverviewRow(out, row, true);
        }
        out.append("\n");
    }

    private void appendOverviewRow(StringBuilder out, SolarReturnNatalComparisonRow row, boolean includeActive) {
        out.append("| ");
        if (includeActive) {
            out.append(row.activeForInquiry() ? "★" : "").append(" | ");
        }
        out.append(row.ageYears())
                .append(" | ").append(format(row.returnDateTime())).append(" to ").append(format(row.periodEndDateTimeExclusive()))
                .append(" | H").append(row.profectedHouse()).append(" ").append(row.profectedSign()).append(" — ").append(row.lordOfYear())
                .append(" | ").append(lordOfYearCell(row.lordOfYearOverlay()))
                .append(" | H").append(row.ascendantNatalHouseOverlay())
                .append(" | H").append(row.midheavenNatalHouseOverlay())
                .append(" | ").append(joinPoints(row.solarReturnPointsInProfectedSign()))
                .append(" | ").append(joinPoints(row.solarReturnPointsOverlayingProfectedHouse()));
        if (includeActive) {
            out.append(" | ").append(row.conjunctions().size())
                    .append(" | ").append(tightContactCount(row));
        }
        out.append(" |\n");
    }

    private void appendDetailedRows(StringBuilder out, SolarReturnNatalComparisonTable table) {
        out.append("## Detailed solar-return overlays and contacts\n\n");
        for (SolarReturnNatalComparisonRow row : table.rows()) {
            out.append("### Age ").append(row.ageYears());
            if (row.activeForInquiry()) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("- Solar-return period: `").append(format(row.returnDateTime())).append("` to `")
                    .append(format(row.periodEndDateTimeExclusive())).append("`\n");
            out.append("- Annual profection context: `H").append(row.profectedHouse()).append(" ")
                    .append(row.profectedSign()).append(" — ").append(row.lordOfYear()).append("`\n");
            out.append("- Solar-return Ascendant overlays natal house: `H").append(row.ascendantNatalHouseOverlay()).append("`\n");
            out.append("- Solar-return Midheaven overlays natal house: `H").append(row.midheavenNatalHouseOverlay()).append("`\n");
            out.append("- Lord of Year in solar return: `").append(lordOfYearCell(row.lordOfYearOverlay())).append("`\n\n");

            appendPointOverlays(out, row);
            appendContacts(out, row);
        }
    }

    private void appendPointOverlays(StringBuilder out, SolarReturnNatalComparisonRow row) {
        out.append("Solar-return points over natal houses\n\n");
        out.append("| Point | Type | SR placement | SR house | Natal house overlay | Retrograde |\n");
        out.append("|---|---|---|---:|---:|---|\n");
        for (SolarReturnPointOverlay overlay : row.pointOverlays()) {
            out.append("| ").append(overlay.point())
                    .append(" | ").append(overlay.type())
                    .append(" | ").append(placement(overlay.sign(), overlay.degreeInSign()))
                    .append(" | ").append(house(overlay.solarReturnHouse()))
                    .append(" | H").append(overlay.natalHouseOverlay())
                    .append(" | ").append(retrograde(overlay))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendContacts(StringBuilder out, SolarReturnNatalComparisonRow row) {
        out.append("Solar-return conjunctions to natal points/lots\n\n");
        if (row.conjunctions().isEmpty()) {
            out.append("No conjunctions within configured orb.\n\n");
            return;
        }
        out.append("| SR point | Natal target type | Natal target | Natal target placement | Natal house | Orb |\n");
        out.append("|---|---|---|---|---:|---:|\n");
        for (SolarReturnNatalContact contact : sortedContacts(row.conjunctions())) {
            out.append("| ").append(contact.solarReturnPoint())
                    .append(" | ").append(contact.natalTargetType())
                    .append(" | ").append(contact.natalTargetName())
                    .append(" | ").append(placement(contact.natalTargetSign(), contact.natalTargetDegreeInSign()))
                    .append(" | H").append(contact.natalTargetHouse())
                    .append(" | ").append(formatDecimal(contact.orbDegrees(), 2)).append("°")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private List<SolarReturnNatalContact> sortedContacts(List<SolarReturnNatalContact> contacts) {
        return contacts.stream()
                .sorted(Comparator
                        .comparing((SolarReturnNatalContact contact) -> contact.solarReturnPoint().ordinal())
                        .thenComparingDouble(SolarReturnNatalContact::orbDegrees)
                        .thenComparing(SolarReturnNatalContact::natalTargetName))
                .toList();
    }

    private String lordOfYearCell(SolarReturnPointOverlay overlay) {
        return overlay.point() + " " + placement(overlay.sign(), overlay.degreeInSign())
                + ", SR H" + house(overlay.solarReturnHouse())
                + ", natal H" + overlay.natalHouseOverlay();
    }

    private String joinPoints(List<PointKey> points) {
        if (points == null || points.isEmpty()) {
            return "—";
        }
        return points.stream().map(PointKey::name).collect(Collectors.joining(", "));
    }

    private long tightContactCount(SolarReturnNatalComparisonRow row) {
        return row.conjunctions().stream()
                .filter(contact -> contact.orbDegrees() <= 1.0)
                .count();
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String house(Integer house) {
        return house == null ? "—" : Integer.toString(house);
    }

    private String retrograde(SolarReturnPointOverlay overlay) {
        if (overlay.retrograde() == null) {
            return "—";
        }
        return overlay.retrograde() ? "R" : "";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "% ." + places + "f", value).trim();
    }
}
