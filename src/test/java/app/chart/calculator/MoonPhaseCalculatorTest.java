package app.chart.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import app.chart.ChartCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.MoonPhaseName;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.MoonPhase;
import app.chart.model.Chart;
import app.reading.CoreDoctrineInfo;
import app.testing.SyntheticTestData;

class MoonPhaseCalculatorTest {

    private static final CoreDoctrineInfo CONVENTIONS = new CoreDoctrineInfo(
            "moon-phase-test",
            "Moon phase test",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN);

    @Test
    void usesSwissEphemerisPhysicalIlluminatedFractionAtJ2000() {
        CalculationContext context = new CalculationContext(
                SyntheticTestData.subject(),
                CONVENTIONS);

        Chart chart = new ChartCalculator().calculate(context);
        MoonPhase moonPhase = chart.getMoonPhase();

        assertEquals(0.230087, moonPhase.getIlluminationFraction(), 1.0e-6);
        assertEquals(MoonPhaseName.LAST_QUARTER_TO_BALSAMIC, moonPhase.getPhase());
        assertFalse(moonPhase.isWaxing());
    }
}
