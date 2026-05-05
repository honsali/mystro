package app.descriptive.valens.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.AstroMath;
import app.basic.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.descriptive.common.data.LotName;
import app.descriptive.common.model.LotEntry;

public final class ValensLotCalculator {
    public Map<LotName, LotEntry> calculate(NatalChart chart) {
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        PlanetPosition sun = chart.requirePlanet(Planet.SUN);
        PlanetPosition moon = chart.requirePlanet(Planet.MOON);
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;

        Map<LotName, LotEntry> lots = new LinkedHashMap<>();
        double fortune = diurnal
                ? AstroMath.normalize(asc + moon.getLongitude() - sun.getLongitude())
                : AstroMath.normalize(asc + sun.getLongitude() - moon.getLongitude());
        double spirit = diurnal
                ? AstroMath.normalize(asc + sun.getLongitude() - moon.getLongitude())
                : AstroMath.normalize(asc + moon.getLongitude() - sun.getLongitude());
        lots.put(LotName.FORTUNE, lot(LotName.FORTUNE, fortune, chart, diurnal ? "ASC + Moon - Sun" : "ASC + Sun - Moon"));
        lots.put(LotName.SPIRIT, lot(LotName.SPIRIT, spirit, chart, diurnal ? "ASC + Sun - Moon" : "ASC + Moon - Sun"));
        return lots;
    }

    private LotEntry lot(LotName name, double longitude, NatalChart chart, String formula) {
        return new LotEntry(
                name,
                longitude,
                AstroMath.signOf(longitude),
                AstroMath.degreeInSign(longitude),
                houseOf(longitude, chart),
                TraditionalTables.domicileRuler(AstroMath.signOf(longitude)),
                formula
        );
    }

    private int houseOf(double longitude, NatalChart chart) {
        double ascendant = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        int ascSign = AstroMath.signOf(ascendant).ordinal();
        int sign = AstroMath.signOf(longitude).ordinal();
        return Math.floorMod(sign - ascSign, 12) + 1;
    }

}
