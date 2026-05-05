package app.chart.model;

import app.chart.data.MoonPhaseName;

public final class MoonPhase {
    private final double illuminationFraction;
    private final MoonPhaseName phase;
    private final boolean waxing;

    public MoonPhase(double illuminationFraction, MoonPhaseName phase, boolean waxing) {
        this.illuminationFraction = illuminationFraction;
        this.phase = phase;
        this.waxing = waxing;
    }

    public double getIlluminationFraction() { return illuminationFraction; }
    public MoonPhaseName getPhase() { return phase; }
    public boolean isWaxing() { return waxing; }
}
