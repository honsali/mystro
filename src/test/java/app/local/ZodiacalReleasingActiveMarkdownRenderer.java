package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.TraditionalTables;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.Chart;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingMarker;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingPeriod;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

final class ZodiacalReleasingActiveMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  Chart chart,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  List<LotTimeline> lotTimelines) {
        StringBuilder out = new StringBuilder();
        out.append("# Zodiacal Releasing Active Zoom\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n");
        out.append("- Timing scale: L1 = sign years × 360 days; L2 = sign years × 30 days; L3 = sign years × 60 hours; L4 = sign years × 5 hours.\n\n");
        appendActiveChains(out, chart, focusDateTime, lotTimelines);
        appendWindowBoundaries(out, chart, windowStart, windowEnd, lotTimelines);
        return out.toString();
    }

    private void appendActiveChains(StringBuilder out, Chart chart, OffsetDateTime focusDateTime, List<LotTimeline> lotTimelines) {
        out.append("## Active chains at focus date\n\n");
        if (lotTimelines == null || lotTimelines.isEmpty()) {
            out.append("No Zodiacal Releasing lots were available.\n\n");
            return;
        }
        out.append("| Lot | Level | Sign | Start | End | Natal house | Ruler | Markers |\n");
        out.append("|---|---:|---|---|---|---:|---|---|\n");
        for (LotTimeline lotTimeline : lotTimelines) {
            List<ZodiacalReleasingPeriod> chain = activeChain(lotTimeline.timeline().periods(), focusDateTime);
            if (chain.isEmpty()) {
                out.append("| ").append(lotTimeline.lot().name()).append(" | — | — | — | — | — | — | — |\n");
                continue;
            }
            for (ZodiacalReleasingPeriod period : chain) {
                out.append("| ").append(lotTimeline.lot().name())
                        .append(" | L").append(period.level())
                        .append(" | ").append(period.sign())
                        .append(" | ").append(format(period.startDateTime()))
                        .append(" | ").append(format(period.endDateTimeExclusive()))
                        .append(" | H").append(houseForSign(chart, period.sign()))
                        .append(" | ").append(TraditionalTables.domicileRuler(period.sign()))
                        .append(" | ").append(markers(period.markers()))
                        .append(" |\n");
            }
        }
        out.append("\n");
    }

    private void appendWindowBoundaries(StringBuilder out, Chart chart, OffsetDateTime windowStart, OffsetDateTime windowEnd, List<LotTimeline> lotTimelines) {
        out.append("## Period boundaries inside the focus window\n\n");
        List<Boundary> boundaries = new ArrayList<>();
        if (lotTimelines != null) {
            for (LotTimeline lotTimeline : lotTimelines) {
                collectBoundaries(boundaries, lotTimeline.lot(), lotTimeline.timeline().periods(), windowStart, windowEnd);
            }
        }
        boundaries = boundaries.stream()
                .sorted(Comparator
                        .comparing(Boundary::dateTime)
                        .thenComparing(boundary -> boundary.lot().name())
                        .thenComparing(boundary -> boundary.period().level()))
                .toList();
        if (boundaries.isEmpty()) {
            out.append("No Zodiacal Releasing period boundary falls inside this ±15-day window.\n\n");
            return;
        }
        out.append("| Date/time | Boundary | Lot | Level | Sign | Natal house | Ruler | Period span | Markers |\n");
        out.append("|---|---|---|---:|---|---:|---|---|---|\n");
        for (Boundary boundary : boundaries) {
            ZodiacalReleasingPeriod period = boundary.period();
            out.append("| ").append(format(boundary.dateTime()))
                    .append(" | ").append(boundary.kind())
                    .append(" | ").append(boundary.lot().name())
                    .append(" | L").append(period.level())
                    .append(" | ").append(period.sign())
                    .append(" | H").append(houseForSign(chart, period.sign()))
                    .append(" | ").append(TraditionalTables.domicileRuler(period.sign()))
                    .append(" | ").append(format(period.startDateTime())).append(" to ").append(format(period.endDateTimeExclusive()))
                    .append(" | ").append(markers(period.markers()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void collectBoundaries(List<Boundary> boundaries,
                                   LotEntry lot,
                                   List<ZodiacalReleasingPeriod> periods,
                                   OffsetDateTime windowStart,
                                   OffsetDateTime windowEnd) {
        for (ZodiacalReleasingPeriod period : periods) {
            if (inside(period.startDateTime(), windowStart, windowEnd)) {
                boundaries.add(new Boundary(lot, period, period.startDateTime(), "start"));
            }
            if (inside(period.endDateTimeExclusive(), windowStart, windowEnd)) {
                boundaries.add(new Boundary(lot, period, period.endDateTimeExclusive(), "end"));
            }
            collectBoundaries(boundaries, lot, period.subPeriods(), windowStart, windowEnd);
        }
    }

    private boolean inside(OffsetDateTime candidate, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        return candidate != null && !candidate.isBefore(windowStart) && !candidate.isAfter(windowEnd);
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

    private int houseForSign(Chart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(house -> house.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing house for sign " + sign));
    }

    private String markers(List<ZodiacalReleasingMarker> markers) {
        if (markers == null || markers.isEmpty()) {
            return "—";
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
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    record LotTimeline(LotEntry lot, ZodiacalReleasingTimeline timeline) {}

    private record Boundary(LotEntry lot, ZodiacalReleasingPeriod period, OffsetDateTime dateTime, String kind) {}
}
