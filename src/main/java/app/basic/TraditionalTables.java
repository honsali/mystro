package app.basic;

import java.util.List;
import app.model.basic.TriplicityRulers;
import app.model.data.Element;
import app.model.data.Planet;
import app.model.data.Terms;
import app.model.data.Triplicity;
import app.model.data.ZodiacSign;

public final class TraditionalTables {
    private record TermBoundary(double upperDegreeExclusive, Planet ruler) {
    }

    public static boolean isTraditionalPlanet(Planet planet) {
        return switch (planet) {
            case SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN -> true;
            case NORTH_NODE, SOUTH_NODE -> false;
        };
    }

    public static Planet domicileRuler(ZodiacSign sign) {
        return switch (sign) {
            case ARIES, SCORPIO -> Planet.MARS;
            case TAURUS, LIBRA -> Planet.VENUS;
            case GEMINI, VIRGO -> Planet.MERCURY;
            case CANCER -> Planet.MOON;
            case LEO -> Planet.SUN;
            case SAGITTARIUS, PISCES -> Planet.JUPITER;
            case CAPRICORN, AQUARIUS -> Planet.SATURN;
        };
    }

    public static Planet exaltationRuler(ZodiacSign sign) {
        return switch (sign) {
            case ARIES -> Planet.SUN;
            case TAURUS -> Planet.MOON;
            case CANCER -> Planet.JUPITER;
            case VIRGO -> Planet.MERCURY;
            case LIBRA -> Planet.SATURN;
            case CAPRICORN -> Planet.MARS;
            case PISCES -> Planet.VENUS;
            case GEMINI, LEO, SCORPIO, SAGITTARIUS, AQUARIUS -> null;
        };
    }

    public static TriplicityRulers triplicityRulers(ZodiacSign sign, Triplicity triplicity) {
        return switch (triplicity) {
            case DOROTHEAN -> dorotheanTriplicityRulers(sign);
            case PTOLEMAIC -> ptolemaicTriplicityRulers(sign);
        };
    }

    private static TriplicityRulers dorotheanTriplicityRulers(ZodiacSign sign) {
        return switch (element(sign)) {
            case FIRE -> new TriplicityRulers(Planet.SUN, Planet.JUPITER, Planet.SATURN);
            case EARTH -> new TriplicityRulers(Planet.VENUS, Planet.MOON, Planet.MARS);
            case AIR -> new TriplicityRulers(Planet.SATURN, Planet.MERCURY, Planet.JUPITER);
            case WATER -> new TriplicityRulers(Planet.VENUS, Planet.MARS, Planet.MOON);
        };
    }

    private static TriplicityRulers ptolemaicTriplicityRulers(ZodiacSign sign) {
        return switch (element(sign)) {
            case FIRE -> new TriplicityRulers(Planet.SUN, Planet.JUPITER, null);
            case EARTH -> new TriplicityRulers(Planet.VENUS, Planet.MOON, null);
            case AIR -> new TriplicityRulers(Planet.SATURN, Planet.MERCURY, null);
            case WATER -> new TriplicityRulers(Planet.MARS, Planet.VENUS, null);
        };
    }

    public static Element element(ZodiacSign sign) {
        return switch (sign) {
            case ARIES, LEO, SAGITTARIUS -> Element.FIRE;
            case TAURUS, VIRGO, CAPRICORN -> Element.EARTH;
            case GEMINI, LIBRA, AQUARIUS -> Element.AIR;
            case CANCER, SCORPIO, PISCES -> Element.WATER;
        };
    }

    public static Planet faceRuler(ZodiacSign sign, double degreeInSign) {
        int signIndex = sign.ordinal();
        int decan = Math.min(2, (int) Math.floor(degreeInSign / 10.0));
        int absoluteDecan = signIndex * 3 + decan;
        List<Planet> chaldeanOrder = List.of(Planet.MARS, Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON, Planet.SATURN, Planet.JUPITER);
        return chaldeanOrder.get(absoluteDecan % chaldeanOrder.size());
    }

    public static ZodiacSign opposite(ZodiacSign sign) {
        ZodiacSign[] signs = ZodiacSign.values();
        return signs[(sign.ordinal() + 6) % signs.length];
    }

    public static Planet termRuler(double longitude, Terms terms) {
        if (terms == Terms.NONE) {
            return null;
        }
        double degree = degreeInSign(longitude);
        ZodiacSign sign = signOf(longitude);
        TermBoundary[] boundaries = terms == Terms.PTOLEMAIC ? ptolemaicTerms(sign) : egyptianTerms(sign);
        for (TermBoundary boundary : boundaries) {
            if (degree < boundary.upperDegreeExclusive()) {
                return boundary.ruler();
            }
        }
        return boundaries[boundaries.length - 1].ruler();
    }

    public static ZodiacSign signOf(double longitude) {
        return ZodiacSign.values()[(int) Math.floor(normalize(longitude) / 30.0)];
    }

    public static double degreeInSign(double longitude) {
        return normalize(longitude) % 30.0;
    }

    public static double normalize(double degrees) {
        double value = degrees % 360.0;
        return value < 0 ? value + 360.0 : value;
    }

