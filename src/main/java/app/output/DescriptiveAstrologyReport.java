package app.output;

import app.basic.model.NatalChart;
import app.doctrine.Doctrine;
import app.input.model.CalculationSetting;
import app.input.model.Subject;

public class DescriptiveAstrologyReport implements AstrologyReport {
    private final String engineVersion;
    private final Subject subject;
    private final Doctrine doctrine;
    private final CalculationSetting calculationSetting;
    private final NatalChart natalChart;

    public DescriptiveAstrologyReport(String engineVersion, Subject subject, Doctrine doctrine, CalculationSetting calculationSetting, NatalChart natalChart) {
        this.engineVersion = engineVersion;
        this.subject = subject;
        this.doctrine = doctrine;
        this.calculationSetting = calculationSetting;
        this.natalChart = natalChart;
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

    public NatalChart getNatalChart() {
        return natalChart;
    }
}
