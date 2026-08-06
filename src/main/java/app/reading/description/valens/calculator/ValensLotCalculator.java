package app.reading.description.valens.calculator;

import java.util.List;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.model.Chart;
import app.chart.model.PlanetPosition;
import app.reading.description.common.calculator.DoctrineLotMath;
import app.reading.description.common.model.LotEntry;

public final class ValensLotCalculator {
        private final DoctrineLotMath lotMath = new DoctrineLotMath();

        public List<LotEntry> calculate(CalculationContext ctx, Chart chart) {
                double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
                PlanetPosition sun = chart.requirePlanet(Planet.SUN);
                PlanetPosition moon = chart.requirePlanet(Planet.MOON);
                boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;

                double fortune = diurnal ? lotMath.lot(asc, sun.getLongitude(), moon.getLongitude()) : lotMath.lot(asc, moon.getLongitude(), sun.getLongitude());
                double spirit = diurnal ? lotMath.lot(asc, moon.getLongitude(), sun.getLongitude()) : lotMath.lot(asc, sun.getLongitude(), moon.getLongitude());
                PlanetPosition venus = chart.requirePlanet(Planet.VENUS);
                double eros = diurnal ? lotMath.lot(asc, fortune, spirit) : lotMath.lot(asc, spirit, fortune);
                double necessity = diurnal ? lotMath.lot(asc, spirit, fortune) : lotMath.lot(asc, fortune, spirit);
                double fortuneToSpiritArc = AstroMath.normalize(spirit - fortune);
                boolean basisUsesFortuneToSpirit = fortuneToSpiritArc <= 180.0;
                double basis = basisUsesFortuneToSpirit ? lotMath.lot(asc, fortune, spirit) : lotMath.lot(asc, spirit, fortune);
                String basisFormula = basisUsesFortuneToSpirit ? "Asc + shorter Fortune/Spirit arc (Fortune -> Spirit)" : "Asc + shorter Fortune/Spirit arc (Spirit -> Fortune)";
                PlanetPosition mars = chart.requirePlanet(Planet.MARS);
                PlanetPosition jupiter = chart.requirePlanet(Planet.JUPITER);
                PlanetPosition saturn = chart.requirePlanet(Planet.SATURN);
                double courage = diurnal ? lotMath.lot(asc, mars.getLongitude(), fortune) : lotMath.lot(asc, fortune, mars.getLongitude());
                double victory = diurnal ? lotMath.lot(asc, spirit, jupiter.getLongitude()) : lotMath.lot(asc, jupiter.getLongitude(), spirit);
                double nemesis = diurnal ? lotMath.lot(asc, saturn.getLongitude(), fortune) : lotMath.lot(asc, fortune, saturn.getLongitude());

                double wedding = diurnal ? lotMath.lot(asc, saturn.getLongitude(), venus.getLongitude()) : lotMath.lot(asc, venus.getLongitude(), saturn.getLongitude());
                double children = diurnal ? lotMath.lot(asc, jupiter.getLongitude(), saturn.getLongitude()) : lotMath.lot(asc, saturn.getLongitude(), jupiter.getLongitude());
                double father = diurnal ? lotMath.lot(asc, sun.getLongitude(), saturn.getLongitude()) : lotMath.lot(asc, saturn.getLongitude(), sun.getLongitude());
                double mother = diurnal ? lotMath.lot(asc, moon.getLongitude(), venus.getLongitude()) : lotMath.lot(asc, venus.getLongitude(), moon.getLongitude());
                double siblings = diurnal ? lotMath.lot(asc, saturn.getLongitude(), jupiter.getLongitude()) : lotMath.lot(asc, jupiter.getLongitude(), saturn.getLongitude());

                return List.of(lot("FORTUNE", "Fortune", "valens", fortune, ctx, chart, diurnal ? "Asc + (Sun -> Moon)" : "Asc + (Moon -> Sun)"), lot("SPIRIT", "Spirit", "valens", spirit, ctx, chart, diurnal ? "Asc + (Moon -> Sun)" : "Asc + (Sun -> Moon)"),
                                lot("EROS", "Eros", "valens", eros, ctx, chart, diurnal ? "Asc + (Fortune -> Spirit)" : "Asc + (Spirit -> Fortune)"), lot("NECESSITY", "Necessity", "valens", necessity, ctx, chart, diurnal ? "Asc + (Spirit -> Fortune)" : "Asc + (Fortune -> Spirit)"),
                                lot("BASIS", "Basis/Foundation", "valens", basis, ctx, chart, basisFormula),
                                lot("COURAGE", "Courage", "hermetic", courage, ctx, chart, diurnal ? "Asc + (Mars -> Fortune)" : "Asc + (Fortune -> Mars)"), lot("VICTORY", "Victory", "hermetic", victory, ctx, chart, diurnal ? "Asc + (Spirit -> Jupiter)" : "Asc + (Jupiter -> Spirit)"),
                                lot("NEMESIS", "Nemesis", "hermetic", nemesis, ctx, chart, diurnal ? "Asc + (Saturn -> Fortune)" : "Asc + (Fortune -> Saturn)"), lot("WEDDING", "Wedding", "dorothean", wedding, ctx, chart, diurnal ? "Asc + (Saturn -> Venus)" : "Asc + (Venus -> Saturn)"),
                                lot("CHILDREN", "Children", "dorothean", children, ctx, chart, diurnal ? "Asc + (Jupiter -> Saturn)" : "Asc + (Saturn -> Jupiter)"), lot("FATHER", "Father", "dorothean", father, ctx, chart, diurnal ? "Asc + (Sun -> Saturn)" : "Asc + (Saturn -> Sun)"),
                                lot("MOTHER", "Mother", "dorothean", mother, ctx, chart, diurnal ? "Asc + (Moon -> Venus)" : "Asc + (Venus -> Moon)"), lot("SIBLINGS", "Siblings", "dorothean", siblings, ctx, chart, diurnal ? "Asc + (Saturn -> Jupiter)" : "Asc + (Jupiter -> Saturn)"));
        }

        private LotEntry lot(String name, String displayName, String doctrine, double longitude, CalculationContext ctx, Chart chart, String formula) {
                return new LotEntry(name, displayName, doctrine, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), ctx.houseOf(longitude, chart.requireAngle(AngleType.ASCENDANT).getLongitude()), TraditionalTables.domicileRuler(AstroMath.signOf(longitude)), formula, List.of());
        }

}
