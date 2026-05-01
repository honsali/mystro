package app.basic.calculator;

import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.MoonPhase;
import app.model.basic.PlanetPosition;
import app.model.data.MoonPhaseName;
import app.model.data.Planet;

public class MoonPhaseCalculator implements Calculator {


    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        PlanetPosition sun = ctx.planet(basicChart.getPlanets(), Planet.SUN);
        PlanetPosition moon = ctx.planet(basicChart.getPlanets(), Planet.MOON);
        if (sun == null || moon == null) {
            return;
        }
        double elongation = ctx.rawAngularSeparation(moon.getLongitude(), sun.getLongitude());
        double directedElongation = ctx.normalize(moon.getLongitude() - sun.getLongitude());
        boolean waxing = directedElongation <= 180.0;
        double illumination = (1.0 - Math.cos(Math.toRadians(elongation))) / 2.0;
        MoonPhase moonPhase = new MoonPhase(ctx.round(illumination), moonPhaseName(directedElongation), waxing);
        basicChart.setMoonPhase(moonPhase);
    }



    private MoonPhaseName moonPhaseName(double directedElongation) {
        double epsilon = 1e-9;
        if (directedElongation < 45.0 || directedElongation >= 360.0 - epsilon)
            return MoonPhaseName.NEW_TO_CRESCENT;
        if (directedElongation < 90.0)
            return MoonPhaseName.CRESCENT_TO_FIRST_QUARTER;
        if (directedElongation < 135.0)
            return MoonPhaseName.FIRST_QUARTER_TO_GIBBOUS;
        if (directedElongation <= 180.0 + epsilon)
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
