package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import app.chart.data.PointKey;
import app.chart.model.Subject;
import app.reading.lifearc.solarreturn.SolarReturnEntry;
import app.reading.lifearc.solarreturn.SolarReturnPointEntry;
import app.reading.lifearc.solarreturn.SolarReturnTable;

final class SolarReturnMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");

    String render(Subject subject, SolarReturnTable table, OffsetDateTime activeDateTime) {
        StringBuilder out = new StringBuilder();
        out.append("# Solar Returns\n\n");
        appendSummary(out, subject, table, activeDateTime);
        appendOverview(out, table, activeDateTime);
        appendDetailedCharts(out, table, activeDateTime);
        return out.toString();
    }

    private void appendSummary(StringBuilder out, Subject subject, SolarReturnTable table, OffsetDateTime activeDateTime) {
        out.append("## Summary\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        if (activeDateTime != null) {
            out.append("- Inquiry date/time: `").append(format(activeDateTime)).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine/source label: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Return location method: `").append(table.locationMethod()).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n");
        out.append("- Natal Sun target: `").append(table.natalSunSign()).append(" ")
                .append(formatDegree(table.natalSunDegreeInSign())).append("` (`")
                .append(formatDegree(table.natalSunLongitude())).append("` longitude)\n\n");
        out.append("Each row is the exact tropical apparent Sun return to the natal Sun longitude, calculated for the natal/request location and fixed request UTC offset. ")
                .append("Rows can be used both backward for validation and forward for prediction. They are timing evidence, not standalone event claims.\n\n");
    }

    private void appendOverview(StringBuilder out, SolarReturnTable table, OffsetDateTime activeDateTime) {
        out.append("## 0–100 overview\n\n");
        out.append("Periods run from one exact solar return to the next.\n\n");
        out.append("| Active | Age | Return start | Period end | SR Asc | SR MC | Sect | Sun house | Moon | Moon house |\n");
        out.append("|---|---:|---|---|---|---|---|---:|---|---:|\n");
        for (SolarReturnEntry row : table.rows()) {
            SolarReturnPointEntry sun = point(row, PointKey.SUN);
            SolarReturnPointEntry moon = point(row, PointKey.MOON);
            out.append("| ").append(active(row, activeDateTime) ? "★" : "")
                    .append(" | ").append(row.ageYears())
                    .append(" | ").append(format(row.returnDateTime()))
                    .append(" | ").append(format(row.periodEndDateTimeExclusive()))
                    .append(" | ").append(placement(row.ascendantSign(), row.ascendantDegreeInSign()))
                    .append(" | ").append(placement(row.midheavenSign(), row.midheavenDegreeInSign()))
                    .append(" | ").append(row.sect())
                    .append(" | ").append(house(sun))
                    .append(" | ").append(placement(moon))
                    .append(" | ").append(house(moon))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDetailedCharts(StringBuilder out, SolarReturnTable table, OffsetDateTime activeDateTime) {
        out.append("## Detailed return charts\n\n");
        for (SolarReturnEntry row : table.rows()) {
            out.append("### Age ").append(row.ageYears());
            if (active(row, activeDateTime)) {
                out.append(" ★");
            }
            out.append("\n\n");
            out.append("- Return period: `").append(format(row.returnDateTime())).append("` to `")
                    .append(format(row.periodEndDateTimeExclusive())).append("`\n");
            out.append("- Julian day UT: `").append(formatDecimal(row.julianDayUt(), 5)).append("`\n");
            out.append("- Solar-return sect: `").append(row.sect()).append("`\n");
            out.append("- Ascendant: `").append(placement(row.ascendantSign(), row.ascendantDegreeInSign())).append("`\n");
            out.append("- Midheaven: `").append(placement(row.midheavenSign(), row.midheavenDegreeInSign())).append("`\n\n");

            out.append("| Point | Type | Placement | Longitude | House | Retrograde |\n");
            out.append("|---|---|---|---:|---:|---|\n");
            for (SolarReturnPointEntry point : row.points()) {
                out.append("| ").append(point.point())
                        .append(" | ").append(point.type())
                        .append(" | ").append(placement(point))
                        .append(" | ").append(formatDegree(point.longitude()))
                        .append(" | ").append(house(point))
                        .append(" | ").append(retrograde(point))
                        .append(" |\n");
            }
            out.append("\n");
        }
    }

    private SolarReturnPointEntry point(SolarReturnEntry row, PointKey point) {
        return row.points().stream()
                .filter(candidate -> candidate.point() == point)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing solar-return point " + point));
    }

    private boolean active(SolarReturnEntry row, OffsetDateTime activeDateTime) {
        return activeDateTime != null
                && !activeDateTime.isBefore(row.returnDateTime())
                && activeDateTime.isBefore(row.periodEndDateTimeExclusive());
    }

    private String placement(SolarReturnPointEntry point) {
        return placement(point.sign(), point.degreeInSign());
    }

    private String placement(app.chart.data.ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDegree(degreeInSign);
    }

    private String house(SolarReturnPointEntry point) {
        return point.house() == null ? "—" : Integer.toString(point.house());
    }

    private String retrograde(SolarReturnPointEntry point) {
        if (point.retrograde() == null) {
            return "—";
        }
        return point.retrograde() ? "R" : "";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime.format(DATE_TIME);
    }

    private String formatDegree(double value) {
        return formatDecimal(value, 2) + "°";
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "% ." + places + "f", value).trim();
    }
}
