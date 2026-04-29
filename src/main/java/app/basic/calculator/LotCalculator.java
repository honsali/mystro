package app.basic.calculator;

import java.util.List;
import app.basic.BaseCalculator;
import app.model.basic.ChartAngle;
import app.model.basic.LotPosition;
import app.model.basic.PlanetPosition;
import app.model.data.Planet;

public class LotCalculator extends BaseCalculator {

    protected void executeCalculation() {
        PlanetPosition sun = ctx.planet(basicChart.getPlanets(), Planet.SUN);
        PlanetPosition moon = ctx.planet(basicChart.getPlanets(), Planet.MOON);
        ChartAngle ascendant = angle(basicChart.getAngles(), "ASCENDANT");
        if (sun == null || moon == null || ascendant == null) {
            return;
        }
        boolean diurnal = sun.getHouse() >= 7 && sun.getHouse() <= 12;
        double fortune = diurnal ? ctx.normalize(ascendant.getLongitude() + moon.getLongitude() - sun.getLongitude()) : ctx.normalize(ascendant.getLongitude() + sun.getLongitude() - moon.getLongitude());
        double spirit = diurnal ? ctx.normalize(ascendant.getLongitude() + sun.getLongitude() - moon.getLongitude()) : ctx.normalize(ascendant.getLongitude() + moon.getLongitude() - sun.getLongitude());
        List<LotPosition> lots = List.of(lot("FORTUNE", fortune, ascendant.getLongitude()), lot("SPIRIT", spirit, ascendant.getLongitude()));
        basicChart.setLots(lots);
    }

    private LotPosition lot(String name, double longitude, double ascendant) {
        return new LotPosition(name, ctx.round(longitude), ctx.signOf(longitude), ctx.round(ctx.degreeInSign(longitude)), ctx.houseOf(longitude, ascendant), ctx.round(ctx.antiscia(longitude)), ctx.round(ctx.contraAntiscia(longitude)));
    }


    private ChartAngle angle(List<ChartAngle> angles, String name) {
        for (ChartAngle angle : angles) {
            if (angle.getName().equals(name)) {
                return angle;
            }
        }
        return null;
    }

}
