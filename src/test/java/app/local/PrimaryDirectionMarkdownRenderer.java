package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import app.chart.data.AspectType;
import app.chart.data.ZodiacSign;
import app.chart.model.Subject;
import app.reading.lifearc.primarydirection.PrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionContactType;
import app.reading.lifearc.primarydirection.PrimaryDirectionCoordinate;
import app.reading.lifearc.primarydirection.PrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.PrimaryDirectionPolarity;
import app.reading.lifearc.primarydirection.PrimaryDirectionSignificator;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;

final class PrimaryDirectionMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, PrimaryDirectionTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Primary Directions\n\n");
        appendSummary(out, subject, inquiryDate, table);
        appendSignificators(out, table.significators());
        appendActiveInquiryYear(out, table);
        appendOverview(out, table);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, LocalDate inquiryDate, PrimaryDirectionTable table) {
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
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Coverage: `").append(format(table.coverageStartDateTime())).append("` to `")
                .append(format(table.coverageEndDateTimeExclusive())).append("`\n");
        if (table.inquiryYearStartDateTime() != null) {
            out.append("- Inquiry birthday-year window: `").append(format(table.inquiryYearStartDateTime()))
                    .append("` to `").append(format(table.inquiryYearEndDateTimeExclusive())).append("`\n");
        }
        out.append("\nThis local research file calculates normalized zodiacal primary directions for the selected Ptolemaic hyleg and core angles. ")
                .append("Ascensional directions use oblique ascension at the birth latitude; Midheaven directions use right ascension. ")
                .append("One equatorial degree is converted to one mean tropical year. Contacts are exact directions to natal traditional planet bodies and Ptolemaic rays, and are timing evidence rather than standalone event claims.\n");
        if (includeDirectionColumn(table)) {
            out.append("Direct and converse rows are separately labelled; cite the direction column with the method id when using this expanded local/research table.\n");
        }
        out.append("\n");
    }

    private void appendSignificators(StringBuilder out, List<PrimaryDirectionSignificator> significators) {
        out.append("## Directed significators\n\n");
        out.append("| Role | Point | Hyleg | Coordinate | Placement | House | Ecliptic latitude | RA | Declination | Direction coordinate |\n");
        out.append("|---|---|---|---|---|---:|---:|---:|---:|---:|\n");
        for (PrimaryDirectionSignificator significator : significators) {
            out.append("| ").append(significator.role())
                    .append(" | ").append(significator.point())
                    .append(" | ").append(significator.selectedHyleg() ? "yes" : "")
                    .append(" | ").append(coordinate(significator.coordinate()))
                    .append(" | ").append(placement(significator.sign(), significator.degreeInSign()))
                    .append(" | H").append(significator.house())
                    .append(" | ").append(formatDegree(significator.eclipticLatitude()))
                    .append(" | ").append(formatDegree(significator.rightAscension()))
                    .append(" | ").append(formatDegree(significator.declination()))
                    .append(" | ").append(formatDegree(significator.directionCoordinateDegrees()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendActiveInquiryYear(StringBuilder out, PrimaryDirectionTable table) {
        if (table.inquiryYearStartDateTime() == null) {
            return;
        }
        out.append("## Primary directions in the inquiry birthday year\n\n");
        out.append("Window: `").append(format(table.inquiryYearStartDateTime())).append("` to `")
                .append(format(table.inquiryYearEndDateTimeExclusive())).append("`.\n\n");
        List<PrimaryDirectionEvent> active = table.events().stream()
                .filter(PrimaryDirectionEvent::activeForInquiryYear)
                .toList();
        if (active.isEmpty()) {
            out.append("No exact primary-direction contacts fall inside this birthday-year window.\n\n");
            return;
        }
        appendEventsTable(out, active, false, includeDirectionColumn(table));
        out.append("\n");
    }

    private void appendOverview(StringBuilder out, PrimaryDirectionTable table) {
        out.append("## 0–100 overview\n\n");
        appendEventsTable(out, table.events(), true, includeDirectionColumn(table));
        out.append("\n");
    }

    private void appendEventsTable(StringBuilder out, List<PrimaryDirectionEvent> events, boolean includeActive,
                                   boolean includeDirection) {
        out.append("|");
        if (includeActive) {
            out.append(" Active |");
        }
        if (includeDirection) {
            out.append(" Direction |");
        }
        out.append(" # | Date | Age | Significator | Coord | Promissor | Contact | Directed target | Arc | Promissor natal | Natal house |\n");
        out.append("|");
        if (includeActive) {
            out.append("---|");
        }
        if (includeDirection) {
            out.append("---|");
        }
        out.append("---:|---|---:|---|---|---|---|---|---:|---|---:|\n");
        for (PrimaryDirectionEvent event : events) {
            out.append("|");
            if (includeActive) {
                out.append(" ").append(event.activeForInquiryYear() ? "★" : "").append(" |");
            }
            if (includeDirection) {
                out.append(" ").append(event.direction()).append(" |");
            }
            out.append(" ").append(event.sequenceIndex())
                    .append(" | ").append(format(event.dateTime()))
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(coordinate(event.coordinate()))
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(contact(event))
                    .append(" | ").append(placement(event.targetSign(), event.targetDegreeInSign()))
                    .append(" | ").append(formatDegree(event.arcDegrees()))
                    .append(" | ").append(placement(event.promissorNatalSign(), event.promissorNatalDegreeInSign()))
                    .append(" | H").append(event.promissorNatalHouse())
                    .append(" |\n");
        }
    }

    private boolean includeDirectionColumn(PrimaryDirectionTable table) {
        return !PrimaryDirectionCalculator.METHOD_ID.equals(table.methodId())
                || table.events().stream().anyMatch(event -> event.direction() != PrimaryDirectionPolarity.DIRECT);
    }

    private String contact(PrimaryDirectionEvent event) {
        if (event.contactType() == PrimaryDirectionContactType.BODY) {
            return "BODY " + aspect(event.aspect());
        }
        return "RAY " + aspect(event.aspect()) + " " + event.rayDirection();
    }

    private String aspect(AspectType aspect) {
        return aspect == null ? "—" : aspect.name();
    }

    private String coordinate(PrimaryDirectionCoordinate coordinate) {
        return switch (coordinate) {
            case OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE -> "OA";
            case RIGHT_ASCENSION -> "RA";
        };
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
