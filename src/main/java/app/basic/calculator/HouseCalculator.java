package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.AstroMath;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.HousePosition;

public class HouseCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        List<HousePosition> houses = new ArrayList<>();

        for (int house = 1; house <= 12; house++) {
            double cuspLongitude = AstroMath.normalize(ctx.getCusps()[house]);
            houses.add(new HousePosition(house, cuspLongitude, AstroMath.signOf(cuspLongitude), AstroMath.degreeInSign(cuspLongitude)));
        }
        natalChart.setHouses(houses);
    }
}
