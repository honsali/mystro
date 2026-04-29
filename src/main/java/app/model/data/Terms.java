package app.model.data;

public enum Terms {
    EGYPTIAN,
    PTOLEMAIC,
    NONE;

    public static Terms parse(String value) {
        if (value == null || value.isBlank()) return EGYPTIAN;
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "PTOLEMAIC", "PTOLEMY" -> PTOLEMAIC;
            case "NONE" -> NONE;
            default -> EGYPTIAN;
        };
    }
}
