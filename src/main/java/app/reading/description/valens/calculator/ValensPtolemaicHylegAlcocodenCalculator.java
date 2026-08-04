package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.Angularity;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Sect;
import app.chart.data.Terms;
import app.chart.data.ZodiacSign;
import app.chart.model.NatalChart;
import app.chart.model.PairwiseRelation;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.reading.description.common.calculator.DoctrineLotMath;
import app.reading.description.common.calculator.SyzygyCalculator;
import app.reading.description.common.data.AphesisBasis;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.SolarCondition;
import app.reading.description.common.data.VitalityYearsTier;
import app.reading.description.common.model.HylegAlcocodenEntry;
import app.reading.description.common.model.PrenatalSyzygyEntry;

public final class ValensPtolemaicHylegAlcocodenCalculator {
    private static final Set<Integer> PTOLEMAIC_PROROGATIVE_HOUSES = Set.of(1, 7, 9, 10, 11);
    private static final List<Planet> BENEFICS = List.of(Planet.VENUS, Planet.JUPITER);
    private static final List<Planet> MALEFICS = List.of(Planet.MARS, Planet.SATURN);
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final DoctrineLotMath lotMath = new DoctrineLotMath();

    public HylegAlcocodenEntry calculate(CalculationContext ctx, NatalChart chart) {
        boolean diurnal = chart.getSect().getSect() == Sect.DIURNAL;
        List<HylegAlcocodenEntry.HylegCandidate> candidates = candidates(ctx, chart, diurnal);
        HylegAlcocodenEntry.HylegCandidate selected = candidates.stream().filter(HylegAlcocodenEntry.HylegCandidate::eligible).findFirst().orElse(candidates.get(0));

        HylegAlcocodenEntry.HylegPoint hyleg = new HylegAlcocodenEntry.HylegPoint(selected.point(), selected.longitude(), selected.sign(), selected.degreeInSign(), selected.house(), selected.aphesisBasis(),
                selected.eligible() ? selected.reason() : "Fallback: no candidate was in a configured aphetic degree zone or prorogative place.");
        HylegAlcocodenEntry.AlcocodenPoint alcocoden = alcocoden(chart, hyleg, diurnal);

        return new HylegAlcocodenEntry("ptolemaic",
                "Ptolemaic hyleg/alcocoden: candidates are checked in sect-priority order against the Ascendant aphetic degree zone (ASC-5° to ASC+25°) and configured prorogative places (1, 10, 11, 7, 9); alcocoden is the configured dignity lord with strongest Ptolemaic dignity over the hyleg. Vitality years are a symbolic traditional vitality measure, not a deterministic lifespan prediction.",
                hyleg, alcocoden, vitalityYears(chart, alcocoden), List.copyOf(candidates));
    }

    private List<HylegAlcocodenEntry.HylegCandidate> candidates(CalculationContext ctx, NatalChart chart, boolean diurnal) {
        List<HylegAlcocodenEntry.HylegCandidate> candidates = new ArrayList<>();
        if (diurnal) {
            addPlanetCandidate(candidates, chart, Planet.SUN, "Sect light checked first in a diurnal nativity.");
            addPlanetCandidate(candidates, chart, Planet.MOON, "Moon checked after the Sun in a diurnal nativity.");
        } else {
            addPlanetCandidate(candidates, chart, Planet.MOON, "Sect light checked first in a nocturnal nativity.");
            addPlanetCandidate(candidates, chart, Planet.SUN, "Sun checked after the Moon in a nocturnal nativity.");
        }
        addSyzygyCandidate(candidates, ctx, chart);
        addAngleCandidate(candidates, chart);
        addFortuneCandidate(candidates, ctx, chart);
        return candidates;
    }

    private void addPlanetCandidate(List<HylegAlcocodenEntry.HylegCandidate> candidates, NatalChart chart, Planet planet, String eligibleReason) {
        PlanetPosition position = chart.requirePlanet(planet);
        candidates.add(candidate(chart, planet.name(), position.getLongitude(), position.getHouse(), eligibleReason));
    }

