package app.astroseek.parser.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativePlanetaryHour;

public final class AstroSeekPlanetaryHourParser implements AstroSeekParser {

    private static final Pattern MINUTES_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*min");

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element table = document.select("table")//
                .stream()//
                .filter(element -> element.text().contains("Planetary Hours"))//
                .findFirst()//
                .orElseThrow(() -> new IllegalArgumentException("Planetary Hours table not found"));

        Element summaryDivider = table.selectFirst("div.cl.p15");
        if (summaryDivider == null) {
            throw new IllegalArgumentException("Planetary hour summary divider div.cl.p15 not found");
        }
        Element summaryCell = summaryDivider.parent();
        String summary = parseSummary(summaryDivider);
        Matcher matcher = Config.PLANETARY_HOUR_PATTERN.matcher(summary);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Planetary hour summary not found: " + summary);
        }
        Elements icons = summaryCell.select("img");
        if (icons.size() < 2) {
            throw new IllegalArgumentException("Planetary hour ruler icons not found");
        }
        Element transitionCell = table.select("tr").get(2).selectFirst("td");
        if (transitionCell == null) {
            throw new IllegalArgumentException("Planetary hour transition cell not found");
        }
        Elements minuteSpans = transitionCell.select("span.form-info");
        if (minuteSpans.size() < 2) {
            throw new IllegalArgumentException("Planetary hour last/next minute spans not found");
        }

        NativePlanetaryHour planetaryHour = new NativePlanetaryHour();
        planetaryHour.setSummary(summary);
        planetaryHour.setHourNumber(Integer.parseInt(matcher.group(1)));
        planetaryHour.setDayRuler(AstroSeekParserUtil.parsePlanetFromImage(icons.get(0).attr("src")));
        planetaryHour.setHourRuler(AstroSeekParserUtil.parsePlanetFromImage(icons.get(1).attr("src")));
        planetaryHour.setLast(parseMinutes(minuteSpans.get(0)));
        planetaryHour.setNext(parseMinutes(minuteSpans.get(1)));
        builder.planetaryHour(planetaryHour);
    }

    private String parseSummary(Element summaryDivider) {
        for (Node sibling = summaryDivider.nextSibling(); sibling != null; sibling = sibling.nextSibling()) {
            String text = sibling.toString().replace("\u00a0", " ").trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        throw new IllegalArgumentException("Planetary hour summary text not found after div.cl.p15");
    }

    private double parseMinutes(Element info) {
        Matcher matcher = MINUTES_PATTERN.matcher(info.text().replace("mins", "min"));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Planetary hour minutes malformed: " + info.text());
        }
        return Double.parseDouble(matcher.group(1));
    }
}
