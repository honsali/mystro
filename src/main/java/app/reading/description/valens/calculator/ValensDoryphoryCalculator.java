package app.reading.description.valens.calculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import app.chart.AstroMath;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.SectCondition;
import app.chart.data.SolarOrientation;
import app.chart.data.ZodiacSign;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PlanetSectInfo;
import app.reading.description.common.data.DignityType;
import app.reading.description.common.data.DoryphoryDirection;
import app.reading.description.common.data.DoryphoryKind;
import app.reading.description.common.model.DoryphoryEntry;

public final class ValensDoryphoryCalculator {
    private static final double SUN_CONJUNCTION_MINIMUM_SEPARATION = 8.5;
    private static final List<Planet> LIGHTS = List.of(Planet.SUN, Planet.MOON);
    private static final List<Planet> TRADITIONAL_NON_LIGHTS = List.of(Planet.MERCURY, Planet.VENUS, Planet.MARS, Planet.JUPITER, Planet.SATURN);
    private static final List<Planet> SUPERIOR_PLANETS = List.of(Planet.MARS, Planet.JUPITER, Planet.SATURN);
    private static final List<Planet> INFERIOR_PLANETS = List.of(Planet.MERCURY, Planet.VENUS);
    private static final List<DignityType> DORYPHORY_DIGNITIES = List.of(DignityType.DOMICILE, DignityType.EXALTATION, DignityType.TRIPLICITY, DignityType.TERM);

    public List<DoryphoryEntry> calculate(Chart chart) {
        if (chart.getSect() == null || chart.getPlanets() == null) {
            return List.of();
        }
        Map<Planet, PlanetSectInfo> planetSects = chart.getSect().getPlanetSects();
        List<DoryphoryEntry> doryphories = new ArrayList<>();

        for (Planet light : LIGHTS) {
            PlanetPosition lightPosition = chart.requirePlanet(light);
            boolean lightOfSect = light == chart.getSect().getLightOfSect();
            for (Planet candidate : TRADITIONAL_NON_LIGHTS) {
                PlanetPosition candidatePosition = chart.requirePlanet(candidate);
                AspectType aspect = aspectBySign(lightPosition.getSign(), candidatePosition.getSign());
                if (aspect == null) {
                    continue;
                }
                if (aspect == AspectType.CONJUNCTION && light == Planet.SUN && AstroMath.rawAngularSeparation(lightPosition.getLongitude(), candidatePosition.getLongitude()) < SUN_CONJUNCTION_MINIMUM_SEPARATION) {
                    continue;
                }
                boolean ofSect = planetSects != null && planetSects.get(candidate) != null && planetSects.get(candidate).getCondition() == SectCondition.OF_SECT;
                if (!ofSect) {
                    continue;
                }
                int signDistance = signDistance(lightPosition.getSign(), candidatePosition.getSign());
                DoryphoryDirection direction = direction(lightPosition.getLongitude(), candidatePosition.getLongitude());
                List<DignityType> qualifyingDignities = qualifyingDignities(chart, candidate);
                List<DoryphoryKind> kinds = kinds(chart, light, lightOfSect, candidate, signDistance, direction, qualifyingDignities);
                doryphories.add(new DoryphoryEntry(light, lightOfSect, candidate, primaryKind(kinds), kinds, qualifyingDignities, strengthScore(kinds), direction, aspect, signDistance, lightPosition.getWholeSignHouse(), candidatePosition.getWholeSignHouse(), true));
            }
        }
        doryphories.sort(Comparator.comparing((DoryphoryEntry entry) -> !entry.lightOfSect()).thenComparingInt(entry -> aspectStrength(entry.aspect())).thenComparing(Comparator.comparingInt(DoryphoryEntry::strengthScore).reversed()).thenComparing(DoryphoryEntry::light)
                .thenComparing(DoryphoryEntry::spearBearer));
        return List.copyOf(doryphories);
    }

