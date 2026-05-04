package app.basic.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import app.basic.data.Planet;
import app.basic.data.Sect;

public final class BasicSect {
    private final Sect sect;
    private final Planet lightOfSect;
    private final Planet lightContraryToSect;
    private final Planet beneficOfSect;
    private final Planet beneficContraryToSect;
    private final Planet maleficOfSect;
    private final Planet maleficContraryToSect;
    private final boolean sunAboveHorizon;
    private final boolean moonAboveHorizon;
    private final double sunAltitude;
    private final double moonAltitude;
    private final Map<Planet, PlanetSectInfo> planetSects;

    public BasicSect(Sect sect, Planet lightOfSect, Planet lightContraryToSect, Planet beneficOfSect, Planet beneficContraryToSect, Planet maleficOfSect, Planet maleficContraryToSect, boolean sunAboveHorizon, boolean moonAboveHorizon, double sunAltitude, double moonAltitude, Map<Planet, PlanetSectInfo> planetSects) {
        this.sect = sect;
        this.lightOfSect = lightOfSect;
        this.lightContraryToSect = lightContraryToSect;
        this.beneficOfSect = beneficOfSect;
        this.beneficContraryToSect = beneficContraryToSect;
        this.maleficOfSect = maleficOfSect;
        this.maleficContraryToSect = maleficContraryToSect;
        this.sunAboveHorizon = sunAboveHorizon;
        this.moonAboveHorizon = moonAboveHorizon;
        this.sunAltitude = sunAltitude;
        this.moonAltitude = moonAltitude;
        this.planetSects = planetSects;
    }

    public Sect getSect() { return sect; }
    public Planet getLightOfSect() { return lightOfSect; }
    public Planet getLightContraryToSect() { return lightContraryToSect; }
    public Planet getBeneficOfSect() { return beneficOfSect; }
    public Planet getBeneficContraryToSect() { return beneficContraryToSect; }
    public Planet getMaleficOfSect() { return maleficOfSect; }
    public Planet getMaleficContraryToSect() { return maleficContraryToSect; }
    public boolean isSunAboveHorizon() { return sunAboveHorizon; }
    public boolean isMoonAboveHorizon() { return moonAboveHorizon; }
    public double getSunAltitude() { return sunAltitude; }
    public double getMoonAltitude() { return moonAltitude; }
    @JsonIgnore
    public Map<Planet, PlanetSectInfo> getPlanetSects() { return planetSects; }
}
