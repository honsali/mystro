package app.output;

import app.chart.model.NatalChart;
import app.input.model.DoctrineInfo;
import app.input.model.Subject;

public class DescriptiveAstrologyReport {
    private final String engineVersion;
    private final Subject subject;
    private final DoctrineInfo doctrine;
    private final NatalChart natalChart;

    public DescriptiveAstrologyReport(String engineVersion, Subject subject, DoctrineInfo doctrine, NatalChart natalChart) {
        this.engineVersion = engineVersion;
        this.subject = subject;
        this.doctrine = doctrine;
        this.natalChart = natalChart;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public Subject getSubject() {
        return subject;
    }

    public DoctrineInfo getDoctrine() {
        return doctrine;
    }

    public NatalChart getNatalChart() {
        return natalChart;
    }
}
