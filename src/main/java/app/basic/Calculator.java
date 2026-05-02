package app.basic;

import app.basic.model.BasicChart;

public interface Calculator {
    void calculate(BasicChart basicChart, BasicCalculationContext ctx);
}
