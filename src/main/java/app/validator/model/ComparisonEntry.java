package app.validator.model;

import java.util.List;

public record ComparisonEntry(
        String name,
        String status,
        List<String> differences
) {
}
