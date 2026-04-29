package app.model.data;

public enum Zodiac {
    TROPICAL,
    SIDEREAL;

    public static Zodiac parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Zodiac value is required");
        }
        return switch (value.trim().toUpperCase()) {
            case "TROPICAL" -> TROPICAL;
            case "SIDEREAL" -> SIDEREAL;
            default -> throw new IllegalArgumentException("Unknown zodiac: " + value);
        };
    }
}
