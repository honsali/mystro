package app.doctrine;

import app.basic.model.BasicChart;
import app.input.model.Input;

public interface Doctrine extends DoctrineDefinition {



    DescriptiveResult describe(Input input, BasicChart chart);
}
