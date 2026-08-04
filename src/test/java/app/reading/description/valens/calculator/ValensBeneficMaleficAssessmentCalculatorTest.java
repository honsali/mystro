package app.reading.description.valens.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import app.chart.AstroMath;
import app.chart.data.Angularity;
import app.chart.data.AspectType;
import app.chart.data.Element;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.BasicSect;
import app.chart.model.NatalChart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;
import app.chart.model.PointEntry;
import app.reading.description.common.data.BeneficMaleficCondition;
import app.reading.description.common.data.ConditionAssessment;
import app.reading.description.common.data.RulerAffliction;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.model.BeneficMaleficAssessmentEntry;

class ValensBeneficMaleficAssessmentCalculatorTest {
        private final ValensBeneficMaleficAssessmentCalculator calculator = new ValensBeneficMaleficAssessmentCalculator();

        @Test
        void identifiesBonificationAndMaltreatmentBySign() {
                NatalChart chart = chartWithPositions(position(Planet.SUN, ZodiacSign.ARIES), position(Planet.MOON, ZodiacSign.ARIES), position(Planet.MERCURY, ZodiacSign.CANCER), position(Planet.VENUS, ZodiacSign.VIRGO), position(Planet.MARS, ZodiacSign.ARIES),
                                position(Planet.JUPITER, ZodiacSign.ARIES), position(Planet.SATURN, ZodiacSign.CAPRICORN));
                chart.setPairwiseRelations(List.of(relation(Planet.MERCURY, Planet.VENUS, AspectType.SEXTILE, 2), relation(Planet.MERCURY, Planet.MARS, AspectType.SQUARE, 3), relation(Planet.MERCURY, Planet.SATURN, AspectType.OPPOSITION, 6)));

                List<BeneficMaleficAssessmentEntry> mercury = calculator.calculate(chart).get(Planet.MERCURY);

                assertEquals(5, mercury.size());
                assertEquals(ConditionAssessment.BONIFICATION, mercury.get(0).assessment());
                assertEquals(BeneficMaleficCondition.BENEFIC_ASPECT, mercury.get(0).condition());
                assertEquals(Planet.VENUS, mercury.get(0).agent());
                assertTrue(mercury.get(0).agentOfSect());
                assertEquals(BeneficMaleficCondition.MALEFIC_ASPECT, mercury.get(1).condition());
                assertEquals(Planet.MARS, mercury.get(1).agent());
                assertEquals(9, mercury.get(1).signDistance());
                assertEquals(BeneficMaleficCondition.MALEFIC_ASPECT, mercury.get(2).condition());
                assertEquals(Planet.SATURN, mercury.get(2).agent());
                assertEquals(BeneficMaleficCondition.MALEFIC_OVERCOMING, mercury.get(3).condition());
                assertEquals(Planet.MARS, mercury.get(3).agent());
                assertEquals(9, mercury.get(3).signDistance());
                assertEquals(BeneficMaleficCondition.MALEFIC_OVERCOMING, mercury.get(4).condition());
        }

        @Test
        void doesNotTreatEleventhSignAsMaleficOvercoming() {
                NatalChart chart = chartWithPositions(position(Planet.SUN, ZodiacSign.ARIES), position(Planet.MOON, ZodiacSign.SCORPIO), position(Planet.MERCURY, ZodiacSign.LIBRA), position(Planet.VENUS, ZodiacSign.LEO), position(Planet.MARS, ZodiacSign.AQUARIUS),
                                position(Planet.JUPITER, ZodiacSign.VIRGO), position(Planet.SATURN, ZodiacSign.CANCER));
                chart.setPairwiseRelations(List.of(relation(Planet.SUN, Planet.MARS, AspectType.SEXTILE, 2)));

                List<BeneficMaleficAssessmentEntry> sun = calculator.calculate(chart).get(Planet.SUN);

                assertTrue(sun.isEmpty());
        }

