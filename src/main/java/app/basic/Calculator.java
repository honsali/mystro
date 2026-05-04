package app.basic;

import app.basic.model.NatalChart;

public interface Calculator {
    void calculate(NatalChart natalChart, CalculationContext ctx);
}
