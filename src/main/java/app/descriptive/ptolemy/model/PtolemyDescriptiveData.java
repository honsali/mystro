package app.descriptive.ptolemy.model;

import java.util.List;
import java.util.Map;
import app.basic.data.Planet;
import app.descriptive.common.model.AspectEntry;
import app.descriptive.common.model.PlanetDignityEntry;
import app.descriptive.common.model.PrenatalSyzygyEntry;
import app.doctrine.DescriptiveResult;

public record PtolemyDescriptiveData(
        PrenatalSyzygyEntry syzygy,
        List<AspectEntry> aspects,
        Map<Planet, PlanetDignityEntry> dignities
) implements DescriptiveResult {
}
