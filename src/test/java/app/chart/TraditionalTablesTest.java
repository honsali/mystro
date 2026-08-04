package app.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.chart.data.Planet;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.TriplicityRulers;

class TraditionalTablesTest {

    @Test
    void ptolemaicTriplicityRulersFollowTetrabiblosSectRulers() {
        assertEquals(new TriplicityRulers(Planet.SUN, Planet.JUPITER, null),
                TraditionalTables.triplicityRulers(ZodiacSign.ARIES, Triplicity.PTOLEMAIC));
        assertEquals(new TriplicityRulers(Planet.VENUS, Planet.MOON, null),
                TraditionalTables.triplicityRulers(ZodiacSign.TAURUS, Triplicity.PTOLEMAIC));
        assertEquals(new TriplicityRulers(Planet.SATURN, Planet.MERCURY, null),
                TraditionalTables.triplicityRulers(ZodiacSign.GEMINI, Triplicity.PTOLEMAIC));
        assertEquals(new TriplicityRulers(Planet.VENUS, Planet.MOON, Planet.MARS),
                TraditionalTables.triplicityRulers(ZodiacSign.PISCES, Triplicity.PTOLEMAIC));
    }

    @Test
    void dorotheanWaterTriplicityRemainsValensBackboneTable() {
        assertEquals(new TriplicityRulers(Planet.VENUS, Planet.MARS, Planet.MOON),
                TraditionalTables.triplicityRulers(ZodiacSign.PISCES, Triplicity.DOROTHEAN));
    }
}
