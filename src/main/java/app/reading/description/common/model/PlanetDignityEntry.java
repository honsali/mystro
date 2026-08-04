package app.reading.description.common.model;

import java.util.List;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.reading.description.common.data.DignityType;

public record PlanetDignityEntry(
        Planet planet,
        ZodiacSign sign,
        Planet domicileRuler,
        Planet exaltationRuler,
        Planet activeMasterTriplicityRuler,
        Planet participatingTriplicityRuler,
        Planet inactiveMasterTriplicityRuler,
        Planet termRuler,
        Planet faceRuler,
        Planet detrimentPlanet,
        Planet fallPlanet,
        List<DignityType> dignities,
        List<DignityType> debilities
) {
}
