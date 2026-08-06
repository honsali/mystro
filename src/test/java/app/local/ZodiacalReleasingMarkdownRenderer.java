package app.local;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import app.chart.TraditionalTables;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingCalculator;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingMarker;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingPeriod;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

final class ZodiacalReleasingMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ZodiacalReleasingCalculator calculator = new ZodiacalReleasingCalculator();

    String render(Subject subject, Chart chart, LotEntry lot, ZodiacalReleasingTimeline timeline, OffsetDateTime activeDateTime) {
        StringBuilder out = new StringBuilder();
        out.append("# Zodiacal Releasing from ").append(lot.displayName()).append("\n\n");
        appendSummary(out, subject, lot, timeline, activeDateTime);
        appendLegend(out);
        appendSignPeriods(out);
        appendNatalSignContext(out, chart);
        appendActiveChain(out, chart, timeline, activeDateTime);
        appendL1Overview(out, chart, timeline, activeDateTime);
        appendDetailedTimeline(out, chart, timeline, activeDateTime);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LotEntry lot, ZodiacalReleasingTimeline timeline, OffsetDateTime activeDateTime) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (activeDateTime != null) {
            out.append("- Inquiry date/time: `").append(format(activeDateTime)).append("`\n");
        }
        out.append("- Source lot: `").append(lot.name()).append("` — ").append(lot.displayName()).append("\n");
        out.append("- Source doctrine label: `").append(lot.doctrine()).append("`\n");
        out.append("- Source lot placement: `").append(lot.sign()).append("` ")
                .append(formatDegree(lot.degreeInSign())).append(", house `").append(lot.house())
                .append("`, ruler `").append(lot.ruler()).append("`\n");
        out.append("- Releasing start sign: `").append(timeline.startSign()).append("`\n");
        out.append("- Timeline span: `").append(format(timeline.startDateTime())).append("` to `")
                .append(format(timeline.endDateTimeExclusive())).append("`\n");
        out.append("- Method: `").append(timeline.methodId()).append("`\n");
        out.append("- Timing scale: L1 = sign years × 360 days; L2 = sign years × 30 days; L3 = sign years × 60 hours; L4 = sign years × 5 hours.\n\n");
    }

    private void appendLegend(StringBuilder out) {
        out.append("## Marker legend\n\n");
        out.append("| Marker | Meaning |\n");
        out.append("|---|---|\n");
        out.append("| `pLB` | Preparatory Loosing of the Bond: first passage through the sign opposite the period's starting sign. |\n");
        out.append("| `LB` | Loosing of the Bond: after the 12-sign sequence, the sequence jumps to the opposite sign. |\n");
        out.append("| `Cu.` | Culmination: the 10th sign from the period's starting sign. |\n");
        out.append("| `Co.` | Completion: return to the period's starting sign after the Loosing sequence. |\n\n");
    }

    private void appendSignPeriods(StringBuilder out) {
        out.append("## Sign period lengths\n\n");
        out.append("| Sign | Years | L2 days | L3 days | L4 hours |\n");
        out.append("|---|---:|---:|---:|---:|\n");
        for (ZodiacSign sign : ZodiacSign.values()) {
            int years = calculator.periodYears(sign);
            out.append("| ").append(sign)
                    .append(" | ").append(years)
                    .append(" | ").append(years * 30)
                    .append(" | ").append(formatDecimal(years * 2.5))
                    .append(" | ").append(years * 5)
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendNatalSignContext(StringBuilder out, Chart chart) {
        out.append("## Natal sign context\n\n");
        out.append("| Sign | Natal house | Ruler | Natal points | Emitted lots |\n");
        out.append("|---|---:|---|---|---|\n");
        Map<ZodiacSign, List<String>> pointsBySign = pointsBySign(chart);
        Map<ZodiacSign, List<String>> lotsBySign = lotsBySign(chart);
        for (ZodiacSign sign : ZodiacSign.values()) {
            out.append("| ").append(sign)
                    .append(" | ").append(houseForSign(chart, sign))
                    .append(" | ").append(TraditionalTables.domicileRuler(sign))
                    .append(" | ").append(join(pointsBySign.get(sign)))
                    .append(" | ").append(join(lotsBySign.get(sign)))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveChain(StringBuilder out, Chart chart, ZodiacalReleasingTimeline timeline, OffsetDateTime activeDateTime) {
        if (activeDateTime == null || activeDateTime.isBefore(timeline.startDateTime()) || !activeDateTime.isBefore(timeline.endDateTimeExclusive())) {
            return;
        }
        List<ZodiacalReleasingPeriod> chain = activeChain(timeline.periods(), activeDateTime);
        if (chain.isEmpty()) {
            return;
        }
        out.append("## Active chain\n\n");
        out.append("| Level | Sign | Start | End | Natal house | Ruler | Markers |\n");
        out.append("|---:|---|---|---|---:|---|---|\n");
        for (ZodiacalReleasingPeriod period : chain) {
            appendPeriodRow(out, chart, period, activeDateTime, true);
        }
        out.append("\n");
    }

    private void appendL1Overview(StringBuilder out, Chart chart, ZodiacalReleasingTimeline timeline, OffsetDateTime activeDateTime) {
        out.append("## L1 overview\n\n");
        out.append("| Active | L1 | Sign | Start | End | Duration | Natal house | Ruler | Markers |\n");
        out.append("|---|---:|---|---|---|---:|---:|---|---|\n");
        for (ZodiacalReleasingPeriod period : timeline.periods()) {
            out.append("| ").append(active(period, activeDateTime) ? "★" : "")
                    .append(" | ").append(period.sequenceIndex() + 1)
                    .append(" | ").append(period.sign())
                    .append(" | ").append(format(period.startDateTime()))
                    .append(" | ").append(format(period.endDateTimeExclusive()))
                    .append(" | ").append(duration(period))
                    .append(" | ").append(houseForSign(chart, period.sign()))
                    .append(" | ").append(TraditionalTables.domicileRuler(period.sign()))
                    .append(" | ").append(markers(period.markers()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDetailedTimeline(StringBuilder out, Chart chart, ZodiacalReleasingTimeline timeline, OffsetDateTime activeDateTime) {
        out.append("## Detailed nested timeline\n\n");
        for (ZodiacalReleasingPeriod l1 : timeline.periods()) {
            out.append("### L1 ").append(l1.sign()).append(" — ").append(format(l1.startDateTime()))
                    .append(" to ").append(format(l1.endDateTimeExclusive()));
            if (active(l1, activeDateTime)) {
                out.append(" ★");
            }
            String markerText = markers(l1.markers());
            if (!markerText.isBlank()) {
                out.append(" — ").append(markerText);
            }
            out.append("\n\n");

            appendLevelTable(out, chart, l1.subPeriods(), activeDateTime, "L2 periods inside this L1");
            for (ZodiacalReleasingPeriod l2 : l1.subPeriods()) {
                out.append("#### L2 ").append(l2.sign()).append(" — ").append(format(l2.startDateTime()))
                        .append(" to ").append(format(l2.endDateTimeExclusive()));
                if (active(l2, activeDateTime)) {
                    out.append(" ★");
                }
                markerText = markers(l2.markers());
                if (!markerText.isBlank()) {
                    out.append(" — ").append(markerText);
                }
                out.append("\n\n");
                appendLevelTable(out, chart, l2.subPeriods(), activeDateTime, "L3 periods inside this L2");
                for (ZodiacalReleasingPeriod l3 : l2.subPeriods()) {
                    out.append("##### L3 ").append(l3.sign()).append(" — ").append(format(l3.startDateTime()))
                            .append(" to ").append(format(l3.endDateTimeExclusive()));
                    if (active(l3, activeDateTime)) {
                        out.append(" ★");
                    }
                    markerText = markers(l3.markers());
                    if (!markerText.isBlank()) {
                        out.append(" — ").append(markerText);
                    }
                    out.append("\n\n");
                    appendLevelTable(out, chart, l3.subPeriods(), activeDateTime, "L4 periods inside this L3");
                }
            }
        }
    }

    private void appendLevelTable(StringBuilder out, Chart chart, List<ZodiacalReleasingPeriod> periods,
                                  OffsetDateTime activeDateTime, String caption) {
        out.append(caption).append("\n\n");
        out.append("| Active | # | Level | Sign | Start | End | Duration | Natal house | Ruler | Markers |\n");
        out.append("|---|---:|---:|---|---|---|---:|---:|---|---|\n");
        for (ZodiacalReleasingPeriod period : periods) {
            out.append("| ").append(active(period, activeDateTime) ? "★" : "")
                    .append(" | ").append(period.sequenceIndex() + 1)
                    .append(" | L").append(period.level())
                    .append(" | ").append(period.sign())
                    .append(" | ").append(format(period.startDateTime()))
                    .append(" | ").append(format(period.endDateTimeExclusive()))
                    .append(" | ").append(duration(period))
                    .append(" | ").append(houseForSign(chart, period.sign()))
                    .append(" | ").append(TraditionalTables.domicileRuler(period.sign()))
                    .append(" | ").append(markers(period.markers()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendPeriodRow(StringBuilder out, Chart chart, ZodiacalReleasingPeriod period,
                                 OffsetDateTime activeDateTime, boolean includeActive) {
        out.append("| L").append(period.level())
                .append(" | ").append(period.sign())
                .append(" | ").append(format(period.startDateTime()))
                .append(" | ").append(format(period.endDateTimeExclusive()))
                .append(" | ").append(houseForSign(chart, period.sign()))
                .append(" | ").append(TraditionalTables.domicileRuler(period.sign()))
                .append(" | ").append((includeActive && active(period, activeDateTime) ? "★ " : "")).append(markers(period.markers()))
                .append(" |\n");
    }

    private List<ZodiacalReleasingPeriod> activeChain(List<ZodiacalReleasingPeriod> periods, OffsetDateTime activeDateTime) {
        for (ZodiacalReleasingPeriod period : periods) {
            if (active(period, activeDateTime)) {
                List<ZodiacalReleasingPeriod> result = new ArrayList<>();
                result.add(period);
                result.addAll(activeChain(period.subPeriods(), activeDateTime));
                return result;
            }
        }
        return List.of();
    }

    private boolean active(ZodiacalReleasingPeriod period, OffsetDateTime activeDateTime) {
        return activeDateTime != null
                && !activeDateTime.isBefore(period.startDateTime())
                && activeDateTime.isBefore(period.endDateTimeExclusive());
    }

    private String markers(List<ZodiacalReleasingMarker> markers) {
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

    private int houseForSign(Chart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(house -> house.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing house for sign " + sign));
    }

    private Map<ZodiacSign, List<String>> pointsBySign(Chart chart) {
        return chart.getPoints().entrySet().stream()
                .map(entry -> pointLabel(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(PointLabel::sign,
                        Collectors.mapping(PointLabel::label, Collectors.collectingAndThen(Collectors.toList(), this::sorted))));
    }

    private PointLabel pointLabel(PointKey key, PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return new PointLabel(planetPoint.sign(), key.name());
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return new PointLabel(anglePoint.sign(), key.name());
        }
        return null;
    }

    private Map<ZodiacSign, List<String>> lotsBySign(Chart chart) {
        if (chart.getLots() == null) {
            return Map.of();
        }
        return chart.getLots().stream()
                .collect(Collectors.groupingBy(LotEntry::sign,
                        Collectors.mapping(LotEntry::name, Collectors.collectingAndThen(Collectors.toList(), this::sorted))));
    }

    private List<String> sorted(List<String> values) {
        return values.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "—";
        }
        return String.join(", ", values);
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String duration(ZodiacalReleasingPeriod period) {
        Duration duration = Duration.between(period.startDateTime(), period.endDateTimeExclusive());
        long totalHours = duration.toHours();
        long days = totalHours / 24;
        long hours = totalHours % 24;
        if (hours == 0) {
            return days + "d";
        }
        return days + "d " + hours + "h";
    }

    private String formatDegree(double degree) {
        return String.format(java.util.Locale.ROOT, "%.2f°", degree);
    }

    private String formatDecimal(double value) {
        if (value == Math.rint(value)) {
            return Long.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private record PointLabel(ZodiacSign sign, String label) {}
}
