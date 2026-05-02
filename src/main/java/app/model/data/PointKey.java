package app.model.data;

public enum PointKey {
    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    NORTH_NODE,
    SOUTH_NODE,
    ASCENDANT,
    MIDHEAVEN,
    DESCENDANT,
    IMUM_COELI;

    public static PointKey of(Planet planet) {
        return switch (planet) {
            case SUN -> SUN;
            case MOON -> MOON;
            case MERCURY -> MERCURY;
            case VENUS -> VENUS;
            case MARS -> MARS;
            case JUPITER -> JUPITER;
            case SATURN -> SATURN;
            case NORTH_NODE -> NORTH_NODE;
            case SOUTH_NODE -> SOUTH_NODE;
        };
    }

    public static PointKey of(AngleType angle) {
        return switch (angle) {
            case ASCENDANT -> ASCENDANT;
            case MIDHEAVEN -> MIDHEAVEN;
            case DESCENDANT -> DESCENDANT;
            case IMUM_COELI -> IMUM_COELI;
        };
    }

}
