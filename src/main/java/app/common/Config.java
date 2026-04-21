package app.common;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class Config {
        public static final Path NATIVE_LIST_PATH = Path.of("input", "native-list.json");
        public static final Path REPORT_PATH = Path.of("output", "report.json");

        public static final Path ASTROSEEK_HTML_DIR = Path.of("input", "astroseek");
        public static final Path ASTROSEEK_OUTPUT_DIR = Path.of("output", "astroseek", "json");
        public static final Path MYSTRO_OUTPUT_DIR = Path.of("output", "mystro", "json");

        public static final Path EPHE_DIR = Path.of("ephe");

        public static final double ACCEPTED_DIFF_ANGLE_DEGREES = 0.9;
        public static final double ACCEPTED_DIFF_COORDINATE_DEGREES = 0.1;

        public static final List<String> SIGNS = List.of("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces");

        public static final Map<String, String> SIGN_RULERS = Map.ofEntries(Map.entry("Aries", "Mars"), Map.entry("Taurus", "Venus"), Map.entry("Gemini", "Mercury"), Map.entry("Cancer", "Moon"), Map.entry("Leo", "Sun"), Map.entry("Virgo", "Mercury"), Map.entry("Libra", "Venus"),
                        Map.entry("Scorpio", "Mars"), Map.entry("Sagittarius", "Jupiter"), Map.entry("Capricorn", "Saturn"), Map.entry("Aquarius", "Saturn"), Map.entry("Pisces", "Jupiter"));

        public static final List<String> PLANETARY_HOUR_SEQUENCE = List.of("Sun", "Venus", "Mercury", "Moon", "Saturn", "Jupiter", "Mars");

        public static final List<String> CHALDEAN_YEAR_SEQUENCE = List.of("Saturn", "Jupiter", "Mars", "Sun", "Venus", "Mercury", "Moon");

        public static final Pattern DEGREE_MINUTE_PATTERN = Pattern.compile("(\\d+)\\D+(\\d+)");
        public static final Pattern COORDINATE_TEXT_PATTERN = Pattern.compile("(\\d+)\\D+(\\d+)\\D*([NSEW])", Pattern.CASE_INSENSITIVE);
        public static final Pattern UTC_OFFSET_PATTERN = Pattern.compile("UT/GMT\\s*([+\\-−–]\\d{1,2}:\\d{2})");
        public static final Pattern PLANETARY_HOUR_PATTERN = Pattern.compile("(\\d+)(?:st|nd|rd|th) hour of (Day|Night)");
        public static final Pattern SYZYGY_PATTERN = Pattern.compile("(New Moon|Full Moon)\\s+([A-Z][a-z]{2} \\d{1,2}, \\d{4})\\s+at\\s+(\\d{2}:\\d{2})", Pattern.DOTALL);

        public static final DateTimeFormatter ASTRO_SEEK_LONG_DATE = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
        public static final DateTimeFormatter ASTRO_SEEK_UTC_DATE_TIME = DateTimeFormatter.ofPattern("d MMMM uuuu - HH:mm", Locale.ENGLISH);
        public static final DateTimeFormatter ASTRO_SEEK_DATE_TIME = DateTimeFormatter.ofPattern("MMM d, uuuu HH:mm", Locale.ENGLISH);
        public static final DateTimeFormatter NATIVE_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ENGLISH);

        private Config() {}
}
