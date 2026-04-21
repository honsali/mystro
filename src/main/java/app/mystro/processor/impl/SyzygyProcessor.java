package app.mystro.processor.impl;

import java.time.OffsetDateTime;
import app.common.NativeReportBuilder;
import app.common.model.NativeChart;
import app.common.model.NativeSyzygy;
import app.mystro.processor.MystroProcessor;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;
import app.swisseph.core.TCPlanetPlanet;
import app.swisseph.core.TransitCalculator;

public final class SyzygyProcessor extends MystroProcessor {

    private SwissEph swissEph;

    @Override
    public void populate(NativeReportBuilder builder) {

        NativeChart chart = builder.nativeChart();
        swissEph = builder.swissEph();

        double previousNewMoonJd = previousNewMoon(chart.julianDayUt());
        double previousFullMoonJd = previousFullMoon(chart.julianDayUt());
        boolean useFullMoon = previousFullMoonJd > previousNewMoonJd;
        String phase = useFullMoon ? "Full Moon" : "New Moon";
        double syzygyJd = useFullMoon ? previousFullMoonJd : previousNewMoonJd;
        OffsetDateTime syzygyDateTime = fromJulian(swissEph, syzygyJd, chart.birthDateTime().getOffset());

        NativeSyzygy syzygy = new NativeSyzygy();
        syzygy.setPhase(phase);
        syzygy.setSyzygyDateTime(syzygyDateTime);
        builder.syzygy(syzygy);
    }

    public double previousNewMoon(double jdUt) {
        TransitCalculator calculator = new TCPlanetPlanet(swissEph, SweConst.SE_MOON, SweConst.SE_SUN, SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE, 0.0);
        return TransitCalculator.getTransitUT(calculator, jdUt, true);
    }

    public double previousFullMoon(double jdUt) {
        TransitCalculator calculator = new TCPlanetPlanet(swissEph, SweConst.SE_MOON, SweConst.SE_SUN, SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE, 180.0);
        return TransitCalculator.getTransitUT(calculator, jdUt, true);
    }
}
