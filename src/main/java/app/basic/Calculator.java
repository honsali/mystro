package app.basic;

import app.chart.model.NatalChart;

public interface Calculator {
    void calculate(NatalChart natalChart, CalculationContext ctx);
}
