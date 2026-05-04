package app.basic.calculator;

import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.basic.model.MoonPhase;
import app.basic.model.PlanetPosition;
import app.basic.data.MoonPhaseName;
import app.basic.data.Planet;

public class MoonPhaseCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        PlanetPosition sun = natalChart.requirePlanet(Planet.SUN);
        PlanetPosition moon = natalChart.requirePlanet(Planet.MOON);
        double elongation = ctx.rawAngularSeparation(moon.getLongitude(), sun.getLongitude());
        double directedElongation = ctx.normalize(moon.getLongitude() - sun.getLongitude());
        boolean waxing = directedElongation <= 180.0;
        double illumination = (1.0 - Math.cos(Math.toRadians(elongation))) / 2.0;
        MoonPhase moonPhase = new MoonPhase(illumination, moonPhaseName(directedElongation), waxing);
        natalChart.setMoonPhase(moonPhase);
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
