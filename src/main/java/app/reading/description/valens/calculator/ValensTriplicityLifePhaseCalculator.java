package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.List;
import app.chart.TraditionalTables;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Sect;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.chart.model.TriplicityRulers;
import app.reading.description.common.data.TriplicityLifePhase;
import app.reading.description.common.data.TriplicityLifeReference;
import app.reading.description.common.data.TriplicityRulerRole;
import app.reading.description.common.model.TriplicityLifePhaseEntry;

public final class ValensTriplicityLifePhaseCalculator {
    private record AgeBounds(Double startAgeYears, Double endAgeYears) {
    }

    private final Triplicity triplicity;

    public ValensTriplicityLifePhaseCalculator(Triplicity triplicity) {
        this.triplicity = triplicity;
    }

    public List<TriplicityLifePhaseEntry> calculate(NatalChart chart) {
        if (chart.getSect() == null || chart.getPlanets() == null) {
            return List.of();
        }
        Double indicatedYears = indicatedVitalityYears(chart);
        List<TriplicityLifePhaseEntry> phases = new ArrayList<>();
        addPlanetReference(phases, chart, indicatedYears, TriplicityLifeReference.LIGHT_OF_SECT, chart.getSect().getLightOfSect());
        addPlanetReference(phases, chart, indicatedYears, TriplicityLifeReference.SUN, Planet.SUN);
        addPlanetReference(phases, chart, indicatedYears, TriplicityLifeReference.MOON, Planet.MOON);
        addFortuneReference(phases, chart, indicatedYears);
        return List.copyOf(phases);
    }

    private void addPlanetReference(List<TriplicityLifePhaseEntry> phases, NatalChart chart, Double indicatedYears, TriplicityLifeReference reference, Planet planet) {
        PlanetPosition position = chart.requirePlanet(planet);
        addReference(phases, chart, indicatedYears, reference, planet.name(), position.getSign(), position.getHouse());
    }

    private void addFortuneReference(List<TriplicityLifePhaseEntry> phases, NatalChart chart, Double indicatedYears) {
        if (chart.getLots() == null) {
            return;
        }
        chart.getLots().stream().filter(lot -> "FORTUNE".equals(lot.name())).findFirst().ifPresent(lot -> addReference(phases, chart, indicatedYears, TriplicityLifeReference.LOT_FORTUNE, lot.name(), lot.sign(), lot.house()));
    }

    private void addReference(List<TriplicityLifePhaseEntry> phases, NatalChart chart, Double indicatedYears, TriplicityLifeReference reference, String referenceName, ZodiacSign referenceSign, Integer referenceHouse) {
        TriplicityRulers rulers = TraditionalTables.triplicityRulers(referenceSign, triplicity);
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;
        addPhase(phases, chart, indicatedYears, reference, referenceName, referenceSign, referenceHouse, TriplicityLifePhase.EARLY_LIFE, TriplicityRulerRole.PRIMARY_RULER, diurnal ? rulers.day() : rulers.night());
        addPhase(phases, chart, indicatedYears, reference, referenceName, referenceSign, referenceHouse, TriplicityLifePhase.MIDDLE_LIFE, TriplicityRulerRole.SECONDARY_RULER, diurnal ? rulers.night() : rulers.day());
        addPhase(phases, chart, indicatedYears, reference, referenceName, referenceSign, referenceHouse, TriplicityLifePhase.LATE_LIFE, TriplicityRulerRole.PARTICIPATING_RULER, rulers.participating());
    }

    private void addPhase(List<TriplicityLifePhaseEntry> phases, NatalChart chart, Double indicatedYears, TriplicityLifeReference reference, String referenceName, ZodiacSign referenceSign, Integer referenceHouse, TriplicityLifePhase phase, TriplicityRulerRole role, Planet ruler) {
        if (ruler == null) {
            return;
        }
        PlanetPosition rulerPosition = chart.requirePlanet(ruler);
        PlanetPointEntry point = planetPoint(chart, ruler);
        AgeBounds ageBounds = ageBounds(phase, indicatedYears);
        phases.add(new TriplicityLifePhaseEntry(reference, referenceName, referenceSign, referenceHouse, phase, ageBounds.startAgeYears(), ageBounds.endAgeYears(), role, ruler, rulerPosition.getHouse(), rulerPosition.getWholeSignHouse(), rulerPosition.getAngularity(), rulerPosition.getRetrograde(),
                point == null ? null : point.solarCondition(), point == null ? List.of() : point.dignities(), point == null ? List.of() : point.debilities()));
    }

    private AgeBounds ageBounds(TriplicityLifePhase phase, Double indicatedYears) {
        if (indicatedYears == null) {
            return new AgeBounds(null, null);
        }
        double third = indicatedYears / 3.0;
        return switch (phase) {
            case EARLY_LIFE -> new AgeBounds(0.0, third);
            case MIDDLE_LIFE -> new AgeBounds(third, 2.0 * third);
            case LATE_LIFE -> new AgeBounds(2.0 * third, indicatedYears);
        };
    }

    private Double indicatedVitalityYears(NatalChart chart) {
        if (chart.getPtolemaicHylegAlcocoden() == null || chart.getPtolemaicHylegAlcocoden().vitalityYears() == null) {
            return null;
        }
        return chart.getPtolemaicHylegAlcocoden().vitalityYears().indicatedYears();
    }

    private PlanetPointEntry planetPoint(NatalChart chart, Planet planet) {
        if (chart.getPoints() == null) {
            return null;
        }
        PointEntry point = chart.getPoints().get(PointKey.of(planet));
        return point instanceof PlanetPointEntry planetPoint ? planetPoint : null;
    }
}
