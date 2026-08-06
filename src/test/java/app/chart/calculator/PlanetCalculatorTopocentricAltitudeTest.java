package app.chart.calculator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import app.chart.ChartCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.ephemeris.SweConst;
import app.reading.CoreDoctrineInfo;

class PlanetCalculatorTopocentricAltitudeTest {

    private static final CoreDoctrineInfo CONVENTIONS = new CoreDoctrineInfo(
            "topocentric-altitude-test",
            "Topocentric altitude test",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN);

    @Test
    void usesTopocentricMoonPositionWhenParallaxMovesTheMoonBelowTheHorizon() {
        Subject subject = new Subject(
                "moon-near-greenwich-horizon",
                OffsetDateTime.parse("2000-01-01T13:10:00Z"),
                51.4769,
                0.0);
        CalculationContext context = new CalculationContext(subject, CONVENTIONS);

        Chart chart = new ChartCalculator().calculate(context);
        PlanetPosition moon = chart.requirePlanet(Planet.MOON);
        double geocentricAltitude = context.horizontalAltitude(
                moon.getLongitude(),
                moon.getLatitude());

        assertTrue(geocentricAltitude > 0.44 && geocentricAltitude < 0.47);
        assertTrue(moon.getAltitude() > -0.47 && moon.getAltitude() < -0.43);
        assertFalse(moon.getAboveHorizon());
    }

    @Test
    void appliesObserverElevationToTopocentricAltitude() {
        OffsetDateTime dateTime = OffsetDateTime.parse("2000-01-01T13:10:00Z");
        Subject seaLevelSubject = new Subject(
                "moon-at-sea-level",
                dateTime,
                51.4769,
                0.0,
                0.0);
        Subject highElevationSubject = new Subject(
                "moon-at-high-elevation",
                dateTime,
                51.4769,
                0.0,
                2_000.0);
        CalculationContext seaLevelContext = new CalculationContext(seaLevelSubject, CONVENTIONS);
        CalculationContext highElevationContext = new CalculationContext(highElevationSubject, CONVENTIONS);

        double seaLevelAltitude = seaLevelContext.topocentricHorizontalAltitude(
                Planet.MOON,
                SweConst.SE_MOON,
                seaLevelContext.getFullJulianDay());
        double highElevationAltitude = highElevationContext.topocentricHorizontalAltitude(
                Planet.MOON,
                SweConst.SE_MOON,
                highElevationContext.getFullJulianDay());

        assertTrue(Math.abs(highElevationAltitude - seaLevelAltitude) > 0.0001);
    }
}
