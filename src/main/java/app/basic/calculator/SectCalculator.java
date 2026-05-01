package app.basic.calculator;

import java.util.LinkedHashMap;
import java.util.Map;
import app.basic.Calculator;
import app.basic.BasicCalculationContext;
import app.model.basic.BasicChart;
import app.model.basic.BasicSect;
import app.model.basic.PlanetPosition;
import app.model.basic.PlanetSectInfo;
import app.model.data.Planet;
import app.model.data.Sect;
import app.model.data.SectCondition;
import app.model.data.SolarOrientation;

public class SectCalculator implements Calculator {

    public void calculate(BasicChart basicChart, BasicCalculationContext ctx) {
        PlanetPosition sun = requiredPlanet(Planet.SUN, basicChart, ctx);
        PlanetPosition moon = requiredPlanet(Planet.MOON, basicChart, ctx);
        PlanetPosition mercury = requiredPlanet(Planet.MERCURY, basicChart, ctx);
        double sunAltitude = ctx.round(ctx.horizontalAltitude(sun.getLongitude(), sun.getLatitude()));
        double moonAltitude = ctx.round(ctx.horizontalAltitude(moon.getLongitude(), moon.getLatitude()));
        boolean sunAboveHorizon = sunAltitude >= 0.0;
        boolean moonAboveHorizon = moonAltitude >= 0.0;
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
                planetSects(diurnal, mercury, sun, ctx)
        );
        basicChart.setSect(data);
    }

    private PlanetPosition requiredPlanet(Planet planet, BasicChart basicChart, BasicCalculationContext ctx) {
        PlanetPosition position = ctx.planet(basicChart.getPlanets(), planet);
        if (position == null) {
            throw new IllegalArgumentException("Calculation failed: missing required planet " + planet);
        }
        return position;
    }

    private Map<Planet, PlanetSectInfo> planetSects(boolean chartDiurnal, PlanetPosition mercury, PlanetPosition sun, BasicCalculationContext ctx) {
        Map<Planet, PlanetSectInfo> planets = new LinkedHashMap<>();
        addPlanetSect(planets, Planet.SUN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.JUPITER, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.SATURN, Sect.DIURNAL, chartDiurnal);
        addPlanetSect(planets, Planet.MOON, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.VENUS, Sect.NOCTURNAL, !chartDiurnal);
        addPlanetSect(planets, Planet.MARS, Sect.NOCTURNAL, !chartDiurnal);
        Sect mercurySect = mercurySect(mercury, sun, ctx);
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

    private Sect mercurySect(PlanetPosition mercury, PlanetPosition sun, BasicCalculationContext ctx) {
        double delta = ctx.normalize(mercury.getLongitude() - sun.getLongitude());
        return delta > 180.0 ? Sect.DIURNAL : Sect.NOCTURNAL;
    }
}
