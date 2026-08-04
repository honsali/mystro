package app.local;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import app.chart.data.Planet;
import app.chart.model.Subject;
import app.reading.lifearc.decennial.DecennialPeriod;
import app.reading.lifearc.decennial.DecennialRulerCondition;
import app.reading.lifearc.decennial.DecennialSubperiod;
import app.reading.lifearc.decennial.DecennialTable;

final class DecennialMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, DecennialTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Decennials\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendSequence(out, table);
        appendActiveChain(out, table);
        appendOverview(out, table);
        appendDetailedSubperiods(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, DecennialTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Natal sect used for sequence: `").append(table.natalSect()).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Coverage: `").append(format(table.coverageStartDateTime())).append("` to `")
                .append(format(table.coverageEndDateTimeExclusive())).append("`\n");
        out.append("- Sequence method: `").append(table.sequenceMethod()).append("`\n");
        out.append("- Subperiod method: `").append(table.subperiodMethod()).append("`\n\n");
        out.append("This is a separated normalized decennial chronocrator file for validation and timing research. ")
                .append("Main periods are ten-year periods. Subperiods split each ten-year period into seven equal partner periods rotating from the main ruler. ")
                .append("Use the ruler condition and ruled natal houses as evidence; do not treat this file alone as an event prediction.\n\n");
    }

    private void appendSequence(StringBuilder out, DecennialTable table) {
        out.append("## Main period sequence and natal ruler context\n\n");
        out.append("| # | Ruler | Natal placement | Natal house | Ruled natal houses |\n");
        out.append("|---:|---|---|---:|---|\n");
        List<Planet> sequence = table.rulerSequence();
        for (int i = 0; i < sequence.size(); i++) {
            Planet ruler = sequence.get(i);
            DecennialRulerCondition condition = conditionFor(table, ruler);
            out.append("| ").append(i + 1)
                    .append(" | ").append(ruler)
                    .append(" | ").append(placement(condition))
                    .append(" | H").append(condition.house())
                    .append(" | ").append(houses(condition.ruledNatalHouses()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveChain(StringBuilder out, DecennialTable table) {
        DecennialPeriod activePeriod = table.periods().stream()
                .filter(DecennialPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (activePeriod == null) {
            return;
        }
        DecennialSubperiod activeSubperiod = activePeriod.subperiods().stream()
                .filter(DecennialSubperiod::activeForInquiry)
                .findFirst()
                .orElse(null);

        out.append("## Active inquiry chain\n\n");
        out.append("| Layer | Ruler | Natal placement | Ruled natal houses | Start | End | Duration |\n");
        out.append("|---|---|---|---|---|---|---:|\n");
        out.append("| Main decennial | ").append(activePeriod.ruler())
                .append(" | ").append(placement(activePeriod.rulerNatalCondition()))
                .append(" | ").append(houses(activePeriod.rulerNatalCondition().ruledNatalHouses()))
                .append(" | ").append(format(activePeriod.startDateTime()))
                .append(" | ").append(format(activePeriod.endDateTimeExclusive()))
                .append(" | ").append(duration(activePeriod.startDateTime(), activePeriod.endDateTimeExclusive()))
                .append(" |\n");
        if (activeSubperiod != null) {
            out.append("| Partner | ").append(activeSubperiod.partner())
                    .append(" | ").append(placement(activeSubperiod.partnerNatalCondition()))
                    .append(" | ").append(houses(activeSubperiod.partnerNatalCondition().ruledNatalHouses()))
                    .append(" | ").append(format(activeSubperiod.startDateTime()))
                    .append(" | ").append(format(activeSubperiod.endDateTimeExclusive()))
                    .append(" | ").append(duration(activeSubperiod.startDateTime(), activeSubperiod.endDateTimeExclusive()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, DecennialTable table) {
        out.append("## Main period overview\n\n");
        out.append("| Active | Cycle | # | Ruler | Ruler natal place | Ruled natal houses | Ages | Start | End | Active partner |\n");
        out.append("|---|---:|---:|---|---|---|---|---|---|---|\n");
        for (DecennialPeriod period : table.periods()) {
            out.append("| ").append(period.activeForInquiry() ? "★" : "")
                    .append(" | ").append(period.cycleNumber())
                    .append(" | ").append(period.sequenceIndex())
                    .append(" | ").append(period.ruler())
                    .append(" | ").append(placement(period.rulerNatalCondition()))
                    .append(" | ").append(houses(period.rulerNatalCondition().ruledNatalHouses()))
                    .append(" | ").append(period.startAgeYears()).append("–").append(period.endAgeYearsExclusive())
                    .append(" | ").append(format(period.startDateTime()))
                    .append(" | ").append(format(period.endDateTimeExclusive()))
                    .append(" | ").append(activePartner(period))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDetailedSubperiods(StringBuilder out, DecennialTable table) {
        out.append("## Detailed partner periods\n\n");
        for (DecennialPeriod period : table.periods()) {
            out.append("### Cycle ").append(period.cycleNumber())
                    .append(", #").append(period.sequenceIndex())
                    .append(" — ").append(period.ruler())
                    .append(" ages ").append(period.startAgeYears()).append("–").append(period.endAgeYearsExclusive());
            if (period.activeForInquiry()) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("| Active | # | Main ruler | Partner | Partner natal place | Ruled natal houses | Start | End | Duration |\n");
            out.append("|---|---:|---|---|---|---|---|---|---:|\n");
            for (DecennialSubperiod subperiod : period.subperiods()) {
                out.append("| ").append(subperiod.activeForInquiry() ? "★" : "")
                        .append(" | ").append(subperiod.sequenceIndex())
                        .append(" | ").append(period.ruler())
                        .append(" | ").append(subperiod.partner())
                        .append(" | ").append(placement(subperiod.partnerNatalCondition()))
                        .append(" | ").append(houses(subperiod.partnerNatalCondition().ruledNatalHouses()))
                        .append(" | ").append(format(subperiod.startDateTime()))
                        .append(" | ").append(format(subperiod.endDateTimeExclusive()))
                        .append(" | ").append(duration(subperiod.startDateTime(), subperiod.endDateTimeExclusive()))
                        .append(" |\n");
            }
            out.append("\n");
        }
    }

    private DecennialRulerCondition conditionFor(DecennialTable table, Planet ruler) {
        return table.periods().stream()
                .filter(period -> period.ruler() == ruler)
                .map(DecennialPeriod::rulerNatalCondition)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing decennial ruler " + ruler));
    }

    private String activePartner(DecennialPeriod period) {
        return period.subperiods().stream()
                .filter(DecennialSubperiod::activeForInquiry)
                .map(DecennialSubperiod::partner)
                .map(Planet::name)
                .findFirst()
                .orElse("—");
    }

    private String placement(DecennialRulerCondition condition) {
        return condition.sign() + " " + String.format(java.util.Locale.ROOT, "%.2f°", condition.degreeInSign())
                + " H" + condition.house();
    }

    private String houses(List<Integer> houses) {
        if (houses == null || houses.isEmpty()) {
            return "—";
        }
        return houses.stream().map(house -> "H" + house).collect(Collectors.joining(", "));
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
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
