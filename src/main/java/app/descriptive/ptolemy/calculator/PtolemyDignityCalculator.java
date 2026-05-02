package app.descriptive.ptolemy.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.basic.TraditionalTables;
import app.basic.data.Planet;
import app.basic.data.Sect;
import app.basic.data.Triplicity;
import app.basic.model.BasicChart;
import app.basic.model.PlanetPosition;
import app.basic.model.TriplicityRulers;
import app.descriptive.common.data.DignityType;
import app.descriptive.common.model.PlanetDignityEntry;

public final class PtolemyDignityCalculator {
    private final Triplicity triplicity;

    public PtolemyDignityCalculator(Triplicity triplicity) {
        this.triplicity = triplicity;
    }

    public Map<Planet, PlanetDignityEntry> calculate(BasicChart chart) {
        Map<Planet, PlanetDignityEntry> result = new LinkedHashMap<>();
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;
        for (PlanetPosition position : chart.getPlanets()) {
            if (!TraditionalTables.isTraditionalPlanet(position.getPlanet())) {
                continue;
            }
            Planet domicile = TraditionalTables.domicileRuler(position.getSign());
            Planet exaltation = TraditionalTables.exaltationRuler(position.getSign());
            TriplicityRulers triplicityRulers = TraditionalTables.triplicityRulers(position.getSign(), triplicity);
            Planet triplicityRuler = diurnal ? triplicityRulers.day() : triplicityRulers.night();
            Planet term = position.getTermRuler();
            Planet face = TraditionalTables.faceRuler(position.getSign(), position.getDegreeInSign());
            Planet detriment = TraditionalTables.domicileRuler(TraditionalTables.opposite(position.getSign()));
            Planet fall = TraditionalTables.exaltationRuler(TraditionalTables.opposite(position.getSign()));

            List<DignityType> dignities = new ArrayList<>();
            List<DignityType> debilities = new ArrayList<>();
            Planet planet = position.getPlanet();
            if (planet == domicile) dignities.add(DignityType.DOMICILE);
            if (planet == exaltation) dignities.add(DignityType.EXALTATION);
            // Ptolemaic triplicity dignity is limited here to the sect-correct day/night ruler; there is no participating ruler in the Ptolemaic table.
            if (planet == triplicityRuler) dignities.add(DignityType.TRIPLICITY);
            if (planet == term) dignities.add(DignityType.TERM);
            if (planet == face) dignities.add(DignityType.FACE);
            if (planet == detriment) debilities.add(DignityType.DETRIMENT);
            if (planet == fall) debilities.add(DignityType.FALL);

            result.put(planet, new PlanetDignityEntry(planet, position.getSign(), domicile, exaltation, triplicityRuler, term, face, detriment, fall, List.copyOf(dignities), List.copyOf(debilities)));
        }
        return result;
    }
}
