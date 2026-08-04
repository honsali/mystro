package app.local;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.chart.data.Planet;
import app.chart.model.Subject;
import app.reading.lifearc.firdaria.FirdariaPeriod;
import app.reading.lifearc.firdaria.FirdariaSubperiod;
import app.reading.lifearc.firdaria.FirdariaTable;

final class FirdariaMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, FirdariaTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Firdaria\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendSequence(out, table);
        appendActiveChain(out, table);
        appendOverview(out, table);
        appendDetailedSubperiods(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, FirdariaTable table) {
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
        out.append("- Subperiod method: `").append(table.subperiodMethod()).append("`\n\n");
        out.append("Firdaria is emitted here as a separated calculation file for validation and timing research. ")
                .append("Main periods follow the diurnal/nocturnal medieval sequence. Planetary main periods are split into seven equal partner periods rotating from the main ruler. ")
                .append("Node periods are emitted as undivided self-partner periods to avoid false precision where node-partner practice varies.\n\n");
    }

    private void appendSequence(StringBuilder out, FirdariaTable table) {
        out.append("## Main period sequence\n\n");
        out.append("| # | Ruler | Years |\n");
        out.append("|---:|---|---:|\n");
        List<Planet> sequence = table.mainPeriodSequence();
        for (int i = 0; i < sequence.size(); i++) {
            Planet ruler = sequence.get(i);
            out.append("| ").append(i + 1)
                    .append(" | ").append(ruler)
                    .append(" | ").append(years(table, ruler))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveChain(StringBuilder out, FirdariaTable table) {
        FirdariaPeriod activePeriod = table.periods().stream()
                .filter(FirdariaPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (activePeriod == null) {
            return;
        }
        FirdariaSubperiod activeSubperiod = activePeriod.subperiods().stream()
                .filter(FirdariaSubperiod::activeForInquiry)
                .findFirst()
                .orElse(null);

        out.append("## Active inquiry chain\n\n");
        out.append("| Layer | Ruler | Start | End | Duration |\n");
        out.append("|---|---|---|---|---:|\n");
        out.append("| Main firdar | ").append(activePeriod.ruler())
                .append(" | ").append(format(activePeriod.startDateTime()))
                .append(" | ").append(format(activePeriod.endDateTimeExclusive()))
                .append(" | ").append(duration(activePeriod.startDateTime(), activePeriod.endDateTimeExclusive()))
                .append(" |\n");
        if (activeSubperiod != null) {
            out.append("| Partner | ").append(activeSubperiod.partner())
                    .append(" | ").append(format(activeSubperiod.startDateTime()))
                    .append(" | ").append(format(activeSubperiod.endDateTimeExclusive()))
                    .append(" | ").append(duration(activeSubperiod.startDateTime(), activeSubperiod.endDateTimeExclusive()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, FirdariaTable table) {
        out.append("## Main period overview\n\n");
        out.append("| Active | Cycle | # | Ruler | Nominal years | Ages | Start | End | Active partner |\n");
        out.append("|---|---:|---:|---|---:|---|---|---|---|\n");
        for (FirdariaPeriod period : table.periods()) {
            out.append("| ").append(period.activeForInquiry() ? "★" : "")
                    .append(" | ").append(period.cycleNumber())
                    .append(" | ").append(period.sequenceIndex())
                    .append(" | ").append(period.ruler())
                    .append(" | ").append(period.nominalYears())
                    .append(" | ").append(period.startAgeYears()).append("–").append(period.endAgeYearsExclusive())
                    .append(" | ").append(format(period.startDateTime()))
                    .append(" | ").append(format(period.endDateTimeExclusive()))
                    .append(" | ").append(activePartner(period))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDetailedSubperiods(StringBuilder out, FirdariaTable table) {
        out.append("## Detailed partner periods\n\n");
        for (FirdariaPeriod period : table.periods()) {
            out.append("### Cycle ").append(period.cycleNumber())
                    .append(", #").append(period.sequenceIndex())
                    .append(" — ").append(period.ruler())
                    .append(" ages ").append(period.startAgeYears()).append("–").append(period.endAgeYearsExclusive());
            if (period.activeForInquiry()) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("| Active | # | Main ruler | Partner | Start | End | Duration |\n");
            out.append("|---|---:|---|---|---|---|---:|\n");
            for (FirdariaSubperiod subperiod : period.subperiods()) {
                out.append("| ").append(subperiod.activeForInquiry() ? "★" : "")
                        .append(" | ").append(subperiod.sequenceIndex())
                        .append(" | ").append(period.ruler())
                        .append(" | ").append(subperiod.partner())
                        .append(" | ").append(format(subperiod.startDateTime()))
                        .append(" | ").append(format(subperiod.endDateTimeExclusive()))
                        .append(" | ").append(duration(subperiod.startDateTime(), subperiod.endDateTimeExclusive()))
                        .append(" |\n");
            }
            out.append("\n");
        }
    }

    private String activePartner(FirdariaPeriod period) {
        return period.subperiods().stream()
                .filter(FirdariaSubperiod::activeForInquiry)
                .map(FirdariaSubperiod::partner)
                .map(Planet::name)
                .findFirst()
                .orElse("—");
    }

    private int years(FirdariaTable table, Planet ruler) {
        return table.periods().stream()
                .filter(period -> period.ruler() == ruler)
                .mapToInt(FirdariaPeriod::nominalYears)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing firdaria ruler " + ruler));
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
