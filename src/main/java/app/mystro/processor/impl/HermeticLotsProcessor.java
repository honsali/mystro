package app.mystro.processor.impl;

import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeChart;
import app.common.model.NativeHermeticLot;
import app.mystro.processor.MystroProcessor;

public final class HermeticLotsProcessor extends MystroProcessor {

        @Override
        public void populate(NativeReportBuilder builder) {
                NativeChart chart = builder.nativeChart();

                boolean diurnal = chart.diurnal();
                ChartPoint fortune = diurnal ? projectLot("Fortune", chart, chart.point("Moon"), chart.point("Sun")) : projectLot("Fortune", chart, chart.point("Sun"), chart.point("Moon"));
                ChartPoint spirit = diurnal ? projectLot("Spirit", chart, chart.point("Sun"), chart.point("Moon")) : projectLot("Spirit", chart, chart.point("Moon"), chart.point("Sun"));

                builder.lot(buildLot("Fortune", diurnal ? "Sun → Moon" : "Moon → Sun", fortune, fortune, chart));
                builder.lot(buildLot("Spirit", diurnal ? "Moon → Sun" : "Sun → Moon", spirit, fortune, chart));
                builder.lot(buildLot("Eros", diurnal ? "Spirit → Venus" : "Venus → Spirit", diurnal ? chart.point("Venus") : spirit, diurnal ? spirit : chart.point("Venus"), fortune, chart));
                builder.lot(buildLot("Victory", diurnal ? "Spirit → Jupiter" : "Jupiter → Spirit", diurnal ? chart.point("Jupiter") : spirit, diurnal ? spirit : chart.point("Jupiter"), fortune, chart));
                builder.lot(buildLot("Necessity", diurnal ? "Mercury → Fortune" : "Fortune → Mercury", diurnal ? fortune : chart.point("Mercury"), diurnal ? chart.point("Mercury") : fortune, fortune, chart));
                builder.lot(buildLot("Courage", diurnal ? "Mars → Fortune" : "Fortune → Mars", diurnal ? fortune : chart.point("Mars"), diurnal ? chart.point("Mars") : fortune, fortune, chart));
                builder.lot(buildLot("Nemesis", diurnal ? "Saturn → Fortune" : "Fortune → Saturn", diurnal ? fortune : chart.point("Saturn"), diurnal ? chart.point("Saturn") : fortune, fortune, chart));

        }

        private ChartPoint projectLot(String lotName, NativeChart chart, ChartPoint p1, ChartPoint p2) {
                double lon = normalize(chart.point("Asc").absoluteLon() + p1.absoluteLon() - p2.absoluteLon());
                String sign = signOf(lon);
                return new ChartPoint(lotName, sign, signLon(lon), lon, signHouse(chart.ascSign(), sign), 0.0, false);
        }

        private String rulerOfSign(String sign) {
                return Config.SIGN_RULERS.get(sign);
        }

        private NativeHermeticLot buildLot(String lotName, String formula, ChartPoint p1, ChartPoint p2, ChartPoint fortune, NativeChart chart) {
                ChartPoint lotPoint = projectLot(lotName, chart, p1, p2);
                return buildLot(lotName, formula, lotPoint, fortune, chart);
        }

        private String relation(String fromSign, String toSign) {
                int distance = Math.floorMod(Config.SIGNS.indexOf(toSign) - Config.SIGNS.indexOf(fromSign), 12);
                if (distance == 1 || distance == 5 || distance == 7 || distance == 11) {
                        return "A";
                }
                return (distance + 1) + ordinalSuffix(distance + 1);
        }

        private NativeHermeticLot buildLot(String lotName, String formula, ChartPoint lotPoint, ChartPoint fortune, NativeChart chart) {
                String ruler = rulerOfSign(lotPoint.sign());
                ChartPoint rulerPoint = chart.point(ruler);
                NativeHermeticLot lot = new NativeHermeticLot();
                lot.setLot(lotName);
                lot.setFormula(formula);
                lot.setSign(lotPoint.sign());
                lot.setSignLon(lotPoint.signLon());
                lot.setHouse(lotPoint.wholeSignHouse());
                lot.setRuler(ruler);
                lot.setRulerSign(rulerPoint.sign());
                lot.setRulerSignLon(rulerPoint.signLon());
                lot.setRulerHouse(rulerPoint.wholeSignHouse());
                lot.setLotToRuler(relation(lotPoint.sign(), rulerPoint.sign()));
                lot.setFortuneToRuler("Fortune".equals(lotName) ? "-" : relation(fortune.sign(), rulerPoint.sign()));
                return lot;
        }
}
