package app.runtime;

import app.basic.BasicCalculator;
import app.chart.model.NatalChart;
import app.doctrine.Doctrine;
import app.input.model.InputListBundle;
import app.input.model.Subject;
import app.output.DescriptiveAstrologyReport;
import app.output.JsonReportWriter;
import app.output.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable facade for descriptive report generation.
 * Performs the subject × doctrine loop, calls doctrine.calculateDescriptive(...),
 * builds the report, and optionally writes it to the standard output path.
 */
public final class DescriptiveReportService {

    private final BasicCalculator basicCalculator;
    private final JsonReportWriter reportWriter;

    public DescriptiveReportService(BasicCalculator basicCalculator, JsonReportWriter reportWriter) {
        this.basicCalculator = basicCalculator;
        this.reportWriter = reportWriter;
    }

    /**
     * Generate descriptive reports in memory without writing files.
     */
    public List<DescriptiveAstrologyReport> generateDescriptiveReports(InputListBundle inputListBundle) {
        List<DescriptiveAstrologyReport> reports = new ArrayList<>();
        for (Subject subject : inputListBundle.getSubjects()) {
            for (Doctrine doctrine : inputListBundle.getDoctrines()) {
                NatalChart natalChart = doctrine.calculateDescriptive(subject, basicCalculator);
                DescriptiveAstrologyReport report = new DescriptiveAstrologyReport(
                        EngineVersion.get(), subject, doctrine, natalChart);
                reports.add(report);
            }
        }
        return reports;
    }

    /**
     * Generate descriptive reports and write them to output files.
     */
    public void runDescriptive(InputListBundle inputListBundle) throws IOException {
        for (DescriptiveAstrologyReport report : generateDescriptiveReports(inputListBundle)) {
            String subjectId = report.getSubject().getId();
            String doctrineId = report.getDoctrine().getId();
            reportWriter.write(
                    Path.of("output", subjectId, doctrineId + "-descriptive.json"),
                    report);
            Logger.instance.info(subjectId,
                    "Wrote descriptive report for doctrine " + doctrineId);
        }
    }
}
