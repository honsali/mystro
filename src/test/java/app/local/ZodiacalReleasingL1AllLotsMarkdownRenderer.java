package app.local;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.TraditionalTables;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingMarker;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingPeriod;

final class ZodiacalReleasingL1AllLotsMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String render(Subject subject,
                  NatalChart chart,
                  List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> briefs,
                  OffsetDateTime activeDateTime) {
        StringBuilder out = new StringBuilder();
        out.append("# Zodiacal Releasing L1 Macro — All Lots\n\n");
        appendSummary(out, subject, briefs, activeDateTime);
        appendLegend(out);
        appendRows(out, subject, chart, briefs, activeDateTime);
        return out.toString();
    }

    private void appendSummary(StringBuilder out,
                               Subject subject,
                               List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> briefs,
                               OffsetDateTime activeDateTime) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (activeDateTime != null) {
            out.append("- Inquiry date/time: `").append(format(activeDateTime)).append("`\n");
        }
        out.append("- Scope: all emitted lots, L1 periods only, birth through 100th birthday.\n");
        out.append("- Lot count: `").append(briefs == null ? 0 : briefs.size()).append("`\n");
        out.append("- Timing scale: L1 = sign years × 360 days.\n\n");
    }

    private void appendLegend(StringBuilder out) {
        out.append("## Marker legend\n\n");
        out.append("| Marker | Meaning |\n");
        out.append("|---|---|\n");
        out.append("| `pLB` | Preparatory Loosing of the Bond. |\n");
        out.append("| `LB` | Loosing of the Bond. |\n");
        out.append("| `Cu.` | Culmination. |\n");
        out.append("| `Co.` | Completion. |\n\n");
    }

    private void appendRows(StringBuilder out,
                            Subject subject,
                            NatalChart chart,
                            List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> briefs,
                            OffsetDateTime activeDateTime) {
        out.append("## L1 periods by lot\n\n");
        out.append("| Lot | Display name | Start sign | Lot house | Lot ruler | Active | L1 # | Period sign | Start | End | Start age | End age | Period house | Period ruler | Markers |\n");
        out.append("|---|---|---|---:|---|---|---:|---|---|---|---:|---:|---:|---|---|\n");
        if (briefs == null) {
            out.append("\n");
            return;
        }
        for (LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief brief : briefs) {
            for (ZodiacalReleasingPeriod period : brief.timeline().periods()) {
                ZodiacSign periodSign = period.sign();
                out.append("| ").append(brief.lot().name())
                        .append(" | ").append(brief.lot().displayName())
                        .append(" | ").append(brief.lot().sign())
                        .append(" | ").append(brief.lot().house())
                        .append(" | ").append(brief.lot().ruler())
                        .append(" | ").append(active(period, activeDateTime) ? "★" : "")
                        .append(" | ").append(period.sequenceIndex() + 1)
                        .append(" | ").append(periodSign)
                        .append(" | ").append(format(period.startDateTime()))
                        .append(" | ").append(format(period.endDateTimeExclusive()))
                        .append(" | ").append(formatAge(subject, period.startDateTime()))
                        .append(" | ").append(formatAge(subject, period.endDateTimeExclusive()))
                        .append(" | ").append(houseForSign(chart, periodSign))
                        .append(" | ").append(TraditionalTables.domicileRuler(periodSign))
                        .append(" | ").append(markers(period.markers()))
                        .append(" |\n");
            }
        }
        out.append("\n");
    }

    private boolean active(ZodiacalReleasingPeriod period, OffsetDateTime activeDateTime) {
        return activeDateTime != null
                && !activeDateTime.isBefore(period.startDateTime())
                && activeDateTime.isBefore(period.endDateTimeExclusive());
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(house -> house.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing house for sign " + sign));
    }

    private String markers(List<ZodiacalReleasingMarker> markers) {
        if (markers == null || markers.isEmpty()) {
            return "";
        }
        return markers.stream().map(this::marker).collect(Collectors.joining(", "));
    }

    private String marker(ZodiacalReleasingMarker marker) {
        return switch (marker) {
            case PREPARATORY_LOOSING_OF_BOND -> "pLB";
            case LOOSING_OF_BOND -> "LB";
            case CULMINATION -> "Cu.";
            case COMPLETION -> "Co.";
        };
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatAge(Subject subject, OffsetDateTime dateTime) {
        double ageYears = Duration.between(subject.getLocalBirthDateTime(), dateTime).toDays() / 365.2425;
        return String.format(Locale.ROOT, "%.1f", ageYears);
    }
}
