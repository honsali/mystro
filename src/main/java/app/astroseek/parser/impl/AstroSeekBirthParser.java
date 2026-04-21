package app.astroseek.parser.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Matcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.astroseek.parser.AstroSeekParser;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativeBirth;

public final class AstroSeekBirthParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        String coordinates = requireDetailValueElement(document, "Latitude, Longitude:").text().replace('\u00a0', ' ').trim();
        String[] coordinateParts = coordinates.split(",", 2);
        if (coordinateParts.length != 2) {
            throw new IllegalArgumentException("Astro-Seek latitude/longitude row is malformed: " + coordinates);
        }
        Element value = requireDetailValueElement(document, "Date of Birth");
        Elements parts = value.select("em");
        if (parts.size() < 3) {
            throw new IllegalArgumentException("Astro-Seek birth date/time row not found");
        }
        LocalDate date = LocalDate.parse(parts.get(0).text().trim(), Config.ASTRO_SEEK_LONG_DATE);
        String birthDate = date.format(Config.NATIVE_DATE);
        String raw = parts.get(2).text().trim();
        String birthTime = raw.length() >= 5 ? raw.substring(0, 5) : raw;

        NativeBirth birth = new NativeBirth();
        birth.setBirthDate(birthDate);
        birth.setBirthTime(birthTime);
        birth.setLatitude(parseCoordinateText(coordinateParts[0]));
        birth.setLongitude(parseCoordinateText(coordinateParts[1]));
        birth.setUtcOffset(parseUtcOffset(document, birthDate, birthTime));
        builder.birth(birth);
    }

    private String parseUtcOffset(Document document, String birthDate, String birthTime) {
        Element value = requireDetailValueElement(document, "Date of Birth");
        Element link = value.selectFirst("a[onmouseover]");
        if (link != null) {
            Matcher matcher = Config.UTC_OFFSET_PATTERN.matcher(link.attr("onmouseover"));
            if (matcher.find()) {
                String raw = matcher.group(1).replace('−', '-').replace('–', '-');
                int colon = raw.indexOf(':');
                String hourPart = raw.substring(0, colon);
                String sign = hourPart.startsWith("-") ? "-" : "+";
                String absHour = hourPart.replace("+", "").replace("-", "");
                return sign + String.format(Locale.ENGLISH, "%02d", Integer.parseInt(absHour)) + raw.substring(colon);
            }
        }

        Element utcValue = requireDetailValueElement(document, "Universal Time");
        String utcText = utcValue.text().replaceAll("(\\d{2}:\\d{2}):\\d{2}", "$1").replaceAll("\\s*\\([^)]*\\)$", "").trim();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(birthDate, Config.NATIVE_DATE), LocalTime.parse(birthTime));
        LocalDateTime utcDateTime = LocalDateTime.parse(utcText, Config.ASTRO_SEEK_UTC_DATE_TIME);
        long offsetMinutes = Duration.between(utcDateTime, localDateTime).toMinutes();
        long hours = Math.abs(offsetMinutes) / 60;
        long minutes = Math.abs(offsetMinutes) % 60;
        return String.format(Locale.ENGLISH, "%s%02d:%02d", offsetMinutes < 0 ? "-" : "+", hours, minutes);
    }



    private double parseCoordinateText(String text) {
        Matcher matcher = Config.COORDINATE_TEXT_PATTERN.matcher(text.trim());
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not parse coordinate from: " + text);
        }
        double degrees = Double.parseDouble(matcher.group(1));
        double minutes = Double.parseDouble(matcher.group(2));
        char direction = Character.toUpperCase(matcher.group(3).charAt(0));
        double sign = direction == 'S' || direction == 'W' ? -1.0 : 1.0;
        return sign * (degrees + minutes / 60.0);
    }

    private Element requireDetailValueElement(Document document, String labelPrefix) {
        for (Element label : document.select("div.ascendent-vypocet-vlevo")) {
            String labelText = label.text().replace('\u00a0', ' ').trim();
            if (!labelText.startsWith(labelPrefix)) {
                continue;
            }
            Element value = label.nextElementSibling();
            if (value != null && value.classNames().contains("ascendent-vypocet-vpravo")) {
                return value;
            }
            break;
        }
        throw new IllegalArgumentException("Astro-Seek detail value element not found for label: " + labelPrefix);
    }
}
