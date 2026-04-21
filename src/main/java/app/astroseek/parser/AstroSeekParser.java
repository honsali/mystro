package app.astroseek.parser;

import org.jsoup.nodes.Document;
import app.common.NativeReportBuilder;

public interface AstroSeekParser {
    void parse(Document document, NativeReportBuilder builder);
}
