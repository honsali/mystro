package app.mystro.processor.impl;

import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativeBirth;
import app.common.model.NativeChart;
import app.common.model.NativePlanetaryHour;
import app.mystro.processor.MystroProcessor;
import app.swisseph.core.DblObj;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;

public final class PlanetaryHourProcessor extends MystroProcessor {


    private SwissEph swissEph;

    public void populate(NativeReportBuilder builder) {

        NativeChart chart = builder.nativeChart();
        NativeBirth birth = builder.birth();
        swissEph = builder.swissEph();

        double[] geopos = {birth.longitude(), birth.latitude(), 0.0};
        double lastSunrise = nextTransit(chart.julianDayUt() - 1.0, SweConst.SE_CALC_RISE, geopos);
        double middleSunset = nextTransit(lastSunrise, SweConst.SE_CALC_SET, geopos);
        double nextSunrise = nextTransit(chart.julianDayUt(), SweConst.SE_CALC_RISE, geopos);
        int weekday = dayOfWeekIndex(fromJulian(swissEph, lastSunrise, chart.birthDateTime().getOffset()).getDayOfWeek().getValue());

        double[][] table = new double[24][2];
        String[] rulers = new String[24];
        double dayLength = (middleSunset - lastSunrise) / 12.0;
        for (int i = 0; i < 12; i++) {
            table[i][0] = lastSunrise + i * dayLength;
            table[i][1] = table[i][0] + dayLength;
            rulers[i] = nthHourRuler(i, weekday);
        }
        double nightLength = (nextSunrise - middleSunset) / 12.0;
        for (int i = 0; i < 12; i++) {
            table[i + 12][0] = middleSunset + i * nightLength;
            table[i + 12][1] = table[i + 12][0] + nightLength;
            rulers[i + 12] = nthHourRuler(i + 12, weekday);
        }
        int index = 0;
        for (int i = 0; i < 24; i++) {
            if (chart.julianDayUt() >= table[i][0] && chart.julianDayUt() <= table[i][1]) {
                index = i;
                break;
            }
        }
        int hourNumber = index + 1;

        NativePlanetaryHour planetaryHour = new NativePlanetaryHour();
        planetaryHour.setSummary(hourNumber + ordinalSuffix(hourNumber) + " hour of Day");
        planetaryHour.setHourNumber(hourNumber);
        planetaryHour.setDayRuler(rulers[0]);
        planetaryHour.setNightRuler(rulers[12]);
        planetaryHour.setHourRuler(rulers[index]);
        planetaryHour.setLast((table[index][0] - chart.julianDayUt()) * 24.0 * 60.0);
        planetaryHour.setNext((table[index][1] - chart.julianDayUt()) * 24.0 * 60.0);
        builder.planetaryHour(planetaryHour);
    }

    public double nextTransit(double jd, int rsmi, double[] geopos) {
        DblObj tret = new DblObj();
        swissEph.swe_rise_trans(jd, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH, rsmi, geopos, 0.0, 0.0, tret, new StringBuilder());
        return tret.val;
    }


    private String nthHourRuler(int n, int weekday) {
        int index = Math.floorMod(weekday * 24 + n, 7);
        return Config.PLANETARY_HOUR_SEQUENCE.get(index);
    }


    private int dayOfWeekIndex(int isoDayOfWeek) {
        return isoDayOfWeek % 7;
    }


}
