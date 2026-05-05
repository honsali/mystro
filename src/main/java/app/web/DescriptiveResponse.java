package app.web;

import app.output.DescriptiveAstrologyReport;

public final class DescriptiveResponse {

    private final DescriptiveAstrologyReport report;
    private final String suggestedFilename;

    public DescriptiveResponse(DescriptiveAstrologyReport report, String suggestedFilename) {
        this.report = report;
        this.suggestedFilename = suggestedFilename;
    }

    public DescriptiveAstrologyReport getReport() {
        return report;
    }

    public String getSuggestedFilename() {
        return suggestedFilename;
    }
}
