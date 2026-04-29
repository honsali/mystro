package app.model.data;

public enum Zodiac {
    TROPICAL,
    SIDEREAL;

    public static Zodiac parse(String value) {
        if (value == null || value.isBlank()) return TROPICAL;
        return value.trim().equalsIgnoreCase("sidereal") ? SIDEREAL : TROPICAL;
    }
}
