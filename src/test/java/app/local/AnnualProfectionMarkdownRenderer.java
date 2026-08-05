package app.local;

import java.time.LocalDate;
import java.util.stream.Collectors;

import app.chart.model.Subject;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;

final class AnnualProfectionMarkdownRenderer {

    String render(Subject subject, LocalDate inquiryDate, AnnualProfectionTable table) {
        StringBuilder out = new StringBuilder();
        AnnualProfectionTableRow activeRow = table.rows().stream()
                .filter(AnnualProfectionTableRow::activeForInquiry)
                .findFirst()
                .orElse(null);

        out.append("# Annual Profections\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date (UTC): `").append(subject.getUtcBirthDateTime().toLocalDate()).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
        }
        if (activeRow != null) {
            out.append("- Active age: `").append(activeRow.ageYears()).append("`\n");
            out.append("- Active period: `").append(activeRow.periodStartDate()).append("` to `")
                    .append(activeRow.periodEndDateExclusive()).append("`\n");
        }
        out.append("- Method: `").append(table.methodId()).append("`\n");
        out.append("- Doctrine: `").append(table.primaryDoctrine()).append("`\n");
        out.append("- Age range: `").append(table.ageStartYears()).append("` to `")
                .append(table.ageEndYearsInclusive()).append("` completed years\n\n");
        out.append("Each profection cell is `H<house> SIGN — LORD`.\n\n");

        appendReferenceSigns(out, table);
        appendCycleTables(out, table);
        return out.toString();
    }

    private void appendReferenceSigns(StringBuilder out, AnnualProfectionTable table) {
        if (table.rows().isEmpty()) {
            return;
        }
        AnnualProfectionTableRow first = table.rows().get(0);
        out.append("## Natal reference signs\n\n");
        out.append("| Reference | Natal sign |\n");
        out.append("|---|---|\n");
        for (AnnualProfectionReference reference : table.referenceOrder()) {
            AnnualProfectionReferenceEntry entry = entry(first, reference);
            out.append("| ").append(label(reference)).append(" | ").append(entry.natalSign()).append(" |\n");
        }
        out.append("\n");
    }

    private void appendCycleTables(StringBuilder out, AnnualProfectionTable table) {
        var rowsByCycle = table.rows().stream()
                .collect(Collectors.groupingBy(AnnualProfectionTableRow::cycleNumber, java.util.LinkedHashMap::new, Collectors.toList()));

        rowsByCycle.forEach((cycle, rows) -> {
            int startAge = rows.get(0).ageYears();
            int endAge = rows.get(rows.size() - 1).ageYears();
            out.append("## Cycle ").append(cycle).append(" — ages ").append(startAge).append("–").append(endAge).append("\n\n");
            appendHeader(out, table);
            for (AnnualProfectionTableRow row : rows) {
                appendRow(out, table, row);
            }
            out.append("\n");
        });
    }

    private void appendHeader(StringBuilder out, AnnualProfectionTable table) {
        out.append("| Age | Period | Year | ");
        out.append(table.referenceOrder().stream().map(this::label).collect(Collectors.joining(" | ")));
        out.append(" |\n");
        out.append("|---:|---|---:|");
        for (int i = 0; i < table.referenceOrder().size(); i++) {
            out.append("---|");
        }
        out.append("\n");
    }

    private void appendRow(StringBuilder out, AnnualProfectionTable table, AnnualProfectionTableRow row) {
        out.append("| ").append(row.activeForInquiry() ? row.ageYears() + " ★" : Integer.toString(row.ageYears()));
        out.append(" | ").append(row.periodStartDate()).append(" to ").append(row.periodEndDateExclusive());
        out.append(" | ").append(row.yearInCycle()).append(" | ");
        out.append(table.referenceOrder().stream()
                .map(reference -> cell(entry(row, reference)))
                .collect(Collectors.joining(" | ")));
        out.append(" |\n");
    }

    private AnnualProfectionReferenceEntry entry(AnnualProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(candidate -> candidate.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing profection reference " + reference));
    }

    private String cell(AnnualProfectionReferenceEntry entry) {
        return "H" + entry.profectedHouse() + " " + entry.profectedSign() + " — " + entry.lord();
    }

    private String label(AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> "Asc / Lord of Year";
            case MIDHEAVEN -> "MC";
            case SUN -> "Sun";
            case MOON -> "Moon";
            case LOT_FORTUNE -> "Fortune";
            case LOT_SPIRIT -> "Spirit";
        };
    }
}
