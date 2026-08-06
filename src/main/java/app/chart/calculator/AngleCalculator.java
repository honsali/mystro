package app.chart.calculator;

import java.util.ArrayList;
import java.util.List;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.Calculator;
import app.chart.data.AngleType;
import app.chart.model.ChartAngle;
import app.chart.model.Chart;

public class AngleCalculator implements Calculator {


    public void calculate(Chart chart, CalculationContext ctx) {

        List<ChartAngle> angles = new ArrayList<>();

        double ascendant = AstroMath.normalize(ctx.getAscmc()[0]);
        double midheaven = AstroMath.normalize(ctx.getAscmc()[1]);
        addAngle(angles, AngleType.ASCENDANT, ascendant);
        addAngle(angles, AngleType.MIDHEAVEN, midheaven);
        addAngle(angles, AngleType.DESCENDANT, AstroMath.normalize(ascendant + 180.0));
        addAngle(angles, AngleType.IMUM_COELI, AstroMath.normalize(midheaven + 180.0));
        chart.setAngles(angles);
    }

    private void addAngle(List<ChartAngle> angles, AngleType name, double longitude) {
        angles.add(new ChartAngle(name, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude)));
    }

}
