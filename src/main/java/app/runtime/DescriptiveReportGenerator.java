package app.runtime;

import app.basic.BasicCalculator;
import app.chart.model.NatalChart;
import app.doctrine.Doctrine;
import app.input.model.Subject;
import app.output.DescriptiveAstrologyReport;

/**
 * Pure in-memory descriptive report generator shared by the REST API.
 */
public final class DescriptiveReportGenerator {

    private final BasicCalculator basicCalculator;

    public DescriptiveReportGenerator(BasicCalculator basicCalculator) {
        this.basicCalculator = basicCalculator;
    }

    public DescriptiveAstrologyReport generate(Subject subject, Doctrine doctrine) {
        NatalChart natalChart = doctrine.calculateDescriptive(subject, basicCalculator);
        return new DescriptiveAstrologyReport(
                EngineVersion.get(), subject, doctrine, natalChart);
    }
}
