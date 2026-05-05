package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.TraditionalTables;
import app.chart.model.AnglePointEntry;
import app.chart.model.NatalChart;
import app.chart.model.ChartAngle;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.chart.model.TriplicityRulers;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Sect;
import app.chart.data.ZodiacSign;

public class PointCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        for (PlanetPosition planet : natalChart.getPlanets()) {
            points.put(PointKey.of(planet.getPlanet()), planetEntry(planet, natalChart, ctx));
        }
        for (ChartAngle angle : natalChart.getAngles()) {
            points.put(PointKey.of(angle.getName()), new AnglePointEntry(
                    angle.getLongitude(),
                    angle.getSign(),
                    angle.getDegreeInSign()
            ));
        }
        natalChart.setPoints(points);
    }

    private PointEntry planetEntry(PlanetPosition planet, NatalChart natalChart, CalculationContext ctx) {
        Planet domicileRuler = null;
        Planet exaltationRuler = null;
        Planet triplicityRuler = null;
        Planet participatingTriplicityRuler = null;
        Planet termRuler = null;
        Planet faceRuler = null;
        Planet detrimentRuler = null;
        Planet fallRuler = null;
        if (isTraditionalPlanet(planet.getPlanet())) {
            domicileRuler = domicileRuler(planet.getSign());
            exaltationRuler = exaltationRuler(planet.getSign());
            TriplicityRulers triplicityRulers = triplicityRulers(planet.getSign(), ctx);
            boolean diurnal = natalChart.getSect().getSect() == Sect.DIURNAL;
            triplicityRuler = diurnal ? triplicityRulers.day() : triplicityRulers.night();
            participatingTriplicityRuler = triplicityRulers.participating();
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
                triplicityRuler,
                participatingTriplicityRuler,
                termRuler,
                faceRuler,
                detrimentRuler,
                fallRuler,
                List.of(),
                List.of(),
                null,
                null,
                null
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

    private TriplicityRulers triplicityRulers(ZodiacSign sign, CalculationContext ctx) {
        return TraditionalTables.triplicityRulers(sign, ctx.getTriplicity());
    }
}
