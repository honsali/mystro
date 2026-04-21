package app.astroseek.parser.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;

public final class AstroSeekHousesParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element container = document.getElementById("vypocty_id_nativ");
        if (container == null) {
            throw new IllegalArgumentException("Astro-Seek native sidebar container not found");
        }
        Elements children = container.children();
        int headerIndex = findChildIndexContaining(children, "Houses:");
        Map<String, ChartPoint> houses = new LinkedHashMap<>();

        int i = nextDataIndex(children, headerIndex + 1);
        houses.put("AC", parseHouse(children, i, "AC", 1));
        houses.put("MC", parseHouse(children, i + 3, "MC", 10));
        i += 6;

        for (int pair = 0; pair < 6; pair++) {
            i = nextDataIndex(children, i);
            String leftLabel = normalizeLabel(children.get(i).text());
            String rightLabel = normalizeLabel(children.get(i + 3).text());
            houses.put(leftLabel, parseHouse(children, i, leftLabel, Integer.parseInt(leftLabel)));
            houses.put(rightLabel, parseHouse(children, i + 3, rightLabel, Integer.parseInt(rightLabel)));
            i += 6;
        }

        builder.houses(houses);
    }

    private int findChildIndexContaining(Elements children, String text) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).text().contains(text) && children.get(i).text().contains("system")) {
                return i;
            }
        }
        throw new IllegalArgumentException("Astro-Seek houses section not found");
    }

    private int nextDataIndex(Elements children, int start) {
        int i = start;
        while (i < children.size()) {
            Element child = children.get(i);
            if ("div".equals(child.tagName()) && !child.classNames().contains("cl") && child.selectFirst("img") == null && !child.text().isBlank()) {
                return i;
            }
            i++;
        }
        throw new IllegalArgumentException("Unexpected end of Astro-Seek houses section");
    }

    private ChartPoint parseHouse(Elements children, int labelIndex, String id, int houseNumber) {
        Element signCell = children.get(labelIndex + 1);
        Element degreeCell = children.get(labelIndex + 2);
        String sign = AstroSeekParserUtil.parseSignFromImage(requireImage(signCell).attr("src"));
        double signLon = AstroSeekParserUtil.parseDegrees(degreeCell.text());
        return new ChartPoint(id, sign, signLon, AstroSeekParserUtil.absoluteLon(sign, signLon), houseNumber, 0.0, false);
    }

    private Element requireImage(Element cell) {
        Element image = cell.selectFirst("img");
        if (image == null) {
            throw new IllegalArgumentException("Astro-Seek house sign icon not found");
        }
        return image;
    }

    private String normalizeLabel(String text) {
        return text.replace(":", "").trim();
    }
}
