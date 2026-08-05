package app.local;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import app.chart.model.Subject;
import app.planetaryhours.PlanetaryHourEntry;
import app.planetaryhours.PlanetaryHourPeriod;
import app.planetaryhours.PlanetaryHoursBoundary;
import app.planetaryhours.PlanetaryHoursCalculation;

final class PlanetaryHoursMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, OffsetDateTime focusDateTime, PlanetaryHoursCalculation calculation) {
        StringBuilder out = new StringBuilder();
        out.append("# Planetary Hours\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Time basis: `").append(calculation.getTimeBasis()).append("`\n");
        out.append("- Planetary day ruler: `").append(calculation.getDayRuler()).append("`\n");
        out.append("- Coverage: `").append(boundary(calculation.getCoverageStart())).append("` to `")
                .append(boundary(calculation.getCoverageEnd())).append("`\n");
        out.append("- Sunrise: `").append(boundary(calculation.getSunrise())).append("`\n");
        out.append("- Sunset: `").append(boundary(calculation.getSunset())).append("`\n");
        out.append("- Next sunrise: `").append(boundary(calculation.getNextSunrise())).append("`\n");
        out.append("- Day hour duration: `").append(formatDecimal(calculation.getDayHourDurationMinutes())).append(" minutes`\n");
        out.append("- Night hour duration: `").append(formatDecimal(calculation.getNightHourDurationMinutes())).append(" minutes`\n\n");
        appendHours(out, "Day hours", calculation.getHours(), PlanetaryHourPeriod.DAY, focusDateTime);
        appendHours(out, "Night hours", calculation.getHours(), PlanetaryHourPeriod.NIGHT, focusDateTime);
        return out.toString();
    }

    private void appendHours(StringBuilder out,
                             String title,
                             List<PlanetaryHourEntry> entries,
                             PlanetaryHourPeriod period,
                             OffsetDateTime focusDateTime) {
        out.append("## ").append(title).append("\n\n");
        out.append("| Focus | Seq | Hour | Ruler | Start | End | Duration | Midpoint |\n");
        out.append("|---|---:|---:|---|---|---|---:|---|\n");
        entries.stream()
                .filter(entry -> entry.getPeriod() == period)
                .forEach(entry -> out.append("| ").append(active(entry, focusDateTime) ? "yes" : "")
                        .append(" | ").append(entry.getSequence())
                        .append(" | ").append(entry.getHour())
                        .append(" | ").append(entry.getRulerGlyph()).append(" ").append(entry.getRuler())
                        .append(" | ").append(dateTime(entry.getStartDate(), entry.getStartTime()))
                        .append(" | ").append(dateTime(entry.getEndDate(), entry.getEndTime()))
                        .append(" | ").append(formatDecimal(entry.getFullDurationMinutes()))
                        .append(" | ").append(boundary(entry.getMidpoint()))
                        .append(" |\n"));
        out.append("\n");
    }

    private boolean active(PlanetaryHourEntry entry, OffsetDateTime focusDateTime) {
        OffsetDateTime start = dateTimeValue(entry.getStartDate(), entry.getStartTime());
        OffsetDateTime end = dateTimeValue(entry.getEndDate(), entry.getEndTime());
        return !focusDateTime.isBefore(start) && focusDateTime.isBefore(end);
    }

    private OffsetDateTime dateTimeValue(LocalDate date, String time) {
        return OffsetDateTime.of(date, LocalTime.parse(time), ZoneOffset.UTC);
    }

    private String dateTime(LocalDate date, String time) {
        return date + " " + time;
    }

    private String boundary(PlanetaryHoursBoundary boundary) {
        return boundary.getDate() + " " + boundary.getTime();
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.withOffsetSameInstant(ZoneOffset.UTC).format(DATE_TIME);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
