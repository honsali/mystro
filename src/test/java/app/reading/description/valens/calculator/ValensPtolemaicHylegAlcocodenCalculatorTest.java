package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.AngleType;
import app.chart.data.Angularity;
import app.chart.data.AspectMotion;
import app.chart.data.AspectType;
import app.chart.data.Element;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.BasicSect;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.calculator.LotCalculatorHouseFixture;
import app.reading.description.common.data.AphesisBasis;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;
import app.chart.data.SyzygyType;
import app.reading.description.common.data.VitalityYearsTier;
import app.reading.description.common.model.HylegAlcocodenEntry;
import app.reading.description.common.model.LotEntry;
import app.reading.description.common.model.PrenatalSyzygyEntry;

class ValensPtolemaicHylegAlcocodenCalculatorTest {
    private final ValensPtolemaicHylegAlcocodenCalculator calculator = new ValensPtolemaicHylegAlcocodenCalculator();

    @Test
    void degreeZoneMakesCandidateEligibleOutsideProrogativeHouse() {
        NatalChart chart = baseChart(100.0, Sect.DIURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 124.0, 2, Angularity.SUCCEDENT), position(Planet.MOON, 250.0, 6, Angularity.SUCCEDENT), position(Planet.MERCURY, 200.0, 8, Angularity.SUCCEDENT), position(Planet.VENUS, 110.0, 5, Angularity.SUCCEDENT),
                position(Planet.MARS, 40.0, 4, Angularity.SUCCEDENT), position(Planet.JUPITER, 10.0, 1, Angularity.SUCCEDENT), position(Planet.SATURN, 300.0, 10, Angularity.SUCCEDENT)));
        chart.setLots(List.of(fortune(250.0, 6)));
        chart.setSyzygy(syzygy(250.0, 6));
        chart.setPairwiseRelations(List.of());

        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);

        assertEquals("SUN", entry.hyleg().point());
        assertEquals(2, entry.hyleg().house());
        assertEquals(AphesisBasis.DEGREE_ZONE, entry.hyleg().aphesisBasis());
        assertEquals(AphesisBasis.DEGREE_ZONE, entry.candidates().get(0).aphesisBasis());
    }

    @Test
    void vitalityYearsUsesAngularityTierAndBeneficMaleficMinorYearModifiers() {
        NatalChart chart = baseChart(0.0, Sect.DIURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 40.0, 10, Angularity.SUCCEDENT), position(Planet.MOON, 190.0, 4, Angularity.SUCCEDENT), position(Planet.MERCURY, 300.0, 8, Angularity.SUCCEDENT), position(Planet.VENUS, 40.0, 10, Angularity.SUCCEDENT),
                position(Planet.MARS, 130.0, 1, Angularity.SUCCEDENT), position(Planet.JUPITER, 160.0, 2, Angularity.SUCCEDENT), position(Planet.SATURN, 220.0, 7, Angularity.SUCCEDENT)));
        chart.setLots(List.of(fortune(250.0, 8)));
        chart.setSyzygy(syzygy(250.0, 8));
        chart.setPairwiseRelations(List.of(relation(Planet.VENUS, Planet.JUPITER, AspectType.TRINE, 120.0, 0.0, AspectMotion.APPLYING), relation(Planet.VENUS, Planet.MARS, AspectType.SQUARE, 90.0, 0.0, AspectMotion.SEPARATING)));
        chart.setPoints(Map.of(PointKey.VENUS, planetPoint(SolarCondition.FREE_OF_SUN)));

        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);

        assertEquals(Planet.VENUS, entry.alcocoden().planet());
        assertEquals(List.of(DignityType.DOMICILE, DignityType.TRIPLICITY), entry.alcocoden().dignityClaims());
        assertNotNull(entry.vitalityYears());
        assertEquals(Angularity.SUCCEDENT, entry.vitalityYears().alcocodenAngularity());
        assertEquals(VitalityYearsTier.MEAN, entry.vitalityYears().baseTier());
        assertEquals(45.0, entry.vitalityYears().baseYears());
        assertEquals(2, entry.vitalityYears().modifiers().size());
        assertEquals(Planet.JUPITER, entry.vitalityYears().modifiers().get(0).planet());
        assertEquals(12.0, entry.vitalityYears().modifiers().get(0).deltaYears());
        assertEquals(Planet.MARS, entry.vitalityYears().modifiers().get(1).planet());
        assertEquals(-15.0, entry.vitalityYears().modifiers().get(1).deltaYears());
        assertEquals(42.0, entry.vitalityYears().indicatedYears());
    }

    @Test
    void usesUnreversedPtolemaicFortuneAsFallbackCandidate() {
        NatalChart chart = baseChart(100.0, Sect.NOCTURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 10.0, 6, Angularity.SUCCEDENT), position(Planet.MOON, 40.0, 6, Angularity.SUCCEDENT), position(Planet.MERCURY, 200.0, 8, Angularity.SUCCEDENT), position(Planet.VENUS, 300.0, 8, Angularity.SUCCEDENT),
                position(Planet.MARS, 220.0, 8, Angularity.SUCCEDENT), position(Planet.JUPITER, 10.0, 6, Angularity.SUCCEDENT), position(Planet.SATURN, 300.0, 8, Angularity.SUCCEDENT)));
        chart.setLots(List.of(fortune(70.0, 12)));
        chart.setSyzygy(syzygy(250.0, 6));
        chart.setPairwiseRelations(List.of());

        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);
        HylegAlcocodenEntry.HylegCandidate fortuneCandidate = entry.candidates().get(entry.candidates().size() - 1);

        assertEquals("PTOLEMAIC_FORTUNE", fortuneCandidate.point());
        assertEquals(130.0, fortuneCandidate.longitude());
        assertEquals(2, fortuneCandidate.house());
    }

    @Test
    void ptolemaicWaterTriplicityIncludesMarsAsCommonRuler() {
        NatalChart chart = baseChart(210.0, Sect.NOCTURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 80.0, 2, Angularity.SUCCEDENT), position(Planet.MOON, 40.0, 2, Angularity.SUCCEDENT), position(Planet.MERCURY, 90.0, 2, Angularity.SUCCEDENT), position(Planet.VENUS, 0.0, 2, Angularity.SUCCEDENT),
                position(Planet.MARS, 210.0, 1, Angularity.SUCCEDENT), position(Planet.JUPITER, 30.0, 2, Angularity.SUCCEDENT), position(Planet.SATURN, 300.0, 2, Angularity.SUCCEDENT)));
        chart.setSyzygy(syzygy(80.0, 2));
        chart.setPairwiseRelations(List.of());
        chart.setPoints(Map.of(PointKey.MARS, planetPoint(SolarCondition.FREE_OF_SUN)));

        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);

        assertEquals(Planet.MARS, entry.alcocoden().planet());
        assertEquals(List.of(DignityType.DOMICILE, DignityType.TRIPLICITY, DignityType.TERM, DignityType.FACE), entry.alcocoden().dignityClaims());
    }

    @Test
    void ptolemaicWaterTriplicityIncludesSectCoRuler() {
        NatalChart chart = baseChart(210.0, Sect.NOCTURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 80.0, 2, Angularity.SUCCEDENT), position(Planet.MOON, 40.0, 2, Angularity.SUCCEDENT), position(Planet.MERCURY, 90.0, 2, Angularity.SUCCEDENT), position(Planet.VENUS, 0.0, 2, Angularity.SUCCEDENT),
                position(Planet.MARS, 240.0, 2, Angularity.SUCCEDENT), position(Planet.JUPITER, 300.0, 2, Angularity.SUCCEDENT), position(Planet.SATURN, 300.0, 2, Angularity.SUCCEDENT)));
        chart.setSyzygy(syzygy(80.0, 2));
        chart.setPairwiseRelations(List.of());
        chart.setPoints(Map.of(PointKey.MOON, planetPoint(SolarCondition.FREE_OF_SUN)));

        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);

        assertEquals(Planet.MOON, entry.alcocoden().planet());
        assertEquals(List.of(DignityType.TRIPLICITY), entry.alcocoden().dignityClaims());
    }

    @Test
    void vitalityYearsUsesEachAngularityTierAndUnderBeamsSelfModifier() {
        assertTier(Angularity.ANGULAR, VitalityYearsTier.GREATEST, 82.0);
        assertTier(Angularity.SUCCEDENT, VitalityYearsTier.MEAN, 45.0);
        assertTier(Angularity.CADENT, VitalityYearsTier.LEAST, 8.0);

        NatalChart chart = chartForVenusTier(Angularity.SUCCEDENT, SolarCondition.UNDER_BEAMS);
        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chart);

        assertEquals(1, entry.vitalityYears().modifiers().size());
        assertEquals(Planet.VENUS, entry.vitalityYears().modifiers().get(0).planet());
        assertEquals(-8.0, entry.vitalityYears().modifiers().get(0).deltaYears());
        assertEquals(37.0, entry.vitalityYears().indicatedYears());
    }

    private void assertTier(Angularity angularity, VitalityYearsTier tier, double baseYears) {
        HylegAlcocodenEntry entry = calculator.calculate(ctx(), chartForVenusTier(angularity, SolarCondition.FREE_OF_SUN));
        assertEquals(tier, entry.vitalityYears().baseTier());
        assertEquals(baseYears, entry.vitalityYears().baseYears());
        assertEquals(baseYears, entry.vitalityYears().indicatedYears());
    }

    private NatalChart chartForVenusTier(Angularity venusAngularity, SolarCondition venusSolarCondition) {
        NatalChart chart = baseChart(0.0, Sect.DIURNAL);
        chart.setPlanets(List.of(position(Planet.SUN, 40.0, 10, Angularity.SUCCEDENT), position(Planet.MOON, 190.0, 4, Angularity.SUCCEDENT), position(Planet.MERCURY, 300.0, 8, Angularity.SUCCEDENT), position(Planet.VENUS, 40.0, 10, venusAngularity),
                position(Planet.MARS, 10.0, 1, Angularity.SUCCEDENT), position(Planet.JUPITER, 280.0, 9, Angularity.SUCCEDENT), position(Planet.SATURN, 220.0, 7, Angularity.SUCCEDENT)));
        chart.setLots(List.of(fortune(250.0, 8)));
        chart.setSyzygy(syzygy(250.0, 8));
        chart.setPairwiseRelations(List.of());
        chart.setPoints(Map.of(PointKey.VENUS, planetPoint(venusSolarCondition)));
        return chart;
    }

    private NatalChart baseChart(double ascendant, Sect sect) {
        NatalChart chart = new NatalChart();
        chart.setAngles(List.of(new ChartAngle(AngleType.ASCENDANT, ascendant, AstroMath.signOf(ascendant), AstroMath.degreeInSign(ascendant))));
        chart.setSect(new BasicSect(sect, sect == Sect.DIURNAL ? Planet.SUN : Planet.MOON, sect == Sect.DIURNAL ? Planet.MOON : Planet.SUN, Planet.JUPITER, Planet.VENUS, Planet.SATURN, Planet.MARS, sect == Sect.DIURNAL, sect != Sect.DIURNAL, 1.0, -1.0, Map.of()));
        chart.setPoints(new LinkedHashMap<>());
        return chart;
    }

    private CalculationContext ctx() {
        return LotCalculatorHouseFixture.ctx(new CoreDoctrineInfo("valens", "Vettius Valens", HouseSystem.WHOLE_SIGN, Terms.EGYPTIAN, Triplicity.DOROTHEAN));
    }

    private PlanetPosition position(Planet planet, double longitude, int wholeSignHouse, Angularity angularity) {
        double normalized = AstroMath.normalize(longitude);
        return new PlanetPosition(planet, normalized, AstroMath.signOf(normalized), AstroMath.degreeInSign(normalized), 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, false, wholeSignHouse, wholeSignHouse, wholeSignHouse, angularity, null, 0.0, 0.0, 0.0);
    }

    private LotEntry fortune(double longitude, int house) {
        return new LotEntry("FORTUNE", "Fortune", "valens", longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), house, Planet.JUPITER, "test");
    }

    private PrenatalSyzygyEntry syzygy(double longitude, int house) {
        return new PrenatalSyzygyEntry(SyzygyType.NEW_MOON, 0.0, Instant.EPOCH, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), house, longitude, longitude, 0.0, AstroMath.signOf(longitude), AstroMath.signOf(longitude));
    }

    private PairwiseRelation relation(Planet a, Planet b, AspectType aspect, double exactAngle, double orb, AspectMotion motion) {
        return new PairwiseRelation(PointKey.of(a), PointKey.of(b), null, null, new PairwiseRelation.AspectByDegree(aspect, exactAngle, exactAngle + orb, orb, 9.0, motion), List.of());
    }

    private PlanetPointEntry planetPoint(SolarCondition solarCondition) {
        return new PlanetPointEntry(0.0, ZodiacSign.TAURUS, Element.EARTH, 10.0, 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, false, 10, 10, 10, Angularity.SUCCEDENT, 0.0, 0.0, Planet.VENUS, Planet.MOON, Planet.VENUS, null, null, Planet.VENUS, null, null, null, List.of(DignityType.DOMICILE), List.of(),
                SolarOrientation.ORIENTAL, SectCondition.OF_SECT, solarCondition, List.of(), false, PointType.PLANET);
    }
}
