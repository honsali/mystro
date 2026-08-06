package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import app.chart.CalculationContext;
import app.chart.data.AngleType;
import app.chart.data.Angularity;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.BasicSect;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.calculator.LotCalculatorHouseFixture;
import app.reading.description.common.model.LotEntry;

class ValensLotCalculatorTest {
    @Test
    void valensDiurnalReturnsValensAndSupplementalLotsWithExpectedNamesAndFormulaLogic() {
        Chart chart = chart(100.0, 10.0, 40.0, Sect.DIURNAL);
        CalculationContext ctx = LotCalculatorHouseFixture.ctx(new CoreDoctrineInfo("valens", "Vettius Valens", HouseSystem.WHOLE_SIGN, Terms.EGYPTIAN, Triplicity.DOROTHEAN));
        List<LotEntry> lots = new ValensLotCalculator().calculate(ctx, chart);
        assertEquals(List.of("FORTUNE", "SPIRIT", "EROS", "NECESSITY", "BASIS", "COURAGE", "VICTORY", "NEMESIS", "WEDDING", "CHILDREN", "FATHER", "MOTHER", "SIBLINGS"), lots.stream().map(LotEntry::name).toList());
        assertEquals(130.0, lots.get(0).longitude());
        assertEquals(70.0, lots.get(1).longitude());
        assertEquals(40.0, lots.get(2).longitude());
        assertEquals(160.0, lots.get(3).longitude());
        assertEquals(160.0, lots.get(4).longitude());
        assertEquals(70.0, lots.get(5).longitude());
        assertEquals(220.0, lots.get(6).longitude());
        assertEquals(340.0, lots.get(7).longitude());
        assertEquals(260.0, lots.get(8).longitude());
        assertEquals(160.0, lots.get(9).longitude());
        assertEquals(340.0, lots.get(10).longitude());
        assertEquals(110.0, lots.get(11).longitude());
        assertEquals(40.0, lots.get(12).longitude());
        assertEquals("Asc + (Fortune -> Spirit)", lots.get(2).formula());
        assertEquals("Asc + (Spirit -> Fortune)", lots.get(3).formula());
        assertEquals("Asc + shorter Fortune/Spirit arc (Spirit -> Fortune)", lots.get(4).formula());
        assertEquals("hermetic", lots.get(5).doctrine());
        assertEquals("dorothean", lots.get(8).doctrine());
    }

    @Test
    void valensNocturnalReversesFortuneSpiritAndDependentLots() {
        Chart chart = chart(100.0, 10.0, 40.0, Sect.NOCTURNAL);
        Chart dayChart = chart(100.0, 10.0, 40.0, Sect.DIURNAL);
        CalculationContext ctx = LotCalculatorHouseFixture.ctx(new CoreDoctrineInfo("valens", "Vettius Valens", HouseSystem.WHOLE_SIGN, Terms.EGYPTIAN, Triplicity.DOROTHEAN));
        ValensLotCalculator calculator = new ValensLotCalculator();
        List<LotEntry> lots = calculator.calculate(ctx, chart);
        List<LotEntry> dayLots = calculator.calculate(ctx, dayChart);
        assertEquals(70.0, lots.get(0).longitude());
        assertEquals(130.0, lots.get(1).longitude());
        assertEquals(40.0, lots.get(2).longitude());
        assertEquals(160.0, lots.get(3).longitude());
        assertEquals(160.0, lots.get(4).longitude());
        assertEquals(190.0, lots.get(5).longitude());
        assertEquals(40.0, lots.get(6).longitude());
        assertEquals(280.0, lots.get(7).longitude());
        assertEquals(300.0, lots.get(8).longitude());
        assertEquals(40.0, lots.get(9).longitude());
        assertEquals(220.0, lots.get(10).longitude());
        assertEquals(90.0, lots.get(11).longitude());
        assertEquals(160.0, lots.get(12).longitude());
        assertNotEquals(dayLots.get(8).longitude(), lots.get(8).longitude());
        assertNotEquals(dayLots.get(9).longitude(), lots.get(9).longitude());
        assertNotEquals(dayLots.get(10).longitude(), lots.get(10).longitude());
        assertNotEquals(dayLots.get(11).longitude(), lots.get(11).longitude());
        assertNotEquals(dayLots.get(12).longitude(), lots.get(12).longitude());
        assertEquals("Asc + (Venus -> Saturn)", lots.get(8).formula());
        assertEquals("Asc + (Saturn -> Jupiter)", lots.get(9).formula());
        assertEquals("Asc + (Jupiter -> Saturn)", lots.get(12).formula());
    }

    private Chart chart(double asc, double sun, double moon, Sect sectValue) {
        Chart chart = new Chart();
        chart.setAngles(List.of(new ChartAngle(AngleType.ASCENDANT, asc, ZodiacSign.CANCER, asc % 30.0)));
        chart.setPlanets(List.of(position(Planet.SUN, sun), position(Planet.MOON, moon), position(Planet.MERCURY, 20.0), position(Planet.VENUS, 50.0), position(Planet.MARS, 160.0), position(Planet.JUPITER, 190.0), position(Planet.SATURN, 250.0)));
        chart.setSect(new BasicSect(sectValue, Planet.SUN, Planet.MOON, Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.MARS, sectValue == Sect.DIURNAL, sectValue != Sect.DIURNAL, 1.0, -1.0, java.util.Map.of()));
        return chart;
    }

    private PlanetPosition position(Planet planet, double longitude) {
        return new PlanetPosition(planet, longitude, ZodiacSign.ARIES, longitude % 30.0, 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, false, 1, 1, 1, Angularity.ANGULAR, null, 0.0, 0.0, 0.0);
    }
}
