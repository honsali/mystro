package app.doctrine;

import app.basic.BasicCalculationContext;
import app.basic.model.BasicChart;

public interface Doctrine extends DoctrineDefinition {



    DescriptiveResult describe(BasicCalculationContext ctx, BasicChart chart);
}
