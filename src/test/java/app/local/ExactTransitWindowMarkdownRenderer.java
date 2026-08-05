package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.transit.ExactTransitHit;
import app.reading.lifearc.transit.ExactTransitHitKind;
import app.reading.lifearc.transit.ExactTransitSearchCalculator;
import app.reading.lifearc.transit.MonthlyTransitActivationReason;
import app.reading.lifearc.transit.MonthlyTransitCheckpointRow;
import app.reading.lifearc.transit.MonthlyTransitCheckpointTable;
import app.reading.lifearc.transit.TransitSearchWindow;
import app.reading.lifearc.transit.TransitSearchWindowCalculator;

final class ExactTransitWindowMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  LocalDate inquiryDate,
                  MonthlyTransitCheckpointTable checkpointTable,
                  List<TransitSearchWindow> windows,
                  List<ExactTransitHit> hits) {
        StringBuilder out = new StringBuilder();
        out.append("# Exact Transit Search Windows\n\n");
        appendSummary(out, subject, inquiryDate, checkpointTable, windows, hits);
        appendActiveCheckpoint(out, checkpointTable);
        appendWindows(out, windows);
        appendHits(out, hits);
        return out.toString();
    }

    private void appendSummary(StringBuilder out,
                               Subject subject,
                               LocalDate inquiryDate,
                               MonthlyTransitCheckpointTable checkpointTable,
                               List<TransitSearchWindow> windows,
                               List<ExactTransitHit> hits) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Window method: `").append(TransitSearchWindowCalculator.METHOD_ID).append("`\n");
        out.append("- Exact-hit method: `").append(ExactTransitSearchCalculator.METHOD_ID).append("`\n");
        out.append("- Source checkpoint method: `")
                .append(checkpointTable == null ? "—" : checkpointTable.methodId())
                .append("`\n");
        out.append("- Default window radius: `±").append(TransitSearchWindowCalculator.DEFAULT_WINDOW_RADIUS.toDays()).append(" days`\n");
        out.append("- Windows retained: `").append(size(windows)).append("`\n");
        out.append("- Exact/station hits found: `").append(size(hits)).append("`\n\n");
        out.append("This local research file narrows the monthly transit-checkpoint activation rows into short exact-search windows. ")
                .append("It does not scan the whole life daily. Exact hits are evidence rows for later synthesis, not standalone event claims. ")
                .append("Station rows are retained only when the transiting point stations very near the same exact-aspect target.\n\n");
    }

    private void appendActiveCheckpoint(StringBuilder out, MonthlyTransitCheckpointTable checkpointTable) {
        out.append("## Source active monthly checkpoint\n\n");
        MonthlyTransitCheckpointRow active = checkpointTable == null ? null : checkpointTable.rows().stream()
                .filter(MonthlyTransitCheckpointRow::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            out.append("No active monthly checkpoint was available; no exact transit search windows were generated.\n\n");
            return;
        }
        out.append("| Checkpoint # | Age/month | Checkpoint window | Annual activation | Monthly activation | Activated contacts |\n");
        out.append("|---:|---:|---|---|---|---:|\n");
        out.append("| ").append(active.checkpointNumber())
                .append(" | ").append(active.ageYears()).append("/M").append(active.monthInYear())
                .append(" | ").append(format(active.checkpointDateTime())).append(" → ").append(format(active.periodEndDateTimeExclusive()))
                .append(" | H").append(active.annualProfectedHouse()).append(" ").append(active.annualProfectedSign()).append(" — ").append(active.lordOfYear())
                .append(" | H").append(active.monthlyProfectedHouse()).append(" ").append(active.monthlyProfectedSign()).append(" — ").append(active.lordOfMonth())
                .append(" | ").append(active.activationContacts().size())
                .append(" |\n\n");
    }

    private void appendWindows(StringBuilder out, List<TransitSearchWindow> windows) {
        out.append("## Search windows\n\n");
        if (windows == null || windows.isEmpty()) {
            out.append("No search windows generated.\n\n");
            return;
        }
        out.append("| # | Window | Transit point | Natal target | Aspect | Target placement | Natal house | Checkpoint orb | Weight | Reasons |\n");
        out.append("|---:|---|---|---|---|---|---:|---:|---:|---|\n");
        for (TransitSearchWindow window : windows) {
            out.append("| ").append(window.sequence())
                    .append(" | ").append(format(window.windowStartDateTime())).append(" → ").append(format(window.windowEndDateTime()))
                    .append(" | ").append(window.transitPoint())
                    .append(" | ").append(window.natalTargetType()).append(" ").append(window.natalTargetName())
                    .append(" | ").append(aspect(window.aspect()))
                    .append(" | ").append(placement(window.natalTargetSign(), window.natalTargetDegreeInSign()))
                    .append(" | H").append(window.natalTargetHouse())
                    .append(" | ").append(formatDecimal(window.checkpointOrbFromExactDegrees(), 2)).append("°")
                    .append(" | ").append(window.activationWeight())
                    .append(" | ").append(reasons(window.activationReasons()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendHits(StringBuilder out, List<ExactTransitHit> hits) {
        out.append("## Exact hits inside windows\n\n");
        if (hits == null || hits.isEmpty()) {
            out.append("No exact transit hits found inside the retained short windows.\n\n");
            return;
        }
        out.append("| # | Kind | Window # | Exact date/time | Transit point | Natal target | Aspect | Target placement | Natal house | Transit longitude | Separation | Orb | Motion |\n");
        out.append("|---:|---|---:|---|---|---|---|---|---:|---:|---:|---:|---|\n");
        for (ExactTransitHit hit : sortedHits(hits)) {
            out.append("| ").append(hit.sequence())
                    .append(" | ").append(kind(hit.hitKind()))
                    .append(" | ").append(hit.sourceWindowSequence())
                    .append(" | ").append(format(hit.exactDateTime()))
                    .append(" | ").append(hit.transitPoint())
                    .append(" | ").append(hit.natalTargetType()).append(" ").append(hit.natalTargetName())
                    .append(" | ").append(aspect(hit.aspect()))
                    .append(" | ").append(placement(hit.natalTargetSign(), hit.natalTargetDegreeInSign()))
                    .append(" | H").append(hit.natalTargetHouse())
                    .append(" | ").append(formatDecimal(hit.transitLongitude(), 2)).append("°")
                    .append(" | ").append(formatDecimal(hit.angularSeparation(), 2)).append("°")
                    .append(" | ").append(formatDecimal(hit.orbFromExactDegrees(), 4)).append("°")
                    .append(" | ").append(motion(hit.aspectMotion()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private List<ExactTransitHit> sortedHits(List<ExactTransitHit> hits) {
        return hits.stream()
                .sorted(Comparator
                        .comparing(ExactTransitHit::exactDateTime)
                        .thenComparingInt(ExactTransitHit::sourceWindowSequence)
                        .thenComparing(hit -> hit.transitPoint().ordinal())
                        .thenComparing(ExactTransitHit::natalTargetName))
                .toList();
    }

    private int size(List<?> rows) {
        return rows == null ? 0 : rows.size();
    }

    private String reasons(List<MonthlyTransitActivationReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "—";
        }
        return reasons.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String kind(ExactTransitHitKind kind) {
        if (kind == ExactTransitHitKind.EXACT_ASPECT) {
            return "exact aspect";
        }
        if (kind == ExactTransitHitKind.STATION_NEAR_EXACT_TARGET) {
            return "station near target";
        }
        return "—";
    }

    private String motion(AspectMotion motion) {
        return motion == null ? "—" : motion.name();
    }

    private String aspect(AspectType aspect) {
        return aspect == null ? "—" : aspect.name();
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }
}
