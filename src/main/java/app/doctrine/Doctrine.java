package app.doctrine;

import app.basic.CalculationContext;
import app.basic.model.BasicChart;
import app.basic.model.CalculationDefinition;

public interface Doctrine extends CalculationDefinition {



    DescriptiveResult describe(CalculationContext ctx, BasicChart chart);
}
