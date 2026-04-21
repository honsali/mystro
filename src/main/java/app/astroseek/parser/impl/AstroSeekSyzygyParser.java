package app.astroseek.parser.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import app.astroseek.parser.AstroSeekParser;
import app.common.Config;
import app.common.NativeReportBuilder;
import app.common.model.NativeBirth;
import app.common.model.NativeSyzygy;

public final class AstroSeekSyzygyParser implements AstroSeekParser {

    @Override
    public void parse(Document document, NativeReportBuilder builder) {
        Element table = document.select("table")//
                .stream()//
                .filter(element -> element.text().contains("Prenatal Syzygy"))//
                .findFirst()//
                .orElseThrow(() -> new IllegalArgumentException("Prenatal Syzygy table not found"));

        String text = table.text();
        String phase = text.contains("Full Moon") ? "Full Moon" : "New Moon";

        Matcher matcher = Config.SYZYGY_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Syzygy details not found: " + text);
        }
        LocalDateTime localDateTime = LocalDateTime.parse(matcher.group(2) + " " + matcher.group(3), Config.ASTRO_SEEK_DATE_TIME);
        NativeBirth birth = builder.birth();
        if (birth == null || birth.utcOffset() == null) {
            throw new IllegalArgumentException("Astro-Seek birth data with UTC offset must be parsed before syzygy");
        }
        ZoneOffset offset = ZoneOffset.of(birth.utcOffset());
        OffsetDateTime syzygyDateTime = OffsetDateTime.of(localDateTime, offset);

        NativeSyzygy syzygy = new NativeSyzygy();
        syzygy.setPhase(phase);
        syzygy.setSyzygyDateTime(syzygyDateTime);
        builder.syzygy(syzygy);
    }

}
