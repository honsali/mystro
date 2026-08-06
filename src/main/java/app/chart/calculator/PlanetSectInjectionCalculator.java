package app.chart.calculator;

import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.model.Chart;

public class PlanetSectInjectionCalculator implements Calculator {

    @Override
    public void calculate(Chart chart, CalculationContext ctx) {
        if (chart.getPoints() == null) {
            throw new IllegalStateException("Planet sect injection requires points to be calculated first");
        }
        if (chart.getSect() == null) {
            throw new IllegalStateException("Planet sect injection requires chart sect to be calculated first");
        }
        chart.applyPlanetSects();
    }
}
