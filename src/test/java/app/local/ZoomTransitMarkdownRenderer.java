package app.local;

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
import app.reading.lifearc.transit.TransitSearchWindow;

final class ZoomTransitMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  List<TransitSearchWindow> windows,
                  List<ExactTransitHit> hits) {
        StringBuilder out = new StringBuilder();
        out.append("# Exact Transits — 30-Day Zoom\n\n");
        appendSummary(out, subject, focusDateTime, windowStart, windowEnd, windows, hits);
        appendHits(out, hits);
        appendWindows(out, windows);
        return out.toString();
    }

    private void appendSummary(StringBuilder out,
                               Subject subject,
                               OffsetDateTime focusDateTime,
                               OffsetDateTime windowStart,
                               OffsetDateTime windowEnd,
                               List<TransitSearchWindow> windows,
                               List<ExactTransitHit> hits) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Search window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n");
        out.append("- Exact-hit method: `").append(ExactTransitSearchCalculator.METHOD_ID).append("`\n");
        out.append("- Source filter: `zoom-date annual/monthly profection activations + core natal anchors + active chronocrator point targets`\n");
        out.append("- Candidate windows retained: `").append(size(windows)).append("`\n");
        out.append("- Exact/station hits found: `").append(size(hits)).append("`\n\n");
        out.append("This file is the bounded transit layer for the requested date only. ")
                .append("It searches exact Ptolemaic transit-to-natal aspects inside the ±15-day window, after filtering targets by the active timing context. ")
                .append("Rows are evidence for synthesis, not standalone event claims.\n\n");
    }

    private void appendHits(StringBuilder out, List<ExactTransitHit> hits) {
        out.append("## Exact hits inside the requested window\n\n");
        if (hits == null || hits.isEmpty()) {
            out.append("No exact transit hits were found inside the retained candidate windows.\n\n");
            return;
        }
        out.append("| # | Kind | Exact date/time | Transit point | Natal target | Aspect | Target placement | House | Transit longitude | Separation | Orb | Motion |\n");
        out.append("|---:|---|---|---|---|---|---|---:|---:|---:|---:|---|\n");
        for (ExactTransitHit hit : sortedHits(hits)) {
            out.append("| ").append(hit.sequence())
                    .append(" | ").append(kind(hit.hitKind()))
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

    private void appendWindows(StringBuilder out, List<TransitSearchWindow> windows) {
        out.append("## Candidate search windows\n\n");
        if (windows == null || windows.isEmpty()) {
            out.append("No candidate windows were generated.\n\n");
            return;
        }
        out.append("Each candidate uses the same requested ±15-day time range; `focus orb` is the approximate orb at the focus date/time before exact root-finding.\n\n");
        out.append("| # | Transit point | Natal target | Aspect | Target placement | House | Focus orb | Weight | Reasons |\n");
        out.append("|---:|---|---|---|---|---:|---:|---:|---|\n");
        for (TransitSearchWindow window : windows) {
            out.append("| ").append(window.sequence())
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

    private List<ExactTransitHit> sortedHits(List<ExactTransitHit> hits) {
        return hits.stream()
                .sorted(Comparator
                        .comparing(ExactTransitHit::exactDateTime)
                        .thenComparing(hit -> hit.transitPoint().ordinal())
                        .thenComparing(ExactTransitHit::natalTargetName)
                        .thenComparing(hit -> hit.aspect().ordinal()))
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
