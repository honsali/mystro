package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.basic.TraditionalTables;
import app.model.basic.AnglePointEntry;
import app.model.basic.BasicChart;
import app.model.basic.BasicSyzygy;
import app.model.basic.ChartAngle;
import app.model.basic.LotPointEntry;
import app.model.basic.LotPosition;
import app.model.basic.PlanetPointEntry;
import app.model.basic.PlanetPosition;
import app.model.basic.PointEntry;
import app.model.basic.SyzygyPointEntry;
import app.model.basic.TriplicityRulers;
import app.model.data.Planet;
import app.model.data.PointKey;
import app.model.data.ZodiacSign;

public class PointCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        for (PlanetPosition planet : basicChart.getPlanets()) {
            points.put(PointKey.of(planet.getPlanet()), planetEntry(planet));
        }
        for (ChartAngle angle : basicChart.getAngles()) {
            points.put(PointKey.of(angle.getName()), new AnglePointEntry(
                    angle.getLongitude(),
                    angle.getSign(),
                    angle.getDegreeInSign()
            ));
        }
        for (LotPosition lot : basicChart.getLots()) {
            points.put(PointKey.of(lot.getName()), new LotPointEntry(
                    lot.getLongitude(),
                    lot.getSign(),
                    lot.getDegreeInSign(),
                    lot.getHouse(),
                    lot.getAntisciaLongitude(),
                    lot.getContraAntisciaLongitude()
            ));
        }
        BasicSyzygy syzygy = basicChart.getSyzygy();
        if (syzygy != null) {
            points.put(PointKey.PRENATAL_SYZYGY, new SyzygyPointEntry(
                    syzygy.getLongitude(),
                    syzygy.getSign(),
                    syzygy.getDegreeInSign(),
                    syzygy.getHouse(),
                    syzygy.getType(),
                    syzygy.getJulianDay(),
                    syzygy.getApproximateUtcInstant(),
                    syzygy.getSunLongitude(),
                    syzygy.getMoonLongitude(),
                    syzygy.getAngularSeparation(),
                    syzygy.getSunSign(),
                    syzygy.getMoonSign()
            ));
        }
        basicChart.setPoints(points);
    }

    private PointEntry planetEntry(PlanetPosition planet) {
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
            triplicityRulers = triplicityRulers(planet.getSign());
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
                planet.getDeclination(),
                planet.getSpeed(),
                planet.getMeanDailySpeed(),
                planet.getSpeedRatio(),
                planet.getRetrograde(),
                planet.getHouse(),
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

    private TriplicityRulers triplicityRulers(ZodiacSign sign) {
        return TraditionalTables.triplicityRulers(sign);
    }
}
