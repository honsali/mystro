package app.basic.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;
import app.basic.model.ChartAngle;
import app.basic.model.ChartPoint;
import app.basic.model.PairwiseRelation;
import app.basic.model.PlanetPosition;
import app.basic.model.RawAspectMatrixEntry;
import app.basic.model.RawDeclinationMatrixEntry;
import app.basic.model.RawSignDistanceMatrixEntry;
import app.basic.data.PointKey;
import app.basic.data.ZodiacSign;

public class ChartPointCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        List<ChartPoint> chartPoints = new ArrayList<>();
        for (PlanetPosition planet : natalChart.getPlanets()) {
            chartPoints.add(new ChartPoint(PointKey.of(planet.getPlanet()), planet.getLongitude(), planet.getSign(), planet.getDegreeInSign(), planet.getHouse()));
        }
        for (ChartAngle angle : natalChart.getAngles()) {
            chartPoints.add(new ChartPoint(PointKey.of(angle.getName()), angle.getLongitude(), angle.getSign(), angle.getDegreeInSign(), null));
        }


        natalChart.setRawAspectMatrix(calculateRawAspectMatrix(chartPoints, ctx));
        natalChart.setRawDeclinationMatrix(calculateRawDeclinationMatrix(natalChart.getPlanets(), ctx));
        natalChart.setRawSignDistanceMatrix(calculateRawSignDistanceMatrix(chartPoints));
        natalChart.setPairwiseRelations(calculatePairwiseRelations(chartPoints, natalChart.getPlanets(), ctx));
    }



    private int signDistance(ZodiacSign signA, ZodiacSign signB) {
        int distance = Math.abs(signA.ordinal() - signB.ordinal());
        return Math.min(distance, 12 - distance);
    }



    private List<RawAspectMatrixEntry> calculateRawAspectMatrix(List<ChartPoint> points, CalculationContext ctx) {
        List<RawAspectMatrixEntry> matrix = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint pointA = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                ChartPoint pointB = points.get(j);
                matrix.add(new RawAspectMatrixEntry(pointA.getType(), pointA.getKey(), pointA.getLongitude(), pointB.getType(), pointB.getKey(), pointB.getLongitude(), ctx.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude())));
            }
        }
        return matrix;
    }



    private List<PairwiseRelation> calculatePairwiseRelations(List<ChartPoint> points, List<PlanetPosition> planets, CalculationContext ctx) {
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
                relations.add(new PairwiseRelation(pointA.getKey(), pointB.getKey(), new PairwiseRelation.EclipticRelation(ctx.rawAngularSeparation(pointA.getLongitude(), pointB.getLongitude()), signDistance(pointA.getSign(), pointB.getSign())), equatorial));
            }
        }
        return relations;
    }

    private List<RawDeclinationMatrixEntry> calculateRawDeclinationMatrix(List<PlanetPosition> planets, CalculationContext ctx) {
        List<RawDeclinationMatrixEntry> matrix = new ArrayList<>();
        for (int i = 0; i < planets.size(); i++) {
            PlanetPosition pointA = planets.get(i);
            for (int j = i + 1; j < planets.size(); j++) {
                PlanetPosition pointB = planets.get(j);
                double difference = Math.abs(pointA.getDeclination() - pointB.getDeclination());
                double contraParallelSeparation = Math.abs(pointA.getDeclination() + pointB.getDeclination());
                boolean sameHemisphere = (pointA.getDeclination() >= 0.0 && pointB.getDeclination() >= 0.0) || (pointA.getDeclination() < 0.0 && pointB.getDeclination() < 0.0);
                matrix.add(new RawDeclinationMatrixEntry(pointA.getPlanet().name(), pointA.getDeclination(), pointB.getPlanet().name(), pointB.getDeclination(), difference, contraParallelSeparation, sameHemisphere));
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
                matrix.add(new RawSignDistanceMatrixEntry(pointA.getKey(), pointA.getSign(), pointB.getKey(), pointB.getSign(), signDistance(pointA.getSign(), pointB.getSign())));
            }
        }
        return matrix;
    }

}
