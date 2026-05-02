package app.basic.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import app.basic.data.Sect;
import app.basic.data.SectCondition;
import app.basic.data.SolarOrientation;

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
