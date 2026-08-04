package app.planetaryhours;

import java.time.LocalDate;

public final class PlanetaryHoursBoundary {

    private final LocalDate date;
    private final String time;

    public PlanetaryHoursBoundary(LocalDate date, String time) {
        this.date = date;
        this.time = time;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
