package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.data.Angularity;
import app.chart.data.NodeType;
import app.chart.data.Planet;
import app.output.Logger;
import app.swisseph.core.SweConst;

public class PlanetCalculator implements Calculator {

    private record EquatorialPosition(double rightAscension, double declination) {
    }

    public void calculate(NatalChart natalChart, CalculationContext ctx) {
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

        PlanetPosition northNode = calculatePlanet(Planet.NORTH_NODE, nodeSwissPlanetId(ctx), julianDay, ascendant, sunLongitude, ctx);
        planets.add(northNode);
        double southNodeLongitude = ctx.normalize(northNode.getLongitude() + 180.0);
        int house = ctx.houseOf(southNodeLongitude, ascendant);
        int wholeSignHouse = ctx.wholeSignHouseOf(southNodeLongitude, ascendant);
        Integer quadrantHouse = ctx.quadrantHouseOf(southNodeLongitude);
        double southNodeLatitude = -northNode.getLatitude();
        double southNodeAltitude = ctx.horizontalAltitude(southNodeLongitude, southNodeLatitude);
        double southNodeMeanDailySpeed = meanDailySpeed(Planet.SOUTH_NODE);
        PlanetPosition southNodePlanet = new PlanetPosition(Planet.SOUTH_NODE, southNodeLongitude, ctx.signOf(southNodeLongitude), ctx.degreeInSign(southNodeLongitude), southNodeLatitude,
                ctx.normalize(northNode.getRightAscension() + 180.0), -northNode.getDeclination(), southNodeAltitude, southNodeAltitude >= 0.0, northNode.getSpeed(), southNodeMeanDailySpeed,
                Math.abs(northNode.getSpeed()) / southNodeMeanDailySpeed, northNode.getRetrograde(), house, wholeSignHouse, quadrantHouse, angularity(house), ctx.termRuler(southNodeLongitude, ctx.getTerms()),
                angularDistance(southNodeLongitude, sunLongitude, ctx), ctx.antiscia(southNodeLongitude), ctx.contraAntiscia(southNodeLongitude));
        planets.add(southNodePlanet);
        natalChart.setPlanets(planets);
    }

    private void addPlanet(List<PlanetPosition> planets, Planet planet, int swissPlanetId, double julianDay, double ascendant, double sunLongitude, CalculationContext ctx) {
        planets.add(calculatePlanet(planet, swissPlanetId, julianDay, ascendant, sunLongitude, ctx));
    }

    private PlanetPosition calculatePlanet(Planet planet, int swissPlanetId, double julianDay, double ascendant, double sunLongitude, CalculationContext ctx) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay, swissPlanetId, ctx.planetFlags(), values, error);
        ctx.requireSwissEphemerisResult(result, planet, "position", error);
        if (Double.isNaN(values[0])) {
            Logger.instance.error(ctx.getSubject().getId(), "Swiss Ephemeris returned invalid values for " + planet + ": " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        double longitude = ctx.normalize(values[0]);
        EquatorialPosition equatorial = equatorialPosition(planet, swissPlanetId, julianDay, ctx);
        int house = ctx.houseOf(longitude, ascendant);
        int wholeSignHouse = ctx.wholeSignHouseOf(longitude, ascendant);
        Integer quadrantHouse = ctx.quadrantHouseOf(longitude);
        double altitude = ctx.horizontalAltitude(longitude, values[1]);
        double meanDailySpeed = meanDailySpeed(planet);
        return new PlanetPosition(planet, longitude, ctx.signOf(longitude), ctx.degreeInSign(longitude), values[1], equatorial.rightAscension(), equatorial.declination(), altitude, altitude >= 0.0, values[3], meanDailySpeed,
                Math.abs(values[3]) / meanDailySpeed, values[3] < 0, house, wholeSignHouse, quadrantHouse, angularity(house), ctx.termRuler(longitude, ctx.getTerms()), angularDistance(longitude, sunLongitude, ctx), ctx.antiscia(longitude),
                ctx.contraAntiscia(longitude));
    }

    private EquatorialPosition equatorialPosition(Planet planet, int swissPlanetId, double julianDay, CalculationContext ctx) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = ctx.getSwissEph().swe_calc_ut(julianDay, swissPlanetId, ctx.planetFlags() | SweConst.SEFLG_EQUATORIAL, values, error);
        ctx.requireSwissEphemerisResult(result, planet, "equatorial position", error);
        if (Double.isNaN(values[0]) || Double.isNaN(values[1])) {
            Logger.instance.error(ctx.getSubject().getId(), "Swiss Ephemeris returned invalid equatorial values for " + planet + ": " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return new EquatorialPosition(ctx.normalize(values[0]), values[1]);
    }

    private int nodeSwissPlanetId(CalculationContext ctx) {
        return ctx.getNodeType() == NodeType.TRUE ? SweConst.SE_TRUE_NODE : SweConst.SE_MEAN_NODE;
    }

    private double meanDailySpeed(Planet planet) {
        return switch (planet) {
            case SUN -> 0.9856;
            case MOON -> 13.1764;
            case MERCURY -> 1.3833;
            case VENUS -> 1.2026;
            case MARS -> 0.5240;
            case JUPITER -> 0.0831;
            case SATURN -> 0.0335;
            case NORTH_NODE, SOUTH_NODE -> 0.05295;
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

    private double angularDistance(double longitude, double sunLongitude, CalculationContext ctx) {
        return ctx.rawAngularSeparation(longitude, sunLongitude);
    }
}
