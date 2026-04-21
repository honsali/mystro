package app.astroseek.parser.impl;

import java.util.regex.Matcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativeHermeticLot;

public final class AstroSeekHermeticLotParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element lotsTable = document.select("table")//
                .stream()//
                .filter(element -> element.text().contains("The Lot of (F)ortune") && element.text().contains("The Lot of Spirit"))//
                .findFirst()//
                .orElseThrow(() -> new IllegalArgumentException("Hermetic lots table not found"));

        boolean found = false;
        for (Element row : lotsTable.select("tr")) {
            Elements cells = row.select("td");
            if (cells.size() < 8) {
                continue;
            }
            Element strong = cells.get(0).selectFirst("strong");
            if (strong == null || !strong.text().startsWith("The Lot of")) {
                continue;
            }
            found = true;

            NativeHermeticLot lot = new NativeHermeticLot();
            lot.setLot(strong.text().replace("The Lot of ", "").replace("(F)", "F"));
            lot.setFormula(parseFormula(cells.get(0)));
            lot.setSign(parseSignFromCell(cells.get(2)));
            lot.setSignLon(parseDegrees(cells.get(2).text()));
            lot.setHouse(Integer.parseInt(cells.get(3).text().trim()));
            lot.setRuler(AstroSeekParserUtil.parsePlanetFromImage(cells.get(4).selectFirst("img").attr("src")));
            lot.setRulerSign(parseSignFromCell(cells.get(4)));
            lot.setRulerSignLon(parseDegrees(cells.get(4).text()));
            lot.setRulerHouse(Integer.parseInt(cells.get(5).text().trim()));
            lot.setLotToRuler(cells.get(6).text().trim());
            lot.setFortuneToRuler(cells.get(7).text().trim());
            builder.lot(lot);
        }
        if (!found) {
            throw new IllegalArgumentException("No Hermetic lots rows parsed");
        }
    }

    private String parseFormula(Element cell) {
        Element link = cell.selectFirst("a.tenky");
        if (link == null) {
            return null;
        }
        String formula = link.text().trim();
        if (formula.isEmpty()) {
            return null;
        }
        return formula.replace("Lot of ", "").replace("The Lot of ", "").replace(" to ", " → ");
    }

    private String parseSignFromCell(Element cell) {
        Elements images = cell.select("img");
        if (images.isEmpty()) {
            throw new IllegalArgumentException("Sign icon not found in cell: " + cell.text());
        }
        return AstroSeekParserUtil.parseSignFromImage(images.get(images.size() - 1).attr("src"));
    }

    private double parseDegrees(String text) {
        Matcher matcher = Config.DEGREE_MINUTE_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not parse degrees from: " + text);
        }
        return Integer.parseInt(matcher.group(1)) + Integer.parseInt(matcher.group(2)) / 60.0;
    }
}
