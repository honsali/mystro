package app.chart.calculator;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.data.MoonPhaseName;
import app.chart.data.Planet;
import app.chart.model.MoonPhase;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.ephemeris.SweConst;

public class MoonPhaseCalculator implements Calculator {

    public void calculate(Chart chart, CalculationContext ctx) {
        PlanetPosition sun = chart.requirePlanet(Planet.SUN);
        PlanetPosition moon = chart.requirePlanet(Planet.MOON);
        double directedElongation = AstroMath.normalize(moon.getLongitude() - sun.getLongitude());
        boolean waxing = directedElongation <= 180.0;
        double illumination = ctx.illuminatedFractionFor(
                Planet.MOON,
                SweConst.SE_MOON,
                ctx.getFullJulianDay());
        MoonPhase moonPhase = new MoonPhase(illumination, moonPhaseName(directedElongation), waxing);
        chart.setMoonPhase(moonPhase);
    }

    private MoonPhaseName moonPhaseName(double directedElongation) {
        if (directedElongation < 45.0)
            return MoonPhaseName.NEW_TO_CRESCENT;
        if (directedElongation < 90.0)
            return MoonPhaseName.CRESCENT_TO_FIRST_QUARTER;
        if (directedElongation < 135.0)
            return MoonPhaseName.FIRST_QUARTER_TO_GIBBOUS;
        if (directedElongation <= 180.0)
            return MoonPhaseName.GIBBOUS_TO_FULL;
        if (directedElongation < 225.0)
            return MoonPhaseName.FULL_TO_DISSEMINATING;
        if (directedElongation < 270.0)
            return MoonPhaseName.DISSEMINATING_TO_LAST_QUARTER;
        if (directedElongation < 315.0)
            return MoonPhaseName.LAST_QUARTER_TO_BALSAMIC;
        return MoonPhaseName.BALSAMIC_TO_NEW;
    }
}
