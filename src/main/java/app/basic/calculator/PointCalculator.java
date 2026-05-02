package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.basic.TraditionalTables;
import app.model.basic.AnglePointEntry;
import app.model.basic.BasicChart;
import app.model.basic.ChartAngle;
import app.model.basic.PlanetPointEntry;
import app.model.basic.PlanetPosition;
import app.model.basic.PointEntry;
import app.model.basic.TriplicityRulers;
import app.model.data.Planet;
import app.model.data.PointKey;
import app.model.data.ZodiacSign;

public class PointCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        for (PlanetPosition planet : basicChart.getPlanets()) {
            points.put(PointKey.of(planet.getPlanet()), planetEntry(planet, ctx));
        }
        for (ChartAngle angle : basicChart.getAngles()) {
            points.put(PointKey.of(angle.getName()), new AnglePointEntry(
                    angle.getLongitude(),
                    angle.getSign(),
                    angle.getDegreeInSign()
            ));
        }
        basicChart.setPoints(points);
    }

    private PointEntry planetEntry(PlanetPosition planet, BasicCalculationContext ctx) {
        Planet domicileRuler = null;
        Planet exaltationRuler = null;
        TriplicityRulers triplicityRulers = null;
        Planet termRuler = null;
        Planet faceRuler = null;
        Planet detrimentRuler = null;
        Planet fallRuler = null;
        if (isTraditionalPlanet(planet.getPlanet())) {
            domicileRuler = domicileRuler(planet.getSign());
            exaltationRuler = exaltationRuler(planet.getSign());
            triplicityRulers = triplicityRulers(planet.getSign(), ctx);
            termRuler = planet.getTermRuler();
            faceRuler = faceRuler(planet.getSign(), planet.getDegreeInSign());
            detrimentRuler = domicileRuler(opposite(planet.getSign()));
            fallRuler = exaltationRuler(opposite(planet.getSign()));
        }
        return new PlanetPointEntry(
                planet.getLongitude(),
                planet.getSign(),
                planet.getDegreeInSign(),
                planet.getLatitude(),
                planet.getRightAscension(),
                planet.getDeclination(),
                planet.getAltitude(),
                planet.getAboveHorizon(),
                planet.getSpeed(),
                planet.getMeanDailySpeed(),
                planet.getSpeedRatio(),
                planet.getRetrograde(),
                planet.getHouse(),
                planet.getWholeSignHouse(),
                planet.getQuadrantHouse(),
                planet.getAngularity(),
                planet.getAntisciaLongitude(),
                planet.getContraAntisciaLongitude(),
                domicileRuler,
                exaltationRuler,
                triplicityRulers,
                termRuler,
                faceRuler,
                detrimentRuler,
                fallRuler
        );
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

    private ZodiacSign opposite(ZodiacSign sign) {
        return TraditionalTables.opposite(sign);
    }

    private Planet faceRuler(ZodiacSign sign, double degreeInSign) {
        return TraditionalTables.faceRuler(sign, degreeInSign);
    }

    private TriplicityRulers triplicityRulers(ZodiacSign sign, BasicCalculationContext ctx) {
        return TraditionalTables.triplicityRulers(sign, ctx.getInput().getDoctrine().getTriplicity());
    }
}
