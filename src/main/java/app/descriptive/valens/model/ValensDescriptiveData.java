package app.descriptive.valens.model;

import java.util.List;
import java.util.Map;
import app.basic.data.Planet;
import app.descriptive.common.data.LotName;
import app.descriptive.common.model.AspectEntry;
import app.descriptive.common.model.LotEntry;
import app.descriptive.common.model.PlanetDignityEntry;
import app.descriptive.common.model.PrenatalSyzygyEntry;
import app.descriptive.common.model.SolarConditionEntry;
import app.doctrine.DescriptiveResult;

public record ValensDescriptiveData(
        PrenatalSyzygyEntry syzygy,
        Map<LotName, LotEntry> lots,
        List<AspectEntry> aspects,
        Map<Planet, PlanetDignityEntry> dignities,
        Map<Planet, SolarConditionEntry> solarConditions
) implements DescriptiveResult {
}
