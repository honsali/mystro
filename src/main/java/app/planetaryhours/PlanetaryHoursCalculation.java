package app.planetaryhours;

import app.chart.data.Planet;

import java.util.List;

public final class PlanetaryHoursCalculation {

    private final Planet dayRuler;
    private final PlanetaryHoursBoundary coverageStart;
    private final PlanetaryHoursBoundary coverageEnd;
    private final PlanetaryHoursBoundary sunrise;
    private final PlanetaryHoursBoundary sunset;
    private final PlanetaryHoursBoundary nextSunrise;
    private final double dayHourDurationMinutes;
    private final double nightHourDurationMinutes;
    private final List<PlanetaryHourEntry> hours;

    public PlanetaryHoursCalculation(Planet dayRuler,
                                     PlanetaryHoursBoundary coverageStart,
                                     PlanetaryHoursBoundary coverageEnd,
                                     PlanetaryHoursBoundary sunrise,
                                     PlanetaryHoursBoundary sunset,
                                     PlanetaryHoursBoundary nextSunrise,
                                     double dayHourDurationMinutes,
                                     double nightHourDurationMinutes,
                                     List<PlanetaryHourEntry> hours) {
        this.dayRuler = dayRuler;
        this.coverageStart = coverageStart;
        this.coverageEnd = coverageEnd;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.nextSunrise = nextSunrise;
        this.dayHourDurationMinutes = dayHourDurationMinutes;
        this.nightHourDurationMinutes = nightHourDurationMinutes;
        this.hours = List.copyOf(hours);
    }

    public Planet getDayRuler() {
        return dayRuler;
    }

    public PlanetaryHoursBoundary getCoverageStart() {
        return coverageStart;
    }

    public PlanetaryHoursBoundary getCoverageEnd() {
        return coverageEnd;
    }

    public PlanetaryHoursBoundary getSunrise() {
        return sunrise;
    }

    public PlanetaryHoursBoundary getSunset() {
        return sunset;
    }

    public PlanetaryHoursBoundary getNextSunrise() {
        return nextSunrise;
    }

    public double getDayHourDurationMinutes() {
        return dayHourDurationMinutes;
    }

    public double getNightHourDurationMinutes() {
        return nightHourDurationMinutes;
    }

    public List<PlanetaryHourEntry> getHours() {
        return hours;
    }
}
