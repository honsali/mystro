package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.PlanetPosition;
import app.model.data.Angularity;
import app.model.data.Planet;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class PlanetCalculator implements Calculator {


    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        double julianDay = ctx.getFullJulianDay();
        double ascendant = ctx.normalize(ctx.getAscmc()[0]);
        double sunLongitude = ctx.longitudeFor(Planet.SUN, SweConst.SE_SUN, julianDay);

        List<PlanetPosition> planets = new ArrayList<>();
        addPlanet(planets, Planet.SUN, SweConst.SE_SUN, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.MOON, SweConst.SE_MOON, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.MERCURY, SweConst.SE_MERCURY, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.VENUS, SweConst.SE_VENUS, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.MARS, SweConst.SE_MARS, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.JUPITER, SweConst.SE_JUPITER, julianDay, ascendant, sunLongitude, ctx);
        addPlanet(planets, Planet.SATURN, SweConst.SE_SATURN, julianDay, ascendant, sunLongitude, ctx);

        PlanetPosition northNode = calculatePlanet(Planet.NORTH_NODE, SweConst.SE_MEAN_NODE, julianDay, ascendant, sunLongitude, ctx);
        if (northNode != null) {
            planets.add(northNode);
            double southNodeLongitude = ctx.normalize(northNode.getLongitude() + 180.0);
            PlanetPosition southNodePlanet = new PlanetPosition(Planet.SOUTH_NODE, ctx.round(southNodeLongitude), ctx.signOf(southNodeLongitude), ctx.round(ctx.degreeInSign(southNodeLongitude)), ctx.round(-northNode.getLatitude()), ctx.round(-northNode.getDeclination()),
                    ctx.round(northNode.getSpeed()), ctx.round(meanDailySpeed(Planet.SOUTH_NODE)), ctx.round(northNode.getSpeed() / meanDailySpeed(Planet.SOUTH_NODE)), northNode.getRetrograde(), ctx.houseOf(southNodeLongitude, ascendant), angularity(ctx.houseOf(southNodeLongitude, ascendant)),
                    ctx.termRuler(southNodeLongitude, ctx.getTerms()), ctx.round(angularDistance(southNodeLongitude, sunLongitude, ctx)), ctx.round(ctx.antiscia(southNodeLongitude)), ctx.round(ctx.contraAntiscia(southNodeLongitude)));
            planets.add(southNodePlanet);
        }
        basicChart.setPlanets(planets);
    }

    private void addPlanet(List<PlanetPosition> planets, Planet planet, int swissPlanetId, double julianDay, double ascendant, double sunLongitude, BasicCalculationContext ctx) {
        PlanetPosition position = calculatePlanet(planet, swissPlanetId, julianDay, ascendant, sunLongitude, ctx);
        if (position != null) {
            planets.add(position);
        }
    }

    private PlanetPosition calculatePlanet(Planet planet, int swissPlanetId, double julianDay, double ascendant, double sunLongitude, BasicCalculationContext ctx) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay, swissPlanetId, ctx.planetFlags(), values, error);
        if (result < 0 || Double.isNaN(values[0])) {
            Logger.instance.error(ctx.getInput(), "Swiss Ephemeris failed for " + planet + ": " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        double longitude = ctx.normalize(values[0]);
        return new PlanetPosition(planet, ctx.round(longitude), ctx.signOf(longitude), ctx.round(ctx.degreeInSign(longitude)), ctx.round(values[1]), ctx.round(declination(swissPlanetId, julianDay, ctx)), ctx.round(values[3]), ctx.round(meanDailySpeed(planet)),
                ctx.round(values[3] / meanDailySpeed(planet)), values[3] < 0, ctx.houseOf(longitude, ascendant), angularity(ctx.houseOf(longitude, ascendant)), ctx.termRuler(longitude, ctx.getTerms()), ctx.round(angularDistance(longitude, sunLongitude, ctx)), ctx.round(ctx.antiscia(longitude)),
                ctx.round(ctx.contraAntiscia(longitude)));
    }

    private double declination(int swissPlanetId, double julianDay, BasicCalculationContext ctx) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay, swissPlanetId, ctx.planetFlags() | SweConst.SEFLG_EQUATORIAL, values, error);
        if (result < 0 || Double.isNaN(values[1])) {
            Logger.instance.error(ctx.getInput(), "Swiss Ephemeris failed to calculate declination: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return values[1];
    }


    private double meanDailySpeed(Planet planet) {
        return switch (planet) {
            case SUN -> 0.9856;
            case MOON -> 13.1764;
            case MERCURY -> 1.3833;
            case VENUS -> 1.2;
            case MARS -> 0.5240;
            case JUPITER -> 0.0831;
            case SATURN -> 0.0335;
            case NORTH_NODE, SOUTH_NODE -> -0.05295;
        };
    }


    private Angularity angularity(int house) {
        return switch (house) {
            case 1, 4, 7, 10 -> Angularity.ANGULAR;
            case 2, 5, 8, 11 -> Angularity.SUCCEDENT;
            case 3, 6, 9, 12 -> Angularity.CADENT;
            default -> Angularity.UNKNOWN;
        };
    }

    private double angularDistance(double longitude, Double sunLongitude, BasicCalculationContext ctx) {
        if (sunLongitude == null) {
            return 0.0;
        }
        return ctx.rawAngularSeparation(longitude, sunLongitude);
    }
}
