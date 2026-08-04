package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import app.chart.model.Subject;
import app.reading.lifearc.lunar.EclipseEvent;
import app.reading.lifearc.lunar.EclipseCandidateType;
import app.reading.lifearc.lunar.LunarReturnEntry;
import app.reading.lifearc.lunar.LunarSignIngressEntry;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.lunar.LunarZoomTable;
import app.reading.lifearc.lunar.LunationEntry;
import app.reading.lifearc.model.DailyProfectionTable;
import app.reading.lifearc.model.DailyProfectionTableRow;
import app.reading.lifearc.transit.ExactTransitHit;
import app.reading.lifearc.transit.ExactTransitHitKind;
import app.reading.lifearc.transit.TransitNatalTargetType;

final class LunarZoomMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  LunarZoomTable lunarZoom,
                  LunarTimingTable lunarTiming,
                  DailyProfectionTable dailyProfections,
                  List<ExactTransitHit> lunarHits) {
        StringBuilder out = new StringBuilder();
        out.append("# Lunar 30-Day Zoom\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n");
        out.append("- Lunar zoom method: `").append(lunarZoom.methodId()).append("`\n");
        out.append("- Sign ingress method: `").append(lunarZoom.signIngressMethod()).append("`\n\n");
        appendSignIngresses(out, lunarZoom);
        appendActiveLunarPeriods(out, lunarTiming);
        appendLunations(out, windowStart, windowEnd, lunarTiming);
        appendEclipses(out, windowStart, windowEnd, lunarTiming);
        appendMoonHits(out, dailyProfections, lunarHits);
        return out.toString();
    }

    private void appendSignIngresses(StringBuilder out, LunarZoomTable lunarZoom) {
        out.append("## Moon sign ingresses\n\n");
        if (lunarZoom.signIngresses().isEmpty()) {
            out.append("No Moon sign ingress falls inside this window.\n\n");
            return;
        }
        out.append("| # | Date/time | From | To | Moon longitude | Degree in sign | Natal house overlay |\n");
        out.append("|---:|---|---|---|---:|---:|---:|\n");
        for (LunarSignIngressEntry entry : lunarZoom.signIngresses()) {
            out.append("| ").append(entry.sequenceIndex())
                    .append(" | ").append(format(entry.dateTime()))
                    .append(" | ").append(entry.fromSign())
                    .append(" | ").append(entry.toSign())
                    .append(" | ").append(formatDecimal(entry.moonLongitude(), 4)).append("°")
                    .append(" | ").append(formatDecimal(entry.moonDegreeInSign(), 4)).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveLunarPeriods(StringBuilder out, LunarTimingTable lunarTiming) {
        out.append("## Active lunar periods at focus date\n\n");
        LunarReturnEntry activeReturn = lunarTiming.lunarReturns().stream()
                .filter(LunarReturnEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        LunationEntry activeLunation = lunarTiming.lunations().stream()
                .filter(LunationEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        out.append("| Layer | Start | End | Placement | House | Node / eclipse data |\n");
        out.append("|---|---|---|---|---:|---|\n");
        if (activeReturn == null) {
            out.append("| Lunar return | — | — | — | — | — |\n");
        } else {
            out.append("| Lunar return | ").append(format(activeReturn.returnDateTime()))
                    .append(" | ").append(format(activeReturn.periodEndDateTimeExclusive()))
                    .append(" | ").append(activeReturn.moonSign()).append(" ").append(formatDecimal(activeReturn.moonDegreeInSign(), 2)).append("°")
                    .append(" | H").append(activeReturn.natalHouseOverlay())
                    .append(" | nearest ").append(activeReturn.nearestNode()).append(" orb ").append(formatDecimal(activeReturn.nearestNodeOrbDegrees(), 2)).append("° |\n");
        }
        if (activeLunation == null) {
            out.append("| Lunation | — | — | — | — | — |\n");
        } else {
            out.append("| ").append(activeLunation.type())
                    .append(" | ").append(format(activeLunation.dateTime()))
                    .append(" | ").append(format(activeLunation.periodEndDateTimeExclusive()))
                    .append(" | ").append(activeLunation.syzygySign()).append(" ").append(formatDecimal(activeLunation.syzygyDegreeInSign(), 2)).append("°")
                    .append(" | H").append(activeLunation.natalHouseOverlay())
                    .append(" | eclipse candidate ").append(activeLunation.eclipseType()).append(" |\n");
        }
        out.append("\n");
    }

    private void appendLunations(StringBuilder out, OffsetDateTime windowStart, OffsetDateTime windowEnd, LunarTimingTable lunarTiming) {
        out.append("## Lunations inside window\n\n");
        List<LunationEntry> rows = lunarTiming.lunations().stream()
                .filter(entry -> inside(entry.dateTime(), windowStart, windowEnd))
                .toList();
        if (rows.isEmpty()) {
            out.append("No new/full Moon falls inside this window.\n\n");
            return;
        }
        out.append("| Type | Date/time | Syzygy placement | House | Sun | Moon | Node | Eclipse candidate |\n");
        out.append("|---|---|---|---:|---|---|---|---|\n");
        for (LunationEntry entry : rows) {
            out.append("| ").append(entry.type())
                    .append(" | ").append(format(entry.dateTime()))
                    .append(" | ").append(entry.syzygySign()).append(" ").append(formatDecimal(entry.syzygyDegreeInSign(), 2)).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | ").append(entry.sunSign()).append(" ").append(formatDecimal(entry.sunDegreeInSign(), 2)).append("°")
                    .append(" | ").append(entry.moonSign()).append(" ").append(formatDecimal(entry.moonDegreeInSign(), 2)).append("°")
                    .append(" | ").append(entry.nearestNode()).append(" orb ").append(formatDecimal(entry.nearestNodeOrbDegrees(), 2)).append("°")
                    .append(" | ").append(entry.eclipseType())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendEclipses(StringBuilder out, OffsetDateTime windowStart, OffsetDateTime windowEnd, LunarTimingTable lunarTiming) {
        out.append("## Eclipse rows inside window\n\n");
        List<LunationEntry> candidates = lunarTiming.lunations().stream()
                .filter(entry -> inside(entry.dateTime(), windowStart, windowEnd))
                .filter(entry -> entry.eclipseType() != EclipseCandidateType.NONE)
                .toList();
        List<EclipseEvent> trueEvents = lunarTiming.eclipseEvents().stream()
                .filter(entry -> inside(entry.maximumDateTime(), windowStart, windowEnd))
                .toList();
        if (candidates.isEmpty() && trueEvents.isEmpty()) {
            out.append("No mean-node eclipse candidate or true Swiss Ephemeris eclipse event falls inside this window.\n\n");
            return;
        }
        out.append("| Row type | Date/time | Kind/type | Placement | House | Node orb | Visibility |\n");
        out.append("|---|---|---|---|---:|---:|---|\n");
        for (LunationEntry entry : candidates) {
            out.append("| Mean-node candidate | ").append(format(entry.dateTime()))
                    .append(" | ").append(entry.type()).append(" / ").append(entry.eclipseType())
                    .append(" | ").append(entry.syzygySign()).append(" ").append(formatDecimal(entry.syzygyDegreeInSign(), 2)).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | ").append(formatDecimal(entry.nearestNodeOrbDegrees(), 2)).append("°")
                    .append(" | — |\n");
        }
        for (EclipseEvent entry : trueEvents) {
            out.append("| True eclipse | ").append(format(entry.maximumDateTime()))
                    .append(" | ").append(entry.kind()).append(" / ").append(entry.eclipseType())
                    .append(" | ").append(entry.syzygySign()).append(" ").append(formatDecimal(entry.syzygyDegreeInSign(), 2)).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | ").append(formatDecimal(entry.nearestNodeOrbDegrees(), 2)).append("°")
                    .append(" | ").append(entry.visibility().localVisibility())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendMoonHits(StringBuilder out, DailyProfectionTable dailyProfections, List<ExactTransitHit> lunarHits) {
        out.append("## Exact transiting Moon aspects to daily-activated natal points/lots\n\n");
        List<ExactTransitHit> activeHits = lunarHits.stream()
                .filter(hit -> activeDailyTarget(hit, dailyProfections))
                .sorted(Comparator
                        .comparing(ExactTransitHit::exactDateTime)
                        .thenComparing(hit -> hit.natalTargetType().ordinal())
                        .thenComparing(ExactTransitHit::natalTargetName)
                        .thenComparing(hit -> hit.aspect().ordinal()))
                .toList();
        if (activeHits.isEmpty()) {
            out.append("No exact transiting Moon aspect to a daily-activated natal point or lot was found inside this window.\n\n");
            return;
        }
        out.append("| # | Exact date/time | Active daily row | Natal target | Aspect | Target placement | House | Moon longitude | Separation | Orb | Kind |\n");
        out.append("|---:|---|---|---|---|---|---:|---:|---:|---:|---|\n");
        int sequence = 1;
        for (ExactTransitHit hit : activeHits) {
            DailyProfectionTableRow row = activeDailyRow(hit.exactDateTime(), dailyProfections);
            out.append("| ").append(sequence++)
                    .append(" | ").append(format(hit.exactDateTime()))
                    .append(" | ").append(row == null ? "—" : row.date() + " D" + row.dayInMonth())
                    .append(" | ").append(hit.natalTargetType()).append(" ").append(hit.natalTargetName())
                    .append(" | ").append(hit.aspect())
                    .append(" | ").append(hit.natalTargetSign()).append(" ").append(formatDecimal(hit.natalTargetDegreeInSign(), 2)).append("°")
                    .append(" | H").append(hit.natalTargetHouse())
                    .append(" | ").append(formatDecimal(hit.transitLongitude(), 2)).append("°")
                    .append(" | ").append(formatDecimal(hit.angularSeparation(), 2)).append("°")
                    .append(" | ").append(formatDecimal(hit.orbFromExactDegrees(), 4)).append("°")
                    .append(" | ").append(kind(hit.hitKind()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private boolean activeDailyTarget(ExactTransitHit hit, DailyProfectionTable table) {
        DailyProfectionTableRow row = activeDailyRow(hit.exactDateTime(), table);
        if (row == null) {
            return false;
        }
        if (hit.natalTargetType() == TransitNatalTargetType.POINT) {
            return row.activatedNatalPoints().stream().anyMatch(point -> point.point().name().equals(hit.natalTargetName()));
        }
        if (hit.natalTargetType() == TransitNatalTargetType.LOT) {
            return row.activatedLots().stream().anyMatch(lot -> lot.name().equals(hit.natalTargetName()));
        }
        return false;
    }

    private DailyProfectionTableRow activeDailyRow(OffsetDateTime dateTime, DailyProfectionTable table) {
        return table.rows().stream()
                .filter(row -> !dateTime.isBefore(row.periodStartDateTime()) && dateTime.isBefore(row.periodEndDateTimeExclusive()))
                .findFirst()
                .orElse(null);
    }

    private boolean inside(OffsetDateTime candidate, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        return candidate != null && !candidate.isBefore(windowStart) && !candidate.isAfter(windowEnd);
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

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }
}
