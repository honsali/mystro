package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.BaseCalculator;
import app.basic.TraditionalTables;
import app.model.basic.BasicSyzygy;
import app.model.basic.ChartAngle;
import app.model.basic.LotPosition;
import app.model.basic.PlanetPosition;
import app.model.data.Planet;
import app.model.data.ZodiacSign;

public class PointCalculator extends BaseCalculator {

    protected void executeCalculation() {
        Map<String, Map<String, Object>> points = new LinkedHashMap<>();
        for (PlanetPosition planet : basicChart.getPlanets()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "PLANET");
            data.put("longitude", planet.getLongitude());
            data.put("sign", planet.getSign());
            data.put("degreeInSign", planet.getDegreeInSign());
            data.put("latitude", planet.getLatitude());
            data.put("declination", planet.getDeclination());
            data.put("speed", planet.getSpeed());
            data.put("meanDailySpeed", planet.getMeanDailySpeed());
            data.put("speedRatio", planet.getSpeedRatio());
            data.put("retrograde", planet.getRetrograde());
            data.put("house", planet.getHouse());
            data.put("angularity", planet.getAngularity());
            data.put("antisciaLongitude", planet.getAntisciaLongitude());
            data.put("contraAntisciaLongitude", planet.getContraAntisciaLongitude());
            if (isTraditionalPlanet(planet.getPlanet())) {
                data.put("domicileRuler", domicileRuler(planet.getSign()));
                data.put("exaltationRuler", exaltationRuler(planet.getSign()));
                data.put("triplicityRulers", triplicityRulers(planet.getSign()));
                data.put("termRuler", planet.getTermRuler());
                data.put("faceRuler", faceRuler(planet.getSign(), planet.getDegreeInSign()));
                data.put("detrimentRuler", domicileRuler(opposite(planet.getSign())));
                data.put("fallRuler", exaltationRuler(opposite(planet.getSign())));
            }
            points.put(planet.getPlanet().name(), data);
        }
        for (ChartAngle angle : basicChart.getAngles()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "ANGLE");
            data.put("longitude", angle.getLongitude());
            data.put("sign", angle.getSign());
            data.put("degreeInSign", angle.getDegreeInSign());
            points.put(angle.getName(), data);
        }
        for (LotPosition lot : basicChart.getLots()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "LOT");
            data.put("longitude", lot.getLongitude());
            data.put("sign", lot.getSign());
            data.put("degreeInSign", lot.getDegreeInSign());
            data.put("house", lot.getHouse());
            data.put("antisciaLongitude", lot.getAntisciaLongitude());
            data.put("contraAntisciaLongitude", lot.getContraAntisciaLongitude());
            points.put(lot.getName(), data);
        }
        BasicSyzygy syzygy = basicChart.getSyzygy();
        if (syzygy != null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "SYZYGY_POINT");
            data.put("longitude", syzygy.getLongitude());
            data.put("sign", syzygy.getSign());
            data.put("degreeInSign", syzygy.getDegreeInSign());
            data.put("house", syzygy.getHouse());
            data.put("syzygyType", syzygy.getType());
            data.put("julianDay", syzygy.getJulianDay());
            data.put("approximateUtcInstant", syzygy.getApproximateUtcInstant());
            data.put("sunLongitudeAtSyzygy", syzygy.getSunLongitude());
            data.put("moonLongitudeAtSyzygy", syzygy.getMoonLongitude());
            data.put("angularSeparation", syzygy.getAngularSeparation());
            data.put("sunSignAtSyzygy", syzygy.getSunSign());
            data.put("moonSignAtSyzygy", syzygy.getMoonSign());
            points.put("PRENATAL_SYZYGY", data);
        }
        basicChart.setPoints(points);
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

    private Map<String, Object> triplicityRulers(ZodiacSign sign) {
        return TraditionalTables.triplicityRulers(sign);
    }


}
