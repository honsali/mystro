package app.reading;

import app.chart.model.Subject;

import java.util.List;

public final class ReadingBundleReport {
    private final String engineVersion;
    private final Subject subject;
    private final List<?> reading;

    public ReadingBundleReport(String engineVersion, Subject subject, List<?> reading) {
        this.engineVersion = engineVersion;
        this.subject = subject;
        this.reading = List.copyOf(reading);
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public Subject getSubject() {
        return subject;
    }

    public List<?> getReading() {
        return reading;
    }
}
