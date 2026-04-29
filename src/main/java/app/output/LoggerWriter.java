package app.output;

import java.io.IOException;
import java.nio.file.Path;

public final class LoggerWriter {
    private final JsonReportWriter writer;

    public LoggerWriter(JsonReportWriter writer) {
        this.writer = writer;
    }

    public void write(Path path) throws IOException {
        writer.write(path, Logger.instance);
    }
}
