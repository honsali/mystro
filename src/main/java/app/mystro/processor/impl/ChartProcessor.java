package app.mystro.processor.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeBirth;
import app.common.model.NativeChart;
import app.mystro.processor.MystroProcessor;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;

public final class ChartProcessor extends MystroProcessor {
    private static final Pattern CHIRON_TEXTAREA_PATTERN = Pattern.compile("(?m)^Chiron,([A-Za-z]+),(\\d+)&deg;(\\d+)");
    private String ascSign;
    private SwissEph swissEph;

    @Override
    public void populate(NativeReportBuilder builder) {


        NativeBirth birth = builder.birth();
        swissEph = builder.swissEph();

        OffsetDateTime localDateTime = parseBirthDateTime(birth);
        OffsetDateTime utcDateTime = localDateTime.withOffsetSameInstant(ZoneOffset.UTC);
        double hourUt = utcDateTime.getHour() + utcDateTime.getMinute() / 60.0 + utcDateTime.getSecond() / 3600.0;
        double jd = swissEph.swe_julday(utcDateTime.getYear(), utcDateTime.getMonthValue(), utcDateTime.getDayOfMonth(), hourUt, SweConst.SE_GREG_CAL);

        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        int hsys = mapHouseSystem(birth.houseSystem());
        swissEph.swe_houses_ex(jd, 0, birth.latitude(), birth.longitude(), hsys, cusps, ascmc);
        ascSign = signOf(ascmc[0]);
        Map<String, ChartPoint> points = new LinkedHashMap<>();
        points.put("Asc", getPoint("Asc", ascmc[0], 0.0));
        points.put("MC", getPoint("MC", ascmc[1], 0.0));
        points.put("Desc", getPoint("Desc", normalize(ascmc[0] + 180.0), 0.0));
        points.put("IC", getPoint("IC", normalize(ascmc[1] + 180.0), 0.0));

        addPlanet(points, "Sun", SweConst.SE_SUN, jd);
        addPlanet(points, "Moon", SweConst.SE_MOON, jd);
        addPlanet(points, "Mercury", SweConst.SE_MERCURY, jd);
        addPlanet(points, "Venus", SweConst.SE_VENUS, jd);
        addPlanet(points, "Mars", SweConst.SE_MARS, jd);
        addPlanet(points, "Jupiter", SweConst.SE_JUPITER, jd);
        addPlanet(points, "Saturn", SweConst.SE_SATURN, jd);
        addPlanet(points, "Uranus", SweConst.SE_URANUS, jd);
        addPlanet(points, "Neptune", SweConst.SE_NEPTUNE, jd);
        addPlanet(points, "Pluto", SweConst.SE_PLUTO, jd);
        addPlanet(points, "North Node", SweConst.SE_MEAN_NODE, jd);
        addPlanet(points, "Lilith", SweConst.SE_MEAN_APOG, jd);
        addPlanet(points, "Chiron", SweConst.SE_CHIRON, jd);
        addChironFallback(points, builder);

        ChartPoint northNode = points.get("North Node");
        points.put("South Node", calculateSouthNode(northNode, signOf(ascmc[0])));


        NativeChart chart = new NativeChart();
        chart.setBirthDateTime(localDateTime);
        chart.setJulianDayUt(jd);
        chart.setDiurnal(isDiurnal(points, ascmc[0]));
        chart.setAscSign(signOf(ascmc[0]));
        chart.setAscSignLon(signLon(ascmc[0]));
        chart.setMcSign(signOf(ascmc[1]));
        chart.setMcSignLon(signLon(ascmc[1]));
        chart.setPoints(points);
        chart.setCusps(cusps);
        chart.setAscmc(ascmc);
        builder.nativeChart(chart);
    }


    private boolean isDiurnal(Map<String, ChartPoint> points, double ascLon) {
        double sunLon = points.get("Sun").absoluteLon();
        double sunArcFromAsc = normalize(sunLon - ascLon);
        return sunArcFromAsc > 180.0;
    }

    private void addPlanet(Map<String, ChartPoint> points, String id, int seId, double jd) {
        ChartPoint point = getPlanet(id, seId, jd);
        if (point != null) {
            points.put(id, point);
        }
    }

    private ChartPoint getPlanet(String id, int seId, double jd) {
        double[] xx = new double[6];
        StringBuilder err = new StringBuilder();
        int result = swissEph.swe_calc_ut(jd, seId, SweConst.SEFLG_SPEED, xx, err);
        if (result < 0 || Double.isNaN(xx[0]) || (xx[0] == 0.0 && err.length() > 0)) {
            return null;
        }
        double lon = xx[0];
        double speed = xx[3];
        boolean retrograde = id.equals("Lilith") || xx[3] < 0;
        int house = signHouse(ascSign, signOf(lon));
        return new ChartPoint(id, signOf(lon), signLon(lon), lon, house, speed, retrograde);
    }

    private ChartPoint getPoint(String id, double lon, double speed) {
        int house = signHouse(ascSign, signOf(lon));
        return new ChartPoint(id, signOf(lon), signLon(lon), normalize(lon), house, speed, false);
    }

    private void addChironFallback(Map<String, ChartPoint> points, NativeReportBuilder builder) {
        if (points.containsKey("Chiron")) {
            return;
        }
        try {
            var htmlPath = Config.ASTROSEEK_HTML_DIR.resolve(builder.name() + ".html");
            if (!Files.exists(htmlPath)) {
                return;
            }
            String html = Files.readString(htmlPath);
            Matcher matcher = CHIRON_TEXTAREA_PATTERN.matcher(html);
            if (!matcher.find()) {
                return;
            }
            String sign = matcher.group(1).trim();
            double signLon = Integer.parseInt(matcher.group(2)) + Integer.parseInt(matcher.group(3)) / 60.0;
            double lon = Config.SIGNS.indexOf(sign) * 30.0 + signLon;
            int house = signHouse(ascSign, sign);
            points.put("Chiron", new ChartPoint("Chiron", sign, signLon, lon, house, 0.0, true));
            app.common.Logger.getInstance().error("CHIRON_HTML_FALLBACK", builder.name(), "Chiron was populated from Astro-Seek HTML fallback");
        } catch (IOException ignored) {
        }
    }

    private ChartPoint calculateSouthNode(ChartPoint northNode, String ascSign) {
        double lon = normalize(northNode.absoluteLon() + 180.0);
        double speed = -northNode.speed();
        boolean retrograde = !northNode.retrograde();
        int house = signHouse(ascSign, signOf(lon));
        return new ChartPoint("South Node", signOf(lon), signLon(lon), normalize(lon), house, speed, retrograde);
    }

    private OffsetDateTime parseBirthDateTime(NativeBirth birth) {
        String[] date = birth.birthDate().split("/");
        LocalDate localDate = LocalDate.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]));
        LocalTime localTime = LocalTime.parse(birth.birthTime() + ":00");
        return OffsetDateTime.of(LocalDateTime.of(localDate, localTime), ZoneOffset.of(birth.utcOffset()));
    }

    private int mapHouseSystem(String houseSystem) {
        return switch (houseSystem) {
            case "Placidus" -> 'P';
            case "Koch" -> 'K';
            case "Porphyrius" -> 'O';
            case "Regiomontanus" -> 'R';
            case "Campanus" -> 'C';
            case "Equal" -> 'A';
            case "Whole Sign", "Whole Horizon" -> 'W';
            case "Alcabitus" -> 'B';
            case "Morinus" -> 'M';
            default -> 'W';
        };
    }

}
