package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.HousePosition;

public class HouseCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {

        List<HousePosition> houses = new ArrayList<>();

        for (int house = 1; house <= 12; house++) {
            double cuspLongitude = ctx.normalize(ctx.getCusps()[house]);
            houses.add(new HousePosition(house, cuspLongitude, ctx.signOf(cuspLongitude), ctx.degreeInSign(cuspLongitude)));
        }
        basicChart.setHouses(houses);
    }
}
