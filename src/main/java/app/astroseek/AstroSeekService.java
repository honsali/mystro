package app.astroseek;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import app.astroseek.parser.AstroSeekParser;
import app.astroseek.parser.impl.AstroSeekAnnualProfectionsParser;
import app.astroseek.parser.impl.AstroSeekBirthParser;
import app.astroseek.parser.impl.AstroSeekDerivedChartsParser;
import app.astroseek.parser.impl.AstroSeekHermeticLotParser;
import app.astroseek.parser.impl.AstroSeekHousesParser;
import app.astroseek.parser.impl.AstroSeekLordOfOrbParser;
import app.astroseek.parser.impl.AstroSeekMainAspectsParser;
import app.astroseek.parser.impl.AstroSeekOtherAspectsParser;
import app.astroseek.parser.impl.AstroSeekPlanetPositionsParser;
import app.astroseek.parser.impl.AstroSeekPlanetaryHourParser;
import app.astroseek.parser.impl.AstroSeekSyzygyParser;
import app.common.Config;
import app.common.Logger;
import app.common.NativeReportBuilder;
import app.common.io.JsonFileSupport;
import app.common.model.NativeReport;

public final class AstroSeekService {

    private final List<AstroSeekParser> sectionParsers = List.of(//
            new AstroSeekBirthParser(), //
            new AstroSeekPlanetPositionsParser(), //
            new AstroSeekHousesParser(), //
            new AstroSeekMainAspectsParser(), //
            new AstroSeekOtherAspectsParser(), //
            new AstroSeekDerivedChartsParser(), //
            new AstroSeekPlanetaryHourParser(), //
            new AstroSeekLordOfOrbParser(), //
            new AstroSeekAnnualProfectionsParser(), //
            new AstroSeekSyzygyParser(), //
            new AstroSeekHermeticLotParser()//
    );


    public List<String> generate(List<String> names) throws IOException {
        List<String> written = new ArrayList<>();
        for (String name : names) {
            Path htmlPath = Config.ASTROSEEK_HTML_DIR.resolve(name + ".html");
            if (!Files.exists(htmlPath)) {
                Logger.getInstance().missingAstroSeekHtml(name);
                continue;
            }
            NativeReport report = buildNativeReport(name, htmlPath);
            JsonFileSupport.write(Config.ASTROSEEK_OUTPUT_DIR.resolve(name + ".json"), report);
            written.add(name);
        }
        return written;
    }

    private NativeReport buildNativeReport(String name, Path htmlPath) throws IOException {
        Document document = Jsoup.parse(htmlPath.toFile(), "UTF-8");
        NativeReportBuilder builder = new NativeReportBuilder(name);
        for (AstroSeekParser parser : sectionParsers) {
            parser.parse(document, builder);
        }
        return builder.build();
    }
}
