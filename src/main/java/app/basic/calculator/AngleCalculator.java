package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.ChartAngle;
import app.model.data.AngleType;

public class AngleCalculator implements Calculator {


    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {

        List<ChartAngle> angles = new ArrayList<>();

        double ascendant = ctx.normalize(ctx.getAscmc()[0]);
        double midheaven = ctx.normalize(ctx.getAscmc()[1]);
        addAngle(angles, AngleType.ASCENDANT, ascendant, ctx);
        addAngle(angles, AngleType.MIDHEAVEN, midheaven, ctx);
        addAngle(angles, AngleType.DESCENDANT, ctx.normalize(ascendant + 180.0), ctx);
        addAngle(angles, AngleType.IMUM_COELI, ctx.normalize(midheaven + 180.0), ctx);
        basicChart.setAngles(angles);
    }

    private void addAngle(List<ChartAngle> angles, AngleType name, double longitude, BasicCalculationContext ctx) {
        angles.add(new ChartAngle(name, ctx.round(longitude), ctx.signOf(longitude), ctx.round(ctx.degreeInSign(longitude))));
    }

}
