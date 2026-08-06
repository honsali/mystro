package app;

import java.util.List;
import app.chart.ChartCalculator;
import app.chart.model.Subject;
import app.reading.ReadingBundleReport;
import app.reading.description.NatalDescriptionReadingCalculator;
import app.reading.description.NatalDescriptionReadingReport;
import app.reading.description.NatalChartCalculator;

/**
 * Tiny non-web façade for calculating the current Mystro reading bundle.
 */
public final class ReadingBundleCalculator {
    private final NatalDescriptionReadingCalculator natalDescriptionReadingCalculator;

    public ReadingBundleCalculator() {
        this(new NatalDescriptionReadingCalculator(new ChartCalculator(), new NatalChartCalculator()));
    }

    public ReadingBundleCalculator(NatalDescriptionReadingCalculator natalDescriptionReadingCalculator) {
        this.natalDescriptionReadingCalculator = natalDescriptionReadingCalculator;
    }

    public ReadingBundleReport calculate(String engineVersion, Subject subject) {
        NatalDescriptionReadingReport natalDescription = natalDescriptionReadingCalculator.calculate(subject);
        return new ReadingBundleReport(engineVersion, subject, List.of(natalDescription));
    }
}
