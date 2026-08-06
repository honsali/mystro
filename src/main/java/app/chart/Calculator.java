package app.chart;

import app.chart.model.Chart;

public interface Calculator {
    void calculate(Chart chart, CalculationContext ctx);
}
