package app.basic.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.basic.AstroMath;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.ChartAngle;
import app.chart.model.ChartPoint;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPosition;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

public class ChartPointCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        List<ChartPoint> chartPoints = new ArrayList<>();
        for (PlanetPosition planet : natalChart.getPlanets()) {
            chartPoints.add(new ChartPoint(PointKey.of(planet.getPlanet()), planet.getLongitude(), planet.getSign(), planet.getDegreeInSign(), planet.getHouse()));
        }
        for (ChartAngle angle : natalChart.getAngles()) {
            chartPoints.add(new ChartPoint(PointKey.of(angle.getName()), angle.getLongitude(), angle.getSign(), angle.getDegreeInSign(), null));
        }

        natalChart.setPairwiseRelations(calculatePairwiseRelations(chartPoints, natalChart.getPlanets()));
    }

    private int signDistance(ZodiacSign signA, ZodiacSign signB) {
        int distance = Math.abs(signA.ordinal() - signB.ordinal());
        return Math.min(distance, 12 - distance);
    }

    private List<PairwiseRelation> calculatePairwiseRelations(List<ChartPoint> points, List<PlanetPosition> planets) {
        Map<PointKey, PlanetPosition> planetByKey = new LinkedHashMap<>();
        for (PlanetPosition planet : planets) {
            planetByKey.put(PointKey.of(planet.getPlanet()), planet);
        }
        List<PairwiseRelation> relations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                PairwiseRelation.EquatorialRelation equatorial = null;
                PlanetPosition planetA = planetByKey.get(pointA.getKey());
                PlanetPosition planetB = planetByKey.get(pointB.getKey());
                if (planetA != null && planetB != null) {
                    double declinationDifference = Math.abs(planetA.getDeclination() - planetB.getDeclination());
                    double contraParallelSeparation = Math.abs(planetA.getDeclination() + planetB.getDeclination());
                    boolean sameHemisphere = (planetA.getDeclination() >= 0.0 && planetB.getDeclination() >= 0.0) || (planetA.getDeclination() < 0.0 && planetB.getDeclination() < 0.0);
                    equatorial = new PairwiseRelation.EquatorialRelation(declinationDifference, contraParallelSeparation, sameHemisphere);
                }
                relations.add(new PairwiseRelation(pointA.getKey(), pointB.getKey(), new PairwiseRelation.EclipticRelation(AstroMath.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude()), signDistance(pointA.getSign(), pointB.getSign())), equatorial));
            }
        }
        return relations;
    }

}
