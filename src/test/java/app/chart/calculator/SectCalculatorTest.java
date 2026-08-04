package app.chart.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
import org.junit.jupiter.api.Test;
import app.chart.calculator.SectCalculator;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;

class SectCalculatorTest {

    @Test
    void exactMercurySolarPhaseDoesNotAssignMercurySect() {
        NatalChart chart = chart(10.0, 10.0, true);

        new SectCalculator().calculate(chart, null);

        PlanetSectInfo mercurySect = chart.getSect().getPlanetSects().get(Planet.MERCURY);
        assertNull(mercurySect.getSect());
        assertNull(mercurySect.getCondition());
        assertEquals(SolarOrientation.EXACT, mercurySect.getPhaseRelativeToSun());
    }

    @Test
    void nonBoundaryMercurySolarPhaseStillAssignsMercurySect() {
        NatalChart oriental = chart(10.0, 350.0, true);
        NatalChart occidental = chart(10.0, 30.0, true);

        SectCalculator calculator = new SectCalculator();
        calculator.calculate(oriental, null);
        calculator.calculate(occidental, null);

        PlanetSectInfo orientalMercury = oriental.getSect().getPlanetSects().get(Planet.MERCURY);
        assertEquals(Sect.DIURNAL, orientalMercury.getSect());
        assertEquals(SectCondition.OF_SECT, orientalMercury.getCondition());
        assertEquals(SolarOrientation.ORIENTAL, orientalMercury.getPhaseRelativeToSun());

        PlanetSectInfo occidentalMercury = occidental.getSect().getPlanetSects().get(Planet.MERCURY);
        assertEquals(Sect.NOCTURNAL, occidentalMercury.getSect());
        assertEquals(SectCondition.CONTRARY_TO_SECT, occidentalMercury.getCondition());
        assertEquals(SolarOrientation.OCCIDENTAL, occidentalMercury.getPhaseRelativeToSun());
    }

    private NatalChart chart(double sunLongitude, double mercuryLongitude, boolean sunAboveHorizon) {
        NatalChart chart = new NatalChart();
        chart.setPlanets(List.of(position(Planet.SUN, sunLongitude, sunAboveHorizon ? 1.0 : -1.0), position(Planet.MOON, 100.0, sunAboveHorizon ? -1.0 : 1.0), position(Planet.MERCURY, mercuryLongitude, 1.0)));
        return chart;
    }

    private PlanetPosition position(Planet planet, double longitude, double altitude) {
        return new PlanetPosition(planet, longitude, ZodiacSign.ARIES, longitude % 30.0, 0.0, 0.0, 0.0, altitude, altitude >= 0.0, 1.0, 1.0, 1.0, false, 1, 1, 1, Angularity.ANGULAR, null, 0.0, 0.0, 0.0);
    }
}
