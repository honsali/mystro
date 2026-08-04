package app.local;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import app.chart.model.Subject;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.DailyProfectionActivatedLot;
import app.reading.lifearc.model.DailyProfectionActivatedPoint;
import app.reading.lifearc.model.DailyProfectionReferenceEntry;
import app.reading.lifearc.model.DailyProfectionTable;
import app.reading.lifearc.model.DailyProfectionTableRow;

final class DailyProfectionMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate focusDate, DailyProfectionTable table) {
        StringBuilder out = new StringBuilder();
        out.append("# Daily Profections\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time: `").append(format(subject.getLocalBirthDateTime())).append("`\n");
        out.append("- Focus date: `").append(focusDate).append("`\n");
        out.append("- Window: `").append(table.windowStartDate()).append("` to `").append(table.windowEndDate()).append("`\n");
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Daily step method: `").append(table.dailyStepMethod()).append("`\n\n");
        appendAscDailyTable(out, table);
        appendReferenceTable(out, table);
        return out.toString();
    }

    private void appendAscDailyTable(StringBuilder out, DailyProfectionTable table) {
        out.append("## Daily Ascendant profection\n\n");
        out.append("| Focus | Date | Period | Age/M/D | Annual | Monthly | Daily | Activated natal points | Activated lots |\n");
        out.append("|---|---|---|---:|---|---|---|---|---|\n");
        for (DailyProfectionTableRow row : table.rows()) {
            DailyProfectionReferenceEntry asc = entry(row, AnnualProfectionReference.ASCENDANT);
            out.append("| ").append(row.focusDate() ? "yes" : "")
                    .append(" | ").append(row.date())
                    .append(" | ").append(format(row.periodStartDateTime())).append(" to ").append(format(row.periodEndDateTimeExclusive()))
                    .append(" | ").append(row.ageYears()).append("/M").append(row.monthInYear()).append("/D").append(row.dayInMonth())
                    .append(" | ").append(cell(asc.annualSign(), asc.annualHouse(), asc.annualLord()))
                    .append(" | ").append(cell(asc.monthlySign(), asc.monthlyHouse(), asc.monthlyLord()))
                    .append(" | ").append(cell(asc.profectedSign(), asc.profectedHouse(), asc.lord()))
                    .append(" | ").append(points(row.activatedNatalPoints()))
                    .append(" | ").append(lots(row.activatedLots()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendReferenceTable(StringBuilder out, DailyProfectionTable table) {
        out.append("## Daily profection by reference\n\n");
        out.append("| Focus | Date | ");
        out.append(table.referenceOrder().stream().map(this::label).collect(Collectors.joining(" | ")));
        out.append(" |\n");
        out.append("|---|---|");
        for (int i = 0; i < table.referenceOrder().size(); i++) {
            out.append("---|");
        }
        out.append("\n");
        for (DailyProfectionTableRow row : table.rows()) {
            out.append("| ").append(row.focusDate() ? "yes" : "")
                    .append(" | ").append(row.date())
                    .append(" | ");
            out.append(table.referenceOrder().stream()
                    .map(reference -> dailyCell(entry(row, reference)))
                    .collect(Collectors.joining(" | ")));
            out.append(" |\n");
        }
        out.append("\n");
    }

    private DailyProfectionReferenceEntry entry(DailyProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(candidate -> candidate.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing daily profection reference " + reference));
    }

    private String dailyCell(DailyProfectionReferenceEntry entry) {
        return cell(entry.profectedSign(), entry.profectedHouse(), entry.lord());
    }

    private String cell(Object sign, Integer house, Object lord) {
        String houseText = house == null ? "H—" : "H" + house;
        if (lord == null) {
            return houseText + " " + sign;
        }
        return houseText + " " + sign + " — " + lord;
    }

    private String points(List<DailyProfectionActivatedPoint> points) {
        if (points == null || points.isEmpty()) {
            return "—";
        }
        return points.stream()
                .map(point -> point.point() + " H" + point.house())
                .collect(Collectors.joining(", "));
    }

    private String lots(List<DailyProfectionActivatedLot> lots) {
        if (lots == null || lots.isEmpty()) {
            return "—";
        }
        return lots.stream()
                .map(lot -> lot.name() + " H" + lot.house() + " — " + lot.ruler())
                .collect(Collectors.joining(", "));
    }

    private String label(AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> "Asc";
            case MIDHEAVEN -> "MC";
            case SUN -> "Sun";
            case MOON -> "Moon";
            case LOT_FORTUNE -> "Fortune";
            case LOT_SPIRIT -> "Spirit";
        };
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }
}
