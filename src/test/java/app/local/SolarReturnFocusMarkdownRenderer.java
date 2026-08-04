package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;
import app.reading.lifearc.solarreturn.SolarReturnEntry;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonRow;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnNatalContact;
import app.reading.lifearc.solarreturn.SolarReturnPointEntry;
import app.reading.lifearc.solarreturn.SolarReturnPointOverlay;
import app.reading.lifearc.solarreturn.SolarReturnTable;

final class SolarReturnFocusMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  SolarReturnTable solarReturns,
                  SolarReturnNatalComparisonTable comparison,
                  AnnualProfectionTable annualProfections,
                  MonthlyProfectionTable monthlyProfections) {
        SolarReturnEntry activeReturn = activeSolarReturn(solarReturns, focusDateTime);
        SolarReturnNatalComparisonRow activeComparison = activeComparison(comparison);
        if (activeReturn == null) {
            throw new IllegalArgumentException("No active solar return for focus date/time " + focusDateTime);
        }
        if (activeComparison == null) {
            throw new IllegalArgumentException("No active solar-return comparison row for focus date/time " + focusDateTime);
        }

        StringBuilder out = new StringBuilder();
        out.append("# Solar Return Focus\n\n");
        appendSummary(out, subject, focusDateTime, solarReturns, comparison, activeReturn, activeComparison);
        appendProfectionContext(out, annualProfections, monthlyProfections);
        appendReturnAngles(out, activeReturn, activeComparison);
        appendReturnPoints(out, activeReturn);
        appendNatalOverlays(out, activeComparison);
        appendContacts(out, activeComparison);
        return out.toString();
    }

    private void appendSummary(StringBuilder out,
                               Subject subject,
                               OffsetDateTime focusDateTime,
                               SolarReturnTable solarReturns,
                               SolarReturnNatalComparisonTable comparison,
                               SolarReturnEntry activeReturn,
                               SolarReturnNatalComparisonRow activeComparison) {
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Solar-return method: `").append(solarReturns.methodId()).append("`\n");
        out.append("- Return location method: `").append(solarReturns.locationMethod()).append("`\n");
        out.append("- Natal comparison method: `").append(comparison.methodId()).append("`\n");
        out.append("- Natal overlay method: `").append(comparison.natalOverlayMethod()).append("`\n");
        out.append("- Natal conjunction orb: `").append(formatDecimal(comparison.conjunctionOrbDegrees(), 2)).append("°`\n");
        out.append("- Active solar-return age: `").append(activeReturn.ageYears()).append("`\n");
        out.append("- Active solar-return period: `").append(format(activeReturn.returnDateTime())).append("` to `")
                .append(format(activeReturn.periodEndDateTimeExclusive())).append("`\n");
        out.append("- Annual profection attached to comparison: `H").append(activeComparison.profectedHouse()).append(" ")
                .append(activeComparison.profectedSign()).append(" — ").append(activeComparison.lordOfYear()).append("`\n\n");
    }

    private void appendProfectionContext(StringBuilder out, AnnualProfectionTable annualProfections, MonthlyProfectionTable monthlyProfections) {
        out.append("## Profection context\n\n");
        AnnualProfectionTableRow annual = activeAnnual(annualProfections);
        MonthlyProfectionTableRow monthly = activeMonthly(monthlyProfections);
        out.append("| Layer | Age/M | Reference | Sign | House | Lord | Period |\n");
        out.append("|---|---:|---|---|---:|---|---|\n");
        if (annual != null) {
            AnnualProfectionReferenceEntry asc = annualEntry(annual, AnnualProfectionReference.ASCENDANT);
            out.append("| Annual | ").append(annual.ageYears())
                    .append(" | Asc | ").append(asc.profectedSign())
                    .append(" | H").append(asc.profectedHouse())
                    .append(" | ").append(asc.lord())
                    .append(" | ").append(annual.periodStartDate()).append(" to ").append(annual.periodEndDateExclusive())
                    .append(" |\n");
        }
        if (monthly != null) {
            MonthlyProfectionReferenceEntry asc = monthlyEntry(monthly, AnnualProfectionReference.ASCENDANT);
            out.append("| Monthly | ").append(monthly.ageYears()).append("/M").append(monthly.monthInYear())
                    .append(" | Asc | ").append(asc.profectedSign())
                    .append(" | H").append(asc.profectedHouse())
                    .append(" | ").append(asc.lord())
                    .append(" | ").append(format(monthly.periodStartDateTime())).append(" to ").append(format(monthly.periodEndDateTimeExclusive()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendReturnAngles(StringBuilder out, SolarReturnEntry activeReturn, SolarReturnNatalComparisonRow activeComparison) {
        out.append("## Solar-return angles and sect\n\n");
        out.append("| Item | Value | Natal overlay |\n");
        out.append("|---|---|---|\n");
        out.append("| Ascendant | ").append(placement(activeReturn.ascendantSign(), activeReturn.ascendantDegreeInSign()))
                .append(" | H").append(activeComparison.ascendantNatalHouseOverlay()).append(" |\n");
        out.append("| Midheaven | ").append(placement(activeReturn.midheavenSign(), activeReturn.midheavenDegreeInSign()))
                .append(" | H").append(activeComparison.midheavenNatalHouseOverlay()).append(" |\n");
        out.append("| Sect | ").append(activeReturn.sect()).append(" | — |\n");
        out.append("| Julian day UT | ").append(formatDecimal(activeReturn.julianDayUt(), 5)).append(" | — |\n\n");
    }

    private void appendReturnPoints(StringBuilder out, SolarReturnEntry activeReturn) {
        out.append("## Solar-return point positions\n\n");
        out.append("| Point | Type | Placement | Longitude | SR house | Retrograde |\n");
        out.append("|---|---|---|---:|---:|---|\n");
        for (SolarReturnPointEntry point : activeReturn.points()) {
            out.append("| ").append(point.point())
                    .append(" | ").append(point.type())
                    .append(" | ").append(placement(point.sign(), point.degreeInSign()))
                    .append(" | ").append(formatDecimal(point.longitude(), 2)).append("°")
                    .append(" | ").append(house(point.house()))
                    .append(" | ").append(retrograde(point.retrograde()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendNatalOverlays(StringBuilder out, SolarReturnNatalComparisonRow activeComparison) {
        out.append("## Solar-return points over natal houses\n\n");
        out.append("- Lord of Year in solar return: `").append(lordOfYearCell(activeComparison.lordOfYearOverlay())).append("`\n");
        out.append("- Solar-return points in annual profected sign: `").append(joinPoints(activeComparison.solarReturnPointsInProfectedSign())).append("`\n");
        out.append("- Solar-return points overlaying annual profected house: `").append(joinPoints(activeComparison.solarReturnPointsOverlayingProfectedHouse())).append("`\n\n");
        out.append("| Point | Type | SR placement | SR house | Natal house overlay | Retrograde |\n");
        out.append("|---|---|---|---:|---:|---|\n");
        for (SolarReturnPointOverlay overlay : activeComparison.pointOverlays()) {
            out.append("| ").append(overlay.point())
                    .append(" | ").append(overlay.type())
                    .append(" | ").append(placement(overlay.sign(), overlay.degreeInSign()))
                    .append(" | ").append(house(overlay.solarReturnHouse()))
                    .append(" | H").append(overlay.natalHouseOverlay())
                    .append(" | ").append(retrograde(overlay.retrograde()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendContacts(StringBuilder out, SolarReturnNatalComparisonRow activeComparison) {
        out.append("## Solar-return conjunctions to natal points/lots\n\n");
        if (activeComparison.conjunctions().isEmpty()) {
            out.append("No solar-return conjunctions to natal points/lots within the configured orb.\n\n");
            return;
        }
        out.append("| SR point | Natal target type | Natal target | Natal target placement | Natal house | Orb |\n");
        out.append("|---|---|---|---|---:|---:|\n");
        for (SolarReturnNatalContact contact : sortedContacts(activeComparison.conjunctions())) {
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

    private SolarReturnEntry activeSolarReturn(SolarReturnTable table, OffsetDateTime focusDateTime) {
        return table.rows().stream()
                .filter(row -> !focusDateTime.isBefore(row.returnDateTime()) && focusDateTime.isBefore(row.periodEndDateTimeExclusive()))
                .findFirst()
                .orElse(null);
    }

    private SolarReturnNatalComparisonRow activeComparison(SolarReturnNatalComparisonTable table) {
        return table.rows().stream()
                .filter(SolarReturnNatalComparisonRow::activeForInquiry)
                .findFirst()
                .orElseGet(() -> table.rows().isEmpty() ? null : table.rows().get(0));
    }

    private AnnualProfectionTableRow activeAnnual(AnnualProfectionTable table) {
        return table == null ? null : table.rows().stream().filter(AnnualProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private MonthlyProfectionTableRow activeMonthly(MonthlyProfectionTable table) {
        return table == null ? null : table.rows().stream().filter(MonthlyProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private AnnualProfectionReferenceEntry annualEntry(AnnualProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(entry -> entry.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing annual profection reference " + reference));
    }

    private MonthlyProfectionReferenceEntry monthlyEntry(MonthlyProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(entry -> entry.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing monthly profection reference " + reference));
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

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String house(Integer house) {
        return house == null ? "—" : Integer.toString(house);
    }

    private String retrograde(Boolean retrograde) {
        if (retrograde == null) {
            return "—";
        }
        return retrograde ? "R" : "";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }
}
