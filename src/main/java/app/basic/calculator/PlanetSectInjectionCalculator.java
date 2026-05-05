package app.basic.calculator;

import app.basic.Calculator;
import app.basic.CalculationContext;
import app.basic.model.NatalChart;

public class PlanetSectInjectionCalculator implements Calculator {

    @Override
    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        if (natalChart.getPoints() == null) {
            throw new IllegalStateException("Planet sect injection requires points to be calculated first");
        }
        if (natalChart.getSect() == null) {
            throw new IllegalStateException("Planet sect injection requires chart sect to be calculated first");
        }
        natalChart.applyPlanetSects();
    }
}
