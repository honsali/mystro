package app.reading.lifearc.solarreturn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import app.chart.AstroMath;
import app.chart.ChartCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.Chart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;

class SolarReturnCalculatorTest {
    private static final CoreDoctrineInfo CORE = new CoreDoctrineInfo(
            "valens",
            "Vettius Valens",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    private final SolarReturnCalculator calculator = new SolarReturnCalculator();

    @Test
    void calculateTableProducesExactSolarReturnsForRequestedAgeRange() {
        Subject subject = subject();
        Chart natalChart = new ChartCalculator().calculate(new CalculationContext(subject, CORE));

        SolarReturnTable table = calculator.calculateTable(subject, natalChart, 0, 2);

        assertEquals(SolarReturnCalculator.METHOD_ID, table.methodId());
        assertEquals(0, table.ageStartYears());
        assertEquals(2, table.ageEndYearsInclusive());
        assertEquals(3, table.rows().size());
        assertEquals(subject.getResolvedUtcInstant(), table.rows().get(0).returnDateTime().toInstant());
        assertEquals(table.rows().get(1).returnDateTime(), table.rows().get(0).periodEndDateTimeExclusive());
        assertEquals(table.rows().get(2).returnDateTime(), table.rows().get(1).periodEndDateTimeExclusive());

        double natalSunLongitude = natalChart.requirePlanet(Planet.SUN).getLongitude();
        for (SolarReturnEntry row : table.rows()) {
            assertTrue(AstroMath.rawAngularSeparation(natalSunLongitude, row.sunLongitude()) < 0.0001,
                    "Solar return age " + row.ageYears() + " should return to natal Sun longitude");
            assertEquals(PointKey.SUN, row.points().get(0).point());
        }
    }

    @Test
    void calculateTableRejectsInvalidRange() {
        Subject subject = subject();
        Chart natalChart = new ChartCalculator().calculate(new CalculationContext(subject, CORE));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, natalChart, 3, 2));
    }

    @Test
    void exactReturnInstantsAreAlwaysEmittedInUtc() {
        Subject subject = new Subject(
                "offset-solar-return",
                OffsetDateTime.parse("2000-01-01T14:00:00+02:00"),
                51.4769,
                0.0);
        Chart natalChart = new ChartCalculator().calculate(new CalculationContext(subject, CORE));

        SolarReturnTable table = calculator.calculateTable(subject, natalChart, 0, 1);

        assertEquals(OffsetDateTime.parse("2000-01-01T12:00:00Z"), table.rows().get(0).returnDateTime());
        assertTrue(table.rows().stream().allMatch(row ->
                row.returnDateTime().getOffset().equals(ZoneOffset.UTC)
                        && row.periodEndDateTimeExclusive().getOffset().equals(ZoneOffset.UTC)));
    }

    private Subject subject() {
        return app.testing.SyntheticTestData.subject();
    }
}
