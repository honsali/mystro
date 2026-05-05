package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.AstroMath;
import app.basic.Calculator;
import app.basic.CalculationContext;
import app.chart.model.NatalChart;
import app.chart.model.BasicSect;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;

public class SectCalculator implements Calculator {

    public void calculate(NatalChart natalChart, CalculationContext ctx) {
        PlanetPosition sun = natalChart.requirePlanet(Planet.SUN);
        PlanetPosition moon = natalChart.requirePlanet(Planet.MOON);
        PlanetPosition mercury = natalChart.requirePlanet(Planet.MERCURY);
        double sunAltitude = sun.getAltitude();
        double moonAltitude = moon.getAltitude();
        boolean sunAboveHorizon = sun.getAboveHorizon();
        boolean moonAboveHorizon = moon.getAboveHorizon();
        boolean diurnal = sunAboveHorizon;
        BasicSect data = new BasicSect(
                diurnal ? Sect.DIURNAL : Sect.NOCTURNAL,
                diurnal ? Planet.SUN : Planet.MOON,
                diurnal ? Planet.MOON : Planet.SUN,
                diurnal ? Planet.JUPITER : Planet.VENUS,
                diurnal ? Planet.VENUS : Planet.JUPITER,
                diurnal ? Planet.SATURN : Planet.MARS,
                diurnal ? Planet.MARS : Planet.SATURN,
                sunAboveHorizon,
                moonAboveHorizon,
                sunAltitude,
                moonAltitude,
                planetSects(diurnal, mercury, sun)
        );
        natalChart.setSect(data);
    }

    private Map<Planet, PlanetSectInfo> planetSects(boolean chartDiurnal, PlanetPosition mercury, PlanetPosition sun) {
        Map<Planet, PlanetSectInfo> planets = new LinkedHashMap<>();
        addPlanetSect(planets, Planet.SUN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.JUPITER, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.SATURN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.MOON, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.VENUS, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.MARS, Sect.NOCTURNAL, !chartDiurnal);
        Sect mercurySect = mercurySect(mercury, sun);
        planets.put(Planet.MERCURY, new PlanetSectInfo(
                mercurySect,
                mercurySect == (chartDiurnal ? Sect.DIURNAL : Sect.NOCTURNAL) ? SectCondition.OF_SECT : SectCondition.CONTRARY_TO_SECT,
                mercurySect == Sect.DIURNAL ? SolarOrientation.ORIENTAL : SolarOrientation.OCCIDENTAL
        ));
        return planets;
    }

    private void addPlanetSect(Map<Planet, PlanetSectInfo> planets, Planet planet, Sect planetSect, boolean ofSect) {
        planets.put(planet, new PlanetSectInfo(planetSect, ofSect ? SectCondition.OF_SECT : SectCondition.CONTRARY_TO_SECT));
    }

    private Sect mercurySect(PlanetPosition mercury, PlanetPosition sun) {
        return AstroMath.orientationToSun(mercury.getLongitude(), sun.getLongitude()) == SolarOrientation.ORIENTAL ? Sect.DIURNAL : Sect.NOCTURNAL;
    }
}
