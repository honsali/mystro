package app.basic.calculator;

import java.util.ArrayList;
import java.util.List;
import app.basic.AstroMath;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.ChartAngle;
import app.chart.data.AngleType;

public class AngleCalculator implements Calculator {


    public void calculate(NatalChart natalChart, CalculationContext ctx) {

        List<ChartAngle> angles = new ArrayList<>();

        double ascendant = AstroMath.normalize(ctx.getAscmc()[0]);
        double midheaven = AstroMath.normalize(ctx.getAscmc()[1]);
        addAngle(angles, AngleType.ASCENDANT, ascendant);
        addAngle(angles, AngleType.MIDHEAVEN, midheaven);
        addAngle(angles, AngleType.DESCENDANT, AstroMath.normalize(ascendant + 180.0));
        addAngle(angles, AngleType.IMUM_COELI, AstroMath.normalize(midheaven + 180.0));
        natalChart.setAngles(angles);
    }

    private void addAngle(List<ChartAngle> angles, AngleType name, double longitude) {
        angles.add(new ChartAngle(name, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude)));
    }

}
