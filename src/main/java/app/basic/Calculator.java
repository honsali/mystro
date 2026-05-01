package app.basic;

import app.model.basic.BasicChart;

public interface Calculator {
    void calculate(BasicChart basicChart, BasicCalculationContext ctx);
}
