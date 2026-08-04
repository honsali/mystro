package app.chart.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.calculator.ChartPointCalculator;
import app.chart.data.AngleType;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPosition;
import app.reading.description.common.data.DignityType;

class ChartPointCalculatorTest {
    private final ChartPointCalculator calculator = new ChartPointCalculator();

    @Test
    void keepsPlanetPlanetWithSignAspect() {
        assertTrue(calculator.isInformativeForTest(relation(PointKey.SUN, PointKey.MOON, new PairwiseRelation.AspectBySign(AspectType.SQUARE, 3), null, List.of())));
    }

    @Test
    void dropsPlanetPlanetAversionWithoutReception() {
        assertFalse(calculator.isInformativeForTest(relation(PointKey.SUN, PointKey.MOON, null, null, List.of())));
    }

    @Test
    void keepsPlanetAngleWithSignAspect() {
        assertTrue(calculator.isInformativeForTest(relation(PointKey.SUN, PointKey.ASCENDANT, new PairwiseRelation.AspectBySign(AspectType.CONJUNCTION, 0), null, List.of())));
    }

    @Test
    void dropsAngleAngleEvenWhenConfiguredBySign() {
        assertFalse(calculator.isInformativeForTest(relation(PointKey.ASCENDANT, PointKey.MIDHEAVEN, new PairwiseRelation.AspectBySign(AspectType.SQUARE, 3), null, List.of())));
    }

    @Test
    void dropsPlanetNodeWholeSignOnlyRelation() {
        assertFalse(calculator.isInformativeForTest(relation(PointKey.MOON, PointKey.NORTH_NODE, new PairwiseRelation.AspectBySign(AspectType.TRINE, 4), null, List.of())));
    }

    @Test
    void keepsPlanetNodeDegreeRelation() {
        assertTrue(calculator.isInformativeForTest(relation(PointKey.MOON, PointKey.NORTH_NODE, new PairwiseRelation.AspectBySign(AspectType.CONJUNCTION, 0), new PairwiseRelation.AspectByDegree(AspectType.CONJUNCTION, 0.0, 0.25, 0.25, 6.0), List.of())));
    }

    @Test
    void keepsMutualReceptionOnlyPlanetPair() {
        assertTrue(calculator.isInformativeForTest(relation(PointKey.MERCURY, PointKey.JUPITER, null, null, List.of(DignityType.DOMICILE))));
    }

    @Test
    void filtersDeterministicallyForSameChart() {
        NatalChart first = syntheticChart();
        NatalChart second = syntheticChart();

        calculator.calculate(first, null);
        calculator.calculate(second, null);

        assertEquals(summarize(first.getPairwiseRelations()), summarize(second.getPairwiseRelations()));
    }

    private PairwiseRelation relation(PointKey a, PointKey b, PairwiseRelation.AspectBySign aspectBySign, PairwiseRelation.AspectByDegree aspectByDegree, List<DignityType> reception) {
        return new PairwiseRelation(a, b, null, aspectBySign, aspectByDegree, reception);
    }

    private NatalChart syntheticChart() {
        NatalChart chart = new NatalChart();
        chart.setPoints(Map.of());
        chart.setPlanets(List.of(position(Planet.SUN, 0.0, 1.0), position(Planet.MOON, 30.0, 12.0), position(Planet.MERCURY, 60.0, 1.2), position(Planet.NORTH_NODE, 100.0, 0.0)));
        chart.setAngles(List.of(angle(AngleType.ASCENDANT, 0.0), angle(AngleType.MIDHEAVEN, 90.0)));
        return chart;
    }

    private PlanetPosition position(Planet planet, double longitude, double speed) {
        return new PlanetPosition(planet, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), 0.0, 0.0, 0.0, 0.0, false, speed, Math.abs(speed), 1.0, speed < 0.0, 1, 1, null, null, null, 0.0, 0.0, 0.0);
    }

    private ChartAngle angle(AngleType angle, double longitude) {
        return new ChartAngle(angle, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude));
    }

    private List<String> summarize(List<PairwiseRelation> relations) {
        return relations.stream().map(relation -> relation.getPointAName() + ":" + relation.getPointBName() + ":sign=" + (relation.getAspectBySign() == null ? "-" : relation.getAspectBySign().getAspect()) + ":degree="
                + (relation.getAspectByDegree() == null ? "-" : relation.getAspectByDegree().getNearestAspect()) + ":reception=" + relation.getMutualReception()).toList();
    }
}
