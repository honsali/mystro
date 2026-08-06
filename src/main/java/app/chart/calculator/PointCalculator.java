package app.chart.calculator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.TraditionalTables;
import app.chart.data.Element;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.chart.model.TriplicityRulers;

public class PointCalculator implements Calculator {

    public void calculate(Chart chart, CalculationContext ctx) {
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        for (PlanetPosition planet : chart.getPlanets()) {
            points.put(PointKey.of(planet.getPlanet()), planetEntry(planet, chart, ctx));
        }
        for (ChartAngle angle : chart.getAngles()) {
            points.put(PointKey.of(angle.getName()), new AnglePointEntry(angle.getLongitude(), angle.getSign(), angle.getDegreeInSign()));
        }
        chart.setPoints(points);
    }

    private PointEntry planetEntry(PlanetPosition planet, Chart chart, CalculationContext ctx) {
        Planet domicileRuler = null;
        Planet exaltationRuler = null;
        Planet activeMasterTriplicityRuler = null;
        Planet participatingTriplicityRuler = null;
        Planet inactiveMasterTriplicityRuler = null;
        Planet termRuler = null;
        Planet faceRuler = null;
        Planet detrimentPlanet = null;
        Planet fallPlanet = null;
        boolean joy = false;
        if (isTraditionalPlanet(planet.getPlanet())) {
            domicileRuler = domicileRuler(planet.getSign());
            exaltationRuler = exaltationRuler(planet.getSign());
            TriplicityRulers triplicityRulers = triplicityRulers(planet.getSign(), ctx);
            boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;
            activeMasterTriplicityRuler = diurnal ? triplicityRulers.day() : triplicityRulers.night();
            participatingTriplicityRuler = triplicityRulers.participating();
            inactiveMasterTriplicityRuler = diurnal ? triplicityRulers.night() : triplicityRulers.day();
            termRuler = planet.getTermRuler();
            faceRuler = faceRuler(planet.getSign(), planet.getDegreeInSign());
            detrimentPlanet = domicileRuler(opposite(planet.getSign()));
            fallPlanet = exaltationRuler(opposite(planet.getSign()));
            if (planet.getHouse() == TraditionalTables.planetJoyHouse(planet.getPlanet())) {
                joy = true;
            }
        }
        return new PlanetPointEntry(planet.getLongitude(), planet.getSign(), element(planet.getSign()), planet.getDegreeInSign(), planet.getLatitude(), planet.getRightAscension(), planet.getDeclination(), planet.getAltitude(), planet.getAboveHorizon(), planet.getSpeed(), planet.getMeanDailySpeed(),
                planet.getSpeedRatio(), planet.getRetrograde(), planet.getHouse(), planet.getWholeSignHouse(), planet.getQuadrantHouse(), planet.getAngularity(), planet.getAntisciaLongitude(), planet.getContraAntisciaLongitude(), domicileRuler, exaltationRuler, activeMasterTriplicityRuler,
                participatingTriplicityRuler, inactiveMasterTriplicityRuler, termRuler, faceRuler, detrimentPlanet, fallPlanet, List.of(), List.of(), null, null, null, List.of(), joy, pointType(planet.getPlanet()));
    }

    private PointType pointType(Planet planet) {
        return switch (planet) {
            case NORTH_NODE, SOUTH_NODE -> PointType.NODE;
            case SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN -> PointType.PLANET;
        };
    }

    private boolean isTraditionalPlanet(Planet planet) {
        return TraditionalTables.isTraditionalPlanet(planet);
    }

    private Planet domicileRuler(ZodiacSign sign) {
        return TraditionalTables.domicileRuler(sign);
    }

    private Planet exaltationRuler(ZodiacSign sign) {
        return TraditionalTables.exaltationRuler(sign);
    }

    private Element element(ZodiacSign sign) {
        return TraditionalTables.element(sign);
    }

    private ZodiacSign opposite(ZodiacSign sign) {
        return TraditionalTables.opposite(sign);
    }

    private Planet faceRuler(ZodiacSign sign, double degreeInSign) {
        return TraditionalTables.faceRuler(sign, degreeInSign);
    }

    private TriplicityRulers triplicityRulers(ZodiacSign sign, CalculationContext ctx) {
        return TraditionalTables.triplicityRulers(sign, ctx.getTriplicity());
    }
}
