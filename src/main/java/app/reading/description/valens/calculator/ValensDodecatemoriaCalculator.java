package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import app.chart.AstroMath;
import app.chart.TraditionalTables;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.reading.description.common.model.DodecatemoriaEntry;
import app.reading.description.common.model.LotEntry;

public final class ValensDodecatemoriaCalculator {
    private static final double TWELFTH_PART_SIZE = 2.5;
    private static final String FORMULA = "Dodecatemoria: sign start + 12 × degree within sign";

    public List<DodecatemoriaEntry> calculate(Chart chart) {
        List<DodecatemoriaEntry> entries = new ArrayList<>();
        addPointEntries(entries, chart);
        addLotEntries(entries, chart);
        return List.copyOf(entries);
    }

    private void addPointEntries(List<DodecatemoriaEntry> entries, Chart chart) {
        if (chart.getPoints() == null) {
            return;
        }
        for (Map.Entry<PointKey, PointEntry> point : chart.getPoints().entrySet()) {
            if (isSkippedPoint(point.getValue())) {
                continue;
            }
            entries.add(entry("points." + point.getKey().name(), longitude(point.getValue())));
        }
    }

    private boolean isSkippedPoint(PointEntry point) {
        return point instanceof PlanetPointEntry planetPoint && planetPoint.getType() == PointType.NODE;
    }

    private double longitude(PointEntry point) {
        if (point instanceof PlanetPointEntry planetPoint) {
            return planetPoint.longitude();
        }
        if (point instanceof AnglePointEntry anglePoint) {
            return anglePoint.longitude();
        }
        throw new IllegalArgumentException("Unsupported point entry " + point.getClass().getName());
    }

    private void addLotEntries(List<DodecatemoriaEntry> entries, Chart chart) {
        if (chart.getLots() == null) {
            return;
        }
        for (LotEntry lot : chart.getLots()) {
            entries.add(entry("lots.name=" + lot.name(), lot.longitude()));
        }
    }

    private DodecatemoriaEntry entry(String sourceRef, double sourceLongitude) {
        double normalizedSource = AstroMath.normalize(sourceLongitude);
        ZodiacSign sourceSign = AstroMath.signOf(normalizedSource);
        double sourceDegreeInSign = AstroMath.degreeInSign(normalizedSource);
        double longitude = AstroMath.normalize(sourceSign.ordinal() * 30.0 + sourceDegreeInSign * 12.0);
        ZodiacSign sign = AstroMath.signOf(longitude);
        return new DodecatemoriaEntry(sourceRef, (int) Math.floor(sourceDegreeInSign / TWELFTH_PART_SIZE) + 1, longitude, sign, AstroMath.degreeInSign(longitude), TraditionalTables.domicileRuler(sign), FORMULA);
    }
}
