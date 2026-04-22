package app.astroseek.parser.impl;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.NativeReportBuilder;
import app.common.model.NativeAnnualProfectionEntry;
import app.common.model.NativeAnnualProfections;

public final class AstroSeekAnnualProfectionsParser implements AstroSeekParser {
    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element table = document.select("table").stream()
                .filter(element -> element.text().contains("Annual Profections") && element.text().contains("Current 12-year period") && element.text().contains("Lord of Year") && element.text().contains("Lord of Orb"))
                .findFirst()
                .orElse(null);
        if (table == null) {
            return;
        }

        List<NativeAnnualProfectionEntry> years = new ArrayList<>();
        for (Element row : table.select("tr")) {
            Elements cells = row.select("> td");
            if (cells.size() < 8) {
                continue;
            }
            String ageText = cells.get(0).text().trim();
            if (!ageText.matches("\\d+")) {
                continue;
            }

            Elements lordOfYearImages = cells.get(2).select("img[src]");
            Elements lordOfOrbImages = cells.get(3).select("img[src]");
            if (lordOfYearImages.size() < 2 || lordOfOrbImages.size() < 2) {
                continue;
            }

            years.add(new NativeAnnualProfectionEntry(
                    Integer.parseInt(ageText),
                    cells.get(1).text().trim(),
                    AstroSeekParserUtil.parseSignFromImage(lordOfYearImages.get(0).attr("src")),
                    AstroSeekParserUtil.parsePlanetFromImage(lordOfYearImages.get(1).attr("src")),
                    AstroSeekParserUtil.parsePlanetFromImage(lordOfOrbImages.get(0).attr("src")),
                    AstroSeekParserUtil.parsePlanetFromImage(lordOfOrbImages.get(1).attr("src")),
                    parseSignCell(cells.get(4)),
                    parseSignCell(cells.get(5)),
                    parseSignCell(cells.get(6)),
                    parseSignCell(cells.get(7))));
        }

        if (years.isEmpty()) {
            return;
        }

        builder.annualProfections(new NativeAnnualProfections(years));
    }

    private String parseSignCell(Element cell) {
        Element image = cell.selectFirst("img[src]");
        if (image == null) {
            throw new IllegalArgumentException("Annual profections sign icon not found");
        }
        return AstroSeekParserUtil.parseSignFromImage(image.attr("src"));
    }
}
