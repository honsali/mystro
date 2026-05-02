package app.descriptive.common.model;

import java.util.List;
import app.basic.data.Planet;
import app.basic.data.ZodiacSign;
import app.descriptive.common.data.DignityType;

public record PlanetDignityEntry(
        Planet planet,
        ZodiacSign sign,
        Planet domicileRuler,
        Planet exaltationRuler,
        Planet triplicityRuler,
        Planet termRuler,
        Planet faceRuler,
        Planet detrimentRuler,
        Planet fallRuler,
        List<DignityType> dignities,
        List<DignityType> debilities
) {
}
