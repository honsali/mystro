package app.astroseek.parser;

import java.util.Locale;
import java.util.regex.Matcher;
import app.common.Config;

public final class AstroSeekParserUtil {


    public static String parsePlanetFromImage(String src) {
        String normalized = normalizeImageName(src);
        if (normalized.contains("slunce") || normalized.contains("sun")) return "Sun";
        if (normalized.contains("luna") || normalized.contains("moon")) return "Moon";
        if (normalized.contains("merkur") || normalized.contains("mercury")) return "Mercury";
        if (normalized.contains("venuse") || normalized.contains("venus")) return "Venus";
        if (normalized.contains("mars")) return "Mars";
        if (normalized.contains("jupiter")) return "Jupiter";
        if (normalized.contains("saturn")) return "Saturn";
        throw new IllegalArgumentException("Unknown planet image: " + src);
    }

    public static String parseSignFromImage(String src) {
        String normalized = normalizeImageName(src);
        if (normalized.contains("beran") || normalized.contains("aries")) return "Aries";
        if (normalized.contains("byk") || normalized.contains("taurus")) return "Taurus";
        if (normalized.contains("blizenci") || normalized.contains("gemini")) return "Gemini";
        if (normalized.contains("rak") || normalized.contains("cancer")) return "Cancer";
        if (normalized.contains("lev") || normalized.contains("leo")) return "Leo";
        if (normalized.contains("panna") || normalized.contains("virgo")) return "Virgo";
        if (normalized.contains("vahy") || normalized.contains("libra")) return "Libra";
        if (normalized.contains("stir") || normalized.contains("scorpio")) return "Scorpio";
        if (normalized.contains("strelec") || normalized.contains("sagittarius")) return "Sagittarius";
        if (normalized.contains("kozoroh") || normalized.contains("capricorn")) return "Capricorn";
        if (normalized.contains("vodnar") || normalized.contains("aquarius")) return "Aquarius";
        if (normalized.contains("ryby") || normalized.contains("pisces")) return "Pisces";
        throw new IllegalArgumentException("Unknown sign image: " + src);
    }

    public static double parseDegrees(String text) {
        Matcher matcher = Config.DEGREE_MINUTE_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not parse degrees from: " + text);
        }
        return Integer.parseInt(matcher.group(1)) + Integer.parseInt(matcher.group(2)) / 60.0;
    }

    public static double absoluteLon(String sign, double signLon) {
        return Config.SIGNS.indexOf(sign) * 30.0 + signLon;
    }


    private static String normalizeImageName(String src) {
        return src.substring(src.lastIndexOf('/') + 1)
                .toLowerCase(Locale.ENGLISH)
                .replace(".png", "")
                .replace(".gif", "");
    }

    private AstroSeekParserUtil() {}
}