    private void addSyzygyCandidate(List<HylegAlcocodenEntry.HylegCandidate> candidates, CalculationContext ctx, NatalChart chart) {
        PrenatalSyzygyEntry syzygy = chart.getSyzygy() == null ? syzygyCalculator.calculate(ctx) : chart.getSyzygy();
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        candidates.add(candidate(chart, "PRENATAL_SYZYGY", syzygy.longitude(), ctx.houseOf(syzygy.longitude(), asc), "Prenatal syzygy checked after the luminaries."));
    }

    private void addAngleCandidate(List<HylegAlcocodenEntry.HylegCandidate> candidates, NatalChart chart) {
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        candidates.add(candidate(chart, "ASCENDANT", asc, 1, "Ascendant checked after luminaries and syzygy."));
    }

    private void addFortuneCandidate(List<HylegAlcocodenEntry.HylegCandidate> candidates, CalculationContext ctx, NatalChart chart) {
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        double fortune = lotMath.lot(asc, chart.requirePlanet(Planet.SUN).getLongitude(), chart.requirePlanet(Planet.MOON).getLongitude());
        candidates.add(candidate(chart, "PTOLEMAIC_FORTUNE", fortune, ctx.houseOf(fortune, asc), "Ptolemaic Lot of Fortune checked as final fallback candidate using unreversed Sun-to-Moon arc."));
    }

    private HylegAlcocodenEntry.HylegCandidate candidate(NatalChart chart, String point, double longitude, int house, String eligibleReason) {
        boolean degreeZoneEligible = inAscendantApheticDegreeZone(chart, longitude);
        boolean houseEligible = PTOLEMAIC_PROROGATIVE_HOUSES.contains(house);
        AphesisBasis aphesisBasis = degreeZoneEligible ? AphesisBasis.DEGREE_ZONE : houseEligible ? AphesisBasis.WHOLE_HOUSE : null;
        boolean eligible = aphesisBasis != null;
        return new HylegAlcocodenEntry.HylegCandidate(point, longitude, AstroMath.signOf(longitude), AstroMath.degreeInSign(longitude), house, eligible, aphesisBasis,
                eligible ? eligibleReason + " Aphesis basis: " + aphesisBasis + "." : "Not in Ascendant aphetic degree zone (ASC-5° to ASC+25°) or configured Ptolemaic prorogative houses: 1, 10, 11, 7, 9.");
    }

    private boolean inAscendantApheticDegreeZone(NatalChart chart, double longitude) {
        double asc = chart.requireAngle(AngleType.ASCENDANT).getLongitude();
        double deltaFromAsc = AstroMath.normalize(longitude - asc);
        return deltaFromAsc <= 25.0 || deltaFromAsc >= 355.0;
    }

    private HylegAlcocodenEntry.AlcocodenPoint alcocoden(NatalChart chart, HylegAlcocodenEntry.HylegPoint hyleg, boolean diurnal) {
        Map<Planet, List<DignityType>> claims = dignityClaims(hyleg.longitude(), diurnal);
        Planet selected = null;
        List<DignityType> selectedClaims = List.of();
        int selectedScore = -1;
        for (Map.Entry<Planet, List<DignityType>> entry : claims.entrySet()) {
            Planet planet = entry.getKey();
            if (!configuredBySign(chart.requirePlanet(planet).getSign(), hyleg.sign())) {
                continue;
            }
            int score = score(entry.getValue());
            if (score > selectedScore) {
                selected = planet;
                selectedClaims = entry.getValue();
                selectedScore = score;
            }
        }
        if (selected == null) {
            return null;
        }
        return new HylegAlcocodenEntry.AlcocodenPoint(selected, selectedScore, selectedClaims, true, "Selected as the strongest Ptolemaic dignity lord configured to the hyleg by sign.");
    }

