package app.descriptive.valens.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.TraditionalTables;
import app.basic.data.AngleType;
import app.basic.data.Planet;
import app.basic.data.Sect;
import app.basic.model.BasicChart;
import app.basic.model.PlanetPosition;
import app.descriptive.common.data.LotName;
import app.descriptive.common.model.LotEntry;

public final class ValensLotCalculator {
    public Map<LotName, LotEntry> calculate(BasicChart chart) {
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        PlanetPosition sun = chart.requirePlanet(Planet.SUN);
        PlanetPosition moon = chart.requirePlanet(Planet.MOON);
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;

        Map<LotName, LotEntry> lots = new LinkedHashMap<>();
        double fortune = diurnal
                ? TraditionalTables.normalize(asc + moon.getLongitude() - sun.getLongitude())
                : TraditionalTables.normalize(asc + sun.getLongitude() - moon.getLongitude());
        double spirit = diurnal
                ? TraditionalTables.normalize(asc + sun.getLongitude() - moon.getLongitude())
                : TraditionalTables.normalize(asc + moon.getLongitude() - sun.getLongitude());
        lots.put(LotName.FORTUNE, lot(LotName.FORTUNE, fortune, chart, diurnal ? "ASC + Moon - Sun" : "ASC + Sun - Moon"));
        lots.put(LotName.SPIRIT, lot(LotName.SPIRIT, spirit, chart, diurnal ? "ASC + Sun - Moon" : "ASC + Moon - Sun"));
        return lots;
    }

    private LotEntry lot(LotName name, double longitude, BasicChart chart, String formula) {
        return new LotEntry(
                name,
                longitude,
                TraditionalTables.signOf(longitude),
                TraditionalTables.degreeInSign(longitude),
                houseOf(longitude, chart),
                TraditionalTables.domicileRuler(TraditionalTables.signOf(longitude)),
                formula
        );
    }

    private int houseOf(double longitude, BasicChart chart) {
        double ascendant = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        int ascSign = TraditionalTables.signOf(ascendant).ordinal();
        int sign = TraditionalTables.signOf(longitude).ordinal();
        return Math.floorMod(sign - ascSign, 12) + 1;
    }

}
