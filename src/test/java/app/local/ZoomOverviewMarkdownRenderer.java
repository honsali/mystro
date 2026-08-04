package app.local;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.chart.model.Subject;

final class ZoomOverviewMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  Path outputDir,
                  List<FileReference> files) {
        StringBuilder out = new StringBuilder();
        out.append("# Mystro Zoom Timing Pack\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n");
        out.append("- Output directory: `").append(outputDir.toString().replace('\\', '/')).append("`\n\n");
        out.append("This zoom pack is the bounded high-resolution layer below the 0–100 macro overview. ")
                .append("It keeps the focus on one requested date and a ±15-day window.\n\n");
        out.append("## Files\n\n");
        out.append("| File | Purpose |\n");
        out.append("|---|---|\n");
        for (FileReference file : files) {
            out.append("| [").append(file.path().getFileName()).append("](")
                    .append(file.path().getFileName())
                    .append(") | ").append(file.description()).append(" |\n");
        }
        out.append("\n");
        return out.toString();
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    record FileReference(Path path, String description) {}
}
