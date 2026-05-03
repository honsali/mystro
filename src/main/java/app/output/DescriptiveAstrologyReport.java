package app.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.basic.model.BasicChart;
import app.doctrine.DescriptiveResult;
import app.doctrine.Doctrine;
import app.input.model.CalculationSetting;
import app.input.model.Subject;

public class DescriptiveAstrologyReport implements AstrologyReport {
    private final String engineVersion;
    private final Subject subject;
    private final Doctrine doctrine;
    private final CalculationSetting calculationSetting;
    private final BasicChart basicChart;
    private final DescriptiveResult descriptive;

    public DescriptiveAstrologyReport(String engineVersion, Subject subject, Doctrine doctrine, CalculationSetting calculationSetting, BasicChart basicChart, DescriptiveResult descriptive) {
        this.engineVersion = engineVersion;
        this.subject = subject;
        this.doctrine = doctrine;
        this.calculationSetting = calculationSetting;
        this.basicChart = basicChart;
        this.descriptive = descriptive;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public Subject getSubject() {
        return subject;
    }

    public Doctrine getDoctrine() {
        return doctrine;
    }

    public CalculationSetting getCalculationSetting() {
        return calculationSetting;
    }

    public BasicChart getBasicChart() {
        return basicChart;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public DescriptiveResult getDescriptive() {
        return descriptive;
    }
}
