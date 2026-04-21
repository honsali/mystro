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

public final class AstroSeekDerivedChartsParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element header = document.select("div.rozbor-hlavicka-left")
                .stream()
                .filter(element -> element.text().contains("Dodecatemoria, Novenaria, Antiscia"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Astro-Seek derived charts section not found"));
        Element items = header.parent().parent().selectFirst("div.detail-rozbor-items");
        if (items == null) {
            throw new IllegalArgumentException("Astro-Seek derived charts items not found");
        }
        Elements tables = items.select("table");
        if (tables.size() < 2) {
            throw new IllegalArgumentException("Astro-Seek derived charts tables not found");
        }

        parseDodecatemoriaAndNovenaria(tables.get(0), builder);
        parseAntiscia(tables.get(1), builder);
    }

    private void parseDodecatemoriaAndNovenaria(Element table, NativeReportBuilder builder) {
        Map<String, ChartPoint> dodecatemoria = new LinkedHashMap<>();
        Map<String, ChartPoint> novenaria = new LinkedHashMap<>();
        for (Element row : table.select("tr")) {
            Elements cells = row.select("td");
            if (cells.size() != 3 || cells.get(0).text().equals("Open:")) {
                continue;
            }
            String id = normalizeId(cells.get(0));
            dodecatemoria.put(id, parsePoint(id, cells.get(1)));
            novenaria.put(id, parsePoint(id, cells.get(2)));
        }
        builder.dodecatemoria(dodecatemoria);
        builder.novenaria(novenaria);
    }

    private void parseAntiscia(Element table, NativeReportBuilder builder) {
        Map<String, ChartPoint> antiscia = new LinkedHashMap<>();
        Map<String, ChartPoint> contra = new LinkedHashMap<>();
        for (Element row : table.select("tr")) {
            Elements cells = row.select("td");
            if (cells.size() != 3 || cells.get(0).text().contains("Transits to:")) {
                continue;
            }
            String id = normalizeId(cells.get(0));
            antiscia.put(id, parsePoint(id, cells.get(1)));
            contra.put(id, parsePoint(id, cells.get(2)));
        }
        builder.antiscia(antiscia);
        builder.contraAntiscia(contra);
    }

    private ChartPoint parsePoint(String id, Element cell) {
        Element image = cell.selectFirst("img");
        if (image == null) {
            throw new IllegalArgumentException("Derived chart sign image not found for " + id);
        }
        String sign = AstroSeekParserUtil.parseSignFromImage(image.attr("src"));
        double signLon = AstroSeekParserUtil.parseDegrees(cell.text());
        return new ChartPoint(id, sign, signLon, AstroSeekParserUtil.absoluteLon(sign, signLon), 0, 0.0, false);
    }

    private String normalizeId(String text) {
        String normalized = text.replace('\u00a0', ' ').trim();
        return switch (normalized) {
            case "ASC" -> "AC";
            default -> normalized;
        };
    }

    private String normalizeId(Element cell) {
        String text = normalizeId(cell.text());
        if (!text.isBlank()) {
            return text;
        }
        Element image = cell.selectFirst("img");
        if (image == null) {
            return text;
        }
        return normalizeId(image.attr("alt"));
    }
}
