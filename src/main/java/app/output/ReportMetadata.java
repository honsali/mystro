package app.output;

public final class ReportMetadata {
    private final String engineVersion;

    public ReportMetadata(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getEngineVersion() { return engineVersion; }
}
