package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.BaseCalculator;
import app.model.basic.PlanetPosition;
import app.model.data.Planet;
import app.model.data.Sect;
import app.model.data.SectCondition;
import app.model.data.SolarOrientation;

public class SectCalculator extends BaseCalculator {

    protected void executeCalculation() {
        PlanetPosition sun = ctx.planet(basicChart.getPlanets(), Planet.SUN);
        PlanetPosition moon = ctx.planet(basicChart.getPlanets(), Planet.MOON);
        PlanetPosition mercury = ctx.planet(basicChart.getPlanets(), Planet.MERCURY);
        boolean diurnal = sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sect", diurnal ? Sect.DIURNAL : Sect.NOCTURNAL);
        data.put("lightOfSect", diurnal ? Planet.SUN : Planet.MOON);
        data.put("lightContraryToSect", diurnal ? Planet.MOON : Planet.SUN);
        data.put("beneficOfSect", diurnal ? Planet.JUPITER : Planet.VENUS);
        data.put("beneficContraryToSect", diurnal ? Planet.VENUS : Planet.JUPITER);
        data.put("maleficOfSect", diurnal ? Planet.SATURN : Planet.MARS);
        data.put("maleficContraryToSect", diurnal ? Planet.MARS : Planet.SATURN);
        data.put("sunAboveHorizon", sun != null && sun.getHouse() >= 7 && sun.getHouse() <= 12);
        data.put("moonAboveHorizon", moon != null && moon.getHouse() >= 7 && moon.getHouse() <= 12);
        data.put("planetSects", planetSects(diurnal, mercury, sun));
        basicChart.setSect(data);
    }

    private Map<String, Object> planetSects(boolean chartDiurnal, PlanetPosition mercury, PlanetPosition sun) {
        Map<String, Object> planets = new LinkedHashMap<>();
        addPlanetSect(planets, Planet.SUN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.JUPITER, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.SATURN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.MOON, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.VENUS, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.MARS, Sect.NOCTURNAL, !chartDiurnal);
        Sect mercurySect = mercurySect(mercury, sun);
        Map<String, Object> mercuryData = new LinkedHashMap<>();
        mercuryData.put("sect", mercurySect);
        mercuryData.put("condition", mercurySect == (chartDiurnal ? Sect.DIURNAL : Sect.NOCTURNAL) ? SectCondition.OF_SECT : SectCondition.CONTRARY_TO_SECT);
        mercuryData.put("phaseRelativeToSun", mercurySect == Sect.DIURNAL ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL);
        planets.put(Planet.MERCURY.name(), mercuryData);
        return planets;
    }

    private void addPlanetSect(Map<String, Object> planets, Planet planet, Sect planetSect, boolean ofSect) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sect", planetSect);
        data.put("condition", ofSect ? SectCondition.OF_SECT : SectCondition.CONTRARY_TO_SECT);
        planets.put(planet.name(), data);
    }

    private Sect mercurySect(PlanetPosition mercury, PlanetPosition sun) {
        if (mercury == null || sun == null) {
            return Sect.UNKNOWN;
        }
        double delta = ctx.normalize(mercury.getLongitude() - sun.getLongitude());
        return delta > 180.0 ? Sect.DIURNAL : Sect.NOCTURNAL;
    }
}
