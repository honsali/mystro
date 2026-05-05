package app.output;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonReportWriter {
    private final ObjectMapper mapper;

    public JsonReportWriter() {
        this.mapper = MystroObjectMapper.create();
    }

    public void write(Path path, Object report) throws IOException {
        Files.createDirectories(path.getParent());
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), report);
    }
}
