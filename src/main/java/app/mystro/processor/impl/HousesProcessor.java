package app.mystro.processor.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeChart;
import app.mystro.processor.MystroProcessor;

public final class HousesProcessor extends MystroProcessor {

    @Override
    public void populate(NativeReportBuilder builder) {
        NativeChart chart = builder.nativeChart();
        if (chart == null) {
            throw new IllegalArgumentException("Chart must be populated before houses");
        }

        Map<String, ChartPoint> houses = new LinkedHashMap<>();
        houses.put("AC", copyAsHouse(chart.point("Asc"), "AC", 1));
        houses.put("MC", copyAsHouse(chart.point("MC"), "MC", 10));
        for (int house = 1; house <= 12; house++) {
            double lon = chart.cusps()[house];
            houses.put(String.valueOf(house), new ChartPoint(String.valueOf(house), signOf(lon), signLon(lon), normalize(lon), house, 0.0, false));
        }
        builder.houses(houses);
    }

    private ChartPoint copyAsHouse(ChartPoint point, String id, int house) {
        if (point == null) {
            throw new IllegalArgumentException("Missing chart point for house section: " + id);
        }
        return new ChartPoint(id, point.sign(), point.signLon(), point.absoluteLon(), house, 0.0, false);
    }
}
