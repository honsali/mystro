package app.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.basic.model.BasicChart;
import app.doctrine.DescriptiveResult;
import app.input.model.Input;

public class DescriptiveAstrologyReport implements AstrologyReport {
    private final String engineVersion;
    private final Input input;
    private final BasicChart basicChart;
    private final DescriptiveResult descriptive;

    public DescriptiveAstrologyReport(String engineVersion, Input input, BasicChart basicChart, DescriptiveResult descriptive) {
        this.engineVersion = engineVersion;
        this.input = input;
        this.basicChart = basicChart;
        this.descriptive = descriptive;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public Input getInput() {
        return input;
    }

    public BasicChart getBasicChart() {
        return basicChart;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public DescriptiveResult getDescriptive() {
        return descriptive;
    }

}
