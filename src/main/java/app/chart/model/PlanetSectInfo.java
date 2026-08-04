package app.chart.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PlanetSectInfo {
    private final Sect sect;
    private final SectCondition condition;
    private final SolarOrientation phaseRelativeToSun;

    public PlanetSectInfo(Sect sect, SectCondition condition) {
        this(sect, condition, null);
    }

    public PlanetSectInfo(Sect sect, SectCondition condition, SolarOrientation phaseRelativeToSun) {
        this.sect = sect;
        this.condition = condition;
        this.phaseRelativeToSun = phaseRelativeToSun;
    }

    public Sect getSect() { return sect; }
    public SectCondition getCondition() { return condition; }
    public SolarOrientation getPhaseRelativeToSun() { return phaseRelativeToSun; }
}
