package app.chart.data;

public enum Terms {
    EGYPTIAN,
    PTOLEMAIC,
    NONE;

    public static Terms parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Terms value is required");
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "EGYPTIAN" -> EGYPTIAN;
            case "PTOLEMAIC", "PTOLEMY" -> PTOLEMAIC;
            case "NONE" -> NONE;
            default -> throw new IllegalArgumentException("Unknown terms: " + value);
        };
    }
}
