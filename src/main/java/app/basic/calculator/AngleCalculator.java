package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.ChartAngle;
import app.chart.data.AngleType;

public class AngleCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        List<ChartAngle> angles = new ArrayList<>();

        double ascendant = ctx.normalize(ctx.getAscmc()[0]);
        double midheaven = ctx.normalize(ctx.getAscmc()[1]);
        addAngle(angles, AngleType.ASCENDANT, ascendant, ctx);
        addAngle(angles, AngleType.MIDHEAVEN, midheaven, ctx);
        addAngle(angles, AngleType.DESCENDANT, ctx.normalize(ascendant + 180.0), ctx);
        addAngle(angles, AngleType.IMUM_COELI, ctx.normalize(midheaven + 180.0), ctx);
        natalChart.setAngles(angles);
    }

    private void addAngle(List<ChartAngle> angles, AngleType name, double longitude, CalculationContext ctx) {
        angles.add(new ChartAngle(name, longitude, ctx.signOf(longitude), ctx.degreeInSign(longitude)));
    }

}
