package app.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import app.basic.model.BasicChart;
import app.input.model.Input;

public class DescriptiveAstrologyReport implements AstrologyReport {
    private final String engineVersion;
    private final Input input;
    private final BasicChart basicChart;
    private final Map<String, Object> descriptive;

    public DescriptiveAstrologyReport(String engineVersion, Input input, BasicChart basicChart, Map<String, Object> descriptive) {
        this.engineVersion = engineVersion;
        this.input = input;
        this.basicChart = basicChart;
        this.descriptive = descriptive == null ? Map.of() : Map.copyOf(descriptive);
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
    public Map<String, Object> getDescriptive() {
        return descriptive;
    }

}