    private static TermBoundary[] egyptianTerms(ZodiacSign sign) {
        return switch (sign) {
            case ARIES -> terms(6, Planet.JUPITER, 12, Planet.VENUS, 20, Planet.MERCURY, 25, Planet.MARS, 30, Planet.SATURN);
            case TAURUS -> terms(8, Planet.VENUS, 14, Planet.MERCURY, 22, Planet.JUPITER, 27, Planet.SATURN, 30, Planet.MARS);
            case GEMINI -> terms(6, Planet.MERCURY, 12, Planet.JUPITER, 17, Planet.VENUS, 24, Planet.MARS, 30, Planet.SATURN);
            case CANCER -> terms(7, Planet.MARS, 13, Planet.VENUS, 19, Planet.MERCURY, 26, Planet.JUPITER, 30, Planet.SATURN);
            case LEO -> terms(6, Planet.JUPITER, 11, Planet.VENUS, 18, Planet.SATURN, 24, Planet.MERCURY, 30, Planet.MARS);
            case VIRGO -> terms(7, Planet.MERCURY, 17, Planet.VENUS, 21, Planet.JUPITER, 28, Planet.MARS, 30, Planet.SATURN);
            case LIBRA -> terms(6, Planet.SATURN, 14, Planet.MERCURY, 21, Planet.JUPITER, 28, Planet.VENUS, 30, Planet.MARS);
            case SCORPIO -> terms(7, Planet.MARS, 11, Planet.VENUS, 19, Planet.MERCURY, 24, Planet.JUPITER, 30, Planet.SATURN);
            case SAGITTARIUS -> terms(12, Planet.JUPITER, 17, Planet.VENUS, 21, Planet.MERCURY, 26, Planet.SATURN, 30, Planet.MARS);
            case CAPRICORN -> terms(7, Planet.MERCURY, 14, Planet.JUPITER, 22, Planet.VENUS, 26, Planet.SATURN, 30, Planet.MARS);
            case AQUARIUS -> terms(7, Planet.MERCURY, 13, Planet.VENUS, 20, Planet.JUPITER, 25, Planet.MARS, 30, Planet.SATURN);
            case PISCES -> terms(12, Planet.VENUS, 16, Planet.JUPITER, 19, Planet.MERCURY, 28, Planet.MARS, 30, Planet.SATURN);
        };
    }

    private static TermBoundary[] ptolemaicTerms(ZodiacSign sign) {
        return switch (sign) {
            case ARIES -> terms(6, Planet.JUPITER, 14, Planet.VENUS, 21, Planet.MERCURY, 26, Planet.MARS, 30, Planet.SATURN);
            case TAURUS -> terms(8, Planet.VENUS, 15, Planet.MERCURY, 22, Planet.JUPITER, 27, Planet.SATURN, 30, Planet.MARS);
            case GEMINI -> terms(7, Planet.MERCURY, 14, Planet.JUPITER, 21, Planet.VENUS, 25, Planet.SATURN, 30, Planet.MARS);
            case CANCER -> terms(6, Planet.MARS, 13, Planet.JUPITER, 20, Planet.MERCURY, 27, Planet.VENUS, 30, Planet.SATURN);
            case LEO -> terms(6, Planet.SATURN, 13, Planet.MERCURY, 19, Planet.VENUS, 25, Planet.JUPITER, 30, Planet.MARS);
            case VIRGO -> terms(7, Planet.MERCURY, 13, Planet.VENUS, 18, Planet.JUPITER, 24, Planet.SATURN, 30, Planet.MARS);
            case LIBRA -> terms(6, Planet.SATURN, 11, Planet.VENUS, 19, Planet.JUPITER, 24, Planet.MERCURY, 30, Planet.MARS);
            case SCORPIO -> terms(6, Planet.MARS, 14, Planet.JUPITER, 21, Planet.VENUS, 27, Planet.MERCURY, 30, Planet.SATURN);
            case SAGITTARIUS -> terms(8, Planet.JUPITER, 14, Planet.VENUS, 19, Planet.MERCURY, 25, Planet.SATURN, 30, Planet.MARS);
            case CAPRICORN -> terms(6, Planet.VENUS, 12, Planet.MERCURY, 19, Planet.JUPITER, 25, Planet.MARS, 30, Planet.SATURN);
            case AQUARIUS -> terms(6, Planet.SATURN, 12, Planet.MERCURY, 20, Planet.VENUS, 25, Planet.JUPITER, 30, Planet.MARS);
            case PISCES -> terms(8, Planet.VENUS, 14, Planet.JUPITER, 20, Planet.MERCURY, 26, Planet.MARS, 30, Planet.SATURN);
        };
    }

    private static TermBoundary[] terms(int upper1, Planet ruler1, int upper2, Planet ruler2, int upper3, Planet ruler3, int upper4, Planet ruler4, int upper5, Planet ruler5) {
        return new TermBoundary[] {new TermBoundary(upper1, ruler1), new TermBoundary(upper2, ruler2), new TermBoundary(upper3, ruler3), new TermBoundary(upper4, ruler4), new TermBoundary(upper5, ruler5)};
    }

    private TraditionalTables() {}
}
