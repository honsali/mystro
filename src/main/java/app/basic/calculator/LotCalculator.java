package app.basic.calculator;

import java.util.List;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.ChartAngle;
import app.model.basic.LotPosition;
import app.model.basic.PlanetPosition;
import app.model.data.AngleType;
import app.model.data.LotType;
import app.model.data.Planet;

public class LotCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        PlanetPosition sun = ctx.planet(basicChart.getPlanets(), Planet.SUN);
        PlanetPosition moon = ctx.planet(basicChart.getPlanets(), Planet.MOON);
        ChartAngle ascendant = angle(basicChart.getAngles(), AngleType.ASCENDANT);
        if (sun == null || moon == null || ascendant == null) {
            return;
        }
        boolean diurnal = ctx.horizontalAltitude(sun.getLongitude(), sun.getLatitude()) >= 0.0;
        double fortune = diurnal ? ctx.normalize(ascendant.getLongitude() + moon.getLongitude() - sun.getLongitude()) : ctx.normalize(ascendant.getLongitude() + sun.getLongitude() - moon.getLongitude());
        double spirit = diurnal ? ctx.normalize(ascendant.getLongitude() + sun.getLongitude() - moon.getLongitude()) : ctx.normalize(ascendant.getLongitude() + moon.getLongitude() - sun.getLongitude());
        List<LotPosition> lots = List.of(lot(LotType.FORTUNE, fortune, ascendant.getLongitude(), ctx), lot(LotType.SPIRIT, spirit, ascendant.getLongitude(), ctx));
        basicChart.setLots(lots);
    }

    private LotPosition lot(LotType name, double longitude, double ascendant, BasicCalculationContext ctx) {
        return new LotPosition(name, ctx.round(longitude), ctx.signOf(longitude), ctx.round(ctx.degreeInSign(longitude)), ctx.houseOf(longitude, ascendant), ctx.round(ctx.antiscia(longitude)), ctx.round(ctx.contraAntiscia(longitude)));
    }


    private ChartAngle angle(List<ChartAngle> angles, AngleType name) {
        for (ChartAngle angle : angles) {
            if (angle.getName().equals(name)) {
                return angle;
            }
        }
        return null;
    }

}
