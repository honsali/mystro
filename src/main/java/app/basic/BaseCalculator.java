package app.basic;

import app.model.basic.BasicChart;

public abstract class BaseCalculator {

    protected BasicChart basicChart;
    protected BasicCalculationContext ctx;

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        this.basicChart = basicChart;
        this.ctx = ctx;
        executeCalculation();
    }

    protected abstract void executeCalculation();
}
