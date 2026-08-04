package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import app.chart.data.AspectType;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.distribution.DistributionContactType;
import app.reading.lifearc.distribution.DistributionThroughBoundsContact;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;
import app.reading.lifearc.primarydirection.PrimaryDirectionContactType;
import app.reading.lifearc.primarydirection.PrimaryDirectionCoordinate;
import app.reading.lifearc.primarydirection.PrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;

final class DirectionsZoomMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  DistributionThroughBoundsTable distribution,
                  List<DistributionThroughBoundsTable> extendedDistributions,
                  PrimaryDirectionTable primaryDirections,
                  MundanePrimaryDirectionTable mundanePrimaryDirections) {
        StringBuilder out = new StringBuilder();
        out.append("# Directions 30-Day Zoom\n\n");
        appendSummary(out, subject, focusDateTime, windowStart, windowEnd, distribution, extendedDistributions, primaryDirections, mundanePrimaryDirections);
        appendActiveDistributionPeriods(out, distribution, extendedDistributions);
        appendDistributionContacts(out, windowStart, windowEnd, distribution, extendedDistributions);
        appendPrimaryDirectionContacts(out, windowStart, windowEnd, primaryDirections);
        appendMundanePrimaryDirectionContacts(out, windowStart, windowEnd, mundanePrimaryDirections);
        return out.toString();
    }

    private void appendSummary(StringBuilder out,
                               Subject subject,
                               OffsetDateTime focusDateTime,
                               OffsetDateTime windowStart,
                               OffsetDateTime windowEnd,
                               DistributionThroughBoundsTable distribution,
                               List<DistributionThroughBoundsTable> extendedDistributions,
                               PrimaryDirectionTable primaryDirections,
                               MundanePrimaryDirectionTable mundanePrimaryDirections) {
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n");
        out.append("- Distribution method: `").append(distribution.methodId()).append("`\n");
        out.append("- Distribution timing method: `").append(distribution.timingMethod()).append("`\n");
        out.append("- Distribution contact method: `").append(distribution.contactMethod()).append("`\n");
        out.append("- Extended distribution tables: `").append(extendedDistributions == null ? 0 : extendedDistributions.size()).append("`\n");
        out.append("- Primary-direction method: `").append(primaryDirections.methodId()).append("`\n");
        out.append("- Primary-direction contact method: `").append(primaryDirections.contactMethod()).append("`\n");
        out.append("- Mundane/semi-arc method: `").append(mundanePrimaryDirections.methodId()).append("`\n");
        out.append("- Mundane/semi-arc caveat label: `").append(mundanePrimaryDirections.prototypeCaveat()).append("`\n\n");
    }

    private void appendActiveDistributionPeriods(StringBuilder out,
                                                 DistributionThroughBoundsTable distribution,
                                                 List<DistributionThroughBoundsTable> extendedDistributions) {
        out.append("## Active distribution bound periods\n\n");
        out.append("| Directed point | Active bound | Bound ruler | Directed span | Age span | Period | Contacts in bound |\n");
        out.append("|---|---|---|---|---:|---|---:|\n");
        List<DistributionThroughBoundsTable> tables = allDistributionTables(distribution, extendedDistributions);
        for (DistributionThroughBoundsTable table : tables) {
            DistributionThroughBoundsPeriod active = activePeriod(table);
            if (active == null) {
                out.append("| ").append(table.directedPoint()).append(" | — | — | — | — | — | 0 |\n");
                continue;
            }
            out.append("| ").append(table.directedPoint())
                    .append(" | ").append(active.sign()).append(" ")
                    .append(formatDecimal(active.boundStartDegreeInSign(), 2)).append("°–")
                    .append(formatDecimal(active.boundEndDegreeInSign(), 2)).append("°")
                    .append(" | ").append(active.boundRuler())
                    .append(" | ").append(placement(active.sign(), active.directedStartDegreeInSign()))
                    .append(" → ").append(placement(active.sign(), active.directedEndDegreeInSign()))
                    .append(" | ").append(formatDecimal(active.startAgeYears(), 2)).append("–")
                    .append(formatDecimal(active.endAgeYearsExclusive(), 2))
                    .append(" | ").append(format(active.startDateTime())).append(" to ").append(format(active.endDateTimeExclusive()))
                    .append(" | ").append(active.contacts().size())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDistributionContacts(StringBuilder out,
                                            OffsetDateTime windowStart,
                                            OffsetDateTime windowEnd,
                                            DistributionThroughBoundsTable distribution,
                                            List<DistributionThroughBoundsTable> extendedDistributions) {
        out.append("## Distribution contacts inside window\n\n");
        List<DistributionContactRow> rows = new ArrayList<>();
        for (DistributionThroughBoundsTable table : allDistributionTables(distribution, extendedDistributions)) {
            for (DistributionThroughBoundsPeriod period : table.periods()) {
                for (DistributionThroughBoundsContact contact : period.contacts()) {
                    if (inside(contact.dateTime(), windowStart, windowEnd)) {
                        rows.add(new DistributionContactRow(table, period, contact));
                    }
                }
            }
        }
        rows = rows.stream()
                .sorted(Comparator
                        .comparing((DistributionContactRow row) -> row.contact().dateTime())
                        .thenComparing(row -> row.table().directedPoint())
                        .thenComparing(row -> row.contact().sourcePlanet().ordinal())
                        .thenComparing(row -> row.contact().contactType().ordinal()))
                .toList();
        if (rows.isEmpty()) {
            out.append("No exact distribution-through-bounds body/ray contact falls inside this ±15-day window.\n\n");
            return;
        }
        out.append("| Date/time | Directed point | Active bound | Source planet | Contact | Directed placement | Bound ruler | Arc | Age | Source natal placement | Source natal house |\n");
        out.append("|---|---|---|---|---|---|---|---:|---:|---|---:|\n");
        for (DistributionContactRow row : rows) {
            DistributionThroughBoundsPeriod period = row.period();
            DistributionThroughBoundsContact contact = row.contact();
            out.append("| ").append(format(contact.dateTime()))
                    .append(" | ").append(row.table().directedPoint())
                    .append(" | ").append(period.sign()).append(" ")
                    .append(formatDecimal(period.boundStartDegreeInSign(), 2)).append("°–")
                    .append(formatDecimal(period.boundEndDegreeInSign(), 2)).append("°")
                    .append(" | ").append(contact.sourcePlanet())
                    .append(" | ").append(distributionContact(contact))
                    .append(" | ").append(placement(contact.directedSign(), contact.directedDegreeInSign()))
                    .append(" | ").append(contact.boundRulerAtContact())
                    .append(" | ").append(formatDecimal(contact.arcDegrees(), 2)).append("°")
                    .append(" | ").append(formatDecimal(contact.ageYears(), 2))
                    .append(" | ").append(placement(contact.sourceNatalSign(), contact.sourceNatalDegreeInSign()))
                    .append(" | H").append(contact.sourceNatalHouse())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendPrimaryDirectionContacts(StringBuilder out,
                                                OffsetDateTime windowStart,
                                                OffsetDateTime windowEnd,
                                                PrimaryDirectionTable primaryDirections) {
        out.append("## Primary-direction contacts inside window\n\n");
        List<PrimaryDirectionEvent> rows = primaryDirections.events().stream()
                .filter(event -> inside(event.dateTime(), windowStart, windowEnd))
                .sorted(Comparator
                        .comparing(PrimaryDirectionEvent::dateTime)
                        .thenComparing(event -> event.direction().ordinal())
                        .thenComparing(PrimaryDirectionEvent::significatorRole)
                        .thenComparing(event -> event.promissorPlanet().ordinal())
                        .thenComparing(event -> event.contactType().ordinal()))
                .toList();
        if (rows.isEmpty()) {
            out.append("No exact normalized zodiacal primary-direction contact falls inside this ±15-day window.\n\n");
            return;
        }
        out.append("| # | Date/time | Direction | Significator | Coord | Promissor | Contact | Directed target | Arc | Age | Promissor natal placement | Natal house |\n");
        out.append("|---:|---|---|---|---|---|---|---|---:|---:|---|---:|\n");
        int sequence = 1;
        for (PrimaryDirectionEvent event : rows) {
            out.append("| ").append(sequence++)
                    .append(" | ").append(format(event.dateTime()))
                    .append(" | ").append(event.direction())
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(coordinate(event.coordinate()))
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(primaryContact(event))
                    .append(" | ").append(placement(event.targetSign(), event.targetDegreeInSign()))
                    .append(" | ").append(formatDecimal(event.arcDegrees(), 2)).append("°")
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(placement(event.promissorNatalSign(), event.promissorNatalDegreeInSign()))
                    .append(" | H").append(event.promissorNatalHouse())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendMundanePrimaryDirectionContacts(StringBuilder out,
                                                       OffsetDateTime windowStart,
                                                       OffsetDateTime windowEnd,
                                                       MundanePrimaryDirectionTable mundanePrimaryDirections) {
        out.append("## Mundane/semi-arc primary-direction contacts inside window\n\n");
        List<MundanePrimaryDirectionEvent> rows = mundanePrimaryDirections.events().stream()
                .filter(event -> inside(event.dateTime(), windowStart, windowEnd))
                .sorted(Comparator
                        .comparing(MundanePrimaryDirectionEvent::dateTime)
                        .thenComparing(MundanePrimaryDirectionEvent::significatorRole)
                        .thenComparing(event -> event.promissorPlanet().ordinal()))
                .toList();
        if (rows.isEmpty()) {
            out.append("No exact mundane/semi-arc prototype contact falls inside this ±15-day window.\n\n");
            return;
        }
        out.append("| # | Date/time | Significator | Promissor body | Contact | Target mundane position | Directed hour angle | Directed ARMC | Arc | Age | Promissor natal placement | Natal house |\n");
        out.append("|---:|---|---|---|---|---|---:|---:|---:|---:|---|---:|\n");
        int sequence = 1;
        for (MundanePrimaryDirectionEvent event : rows) {
            out.append("| ").append(sequence++)
                    .append(" | ").append(format(event.dateTime()))
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(event.contactType())
                    .append(" | ").append(formatDecimal(event.targetMundanePositionDegrees(), 2)).append("° ").append(event.targetMundanePositionSegment())
                    .append(" | ").append(formatDecimal(event.directedHourAngleDegrees(), 2)).append("°")
                    .append(" | ").append(formatDecimal(event.directedArmcDegrees(), 2)).append("°")
                    .append(" | ").append(formatDecimal(event.arcDegrees(), 2)).append("°")
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(placement(event.promissorNatalSign(), event.promissorNatalDegreeInSign()))
                    .append(" | H").append(event.promissorNatalHouse())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private List<DistributionThroughBoundsTable> allDistributionTables(DistributionThroughBoundsTable distribution,
                                                                       List<DistributionThroughBoundsTable> extendedDistributions) {
        List<DistributionThroughBoundsTable> tables = new ArrayList<>();
        if (distribution != null) {
            tables.add(distribution);
        }
        if (extendedDistributions != null) {
            tables.addAll(extendedDistributions);
        }
        return tables;
    }

    private DistributionThroughBoundsPeriod activePeriod(DistributionThroughBoundsTable table) {
        return table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
    }

    private boolean inside(OffsetDateTime candidate, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        return candidate != null && !candidate.isBefore(windowStart) && !candidate.isAfter(windowEnd);
    }

    private String distributionContact(DistributionThroughBoundsContact contact) {
        if (contact.contactType() == DistributionContactType.BODY) {
            return "BODY " + aspect(contact.aspect());
        }
        return "RAY " + aspect(contact.aspect()) + " " + contact.rayDirection();
    }

    private String primaryContact(PrimaryDirectionEvent event) {
        if (event.contactType() == PrimaryDirectionContactType.BODY) {
            return "BODY " + aspect(event.aspect());
        }
        return "RAY " + aspect(event.aspect()) + " " + event.rayDirection();
    }

    private String coordinate(PrimaryDirectionCoordinate coordinate) {
        return switch (coordinate) {
            case OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE -> "OA";
            case RIGHT_ASCENSION -> "RA";
        };
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

    private record DistributionContactRow(
            DistributionThroughBoundsTable table,
            DistributionThroughBoundsPeriod period,
            DistributionThroughBoundsContact contact
    ) {}
}
