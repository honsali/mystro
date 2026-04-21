package app.validator.model;

import java.util.List;

public record ComparisonSummary(
        List<String> requestedNames,
        int totalRequested,
        int comparedCount,
        int matchCount,
        int mismatchCount,
        List<ComparisonEntry> entries
) {
}
