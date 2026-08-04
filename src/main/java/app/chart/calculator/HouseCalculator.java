package app.chart.calculator;

import java.util.ArrayList;
import java.util.List;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;

public class HouseCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        List<HousePosition> houses = new ArrayList<>();

        for (int house = 1; house <= 12; house++) {
            double cuspLongitude = AstroMath.normalize(ctx.getCusps()[house]);
            houses.add(new HousePosition(house, cuspLongitude, AstroMath.signOf(cuspLongitude)));
        }
        natalChart.setHouses(houses);
    }
}
