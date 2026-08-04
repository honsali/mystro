package app.reading.description.common.data;

import java.util.List;

import app.chart.data.Planet;

/**
 * Explicit traditional fixed-star catalogue for the Valens natal-description fixed-star pass.
 *
 * <p>The Swiss Ephemeris identifiers are nomenclature names from {@code ephe/sefstars.txt},
 * prefixed with a comma to avoid ambiguous traditional-name prefix matches. The planetary natures
 * are the compact traditional equivalents transmitted in Robson's fixed-star tables.
 */
public final class FixedStarCatalogue {
    private static final String ROBSON_SOURCE = "Robson fixed-star planetary nature; Swiss Ephemeris sefstars.txt";

    private static final List<FixedStarDefinition> BRIGHT_TRADITIONAL_STARS = List.of(
            star("Achernar", ",alEri", 0.46, List.of(Planet.JUPITER)),
            star("Acrux", ",alCru", 0.81, List.of(Planet.JUPITER)),
            star("Agena", ",beCen", 0.60, List.of(Planet.VENUS, Planet.JUPITER)),
            star("Aldebaran", ",alTau", 0.86, List.of(Planet.MARS)),
            star("Algol", ",bePer", 2.12, List.of(Planet.SATURN, Planet.JUPITER)),
            star("Alcyone (Pleiades)", ",etTau", 2.87, List.of(Planet.MOON, Planet.MARS)),
            star("Alpheratz", ",alAnd", 2.06, List.of(Planet.JUPITER, Planet.VENUS)),
            star("Altair", ",alAql", 0.76, List.of(Planet.MARS, Planet.JUPITER)),
            star("Antares", ",alSco", 0.91, List.of(Planet.MARS, Planet.JUPITER)),
            star("Arcturus", ",alBoo", -0.05, List.of(Planet.MARS, Planet.JUPITER)),
            star("Bellatrix", ",gaOri", 1.64, List.of(Planet.MARS, Planet.MERCURY)),
            star("Betelgeuse", ",alOri", 0.42, List.of(Planet.MARS, Planet.MERCURY)),
            star("Canopus", ",alCar", -0.74, List.of(Planet.SATURN, Planet.JUPITER)),
            star("Capella", ",alAur", 0.08, List.of(Planet.MARS, Planet.MERCURY)),
            star("Castor", ",alGem", 1.58, List.of(Planet.MERCURY)),
            star("Deneb", ",alCyg", 1.25, List.of(Planet.VENUS, Planet.MERCURY)),
            star("Denebola", ",beLeo", 2.13, List.of(Planet.SATURN, Planet.VENUS)),
            star("Fomalhaut", ",alPsA", 1.16, List.of(Planet.VENUS, Planet.MERCURY)),
            star("Hamal", ",alAri", 2.01, List.of(Planet.MARS, Planet.SATURN)),
            star("Markab", ",alPeg", 2.48, List.of(Planet.MARS, Planet.MERCURY)),
            star("Menkar", ",alCet", 2.53, List.of(Planet.SATURN)),
            star("Pollux", ",beGem", 1.14, List.of(Planet.MARS)),
            star("Procyon", ",alCMi", 0.37, List.of(Planet.MERCURY, Planet.MARS)),
            star("Regulus", ",alLeo", 1.40, List.of(Planet.MARS, Planet.JUPITER)),
            star("Rigel", ",beOri", 0.13, List.of(Planet.JUPITER, Planet.SATURN)),
            star("Scheat", ",bePeg", 2.42, List.of(Planet.MARS, Planet.MERCURY)),
            star("Sirius", ",alCMa", -1.46, List.of(Planet.JUPITER, Planet.MARS)),
            star("Spica", ",alVir", 0.97, List.of(Planet.VENUS, Planet.MARS)),
            star("Vega", ",alLyr", 0.03, List.of(Planet.VENUS, Planet.MERCURY)),
            star("Vindemiatrix", ",epVir", 2.79, List.of(Planet.SATURN, Planet.MERCURY))
    );

    public static List<FixedStarDefinition> brightTraditionalStars() {
        return BRIGHT_TRADITIONAL_STARS;
    }

    private static FixedStarDefinition star(String name, String swissEphemerisId, double magnitude, List<Planet> traditionalNature) {
        return new FixedStarDefinition(name, swissEphemerisId, magnitude, traditionalNature, ROBSON_SOURCE);
    }

    public record FixedStarDefinition(
            String name,
            String swissEphemerisId,
            double magnitude,
            List<Planet> traditionalNature,
            String source
    ) {
        public FixedStarDefinition {
            traditionalNature = List.copyOf(traditionalNature);
        }
    }

    private FixedStarCatalogue() {
    }
}
