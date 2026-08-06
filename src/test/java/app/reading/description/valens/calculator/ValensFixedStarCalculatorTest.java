package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.AngleType;
import app.chart.data.Angularity;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.data.FixedStarCatalogue.FixedStarDefinition;
import app.reading.description.common.data.FixedStarTargetType;
import app.reading.description.common.model.FixedStarEntry;
import app.reading.description.common.model.LotEntry;

class ValensFixedStarCalculatorTest {
    @Test
    void usesSwissEphemerisFixedStarLongitudeForCatalogueStar() {
        double expectedAldebaranLongitudeAtJ2000 = 69.79031729409506;
        FixedStarDefinition aldebaran = new FixedStarDefinition("Aldebaran", ",alTau", 0.86, List.of(Planet.MARS), "test-source");
        ValensFixedStarCalculator calculator = new ValensFixedStarCalculator(List.of(aldebaran), new ValensFixedStarCalculator.SwissEphemerisFixedStarPositionResolver());
        Chart chart = new Chart();
        chart.setLots(List.of(lot("FORTUNE", expectedAldebaranLongitudeAtJ2000)));

        List<FixedStarEntry> entries = calculator.calculate(j2000Context(), chart);

        assertEquals(1, entries.size());
        FixedStarEntry entry = entries.get(0);
        assertEquals("Aldebaran", entry.star());
        assertEquals(expectedAldebaranLongitudeAtJ2000, entry.longitude(), 1.0e-9);
        assertEquals("LOT_FORTUNE", entry.conjoinedPoint());
        assertEquals(FixedStarTargetType.LOT, entry.conjoinedPointType());
        assertEquals(List.of(Planet.MARS), entry.nature());
    }

    @Test
    void brightStarsGetOneDegreeOrbOnlyForLuminariesAndAngles() {
        FixedStarDefinition brightStar = new FixedStarDefinition("Bright Star", ",bright", 1.0, List.of(Planet.JUPITER), "test-source");
        ValensFixedStarCalculator calculator = calculatorWithFixedLongitude(brightStar, 10.0);
        Chart chart = new Chart();
        chart.setAngles(List.of(new ChartAngle(AngleType.ASCENDANT, 11.0, ZodiacSign.ARIES, 11.0)));
        chart.setPlanets(List.of(planet(Planet.MERCURY, 11.0)));

        List<FixedStarEntry> entries = calculator.calculate(null, chart);

        assertEquals(1, entries.size());
        assertEquals("ASCENDANT", entries.get(0).conjoinedPoint());
        assertEquals(1.0, entries.get(0).orbDeg(), 1.0e-12);
        assertEquals(1.0, entries.get(0).maxOrbDeg(), 1.0e-12);
    }

    @Test
    void mediumAndFaintOrbBoundariesAreInclusive() {
        FixedStarDefinition mediumStar = new FixedStarDefinition("Medium Star", ",medium", 2.5, List.of(Planet.VENUS), "test-source");
        FixedStarDefinition faintStar = new FixedStarDefinition("Faint Star", ",faint", 2.51, List.of(Planet.SATURN), "test-source");
        ValensFixedStarCalculator calculator = new ValensFixedStarCalculator(List.of(mediumStar, faintStar), (ctx, star) -> star.name().equals("Medium Star") ? 20.0 : 30.0);
        Chart chart = new Chart();
        chart.setPlanets(List.of(planet(Planet.VENUS, 20.0 + 40.0 / 60.0)));
        chart.setLots(List.of(lot("SPIRIT", 30.5)));

        List<FixedStarEntry> entries = calculator.calculate(null, chart);

        assertEquals(2, entries.size());
        FixedStarEntry medium = entries.stream().filter(entry -> entry.star().equals("Medium Star")).findFirst().orElseThrow();
        assertEquals("VENUS", medium.conjoinedPoint());
        assertEquals(40.0 / 60.0, medium.orbDeg(), 1.0e-12);
        assertEquals(40.0 / 60.0, medium.maxOrbDeg(), 1.0e-12);

        FixedStarEntry faint = entries.stream().filter(entry -> entry.star().equals("Faint Star")).findFirst().orElseThrow();
        assertEquals("LOT_SPIRIT", faint.conjoinedPoint());
        assertEquals(0.5, faint.orbDeg(), 1.0e-12);
        assertEquals(0.5, faint.maxOrbDeg(), 1.0e-12);
    }

    @Test
    void returnsEmptyListWhenNoConjunctionFallsWithinOrb() {
        FixedStarDefinition star = new FixedStarDefinition("Unconfigured Star", ",none", 1.0, List.of(Planet.MARS), "test-source");
        ValensFixedStarCalculator calculator = calculatorWithFixedLongitude(star, 0.0);
        Chart chart = new Chart();
        chart.setAngles(List.of(new ChartAngle(AngleType.MIDHEAVEN, 2.0, ZodiacSign.ARIES, 2.0)));
        chart.setLots(List.of(lot("FORTUNE", 3.0)));

        List<FixedStarEntry> entries = calculator.calculate(null, chart);

        assertTrue(entries.isEmpty());
    }

    private ValensFixedStarCalculator calculatorWithFixedLongitude(FixedStarDefinition star, double longitude) {
        return new ValensFixedStarCalculator(List.of(star), (ctx, ignored) -> longitude);
    }

    private CalculationContext j2000Context() {
        Subject subject = new Subject("star-test", OffsetDateTime.parse("2000-01-01T12:00:00Z"), 0.0, 0.0);
        return new CalculationContext(subject, new CoreDoctrineInfo("valens", "Vettius Valens", HouseSystem.WHOLE_SIGN, Terms.EGYPTIAN, Triplicity.DOROTHEAN));
    }

    private PlanetPosition planet(Planet planet, double longitude) {
        return new PlanetPosition(planet, AstroMath.normalize(longitude), AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), 0.0, 0.0, 0.0, 0.0, true, 0.0, 1.0, 0.0, false, 1, 1, null, Angularity.ANGULAR, Planet.MARS, 0.0, 0.0, 0.0);
    }

    private LotEntry lot(String name, double longitude) {
        double normalized = AstroMath.normalize(longitude);
        return new LotEntry(name, "Lot of " + name, "test", normalized, AstroMath.signOf(normalized), AstroMath.degreeInSign(normalized), 1, Planet.MOON, "test formula", List.of());
    }
}
