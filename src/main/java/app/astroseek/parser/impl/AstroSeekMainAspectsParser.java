package app.astroseek.parser.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import app.astroseek.parser.AstroSeekParser;
import app.common.NativeReportBuilder;
import app.common.model.NativeAspect;

public final class AstroSeekMainAspectsParser implements AstroSeekParser {
    private static final Pattern DETAIL_PATTERN = Pattern.compile("-\\s*(.+?)\\s+(Conjunction|Sextile|Square|Trine|Opposition)\\s+(.+?)\\s*\\((\\d+)[^\\d]+(\\d+).+?,\\s*(Applying|Separating)\\)");

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        List<NativeAspect> aspects = parseAspectSection(document, "Main aspects:");
        Map<String, NativeAspect> metadata = parseAspectMetadata(document);
        for (NativeAspect aspect : aspects) {
            NativeAspect detailed = metadata.get(key(aspect.left(), aspect.aspect(), aspect.right()));
            if (detailed != null) {
                aspect.setOrb(detailed.orb());
                aspect.setMotion(detailed.motion());
            }
        }
        builder.mainAspects(aspects);
    }

    private List<NativeAspect> parseAspectSection(Document document, String title) {
        Element header = document.select("div.zalozka-rozbor-nej")
                .stream()
                .filter(element -> element.text().contains(title))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Astro-Seek section not found: " + title));
        Element table = header.nextElementSibling();
        if (table == null || !table.classNames().contains("vypocet-planet")) {
            throw new IllegalArgumentException("Astro-Seek aspect table not found for section: " + title);
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
            throw new IllegalArgumentException("No Astro-Seek main aspects were parsed");
        }
        return aspects;
    }

    private Map<String, NativeAspect> parseAspectMetadata(Document document) {
        if (document.select("div.rozbor-hlavicka-left").stream().noneMatch(element -> element.text().contains("Main aspects - Aspects by Orbs"))) {
            throw new IllegalArgumentException("Astro-Seek main aspect details header not found");
        }
        Map<String, NativeAspect> metadata = new LinkedHashMap<>();
        for (Element detail : document.select("div.detail-rozbor-item")) {
            Element line = detail.selectFirst("div.cl.nulka > div");
            if (line == null) {
                continue;
            }
            Matcher matcher = DETAIL_PATTERN.matcher(line.text().replace('\u00a0', ' '));
            if (!matcher.find()) {
                continue;
            }
            String left = matcher.group(1).trim();
            String aspect = matcher.group(2).trim();
            String right = matcher.group(3).trim();
            double orb = Integer.parseInt(matcher.group(4)) + Integer.parseInt(matcher.group(5)) / 60.0;
            metadata.put(key(left, aspect, right), new NativeAspect(left, aspect, right, orb, matcher.group(6).trim()));
        }
        return metadata;
    }

    private String key(String left, String aspect, String right) {
        return left + "|" + aspect + "|" + right;
    }
}
