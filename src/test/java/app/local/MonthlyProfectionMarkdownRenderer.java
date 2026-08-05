package app.local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import app.chart.model.Subject;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;

final class MonthlyProfectionMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject, LocalDate inquiryDate, MonthlyProfectionTable table) {
        StringBuilder out = new StringBuilder();
        MonthlyProfectionTableRow activeRow = table.rows().stream()
                .filter(MonthlyProfectionTableRow::activeForInquiry)
                .findFirst()
                .orElse(null);

        out.append("# Monthly Profections\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(subject.getUtcBirthDateTime().format(DATE_TIME)).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        if (activeRow != null) {
            out.append("- Active age/month: `").append(activeRow.ageYears()).append("/M")
                    .append(activeRow.monthInYear()).append("`\n");
            out.append("- Active period: `").append(activeRow.periodStartDateTime().format(DATE_TIME)).append("` to `")
                    .append(activeRow.periodEndDateTimeExclusive().format(DATE_TIME)).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n\n");
        out.append("Monthly periods are local monthly anniversaries from the birth date/time. ")
                .append("The first month of each birthday year repeats that year's annual profection; each later month advances one sign. ")
                .append("Each profection cell is `H<house> SIGN — LORD`.\n\n");

        appendReferenceSigns(out, table);
        appendCycleTables(out, table);
        return out.toString();
    }

    private void appendReferenceSigns(StringBuilder out, MonthlyProfectionTable table) {
        if (table.rows().isEmpty()) {
            return;
        }
        MonthlyProfectionTableRow first = table.rows().get(0);
        out.append("## Natal reference signs\n\n");
        out.append("| Reference | Natal sign |\n");
        out.append("|---|---|\n");
        for (AnnualProfectionReference reference : table.referenceOrder()) {
            MonthlyProfectionReferenceEntry entry = entry(first, reference);
            out.append("| ").append(label(reference)).append(" | ").append(entry.natalSign()).append(" |\n");
        }
        out.append("\n");
    }

    private void appendCycleTables(StringBuilder out, MonthlyProfectionTable table) {
        int currentCycle = -1;
        int currentAge = -1;
        boolean tableOpen = false;
        for (MonthlyProfectionTableRow row : table.rows()) {
            if (row.cycleNumber() != currentCycle) {
                if (tableOpen) {
                    out.append("\n");
                }
                currentCycle = row.cycleNumber();
                currentAge = -1;
                int startAge = row.ageYears();
                int cycleNumber = currentCycle;
                int endAge = table.rows().stream()
                        .filter(candidate -> candidate.cycleNumber() == cycleNumber)
                        .mapToInt(MonthlyProfectionTableRow::ageYears)
                        .max()
                        .orElse(startAge);
                out.append("## Cycle ").append(currentCycle).append(" — ages ").append(startAge).append("–").append(endAge).append("\n\n");
                tableOpen = false;
            }
            if (row.ageYears() != currentAge) {
                if (tableOpen) {
                    out.append("\n");
                }
                currentAge = row.ageYears();
                out.append("### Age ").append(row.ageYears())
                        .append(" — annual year ").append(row.yearInCycle())
                        .append("\n\n");
                appendHeader(out, table);
                tableOpen = true;
            }
            appendRow(out, table, row);
        }
        if (tableOpen) {
            out.append("\n");
        }
    }

    private void appendHeader(StringBuilder out, MonthlyProfectionTable table) {
        out.append("| Month | Period | ");
        out.append(table.referenceOrder().stream().map(this::label).collect(Collectors.joining(" | ")));
        out.append(" |\n");
        out.append("|---:|---|");
        for (int i = 0; i < table.referenceOrder().size(); i++) {
            out.append("---|");
        }
        out.append("\n");
    }

    private void appendRow(StringBuilder out, MonthlyProfectionTable table, MonthlyProfectionTableRow row) {
        out.append("| ").append(row.activeForInquiry() ? row.monthInYear() + " ★" : Integer.toString(row.monthInYear()));
        out.append(" | ").append(row.periodStartDateTime().format(DATE_TIME)).append(" to ")
                .append(row.periodEndDateTimeExclusive().format(DATE_TIME));
        out.append(" | ");
        out.append(table.referenceOrder().stream()
                .map(reference -> cell(entry(row, reference)))
                .collect(Collectors.joining(" | ")));
        out.append(" |\n");
    }

    private MonthlyProfectionReferenceEntry entry(MonthlyProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(candidate -> candidate.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing monthly profection reference " + reference));
    }

    private String cell(MonthlyProfectionReferenceEntry entry) {
        return "H" + entry.profectedHouse() + " " + entry.profectedSign() + " — " + entry.lord();
    }

    private String label(AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> "Asc / Lord of Month";
            case MIDHEAVEN -> "MC";
            case SUN -> "Sun";
            case MOON -> "Moon";
            case LOT_FORTUNE -> "Fortune";
            case LOT_SPIRIT -> "Spirit";
        };
    }
}
