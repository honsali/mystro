package app.astroseek.parser.impl;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import app.astroseek.parser.AstroSeekParser;
import app.common.NativeReportBuilder;
import app.common.model.NativeAspect;

public final class AstroSeekOtherAspectsParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element header = document.select("div.zalozka-rozbor-nej")
                .stream()
                .filter(element -> element.text().contains("Other aspects:"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Astro-Seek section not found: Other aspects"));
        Element table = header.nextElementSibling();
        if (table == null || !table.classNames().contains("vypocet-planet")) {
            throw new IllegalArgumentException("Astro-Seek aspect table not found for other aspects");
        }

        List<NativeAspect> aspects = new ArrayList<>();
        for (Element row : table.children()) {
            if (!"div".equals(row.tagName()) || row.childrenSize() != 3) {
                continue;
            }
            String left = row.child(0).text().trim();
            String aspect = row.child(1).text().trim();
            String right = row.child(2).text().trim();
            if (left.equals("Planet") || left.equals("Object") || left.isBlank()) {
                continue;
            }
            aspects.add(new NativeAspect(left, aspect, right));
        }
        if (aspects.isEmpty()) {
            throw new IllegalArgumentException("No Astro-Seek other aspects were parsed");
        }
        builder.otherAspects(aspects);
    }
}
