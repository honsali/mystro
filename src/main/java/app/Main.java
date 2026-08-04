package app;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.input.NativeListInputLoader;
import app.input.ReadingInput;
import app.input.ReadingInputMapper;
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

        String alias = requireAlias(args[0]);
        ObjectMapper objectMapper = MystroObjectMapper.create();
        ReadingInput input = new NativeListInputLoader().load(alias, objectMapper);
        ReadingInputMapper.ResolvedBundle resolved = new ReadingInputMapper().resolve(input);
        ReadingBundleReport report = new ReadingBundleCalculator().calculate(AppVersion.get(), resolved);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report) + System.lineSeparator();

        Path outputPath = Path.of("output", alias, "reading_output.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, json);
        System.out.println("Wrote Mystro reading bundle to " + outputPath.toAbsolutePath());
    }

    private static String requireAlias(String value) {
        if (value == null || value.isBlank()) {
            printUsage();
            throw new IllegalArgumentException("Native-list alias is required");
        }
        String alias = value.trim();
        if (alias.contains("/") || alias.contains("\\") || ".".equals(alias) || "..".equals(alias)
                || alias.toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            throw new IllegalArgumentException("Native-list alias must be a name, not a path: " + value);
        }
        return alias;
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar target/mystro-<version>.jar <native-list-alias>");
        System.err.println("Native-list aliases are loaded from native-list.json by matching the entry name field.");
        System.err.println("Output is always written to output/<native-list-alias>/reading_output.json.");
    }

    private Main() {}
}