        @Test
        void identifiesSignEnclosureAndBeneficBreaksIt() {
                NatalChart enclosed = chartWithPositions(position(Planet.SUN, ZodiacSign.ARIES), position(Planet.MOON, ZodiacSign.PISCES), position(Planet.MERCURY, ZodiacSign.CANCER), position(Planet.VENUS, ZodiacSign.LIBRA), position(Planet.MARS, ZodiacSign.GEMINI),
                                position(Planet.JUPITER, ZodiacSign.SAGITTARIUS), position(Planet.SATURN, ZodiacSign.LEO));
                enclosed.setPairwiseRelations(List.of());

                List<BeneficMaleficAssessmentEntry> mercury = calculator.calculate(enclosed).get(Planet.MERCURY);

                assertEquals(1, mercury.size());
                assertEquals(BeneficMaleficCondition.ENCLOSURE, mercury.get(0).condition());
                assertEquals(ConditionAssessment.MALTREATMENT, mercury.get(0).assessment());
                assertEquals(Planet.MARS, mercury.get(0).agent());
                assertEquals(Planet.SATURN, mercury.get(0).coAgent());
                assertEquals(11, mercury.get(0).signDistance());
                assertEquals(1, mercury.get(0).coSignDistance());

                NatalChart broken = chartWithPositions(position(Planet.SUN, ZodiacSign.ARIES), position(Planet.MOON, ZodiacSign.PISCES), position(Planet.MERCURY, ZodiacSign.CANCER), position(Planet.VENUS, ZodiacSign.GEMINI), position(Planet.MARS, ZodiacSign.GEMINI),
                                position(Planet.JUPITER, ZodiacSign.SAGITTARIUS), position(Planet.SATURN, ZodiacSign.LEO));
                broken.setPairwiseRelations(List.of());

                assertFalse(calculator.calculate(broken).get(Planet.MERCURY).stream().anyMatch(entry -> entry.condition() == BeneficMaleficCondition.ENCLOSURE));
        }

        @Test
        void identifiesAdherenceAndBesiegementWithSevenDegreeBoundary() {
                NatalChart adherence = chartWithPositions(position(Planet.SUN, 0.0), position(Planet.MOON, 30.0), position(Planet.MERCURY, 100.0), position(Planet.VENUS, 102.0), position(Planet.MARS, 104.0), position(Planet.JUPITER, 250.0), position(Planet.SATURN, 300.0));
                adherence.setPairwiseRelations(List.of());

                BeneficMaleficAssessmentEntry adherenceEntry = calculator.calculate(adherence).get(Planet.MERCURY).stream().filter(entry -> entry.condition() == BeneficMaleficCondition.ADHERENCE).findFirst().orElseThrow();
                assertEquals(ConditionAssessment.BONIFICATION, adherenceEntry.assessment());
                assertEquals(Planet.VENUS, adherenceEntry.agent());
                assertEquals(2.0, adherenceEntry.orbFromExact());

                NatalChart besieged = chartWithPositions(position(Planet.SUN, 0.0), position(Planet.MOON, 30.0), position(Planet.MERCURY, 100.0), position(Planet.VENUS, 200.0), position(Planet.MARS, 96.0), position(Planet.JUPITER, 250.0), position(Planet.SATURN, 105.0));
                besieged.setPairwiseRelations(List.of());

                BeneficMaleficAssessmentEntry besiegement = calculator.calculate(besieged).get(Planet.MERCURY).stream().filter(entry -> entry.condition() == BeneficMaleficCondition.BESIEGEMENT).findFirst().orElseThrow();
                assertEquals(ConditionAssessment.MALTREATMENT, besiegement.assessment());
                assertEquals(Planet.MARS, besiegement.agent());
                assertEquals(Planet.SATURN, besiegement.coAgent());
                assertEquals(4.0, besiegement.orbFromExact());
                assertEquals(5.0, besiegement.coOrbFromExact());

                NatalChart outsideBoundary = chartWithPositions(position(Planet.SUN, 0.0), position(Planet.MOON, 30.0), position(Planet.MERCURY, 100.0), position(Planet.VENUS, 200.0), position(Planet.MARS, 92.0), position(Planet.JUPITER, 250.0), position(Planet.SATURN, 105.0));
                outsideBoundary.setPairwiseRelations(List.of());

                assertFalse(calculator.calculate(outsideBoundary).get(Planet.MERCURY).stream().anyMatch(entry -> entry.condition() == BeneficMaleficCondition.BESIEGEMENT));
        }

