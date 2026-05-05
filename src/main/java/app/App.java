package app;

import app.basic.BasicCalculator;
import app.input.InputLoader;
import app.input.model.InputListBundle;
import app.output.JsonReportWriter;
import app.output.LoggerWriter;
import app.runtime.DescriptiveReportService;

import java.nio.file.Path;

public final class App {

    public static void main(String[] args) throws Exception {
        InputLoader loader = new InputLoader();
        JsonReportWriter reportWriter = new JsonReportWriter();

        try {
            InputListBundle inputListBundle = loader.load(args);
            DescriptiveReportService service = new DescriptiveReportService(new BasicCalculator(), reportWriter);
            service.runDescriptive(inputListBundle);
        } finally {
            new LoggerWriter(reportWriter).write(Path.of("output", "run-logger.json"));
        }
    }

    private App() {}
}
