package app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.basic.BasicCalculator;
import app.chart.model.NatalChart;
import app.doctrine.Doctrine;
import app.input.InputLoader;
import app.input.model.InputListBundle;
import app.input.model.Subject;
import app.output.DescriptiveAstrologyReport;
import app.output.JsonReportWriter;
import app.output.Logger;
import app.output.LoggerWriter;

public final class App {
    private static final String ENGINE_VERSION = readProjectVersion();

    public static void main(String[] args) throws Exception {
        InputLoader loader = new InputLoader();
        JsonReportWriter reportWriter = new JsonReportWriter();
        BasicCalculator basicCalculator = new BasicCalculator();

        try {
            InputListBundle inputListBundle = loader.load(args);

            for (Subject subject : inputListBundle.getSubjects()) {
                for (Doctrine doctrine : inputListBundle.getDoctrines()) {
                    NatalChart natalChart = doctrine.calculateDescriptive(subject, basicCalculator);
                    DescriptiveAstrologyReport report = new DescriptiveAstrologyReport(ENGINE_VERSION, subject, doctrine, natalChart);
                    reportWriter.write(Path.of("output", subject.getId(), doctrine.getId() + "-descriptive.json"), report);
                    Logger.instance.info(subject.getId(), "Wrote descriptive report for doctrine " + doctrine.getId());
                }
            }
        } finally {
            new LoggerWriter(reportWriter).write(Path.of("output", "run-logger.json"));
        }
    }

    private static String readProjectVersion() {
        try {
            String pom = Files.readString(Path.of("pom.xml"));
            Matcher matcher = Pattern.compile("<version>([^<]+)</version>").matcher(pom);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (IOException e) {
            Logger.instance.info("app", "Could not read pom.xml for engine version: " + e.getMessage());
        }
        return "unknown";
    }

    private App() {}
}
