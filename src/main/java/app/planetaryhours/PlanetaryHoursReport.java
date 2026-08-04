package app.planetaryhours;

import app.chart.data.Planet;

import java.util.List;

public final class PlanetaryHoursReport {

    public static final String METHOD_ID = "PLANETARY_HOURS_CIVIL_DAY_V1";

    private final String engineVersion;
    private final String methodId;
    private final PlanetaryHoursBirthData birthData;
    private final Planet dayRuler;
    private final PlanetaryHoursBoundary coverageStart;
    private final PlanetaryHoursBoundary coverageEnd;
    private final PlanetaryHoursBoundary sunrise;
    private final PlanetaryHoursBoundary sunset;
    private final PlanetaryHoursBoundary nextSunrise;
    private final double dayHourDurationMinutes;
    private final double nightHourDurationMinutes;
    private final List<PlanetaryHourEntry> hours;

    public PlanetaryHoursReport(String engineVersion,
                                PlanetaryHoursInput input,
                                PlanetaryHoursCalculation calculation) {
        this.engineVersion = engineVersion;
        this.methodId = METHOD_ID;
        this.birthData = new PlanetaryHoursBirthData(input);
        this.dayRuler = calculation.getDayRuler();
        this.coverageStart = calculation.getCoverageStart();
        this.coverageEnd = calculation.getCoverageEnd();
        this.sunrise = calculation.getSunrise();
        this.sunset = calculation.getSunset();
        this.nextSunrise = calculation.getNextSunrise();
        this.dayHourDurationMinutes = calculation.getDayHourDurationMinutes();
        this.nightHourDurationMinutes = calculation.getNightHourDurationMinutes();
        this.hours = List.copyOf(calculation.getHours());
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public String getMethodId() {
        return methodId;
    }

    public PlanetaryHoursBirthData getBirthData() {
        return birthData;
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
