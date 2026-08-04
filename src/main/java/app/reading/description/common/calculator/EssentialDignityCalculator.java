package app.reading.description.common.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.chart.TraditionalTables;
import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.TriplicityRulers;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.model.PlanetDignityEntry;

public final class EssentialDignityCalculator {
    private final Triplicity triplicity;

    public EssentialDignityCalculator(Triplicity triplicity) {
        this.triplicity = triplicity;
    }

    public Map<Planet, PlanetDignityEntry> calculate(NatalChart chart) {
        Map<Planet, PlanetDignityEntry> result = new LinkedHashMap<>();
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;
        for (PlanetPosition position : chart.getPlanets()) {
            Planet planet = position.getPlanet();
            if (!TraditionalTables.isTraditionalPlanet(planet)) {
                continue;
            }

            Planet domicile = TraditionalTables.domicileRuler(position.getSign());
            Planet exaltation = TraditionalTables.exaltationRuler(position.getSign());
            TriplicityRulers triplicityRulers = TraditionalTables.triplicityRulers(position.getSign(), triplicity);
            Planet activeMasterTriplicityRuler = diurnal ? triplicityRulers.day() : triplicityRulers.night();
            Planet participatingTriplicityRuler = triplicityRulers.participating();
            Planet inactiveMasterTriplicityRuler = diurnal ? triplicityRulers.night() : triplicityRulers.day();
            Planet term = position.getTermRuler();
            Planet face = TraditionalTables.faceRuler(position.getSign(), position.getDegreeInSign());
            Planet detrimentPlanet = TraditionalTables.domicileRuler(TraditionalTables.opposite(position.getSign()));
            Planet fallPlanet = TraditionalTables.exaltationRuler(TraditionalTables.opposite(position.getSign()));

            List<DignityType> dignities = new ArrayList<>();
            List<DignityType> debilities = new ArrayList<>();
            if (planet == domicile)
                dignities.add(DignityType.DOMICILE);
            if (planet == exaltation)
                dignities.add(DignityType.EXALTATION);
            if (planet == activeMasterTriplicityRuler)
                dignities.add(DignityType.TRIPLICITY);
            if (planet == term)
                dignities.add(DignityType.TERM);
            if (planet == face)
                dignities.add(DignityType.FACE);
            if (planet == detrimentPlanet)
                debilities.add(DignityType.DETRIMENT);
            if (planet == fallPlanet)
                debilities.add(DignityType.FALL);

            result.put(planet, new PlanetDignityEntry(planet, position.getSign(), domicile, exaltation, activeMasterTriplicityRuler, participatingTriplicityRuler, inactiveMasterTriplicityRuler, term, face, detrimentPlanet, fallPlanet, List.copyOf(dignities), List.copyOf(debilities)));
        }
        return result;
    }

}
