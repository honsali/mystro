package app.doctrine;

import app.model.basic.BasicChart;
import app.model.input.Input;

public interface Doctrine extends DoctrineDefinition {



    DescriptiveResult describe(Input input, BasicChart chart);
}
