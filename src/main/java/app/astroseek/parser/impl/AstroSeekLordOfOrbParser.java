package app.astroseek.parser.impl;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.NativeReportBuilder;
import app.common.model.NativeLordOfOrb;
import app.common.model.NativeLordOfOrbEntry;

public final class AstroSeekLordOfOrbParser implements AstroSeekParser {
    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element orbHeader = document.selectFirst("img[alt=Orb]");
        if (orbHeader == null) {
            return;
        }
        Element table = orbHeader.closest("table");
        if (table == null) {
            return;
        }

        List<NativeLordOfOrbEntry> years = new ArrayList<>();
        for (Element row : table.select("tr")) {
            Elements cells = row.select("> td");
            if (cells.size() < 4) {
                continue;
            }
            String ageText = cells.get(0).text().trim();
            if (!ageText.matches("\\d+")) {
                continue;
            }
            Elements orbImages = cells.get(3).select("img[src]");
            if (orbImages.size() < 2) {
                continue;
            }
            years.add(new NativeLordOfOrbEntry(
                    Integer.parseInt(ageText),
                    cells.get(1).text().trim(),
                    AstroSeekParserUtil.parsePlanetFromImage(orbImages.get(0).attr("src")),
                    AstroSeekParserUtil.parsePlanetFromImage(orbImages.get(1).attr("src"))));
        }

        if (years.isEmpty()) {
            return;
        }

        NativeLordOfOrb lordOfOrb = new NativeLordOfOrb();
        NativeLordOfOrbEntry ageZero = years.stream().filter(entry -> entry.age() == 0).findFirst().orElse(null);
        lordOfOrb.setStartingRuler(ageZero == null ? null : ageZero.mod84());
        lordOfOrb.setYears(years);
        builder.lordOfOrb(lordOfOrb);
    }
}
