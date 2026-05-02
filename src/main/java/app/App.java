package app;

import java.nio.file.Path;
import app.basic.BasicCalculator;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.input.InputLoader;
import app.basic.model.BasicChart;
import app.input.model.CalculationSetting;
import app.input.model.Input;
import app.input.model.InputListBundle;
import app.input.model.Subject;
import app.output.DescriptiveAstrologyReport;
import app.output.JsonReportWriter;
import app.output.Logger;
import app.output.LoggerWriter;

public final class App {
    private static final String ENGINE_VERSION = "0.1.0";

    public static void main(String[] args) throws Exception {
        InputLoader loader = new InputLoader();
        JsonReportWriter reportWriter = new JsonReportWriter();
        BasicCalculator basicCalculator = new BasicCalculator();

        try {
            InputListBundle inputListBundle = loader.load(args);

            CalculationSetting calculationSetting = inputListBundle.getCalculationSetting();
            for (Subject subject : inputListBundle.getSubjects()) {
                for (Doctrine doctrine : inputListBundle.getDoctrines()) {
                    Input input = new Input(subject, doctrine, calculationSetting);
                    BasicChart basicChart = basicCalculator.calculate(input);
                    DescriptiveResult descriptive = doctrine.describe(input, basicChart);
                    DescriptiveAstrologyReport report = new DescriptiveAstrologyReport(ENGINE_VERSION, input, basicChart, descriptive.getData());
                    reportWriter.write(Path.of("output", "descriptive", subject.getId(), doctrine.getId() + ".json"), report);
                    Logger.instance.info(subject.getId(), "Wrote descriptive report for doctrine " + doctrine.getId());
                }
            }
        } finally {
            new LoggerWriter(reportWriter).write(Path.of("output", "run-logger.json"));
        }
    }


    private App() {}
}