    private HylegAlcocodenEntry.VitalityYearsIndicator vitalityYears(NatalChart chart, HylegAlcocodenEntry.AlcocodenPoint alcocoden) {
        if (alcocoden == null) {
            return null;
        }
        Planet planet = alcocoden.planet();
        PlanetPosition position = chart.requirePlanet(planet);
        VitalityYearsTier tier = vitalityTier(position.getAngularity());
        double baseYears = planetaryYears(planet, tier);
        List<HylegAlcocodenEntry.VitalityYearsModifier> modifiers = vitalityModifiers(chart, planet);
        double indicatedYears = Math.max(0.0, baseYears + modifiers.stream().mapToDouble(HylegAlcocodenEntry.VitalityYearsModifier::deltaYears).sum());
        return new HylegAlcocodenEntry.VitalityYearsIndicator(planet, position.getAngularity(), baseYears, tier, modifiers, indicatedYears,
                "Traditional vitality-years indicator: Ptolemaic planetary years by alcocoden angularity with benefic/malefic minor-year modifiers. This is symbolic vitality doctrine, not a deterministic lifespan prediction.");
    }

    private List<HylegAlcocodenEntry.VitalityYearsModifier> vitalityModifiers(NatalChart chart, Planet alcocoden) {
        List<HylegAlcocodenEntry.VitalityYearsModifier> modifiers = new ArrayList<>();
        for (Planet planet : BENEFICS) {
            if (planet == alcocoden) {
                continue;
            }
            PairwiseRelation.AspectByDegree aspect = aspectByDegree(chart, alcocoden, planet);
            if (aspect != null) {
                modifiers.add(new HylegAlcocodenEntry.VitalityYearsModifier(planet, aspect.getNearestAspect(), planetaryYears(planet, VitalityYearsTier.LEAST), "Benefic configured to alcocoden by degree within moiety adds its minor years."));
            }
        }
        for (Planet planet : MALEFICS) {
            if (planet == alcocoden) {
                continue;
            }
            PairwiseRelation.AspectByDegree aspect = aspectByDegree(chart, alcocoden, planet);
            if (aspect != null) {
                modifiers.add(new HylegAlcocodenEntry.VitalityYearsModifier(planet, aspect.getNearestAspect(), -planetaryYears(planet, VitalityYearsTier.LEAST), "Malefic configured to alcocoden by degree within moiety subtracts its minor years."));
            }
        }
        PlanetPointEntry alcocodenPoint = planetPoint(chart, alcocoden);
        if (alcocodenPoint != null && (alcocodenPoint.solarCondition() == SolarCondition.COMBUST || alcocodenPoint.solarCondition() == SolarCondition.UNDER_BEAMS)) {
            modifiers.add(new HylegAlcocodenEntry.VitalityYearsModifier(alcocoden, AspectType.CONJUNCTION, -planetaryYears(alcocoden, VitalityYearsTier.LEAST), "Alcocoden under the Sun's beams subtracts its own minor years."));
        }
        return List.copyOf(modifiers);
    }

    private PairwiseRelation.AspectByDegree aspectByDegree(NatalChart chart, Planet first, Planet second) {
        if (chart.getPairwiseRelations() == null) {
            return null;
        }
        PointKey firstKey = PointKey.of(first);
        PointKey secondKey = PointKey.of(second);
        return chart.getPairwiseRelations().stream().filter(relation -> (relation.getPointAName() == firstKey && relation.getPointBName() == secondKey) || (relation.getPointAName() == secondKey && relation.getPointBName() == firstKey)).map(PairwiseRelation::getAspectByDegree)
                .filter(aspect -> aspect != null).findFirst().orElse(null);
    }

