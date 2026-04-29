package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.BaseCalculator;
import app.model.basic.ChartAngle;

public class AngleCalculator extends BaseCalculator {


    protected void executeCalculation() {

        List<ChartAngle> angles = new ArrayList<>();

        double ascendant = ctx.normalize(ctx.getAscmc()[0]);
        double midheaven = ctx.normalize(ctx.getAscmc()[1]);
        addAngle(angles, "ASCENDANT", ascendant);
        addAngle(angles, "MIDHEAVEN", midheaven);
        addAngle(angles, "DESCENDANT", ctx.normalize(ascendant + 180.0));
        addAngle(angles, "IMUM_COELI", ctx.normalize(midheaven + 180.0));
        basicChart.setAngles(angles);
    }

    private void addAngle(List<ChartAngle> angles, String name, double longitude) {
        angles.add(new ChartAngle(name, ctx.round(longitude), ctx.signOf(longitude), ctx.round(ctx.degreeInSign(longitude))));
    }

}
