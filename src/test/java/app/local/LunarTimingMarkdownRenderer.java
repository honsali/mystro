package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.description.common.data.SyzygyType;
import app.reading.lifearc.lunar.EclipseCandidateType;
import app.reading.lifearc.lunar.EclipseContact;
import app.reading.lifearc.lunar.EclipseEvent;
import app.reading.lifearc.lunar.LunarReturnEntry;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.lunar.LunationEntry;

final class LunarTimingMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, LunarTimingTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Lunar Timing Overview\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveLunarReturn(out, table);
        appendActiveLunation(out, table);
        appendActiveYearEclipses(out, subject, inquiryDate, table);
        appendSplitFilesNote(out);
        return out.toString();
    }

    String renderEclipseTables(Subject subject, LocalDate inquiryDate, LunarTimingTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Lunar Timing — Eclipse Tables\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveLunarReturn(out, table);
        appendActiveLunation(out, table);
        appendActiveYearEclipses(out, subject, inquiryDate, table);
        appendTrueEclipses(out, table);
        appendEclipseCandidates(out, table);
        return out.toString();
    }

    String renderFullTables(Subject subject, LocalDate inquiryDate, LunarTimingTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Lunar Returns and Lunations — Full Tables\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendActiveLunarReturn(out, table);
        appendActiveLunation(out, table);
        appendLunarReturnsOverview(out, table);
        appendLunationsOverview(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, LunarTimingTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Lunar-return method: `").append(table.lunarReturnMethod()).append("`\n");
        out.append("- Lunation method: `").append(table.lunationMethod()).append("`\n");
        out.append("- Eclipse-candidate method: `").append(table.eclipseCandidateMethod()).append("`\n");
        out.append("- True-eclipse method: `").append(table.trueEclipseMethod()).append("`\n");
        out.append("- Eclipse node orbs: solar new moons <= `").append(formatDegree(table.solarEclipseNodeOrbDegrees()))
                .append("`, lunar full moons <= `").append(formatDegree(table.lunarEclipseNodeOrbDegrees())).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Coverage: `").append(format(table.coverageStartDateTime())).append("` to `")
                .append(format(table.coverageEndDateTimeExclusive())).append("`\n");
        out.append("- Natal Moon: `").append(placement(table.natalMoonSign(), table.natalMoonDegreeInSign()))
                .append("`, H").append(table.natalMoonHouse()).append("\n");
        out.append("- Lunar returns: `").append(table.lunarReturns().size()).append("`\n");
        out.append("- Lunations: `").append(table.lunations().size()).append("`\n");
        out.append("- True eclipses with local visibility checks: `").append(table.eclipseEvents().size()).append("`\n");
        out.append("- Mean-node eclipse candidates: `").append(eclipseCandidates(table).size()).append("`\n\n");
        out.append("This local research file calculates exact Moon returns to the natal Moon longitude, exact new/full Moon syzygies, ")
                .append("and Swiss Ephemeris true global solar/lunar eclipses with subject-location visibility checks. True-eclipse rows include magnitude/contact data when Swiss Ephemeris provides it; ")
                .append("local visibility is `VISIBLE`, `NOT_VISIBLE`, or `UNKNOWN` with a reason when calculation support is unsafe. Mean-node candidates are retained as supporting reference evidence. ")
                .append("Rows are timing evidence, not standalone event claims. The overview file keeps active rows and active-year eclipse pointers near the top; `lunar_timing_eclipses.md` keeps all eclipse rows; `lunar_timing_full.md` keeps all lunar-return and lunation rows.\n\n");
    }

    private void appendActiveLunarReturn(StringBuilder out, LunarTimingTable table) {
        LunarReturnEntry active = table.lunarReturns().stream()
                .filter(LunarReturnEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return;
        }
        out.append("## Active lunar-return period\n\n");
        appendLunarReturnHeader(out, false);
        appendLunarReturnRow(out, active, false);
        out.append("\n");
    }

    private void appendActiveLunation(StringBuilder out, LunarTimingTable table) {
        LunationEntry active = table.lunations().stream()
                .filter(LunationEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return;
        }
        out.append("## Active lunation period\n\n");
        appendLunationHeader(out, false);
        appendLunationRow(out, active, false);
        out.append("\n");
    }

    private void appendActiveYearEclipses(StringBuilder out, Subject subject, LocalDate inquiryDate, LunarTimingTable table) {
        if (inquiryDate == null) {
            return;
        }
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        int completedAge = inquiryDate.getYear() - birthDate.getYear();
        if (inquiryDate.isBefore(birthDate.plusYears(completedAge))) {
            completedAge--;
        }
        OffsetDateTime activeYearStart = subject.getUtcBirthDateTime().plusYears(completedAge);
        OffsetDateTime activeYearEnd = activeYearStart.plusYears(1);
        List<EclipseEvent> trueEclipses = table.eclipseEvents().stream()
                .filter(event -> !event.maximumDateTime().isBefore(activeYearStart) && event.maximumDateTime().isBefore(activeYearEnd))
                .toList();
        List<LunationEntry> candidates = eclipseCandidates(table).stream()
                .filter(row -> !row.dateTime().isBefore(activeYearStart) && row.dateTime().isBefore(activeYearEnd))
                .toList();
        out.append("## Active birthday-year eclipse pointers\n\n");
        out.append("Window: `").append(format(activeYearStart)).append("` to `").append(format(activeYearEnd)).append("`. Open `lunar_timing_eclipses.md` for full eclipse rows and contacts.\n\n");
        if (trueEclipses.isEmpty() && candidates.isEmpty()) {
            out.append("No true eclipses or mean-node candidates fall in this birthday-year window.\n\n");
            return;
        }
        if (!trueEclipses.isEmpty()) {
            out.append("True eclipses in active birthday year:\n\n");
            out.append("| Kind | Type | Maximum | Syzygy point | Natal house | Visibility | Visible phases |\n");
            out.append("|---|---|---|---|---:|---|---|\n");
            for (EclipseEvent event : trueEclipses) {
                out.append("| ").append(event.kind())
                        .append(" | ").append(event.eclipseType())
                        .append(" | ").append(format(event.maximumDateTime()))
                        .append(" | ").append(placement(event.syzygySign(), event.syzygyDegreeInSign()))
                        .append(" | H").append(event.natalHouseOverlay())
                        .append(" | ").append(event.visibility().localVisibility())
                        .append(" | ").append(visiblePhases(event))
                        .append(" |\n");
            }
            out.append("\n");
        }
        if (!candidates.isEmpty()) {
            out.append("Mean-node candidates in active birthday year:\n\n");
            appendLunationHeader(out, false);
            for (LunationEntry candidate : candidates) {
                appendLunationRow(out, candidate, false);
            }
            out.append("\n");
        }
    }

    private void appendTrueEclipses(StringBuilder out, LunarTimingTable table) {
        out.append("## True eclipses with subject-location visibility\n\n");
        if (table.eclipseEvents().isEmpty()) {
            out.append("No Swiss Ephemeris true global eclipses in this age range.\n\n");
            return;
        }
        out.append("These rows are distinct from mean-node candidates. Local visibility is calculated for the subject location when the bundled Swiss Ephemeris local eclipse APIs support it safely. ")
                .append("For visible solar events, contact-status markers on global contact instants can remain `UNKNOWN` because Swiss Ephemeris returns separate local contact times; use the Visible phases column for the local solar phase summary.\n\n");
        out.append("| # | Kind | Eclipse type | Maximum | Syzygy point | Natal house | Nearest node | Node orb | Magnitude | Obscuration | Penumbral mag | Candidate ref | Contacts | Local visibility | Visible phases | Visibility reason |\n");
        out.append("|---:|---|---|---|---|---:|---|---:|---:|---:|---:|---|---|---|---|---|\n");
        for (EclipseEvent event : table.eclipseEvents()) {
            out.append("| ").append(event.sequenceIndex())
                    .append(" | ").append(event.kind())
                    .append(" | ").append(event.eclipseType())
                    .append(" | ").append(format(event.maximumDateTime()))
                    .append(" | ").append(placement(event.syzygySign(), event.syzygyDegreeInSign()))
                    .append(" | H").append(event.natalHouseOverlay())
                    .append(" | ").append(node(event.nearestNode()))
                    .append(" | ").append(formatDegree(event.nearestNodeOrbDegrees()))
                    .append(" | ").append(formatOptional(event.magnitude()))
                    .append(" | ").append(formatOptional(event.obscuration()))
                    .append(" | ").append(formatOptional(event.penumbralMagnitude()))
                    .append(" | ").append(eclipse(event.candidateReference()))
                    .append(" | ").append(contacts(event.contacts()))
                    .append(" | ").append(event.visibility().localVisibility())
                    .append(" | ").append(visiblePhases(event))
                    .append(" | ").append(event.visibility().reason())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendEclipseCandidates(StringBuilder out, LunarTimingTable table) {
        List<LunationEntry> candidates = eclipseCandidates(table);
        out.append("## Mean-node eclipse candidates\n\n");
        if (candidates.isEmpty()) {
            out.append("No mean-node eclipse candidates in this age range.\n\n");
            return;
        }
        appendLunationHeader(out, false);
        for (LunationEntry candidate : candidates) {
            appendLunationRow(out, candidate, false);
        }
        out.append("\n");
    }

    private void appendSplitFilesNote(StringBuilder out) {
        out.append("## Full eclipse and lunar tables\n\n");
        out.append("All true-eclipse rows and mean-node eclipse candidates are generated separately as `lunar_timing_eclipses.md`. The complete 0–100 lunar-return and lunation tables are generated separately as `lunar_timing_full.md`. Start from this overview, then open the larger companions only when needed.\n\n");
    }

    private void appendLunarReturnsOverview(StringBuilder out, LunarTimingTable table) {
        out.append("## Lunar returns overview\n\n");
        appendLunarReturnHeader(out, true);
        for (LunarReturnEntry row : table.lunarReturns()) {
            appendLunarReturnRow(out, row, true);
        }
        out.append("\n");
    }

    private void appendLunationsOverview(StringBuilder out, LunarTimingTable table) {
        out.append("## Lunations overview\n\n");
        out.append("Eclipse candidates are marked in the Eclipse column.\n\n");
        appendLunationHeader(out, true);
        for (LunationEntry row : table.lunations()) {
            appendLunationRow(out, row, true);
        }
        out.append("\n");
    }

    private void appendLunarReturnHeader(StringBuilder out, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        out.append(" # | Return # | Date | Period end | Age | Moon | Moon lat | Sun | Moon-Sun elongation | Nearest node | Node orb | Natal house |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        out.append("---:|---:|---|---|---:|---|---:|---|---:|---|---:|---:|\n");
    }

    private void appendLunarReturnRow(StringBuilder out, LunarReturnEntry row, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" ").append(row.activeForInquiry() ? "★" : "").append(" |");
        }
        out.append(" ").append(row.sequenceIndex())
                .append(" | ").append(row.returnNumberFromBirth())
                .append(" | ").append(format(row.returnDateTime()))
                .append(" | ").append(format(row.periodEndDateTimeExclusive()))
                .append(" | ").append(formatDecimal(row.ageYears(), 2))
                .append(" | ").append(placement(row.moonSign(), row.moonDegreeInSign()))
                .append(" | ").append(formatDegree(row.moonLatitude()))
                .append(" | ").append(placement(row.sunSign(), row.sunDegreeInSign()))
                .append(" | ").append(formatDegree(row.lunarElongationFromSun()))
                .append(" | ").append(node(row.nearestNode()))
                .append(" | ").append(formatDegree(row.nearestNodeOrbDegrees()))
                .append(" | H").append(row.natalHouseOverlay())
                .append(" |\n");
    }

    private void appendLunationHeader(StringBuilder out, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        out.append(" # | Type | Date | Period end | Age | Syzygy point | Natal house | Moon lat | Nearest node | Node orb | Eclipse |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        out.append("---:|---|---|---|---:|---|---:|---:|---|---:|---|\n");
    }

    private void appendLunationRow(StringBuilder out, LunationEntry row, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" ").append(row.activeForInquiry() ? "★" : "").append(" |");
        }
        out.append(" ").append(row.sequenceIndex())
                .append(" | ").append(type(row.type()))
                .append(" | ").append(format(row.dateTime()))
                .append(" | ").append(format(row.periodEndDateTimeExclusive()))
                .append(" | ").append(formatDecimal(row.ageYears(), 2))
                .append(" | ").append(placement(row.syzygySign(), row.syzygyDegreeInSign()))
                .append(" | H").append(row.natalHouseOverlay())
                .append(" | ").append(formatDegree(row.moonLatitude()))
                .append(" | ").append(node(row.nearestNode()))
                .append(" | ").append(formatDegree(row.nearestNodeOrbDegrees()))
                .append(" | ").append(eclipse(row.eclipseType()))
                .append(" |\n");
    }

    private List<LunationEntry> eclipseCandidates(LunarTimingTable table) {
        return table.lunations().stream()
                .filter(row -> row.eclipseType() != EclipseCandidateType.NONE)
                .toList();
    }

    private String type(SyzygyType type) {
        return type == SyzygyType.NEW_MOON ? "New" : "Full";
    }

    private String eclipse(EclipseCandidateType eclipseType) {
        return eclipseType == EclipseCandidateType.NONE ? "" : eclipseType.name();
    }

    private String contacts(List<EclipseContact> contacts) {
        if (contacts.isEmpty()) {
            return "";
        }
        return contacts.stream()
                .map(contact -> contact.phase() + " " + format(contact.dateTime()) + " (" + contact.visibilityStatus() + ")")
                .collect(Collectors.joining("<br>"));
    }

    private String visiblePhases(EclipseEvent event) {
        if (event.visibility().visibleContactPhases().isEmpty()) {
            return "";
        }
        return event.visibility().visibleContactPhases().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private String formatOptional(Double value) {
        return value == null ? "" : formatDecimal(value, 3);
    }

    private String node(Planet node) {
        return node == Planet.NORTH_NODE ? "North" : "South";
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDegree(degreeInSign);
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatDegree(double value) {
        return formatDecimal(value, 2) + "°";
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }
}
