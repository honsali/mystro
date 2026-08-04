package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionSignificator;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;

final class MundanePrimaryDirectionMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, MundanePrimaryDirectionTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Mundane / Semi-Arc Primary Direction Prototype\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendCaveats(out, table);
        appendSignificators(out, table.significators());
        appendActiveInquiryYear(out, table);
        appendOverview(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, MundanePrimaryDirectionTable table) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Direction method: `").append(table.directionMethod()).append("`\n");
        out.append("- Arc conversion: `").append(table.arcConversionMethod()).append("`\n");
        out.append("- Contact method: `").append(table.contactMethod()).append("`\n");
        out.append("- Birth latitude: `").append(formatDecimal(table.birthLatitude(), 4)).append("°`\n");
        out.append("- Natal ARMC: `").append(formatDegree(table.natalArmcDegrees())).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Coverage: `").append(format(table.coverageStartDateTime())).append("` to `")
                .append(format(table.coverageEndDateTimeExclusive())).append("`\n");
        if (table.inquiryYearStartDateTime() != null) {
            out.append("- Inquiry birthday-year window: `").append(format(table.inquiryYearStartDateTime()))
                    .append("` to `").append(format(table.inquiryYearEndDateTimeExclusive())).append("`\n");
        }
        out.append("\n");
    }

    private void appendCaveats(StringBuilder out, MundanePrimaryDirectionTable table) {
        out.append("## Prototype caveats\n\n");
        out.append("- Caveat label: `").append(table.prototypeCaveat()).append("`\n");
        out.append("- This is a separately labelled local/research prototype, not the public reading contract and not a replacement for `output/<alias>/primary_directions.md`.\n");
        out.append("- It uses a normalized semi-arc geometry: Ascendant = 0°, MC = 90°, Descendant = 180°, IC = 270° on a mundane position circle.\n");
        out.append("- Only direct body promissors from the seven traditional planets are included; rays, converse directions, mundane latitude refinements, and competing historical variants are deferred.\n");
        out.append("- The selected hyleg is directed as vitality evidence only. Do not infer death timing, deterministic lifespan, or standalone event claims from this file.\n\n");
    }

    private void appendSignificators(StringBuilder out, List<MundanePrimaryDirectionSignificator> significators) {
        out.append("## Directed significators\n\n");
        out.append("| Role | Point | Hyleg | Placement | House | Ecliptic latitude | RA | Declination | Diurnal SA | Nocturnal SA | Natal hour angle | Mundane position | Segment |\n");
        out.append("|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|\n");
        for (MundanePrimaryDirectionSignificator significator : significators) {
            out.append("| ").append(significator.role())
                    .append(" | ").append(significator.point())
                    .append(" | ").append(significator.selectedHyleg() ? "yes" : "")
                    .append(" | ").append(placement(significator.sign(), significator.degreeInSign()))
                    .append(" | H").append(significator.house())
                    .append(" | ").append(formatDegree(significator.eclipticLatitude()))
                    .append(" | ").append(formatDegree(significator.rightAscension()))
                    .append(" | ").append(formatDegree(significator.declination()))
                    .append(" | ").append(formatDegree(significator.diurnalSemiArcDegrees()))
                    .append(" | ").append(formatDegree(significator.nocturnalSemiArcDegrees()))
                    .append(" | ").append(formatDegree(significator.natalHourAngleDegrees()))
                    .append(" | ").append(formatDegree(significator.mundanePositionDegrees()))
                    .append(" | ").append(significator.mundanePositionSegment())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveInquiryYear(StringBuilder out, MundanePrimaryDirectionTable table) {
        if (table.inquiryYearStartDateTime() == null) {
            return;
        }
        out.append("## Prototype directions in the inquiry birthday year\n\n");
        out.append("Window: `").append(format(table.inquiryYearStartDateTime())).append("` to `")
                .append(format(table.inquiryYearEndDateTimeExclusive())).append("`.\n\n");
        List<MundanePrimaryDirectionEvent> active = table.events().stream()
                .filter(MundanePrimaryDirectionEvent::activeForInquiryYear)
                .toList();
        if (active.isEmpty()) {
            out.append("No exact mundane/semi-arc prototype contacts fall inside this birthday-year window.\n\n");
            return;
        }
        appendEventsTable(out, active, false);
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, MundanePrimaryDirectionTable table) {
        out.append("## 0–100 prototype overview\n\n");
        appendEventsTable(out, table.events(), true);
        out.append("\n");
    }

    private void appendEventsTable(StringBuilder out, List<MundanePrimaryDirectionEvent> events, boolean includeActive) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        out.append(" # | Date | Age | Significator | Promissor body | Target mundane position | Directed hour angle | Directed ARMC | Arc | Promissor natal | Natal house |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        out.append("---:|---|---:|---|---|---|---:|---:|---:|---|---:|\n");
        for (MundanePrimaryDirectionEvent event : events) {
            out.append("|");
            if (includeActive) {
                out.append(" ").append(event.activeForInquiryYear() ? "★" : "").append(" |");
            }
            out.append(" ").append(event.sequenceIndex())
                    .append(" | ").append(format(event.dateTime()))
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(formatDegree(event.targetMundanePositionDegrees())).append(" ").append(event.targetMundanePositionSegment())
                    .append(" | ").append(formatDegree(event.directedHourAngleDegrees()))
                    .append(" | ").append(formatDegree(event.directedArmcDegrees()))
                    .append(" | ").append(formatDegree(event.arcDegrees()))
                    .append(" | ").append(placement(event.promissorNatalSign(), event.promissorNatalDegreeInSign()))
                    .append(" | H").append(event.promissorNatalHouse())
                    .append(" |\n");
        }
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
