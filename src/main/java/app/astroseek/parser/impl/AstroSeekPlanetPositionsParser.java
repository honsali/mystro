package app.astroseek.parser.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.AstroSeekParserUtil;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;

public final class AstroSeekPlanetPositionsParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element textarea = document.getElementById("txtarea1");
        if (textarea == null) {
            throw new IllegalArgumentException("Astro-Seek clipboard positions textarea not found");
        }

        Map<String, ParsedLine> parsed = new LinkedHashMap<>();
        for (String rawLine : textarea.text().split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            ParsedLine entry = parseLine(line);
            parsed.put(entry.name(), entry);
        }

        ParsedLine houseOne = parsed.get("H1");
        if (houseOne == null) {
            throw new IllegalArgumentException("Astro-Seek H1 line not found in clipboard positions");
        }
        String ascSign = houseOne.sign();

        Map<String, Boolean> retrogrades = parseRetrogrades(document);
        Map<String, ChartPoint> planets = new LinkedHashMap<>();
        addPlanet(parsed, retrogrades, planets, ascSign, "Sun", "Sun");
        addPlanet(parsed, retrogrades, planets, ascSign, "Moon", "Moon");
        addPlanet(parsed, retrogrades, planets, ascSign, "Mercury", "Mercury");
        addPlanet(parsed, retrogrades, planets, ascSign, "Venus", "Venus");
        addPlanet(parsed, retrogrades, planets, ascSign, "Mars", "Mars");
        addPlanet(parsed, retrogrades, planets, ascSign, "Jupiter", "Jupiter");
        addPlanet(parsed, retrogrades, planets, ascSign, "Saturn", "Saturn");
        addPlanet(parsed, retrogrades, planets, ascSign, "Uranus", "Uranus");
        addPlanet(parsed, retrogrades, planets, ascSign, "Neptune", "Neptune");
        addPlanet(parsed, retrogrades, planets, ascSign, "Pluto", "Pluto");
        addPlanet(parsed, retrogrades, planets, ascSign, "Node", "Node");
        addPlanet(parsed, retrogrades, planets, ascSign, "Lilith", "Lilith");
        addPlanet(parsed, retrogrades, planets, ascSign, "Chiron", "Chiron");
        addPlanet(parsed, retrogrades, planets, ascSign, "Fortune", "Fortune");
        addPlanet(parsed, retrogrades, planets, ascSign, "Spirit", "Spirit");
        addPlanet(parsed, retrogrades, planets, ascSign, "Syzygy", "Syzygy");

        if (planets.isEmpty()) {
            throw new IllegalArgumentException("No Astro-Seek planet positions were parsed");
        }
        builder.planets(planets);
    }

    private void addPlanet(Map<String, ParsedLine> parsed, Map<String, Boolean> retrogrades, Map<String, ChartPoint> planets, String ascSign, String sourceName, String targetName) {
        ParsedLine line = parsed.get(sourceName);
        if (line == null) {
            return;
        }
        boolean retrograde = retrogrades.getOrDefault(sourceName, line.retrograde());
        planets.put(targetName, new ChartPoint(targetName,
                line.sign(),
                line.signLon(),
                AstroSeekParserUtil.absoluteLon(line.sign(), line.signLon()),
                signHouse(ascSign, line.sign()),
                0.0,
                retrograde));
    }

    private ParsedLine parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Malformed Astro-Seek clipboard position line: " + line);
        }
        String name = parts[0].trim();
        String sign = parts[1].trim();
        double signLon = AstroSeekParserUtil.parseDegrees(parts[2]);
        boolean retrograde = parts.length > 3 && parts[3].trim().equals("R");
        return new ParsedLine(name, sign, signLon, retrograde);
    }

    private Map<String, Boolean> parseRetrogrades(Document document) {
        Map<String, Boolean> retrogrades = new LinkedHashMap<>();
        for (Element image : document.select("img[src*='planeta_neptun'], img[src*='planeta_uzel'], img[src*='planeta_lilith'], img[src*='planeta_chiron']")) {
            String src = image.attr("src");
            String lower = src.toLowerCase();
            if (!lower.contains("planeta_")) {
                continue;
            }
            String query = src.contains("?") ? src.substring(src.indexOf('?') + 1) : "";
            if (lower.contains("planeta_neptun")) {
                retrogrades.put("Neptune", query.contains("r_neptun=ANO"));
            }
            if (lower.contains("planeta_uzel")) {
                retrogrades.put("Node", query.contains("r_uzel=ANO") || retrogrades.getOrDefault("Node", false));
            }
        }
        return retrogrades;
    }

    private int signHouse(String ascSign, String sign) {
        return Math.floorMod(app.common.Config.SIGNS.indexOf(sign) - app.common.Config.SIGNS.indexOf(ascSign), 12) + 1;
    }

    private record ParsedLine(String name, String sign, double signLon, boolean retrograde) {
    }
}