    private PlanetPointEntry planetPoint(NatalChart chart, Planet planet) {
        if (chart.getPoints() == null) {
            return null;
        }
        if (chart.getPoints().get(PointKey.of(planet)) instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        return null;
    }

    private VitalityYearsTier vitalityTier(Angularity angularity) {
        return switch (angularity) {
            case ANGULAR -> VitalityYearsTier.GREATEST;
            case SUCCEDENT, UNKNOWN -> VitalityYearsTier.MEAN;
            case CADENT -> VitalityYearsTier.LEAST;
        };
    }

    private double planetaryYears(Planet planet, VitalityYearsTier tier) {
        return switch (planet) {
            case SATURN -> switch (tier) {
                case GREATEST -> 57.0;
                case MEAN -> 43.5;
                case LEAST -> 30.0;
            };
            case JUPITER -> switch (tier) {
                case GREATEST -> 79.0;
                case MEAN -> 45.0;
                case LEAST -> 12.0;
            };
            case MARS -> switch (tier) {
                case GREATEST -> 66.0;
                case MEAN -> 40.5;
                case LEAST -> 15.0;
            };
            case SUN -> switch (tier) {
                case GREATEST -> 120.0;
                case MEAN -> 69.5;
                case LEAST -> 19.0;
            };
            case VENUS -> switch (tier) {
                case GREATEST -> 82.0;
                case MEAN -> 45.0;
                case LEAST -> 8.0;
            };
            case MERCURY -> switch (tier) {
                case GREATEST -> 76.0;
                case MEAN -> 48.0;
                case LEAST -> 20.0;
            };
            case MOON -> switch (tier) {
                case GREATEST -> 108.0;
                case MEAN -> 66.5;
                case LEAST -> 25.0;
            };
            case NORTH_NODE, SOUTH_NODE -> 0.0;
        };
    }

    private Map<Planet, List<DignityType>> dignityClaims(double longitude, boolean diurnal) {
        ZodiacSign sign = AstroMath.signOf(longitude);
        double degreeInSign = AstroMath.degreeInSign(longitude);
        Map<Planet, List<DignityType>> claims = new LinkedHashMap<>();
        addClaim(claims, TraditionalTables.domicileRuler(sign), DignityType.DOMICILE);
        addClaim(claims, TraditionalTables.exaltationRuler(sign), DignityType.EXALTATION);
        addPtolemaicTriplicityClaims(claims, sign, diurnal);
        addClaim(claims, TraditionalTables.termRuler(longitude, Terms.PTOLEMAIC), DignityType.TERM);
        addClaim(claims, TraditionalTables.faceRuler(sign, degreeInSign), DignityType.FACE);
        return claims;
    }

    private void addPtolemaicTriplicityClaims(Map<Planet, List<DignityType>> claims, ZodiacSign sign, boolean diurnal) {
        switch (TraditionalTables.element(sign)) {
            case FIRE -> addClaim(claims, diurnal ? Planet.SUN : Planet.JUPITER, DignityType.TRIPLICITY);
            case EARTH -> addClaim(claims, diurnal ? Planet.VENUS : Planet.MOON, DignityType.TRIPLICITY);
            case AIR -> addClaim(claims, diurnal ? Planet.SATURN : Planet.MERCURY, DignityType.TRIPLICITY);
            case WATER -> {
                addClaim(claims, Planet.MARS, DignityType.TRIPLICITY);
                addClaim(claims, diurnal ? Planet.VENUS : Planet.MOON, DignityType.TRIPLICITY);
            }
        }
    }

    private void addClaim(Map<Planet, List<DignityType>> claims, Planet planet, DignityType dignity) {
        if (planet == null) {
            return;
        }
        List<DignityType> current = claims.getOrDefault(planet, List.of());
        List<DignityType> updated = new ArrayList<>(current);
        updated.add(dignity);
        claims.put(planet, List.copyOf(updated));
    }

    private int score(List<DignityType> claims) {
        int score = 0;
        for (DignityType claim : claims) {
            score += switch (claim) {
                case DOMICILE -> 5;
                case EXALTATION -> 4;
                case TRIPLICITY -> 3;
                case TERM -> 2;
                case FACE -> 1;
                case DETRIMENT, FALL -> 0;
            };
        }
        return score;
    }

    private boolean configuredBySign(ZodiacSign planetSign, ZodiacSign hylegSign) {
        int distance = Math.floorMod(planetSign.ordinal() - hylegSign.ordinal(), 12);
        return distance == 0 || distance == 2 || distance == 3 || distance == 4 || distance == 6 || distance == 8 || distance == 9 || distance == 10;
    }
}
