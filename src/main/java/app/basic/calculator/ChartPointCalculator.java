package app.basic.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.ChartAngle;
import app.model.basic.ChartPoint;
import app.model.basic.PairwiseRelation;
import app.model.basic.PlanetPosition;
import app.model.basic.RawAspectMatrixEntry;
import app.model.basic.RawDeclinationMatrixEntry;
import app.model.basic.RawSignDistanceMatrixEntry;
import app.model.data.PointType;
import app.model.data.ZodiacSign;

public class ChartPointCalculator implements Calculator {


    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        List<ChartPoint> chartPoints = new ArrayList<>();
        for (PlanetPosition planet : basicChart.getPlanets()) {
            chartPoints.add(new ChartPoint(PointType.PLANET, planet.getPlanet().name(), planet.getLongitude(), planet.getSign(), planet.getDegreeInSign(), planet.getHouse()));
        }
        for (ChartAngle angle : basicChart.getAngles()) {
            chartPoints.add(new ChartPoint(PointType.ANGLE, angle.getName().name(), angle.getLongitude(), angle.getSign(), angle.getDegreeInSign(), null));
        }


        basicChart.setRawAspectMatrix(calculateRawAspectMatrix(chartPoints, ctx));
        basicChart.setRawDeclinationMatrix(calculateRawDeclinationMatrix(basicChart.getPlanets(), ctx));
        basicChart.setRawSignDistanceMatrix(calculateRawSignDistanceMatrix(chartPoints));
        basicChart.setPairwiseRelations(calculatePairwiseRelations(chartPoints, basicChart.getPlanets(), ctx));
    }



    private int signDistance(ZodiacSign signA, ZodiacSign signB) {
        int distance = Math.abs(signA.ordinal() - signB.ordinal());
        return Math.min(distance, 12 - distance);
    }



    private List<RawAspectMatrixEntry> calculateRawAspectMatrix(List<ChartPoint> points, BasicCalculationContext ctx) {
        List<RawAspectMatrixEntry> matrix = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                matrix.add(new RawAspectMatrixEntry(pointA.getType(), pointA.getName(), pointA.getLongitude(), pointB.getType(), pointB.getName(), pointB.getLongitude(), ctx.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude())));
            }
        }
        return matrix;
    }



    private List<PairwiseRelation> calculatePairwiseRelations(List<ChartPoint> points, List<PlanetPosition> planets, BasicCalculationContext ctx) {
        Map<String, PlanetPosition> planetByName = new LinkedHashMap<>();
        for (PlanetPosition planet : planets) {
            planetByName.put(planet.getPlanet().name(), planet);
        }
        List<PairwiseRelation> relations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                PairwiseRelation.EquatorialRelation equatorial = null;
                PlanetPosition planetA = planetByName.get(pointA.getName());
                PlanetPosition planetB = planetByName.get(pointB.getName());
                if (planetA != null && planetB != null) {
                    double declinationDifference = Math.abs(planetA.getDeclination() - planetB.getDeclination());
                    boolean sameHemisphere = (planetA.getDeclination() >= 0.0 && planetB.getDeclination() >= 0.0) || (planetA.getDeclination() < 0.0 && planetB.getDeclination() < 0.0);
                    equatorial = new PairwiseRelation.EquatorialRelation(declinationDifference, sameHemisphere);
                }
                relations.add(new PairwiseRelation(pointA.getName(), pointB.getName(), new PairwiseRelation.EclipticRelation(ctx.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude()), signDistance(pointA.getSign(), pointB.getSign())), equatorial));
            }
        }
        return relations;
    }

    private List<RawDeclinationMatrixEntry> calculateRawDeclinationMatrix(List<PlanetPosition> planets, BasicCalculationContext ctx) {
        List<RawDeclinationMatrixEntry> matrix = new ArrayList<>();
        for (int i = 0; i < planets.size(); i++) {
            PlanetPosition pointA = planets.get(i);
            for (int j = i + 1; j < planets.size(); j++) {
                PlanetPosition pointB = planets.get(j);
                double difference = Math.abs(pointA.getDeclination() - pointB.getDeclination());
                boolean sameHemisphere = (pointA.getDeclination() >= 0.0 && pointB.getDeclination() >= 0.0) || (pointA.getDeclination() < 0.0 && pointB.getDeclination() < 0.0);
                matrix.add(new RawDeclinationMatrixEntry(pointA.getPlanet().name(), pointA.getDeclination(), pointB.getPlanet().name(), pointB.getDeclination(), difference, sameHemisphere));
            }
        }
        return matrix;
    }

    private List<RawSignDistanceMatrixEntry> calculateRawSignDistanceMatrix(List<ChartPoint> points) {
        List<RawSignDistanceMatrixEntry> matrix = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                matrix.add(new RawSignDistanceMatrixEntry(pointA.getName(), pointA.getSign(), pointB.getName(), pointB.getSign(), signDistance(pointA.getSign(), pointB.getSign())));
            }
        }
        return matrix;
    }

}
