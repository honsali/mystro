package app.reading.description;

import app.chart.ChartCalculator;
import app.chart.model.Chart;
import app.chart.model.Subject;

public final class NatalDescriptionReadingCalculator {

    private final ChartCalculator chartCalculator;
    private final NatalChartCalculator natalChartCalculator;

    public NatalDescriptionReadingCalculator(ChartCalculator chartCalculator, NatalChartCalculator natalChartCalculator) {
        this.chartCalculator = chartCalculator;
        this.natalChartCalculator = natalChartCalculator;
    }

    public NatalDescriptionReadingReport calculate(Subject subject) {
        Chart natalChart = natalChartCalculator.calculate(subject, chartCalculator);
        return new NatalDescriptionReadingReport(natalChartCalculator.getCoreDoctrineInfo(), natalChart);
    }
}