    private List<DoryphoryKind> kinds(Chart chart, Planet light, boolean lightOfSect, Planet candidate, int signDistance, DoryphoryDirection direction, List<DignityType> qualifyingDignities) {
        List<DoryphoryKind> kinds = new ArrayList<>();
        if (signDistance == 9) {
            kinds.add(DoryphoryKind.BY_OVERCOMING);
        }
        if (!qualifyingDignities.isEmpty()) {
            kinds.add(DoryphoryKind.BY_DIGNITY);
        }
        if (properPhase(chart, light, lightOfSect, candidate, direction)) {
            kinds.add(DoryphoryKind.BY_PHASE);
        }
        kinds.add(DoryphoryKind.BY_CONFIGURATION);
        return List.copyOf(kinds);
    }

    private boolean properPhase(Chart chart, Planet light, boolean lightOfSect, Planet candidate, DoryphoryDirection direction) {
        if (!lightOfSect) {
            return false;
        }
        if (light == Planet.SUN) {
            SolarOrientation orientation = solarOrientation(chart, candidate);
            if (SUPERIOR_PLANETS.contains(candidate)) {
                return orientation == SolarOrientation.ORIENTAL;
            }
            if (INFERIOR_PLANETS.contains(candidate)) {
                return orientation == SolarOrientation.OCCIDENTAL;
            }
        }
        if (light == Planet.MOON) {
            return direction == DoryphoryDirection.TRAILING;
        }
        return false;
    }

    private SolarOrientation solarOrientation(Chart chart, Planet candidate) {
        PlanetPointEntry point = planetPoint(chart, candidate);
        if (point != null && point.solarPhase() != null) {
            return point.solarPhase();
        }
        return AstroMath.orientationToSun(chart.requirePlanet(candidate).getLongitude(), chart.requirePlanet(Planet.SUN).getLongitude());
    }

    private List<DignityType> qualifyingDignities(Chart chart, Planet candidate) {
        PlanetPointEntry point = planetPoint(chart, candidate);
        if (point == null || point.dignities() == null) {
            return List.of();
        }
        return DORYPHORY_DIGNITIES.stream().filter(point.dignities()::contains).toList();
    }

    private PlanetPointEntry planetPoint(Chart chart, Planet planet) {
        if (chart.getPoints() == null) {
            return null;
        }
        if (chart.getPoints().get(PointKey.of(planet)) instanceof PlanetPointEntry planetPoint) {
            return planetPoint;
        }
        return null;
    }

    private DoryphoryKind primaryKind(List<DoryphoryKind> kinds) {
        return kinds.stream().max(Comparator.comparingInt(this::kindStrength)).orElse(DoryphoryKind.BY_CONFIGURATION);
    }

    private int strengthScore(List<DoryphoryKind> kinds) {
        return kinds.stream().mapToInt(this::kindStrength).sum();
    }

    private int kindStrength(DoryphoryKind kind) {
        return switch (kind) {
            case BY_OVERCOMING -> 4;
            case BY_DIGNITY -> 3;
            case BY_PHASE -> 2;
            case BY_CONFIGURATION -> 1;
        };
    }

    private AspectType aspectBySign(ZodiacSign a, ZodiacSign b) {
        int distance = signDistance(a, b);
        return switch (distance) {
            case 0 -> AspectType.CONJUNCTION;
            case 2, 10 -> AspectType.SEXTILE;
            case 3, 9 -> AspectType.SQUARE;
            case 4, 8 -> AspectType.TRINE;
            case 6 -> AspectType.OPPOSITION;
            default -> null;
        };
    }

    private int signDistance(ZodiacSign a, ZodiacSign b) {
        return Math.floorMod(b.ordinal() - a.ordinal(), 12);
    }

    private DoryphoryDirection direction(double lightLongitude, double candidateLongitude) {
        double delta = AstroMath.normalize(candidateLongitude - lightLongitude);
        if (delta == 0.0) {
            return DoryphoryDirection.CO_PRESENT;
        }
        return delta < 180.0 ? DoryphoryDirection.TRAILING : DoryphoryDirection.LEADING;
    }

    private int aspectStrength(AspectType aspect) {
        return switch (aspect) {
            case CONJUNCTION -> 0;
            case TRINE -> 1;
            case SEXTILE -> 2;
            case SQUARE -> 3;
            case OPPOSITION -> 4;
        };
    }
}
