package app.planetaryhours;

import app.chart.data.Planet;

import java.time.LocalDate;

public final class PlanetaryHourEntry {

    private final int sequence;
    private final int hour;
    private final LocalDate planetaryDayDate;
    private final PlanetaryHourPeriod period;
    private final Planet ruler;
    private final String rulerGlyph;
    private final LocalDate startDate;
    private final String startTime;
    private final LocalDate endDate;
    private final String endTime;
    private final double durationMinutes;
    private final PlanetaryHoursBoundary fullPlanetaryHourStart;
    private final PlanetaryHoursBoundary fullPlanetaryHourEnd;
    private final double fullDurationMinutes;
    private final PlanetaryHoursBoundary midpoint;
    private final PlanetaryHourChartSnapshot midpointChart;

    public PlanetaryHourEntry(int sequence,
                              int hour,
                              LocalDate planetaryDayDate,
                              PlanetaryHourPeriod period,
                              Planet ruler,
                              String rulerGlyph,
                              LocalDate startDate,
                              String startTime,
                              LocalDate endDate,
                              String endTime,
                              double durationMinutes,
                              PlanetaryHoursBoundary fullPlanetaryHourStart,
                              PlanetaryHoursBoundary fullPlanetaryHourEnd,
                              double fullDurationMinutes,
                              PlanetaryHoursBoundary midpoint,
                              PlanetaryHourChartSnapshot midpointChart) {
        this.sequence = sequence;
        this.hour = hour;
        this.planetaryDayDate = planetaryDayDate;
        this.period = period;
        this.ruler = ruler;
        this.rulerGlyph = rulerGlyph;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.fullPlanetaryHourStart = fullPlanetaryHourStart;
        this.fullPlanetaryHourEnd = fullPlanetaryHourEnd;
        this.fullDurationMinutes = fullDurationMinutes;
        this.midpoint = midpoint;
        this.midpointChart = midpointChart;
    }

    public int getSequence() {
        return sequence;
    }

    public int getHour() {
        return hour;
    }

    public LocalDate getPlanetaryDayDate() {
        return planetaryDayDate;
    }

    public PlanetaryHourPeriod getPeriod() {
        return period;
    }

    public Planet getRuler() {
        return ruler;
    }

    public String getRulerGlyph() {
        return rulerGlyph;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public PlanetaryHoursBoundary getFullPlanetaryHourStart() {
        return fullPlanetaryHourStart;
    }

    public PlanetaryHoursBoundary getFullPlanetaryHourEnd() {
        return fullPlanetaryHourEnd;
    }

    public double getFullDurationMinutes() {
        return fullDurationMinutes;
    }

    public PlanetaryHoursBoundary getMidpoint() {
        return midpoint;
    }

    public PlanetaryHourChartSnapshot getMidpointChart() {
        return midpointChart;
    }
}
