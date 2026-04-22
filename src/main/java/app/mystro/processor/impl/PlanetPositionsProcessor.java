package app.mystro.processor.impl;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeChart;
import app.common.model.NativeHermeticLot;
import app.common.model.NativeSyzygy;
import app.mystro.processor.MystroProcessor;
import app.swisseph.core.SweConst;

public final class PlanetPositionsProcessor extends MystroProcessor {

    @Override
    public void populate(NativeReportBuilder builder) {
        NativeChart chart = builder.nativeChart();
        if (chart == null) {
            throw new IllegalArgumentException("Chart must be populated before planet positions");
        }

        Map<String, ChartPoint> positions = new LinkedHashMap<>();
        copyPoint(chart, positions, "Sun", "Sun");
        copyPoint(chart, positions, "Moon", "Moon");
        copyPoint(chart, positions, "Mercury", "Mercury");
        copyPoint(chart, positions, "Venus", "Venus");
        copyPoint(chart, positions, "Mars", "Mars");
        copyPoint(chart, positions, "Jupiter", "Jupiter");
        copyPoint(chart, positions, "Saturn", "Saturn");
        copyOptionalPoint(chart, positions, "Uranus", "Uranus");
        copyOptionalPoint(chart, positions, "Neptune", "Neptune");
        copyOptionalPoint(chart, positions, "Pluto", "Pluto");
        copyPoint(chart, positions, "North Node", "Node");
        copyOptionalPoint(chart, positions, "Lilith", "Lilith");
        copyOptionalPoint(chart, positions, "Chiron", "Chiron");
        addLotPosition(builder, positions, "Fortune");
        addLotPosition(builder, positions, "Spirit");
        addSyzygyPosition(builder, positions, chart);
        builder.planets(positions);
    }

    private void copyPoint(NativeChart chart, Map<String, ChartPoint> positions, String sourceName, String targetName) {
        ChartPoint point = chart.point(sourceName);
        if (point == null) {
            throw new IllegalArgumentException("Missing chart point: " + sourceName);
        }
        positions.put(targetName, new ChartPoint(targetName, point.sign(), point.signLon(), point.absoluteLon(), point.wholeSignHouse(), point.speed(), point.retrograde()));
    }

    private void copyOptionalPoint(NativeChart chart, Map<String, ChartPoint> positions, String sourceName, String targetName) {
        ChartPoint point = chart.point(sourceName);
        if (point != null) {
            positions.put(targetName, new ChartPoint(targetName, point.sign(), point.signLon(), point.absoluteLon(), point.wholeSignHouse(), point.speed(), point.retrograde()));
        }
    }

    private void addLotPosition(NativeReportBuilder builder, Map<String, ChartPoint> positions, String lotName) {
        NativeHermeticLot lot = builder.lot(lotName);
        if (lot == null) {
            throw new IllegalArgumentException("Missing lot for planet positions: " + lotName);
        }
        double absoluteLon = app.common.Config.SIGNS.indexOf(lot.sign()) * 30.0 + lot.signLon();
        positions.put(lotName, new ChartPoint(lotName, lot.sign(), lot.signLon(), absoluteLon, lot.house(), 0.0, false));
    }

    private void addSyzygyPosition(NativeReportBuilder builder, Map<String, ChartPoint> positions, NativeChart chart) {
        NativeSyzygy syzygy = builder.syzygy();
        if (syzygy == null || syzygy.syzygyDateTime() == null) {
            throw new IllegalArgumentException("Syzygy must be populated before planet positions");
        }
        var utcDateTime = syzygy.syzygyDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        double hourUt = utcDateTime.getHour() + utcDateTime.getMinute() / 60.0 + utcDateTime.getSecond() / 3600.0;
        double jd = builder.swissEph().swe_julday(utcDateTime.getYear(), utcDateTime.getMonthValue(), utcDateTime.getDayOfMonth(), hourUt, SweConst.SE_GREG_CAL);
        int planetId = resolveSyzygyPlanetId(syzygy, chart);
        double[] xx = new double[6];
        builder.swissEph().swe_calc_ut(jd, planetId, SweConst.SEFLG_SPEED, xx, new StringBuilder());
        double lon = xx[0];
        positions.put("Syzygy", new ChartPoint("Syzygy", signOf(lon), signLon(lon), normalize(lon), signHouse(chart.ascSign(), signOf(lon)), xx[3], xx[3] < 0));
    }

    private int resolveSyzygyPlanetId(NativeSyzygy syzygy, NativeChart chart) {
        if ("Full Moon".equalsIgnoreCase(syzygy.phase())) {
            return chart.diurnal() ? SweConst.SE_SUN : SweConst.SE_MOON;
        }
        return SweConst.SE_SUN;
    }
}
