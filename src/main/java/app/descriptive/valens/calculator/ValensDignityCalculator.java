package app.descriptive.valens.calculator;

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

public final class ValensDignityCalculator {
    private final Triplicity triplicity;

    public ValensDignityCalculator(Triplicity triplicity) {
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
            // Valens/Hellenistic dignity recognizes all three Dorothean triplicity rulers as participating in the triplicity dignity.
            if (planet == triplicityRulers.day() || planet == triplicityRulers.night() || planet == triplicityRulers.participating()) dignities.add(DignityType.TRIPLICITY);
            if (planet == term) dignities.add(DignityType.TERM);
            if (planet == face) dignities.add(DignityType.FACE);
            if (planet == detriment) debilities.add(DignityType.DETRIMENT);
            if (planet == fall) debilities.add(DignityType.FALL);

            result.put(planet, new PlanetDignityEntry(planet, position.getSign(), domicile, exaltation, triplicityRuler, term, face, detriment, fall, List.copyOf(dignities), List.copyOf(debilities)));
        }
        return result;
    }
}
