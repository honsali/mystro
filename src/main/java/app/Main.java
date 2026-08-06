package app;

import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.chart.model.Subject;
import app.input.NatalInput;
import app.input.NatalInputLoader;
import app.input.SubjectFactory;
import app.io.MystroObjectMapper;
import app.reading.ReadingBundleReport;

public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printUsage();
            return;
        }
        if (args.length != 1) {
            printUsage();
            System.exit(2);
            return;
        }

        NatalInput natalInput = new NatalInputLoader().load(args[0]);
        Subject subject = new SubjectFactory().create(natalInput);
        ReadingBundleReport report = new ReadingBundleCalculator().calculate(AppVersion.get(), subject);

        ObjectMapper objectMapper = MystroObjectMapper.create();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report) + System.lineSeparator();

        Path outputPath = Path.of("output", subject.getId(), "reading_output.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, json);
        System.out.println("Wrote Mystro reading bundle to " + outputPath.toAbsolutePath());
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar target/mystro-<version>.jar <native-list-alias>");
        System.err.println("Native-list aliases are loaded from native-list.json by matching the entry name field.");
        System.err.println("Output is always written to output/<native-list-alias>/reading_output.json.");
    }

    private Main() {}
}
