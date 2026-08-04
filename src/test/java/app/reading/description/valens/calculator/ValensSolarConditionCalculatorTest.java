package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.SolarConditionEntry;

class ValensSolarConditionCalculatorTest {
    private final ValensSolarConditionCalculator calculator = new ValensSolarConditionCalculator();

    @Test
    void distinguishesCazimiCombustUnderBeamsAndFreeOfSun() {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(
                position(Planet.SUN, 0.0),
                position(Planet.MERCURY, 0.2),
                position(Planet.VENUS, 4.0),
                position(Planet.MARS, 12.0),
                position(Planet.JUPITER, 20.0)
        ));

        Map<Planet, SolarConditionEntry> conditions = calculator.calculate(chart);

        assertFalse(conditions.containsKey(Planet.SUN));
        assertEquals(SolarCondition.CAZIMI, conditions.get(Planet.MERCURY).condition());
        assertEquals(SolarCondition.COMBUST, conditions.get(Planet.VENUS).condition());
        assertEquals(SolarCondition.UNDER_BEAMS, conditions.get(Planet.MARS).condition());
        assertEquals(SolarCondition.FREE_OF_SUN, conditions.get(Planet.JUPITER).condition());
    }

    private PlanetPosition position(Planet planet, double angularDistanceFromSun) {
        return new PlanetPosition(planet, 0.0, ZodiacSign.ARIES, 0.0, 0.0, 0.0, 0.0, 0.0, false,
                1.0, 1.0, 1.0, false, 1, 1, 1, Angularity.ANGULAR, null, angularDistanceFromSun, 0.0, 0.0);
    }
}