        @Test
        void emitsMaltreatmentByAfflictedMaleficRuler() {
                NatalChart chart = chartWithPositions(position(Planet.SUN, 0.0), position(Planet.MOON, 60.0), position(Planet.MERCURY, 90.0), position(Planet.VENUS, 180.0), position(Planet.MARS, 15.0, true, Angularity.CADENT), position(Planet.JUPITER, 240.0), position(Planet.SATURN, 120.0));
                chart.setPairwiseRelations(List.of());
                Map<PointKey, PointEntry> points = new LinkedHashMap<>();
                points.put(PointKey.MARS, planetPoint(Planet.MARS, SolarCondition.COMBUST));
                chart.setPoints(points);

                BeneficMaleficAssessmentEntry entry = calculator.calculate(chart).get(Planet.SUN).stream().filter(candidate -> candidate.condition() == BeneficMaleficCondition.BY_RULERSHIP).findFirst().orElseThrow();

                assertEquals(ConditionAssessment.MALTREATMENT, entry.assessment());
                assertEquals(Planet.MARS, entry.agent());
                assertEquals(List.of(RulerAffliction.RETROGRADE, RulerAffliction.CADENT, RulerAffliction.COMBUST), entry.rulerAfflictions());
        }

        @Test
        void doesNotEmitMaltreatmentBySelfRulership() {
                NatalChart chart = chartWithPositions(position(Planet.SUN, 60.0), position(Planet.MOON, 90.0), position(Planet.MERCURY, 120.0), position(Planet.VENUS, 180.0), position(Planet.MARS, 15.0, true, Angularity.CADENT), position(Planet.JUPITER, 240.0), position(Planet.SATURN, 300.0));
                chart.setPairwiseRelations(List.of());
                Map<PointKey, PointEntry> points = new LinkedHashMap<>();
                points.put(PointKey.MARS, planetPoint(Planet.MARS, SolarCondition.COMBUST));
                chart.setPoints(points);

                assertFalse(calculator.calculate(chart).get(Planet.MARS).stream().anyMatch(candidate -> candidate.condition() == BeneficMaleficCondition.BY_RULERSHIP));
        }

        private NatalChart chartWithPositions(PlanetPosition... positions) {
                NatalChart chart = new NatalChart();
                chart.setSect(new BasicSect(Sect.NOCTURNAL, Planet.MOON, Planet.SUN, Planet.VENUS, Planet.JUPITER, Planet.MARS, Planet.SATURN, false, true, -1.0, 1.0, Map.of(Planet.VENUS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.OF_SECT), Planet.JUPITER,
                                new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT), Planet.MARS, new PlanetSectInfo(Sect.NOCTURNAL, SectCondition.OF_SECT), Planet.SATURN, new PlanetSectInfo(Sect.DIURNAL, SectCondition.CONTRARY_TO_SECT))));
                chart.setPlanets(List.of(positions));
                return chart;
        }

        private PlanetPosition position(Planet planet, ZodiacSign sign) {
                return position(planet, sign.ordinal() * 30.0);
        }

        private PlanetPosition position(Planet planet, double longitude) {
                return position(planet, longitude, false, Angularity.SUCCEDENT);
        }

        private PlanetPosition position(Planet planet, double longitude, boolean retrograde, Angularity angularity) {
                double normalized = AstroMath.normalize(longitude);
                return new PlanetPosition(planet, normalized, AstroMath.signOf(normalized), AstroMath.degreeInSign(normalized), 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, retrograde, 1, 1, 1, angularity, null, 0.0, 0.0, 0.0);
        }

        private PlanetPointEntry planetPoint(Planet planet, SolarCondition solarCondition) {
                return new PlanetPointEntry(0.0, ZodiacSign.ARIES, Element.FIRE, 0.0, 0.0, 0.0, 0.0, 0.0, false, 1.0, 1.0, 1.0, false, 1, 1, 1, Angularity.CADENT, 0.0, 0.0, null, null, null, null, null, null, null, null, null, List.of(), List.of(), SolarOrientation.ORIENTAL, SectCondition.OF_SECT,
                                solarCondition, List.of(), false, PointType.PLANET);
        }

        private PairwiseRelation relation(Planet a, Planet b, AspectType aspect, int signDistance) {
                return new PairwiseRelation(PointKey.of(a), PointKey.of(b), null, new PairwiseRelation.AspectBySign(aspect, signDistance), null, List.of());
        }
}
