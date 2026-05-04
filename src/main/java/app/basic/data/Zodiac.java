package app.basic.data;

public enum Zodiac {
    TROPICAL;

    public static Zodiac parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Zodiac value is required");
        }
        return switch (value.trim().toUpperCase()) {
            case "TROPICAL" -> TROPICAL;
            default -> throw new IllegalArgumentException("Unknown zodiac: " + value);
        };
    }
}
