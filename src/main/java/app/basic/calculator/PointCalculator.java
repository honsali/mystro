package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.BaseCalculator;
import app.basic.TraditionalTables;
import app.model.basic.BasicSyzygy;
import app.model.basic.ChartAngle;
import app.model.basic.LotPosition;
import app.model.basic.PlanetPosition;
import app.model.basic.PointEntry;
import app.model.basic.TriplicityRulers;
import app.model.data.Planet;
import app.model.data.PointType;
import app.model.data.ZodiacSign;

public class PointCalculator extends BaseCalculator {

    protected void executeCalculation() {
        Map<String, PointEntry> points = new LinkedHashMap<>();
        for (PlanetPosition planet : basicChart.getPlanets()) {
            PointEntry.Builder entry = PointEntry.builder(PointType.PLANET)
                    .longitude(planet.getLongitude())
                    .sign(planet.getSign())
                    .degreeInSign(planet.getDegreeInSign())
                    .latitude(planet.getLatitude())
                    .declination(planet.getDeclination())
                    .speed(planet.getSpeed())
                    .meanDailySpeed(planet.getMeanDailySpeed())
                    .speedRatio(planet.getSpeedRatio())
                    .retrograde(planet.getRetrograde())
                    .house(planet.getHouse())
                    .angularity(planet.getAngularity())
                    .antisciaLongitude(planet.getAntisciaLongitude())
                    .contraAntisciaLongitude(planet.getContraAntisciaLongitude());
            if (isTraditionalPlanet(planet.getPlanet())) {
                entry.domicileRuler(domicileRuler(planet.getSign()))
                        .exaltationRuler(exaltationRuler(planet.getSign()))
                        .triplicityRulers(triplicityRulers(planet.getSign()))
                        .termRuler(planet.getTermRuler())
                        .faceRuler(faceRuler(planet.getSign(), planet.getDegreeInSign()))
                        .detrimentRuler(domicileRuler(opposite(planet.getSign())))
                        .fallRuler(exaltationRuler(opposite(planet.getSign())));
            }
            points.put(planet.getPlanet().name(), entry.build());
        }
        for (ChartAngle angle : basicChart.getAngles()) {
            PointEntry entry = PointEntry.builder(PointType.ANGLE)
                    .longitude(angle.getLongitude())
                    .sign(angle.getSign())
                    .degreeInSign(angle.getDegreeInSign())
                    .build();
            points.put(angle.getName(), entry);
        }
        for (LotPosition lot : basicChart.getLots()) {
            PointEntry entry = PointEntry.builder(PointType.LOT)
                    .longitude(lot.getLongitude())
                    .sign(lot.getSign())
                    .degreeInSign(lot.getDegreeInSign())
                    .house(lot.getHouse())
                    .antisciaLongitude(lot.getAntisciaLongitude())
                    .contraAntisciaLongitude(lot.getContraAntisciaLongitude())
                    .build();
            points.put(lot.getName(), entry);
        }
        BasicSyzygy syzygy = basicChart.getSyzygy();
        if (syzygy != null) {
            PointEntry entry = PointEntry.builder(PointType.SYZYGY_POINT)
                    .longitude(syzygy.getLongitude())
                    .sign(syzygy.getSign())
                    .degreeInSign(syzygy.getDegreeInSign())
                    .house(syzygy.getHouse())
                    .syzygyType(syzygy.getType())
                    .julianDay(syzygy.getJulianDay())
                    .approximateUtcInstant(syzygy.getApproximateUtcInstant())
                    .sunLongitudeAtSyzygy(syzygy.getSunLongitude())
                    .moonLongitudeAtSyzygy(syzygy.getMoonLongitude())
                    .angularSeparation(syzygy.getAngularSeparation())
                    .sunSignAtSyzygy(syzygy.getSunSign())
                    .moonSignAtSyzygy(syzygy.getMoonSign())
                    .build();
            points.put("PRENATAL_SYZYGY", entry);
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

    private TriplicityRulers triplicityRulers(ZodiacSign sign) {
        return TraditionalTables.triplicityRulers(sign);
    }
}
