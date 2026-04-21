package app.mystro.processor.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.common.model.NativeAspect;
import app.common.model.NativeChart;
import app.mystro.processor.MystroProcessor;

public final class AspectsProcessor extends MystroProcessor {
    private static final List<String> MAIN_ASPECT_PLANETS = List.of("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn");
    private static final List<String> OTHER_ASPECT_OBJECTS = List.of("Ascendant", "MC", "Node");
    private static final List<String> OTHER_ASPECT_PLANETS = List.of("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Node");
    private static final List<AspectDefinition> DEGREE_ASPECTS = List.of(
            new AspectDefinition("Conjunction", 0.0, 8.0),
            new AspectDefinition("Sextile", 60.0, 5.5),
            new AspectDefinition("Square", 90.0, 6.0),
            new AspectDefinition("Trine", 120.0, 6.0),
            new AspectDefinition("Opposition", 180.0, 8.0));

    @Override
    public void populate(NativeReportBuilder builder) {
        NativeChart chart = builder.nativeChart();
        if (chart == null) {
            throw new IllegalArgumentException("Chart must be populated before aspects");
        }
        builder.mainAspects(buildMainAspects(chart));
        builder.otherAspects(buildOtherAspects(chart));
    }

    private List<NativeAspect> buildMainAspects(NativeChart chart) {
        List<NativeAspect> aspects = new ArrayList<>();
        for (int i = 0; i < MAIN_ASPECT_PLANETS.size(); i++) {
            ChartPoint left = requirePoint(chart, MAIN_ASPECT_PLANETS.get(i));
            for (int j = i + 1; j < MAIN_ASPECT_PLANETS.size(); j++) {
                ChartPoint right = requirePoint(chart, MAIN_ASPECT_PLANETS.get(j));
                AspectMatch match = detectDegreeAspect(left, right);
                if (match != null) {
                    aspects.add(new NativeAspect(MAIN_ASPECT_PLANETS.get(i), match.name(), MAIN_ASPECT_PLANETS.get(j), match.orb(), match.motion()));
                }
            }
        }
        return aspects;
    }

    private List<NativeAspect> buildOtherAspects(NativeChart chart) {
        Map<String, ChartPoint> objects = new LinkedHashMap<>();
        objects.put("Ascendant", requirePoint(chart, "Asc"));
        objects.put("MC", requirePoint(chart, "MC"));
        objects.put("Node", requirePoint(chart, "North Node"));

        Map<String, ChartPoint> planets = new LinkedHashMap<>();
        planets.put("Sun", requirePoint(chart, "Sun"));
        planets.put("Moon", requirePoint(chart, "Moon"));
        planets.put("Mercury", requirePoint(chart, "Mercury"));
        planets.put("Venus", requirePoint(chart, "Venus"));
        planets.put("Mars", requirePoint(chart, "Mars"));
        planets.put("Jupiter", requirePoint(chart, "Jupiter"));
        planets.put("Saturn", requirePoint(chart, "Saturn"));
        planets.put("Node", requirePoint(chart, "North Node"));

        List<NativeAspect> aspects = new ArrayList<>();
        for (String leftName : OTHER_ASPECT_OBJECTS) {
            ChartPoint left = objects.get(leftName);
            for (String rightName : OTHER_ASPECT_PLANETS) {
                if (leftName.equals(rightName)) {
                    continue;
                }
                String aspect = detectSignAspect(left.sign(), planets.get(rightName).sign());
                if (aspect != null) {
                    aspects.add(new NativeAspect(leftName, aspect, rightName));
                }
            }
        }
        return aspects;
    }

    private ChartPoint requirePoint(NativeChart chart, String id) {
        ChartPoint point = chart.point(id);
        if (point == null) {
            throw new IllegalArgumentException("Missing chart point for aspects: " + id);
        }
        return point;
    }

    private AspectMatch detectDegreeAspect(ChartPoint left, ChartPoint right) {
        double delta = shortestArc(left.absoluteLon(), right.absoluteLon());
        AspectDefinition best = null;
        double bestOrb = Double.MAX_VALUE;
        for (AspectDefinition definition : DEGREE_ASPECTS) {
            double orb = Math.abs(delta - definition.angle());
            if (orb <= definition.maxOrb() && orb < bestOrb) {
                best = definition;
                bestOrb = orb;
            }
        }
        if (best == null) {
            return null;
        }
        String motion = determineMotion(left, right, best.angle(), bestOrb);
        return new AspectMatch(best.name(), bestOrb, motion);
    }

    private String determineMotion(ChartPoint left, ChartPoint right, double exactAngle, double currentOrb) {
        double dt = 1.0 / 24.0;
        double futureLeft = normalize(left.absoluteLon() + left.speed() * dt);
        double futureRight = normalize(right.absoluteLon() + right.speed() * dt);
        double futureOrb = Math.abs(shortestArc(futureLeft, futureRight) - exactAngle);
        return futureOrb < currentOrb ? "Applying" : "Separating";
    }

    private double shortestArc(double leftLon, double rightLon) {
        double delta = Math.abs(normalize(leftLon - rightLon));
        return delta > 180.0 ? 360.0 - delta : delta;
    }

    private String detectSignAspect(String leftSign, String rightSign) {
        int leftIndex = app.common.Config.SIGNS.indexOf(leftSign);
        int rightIndex = app.common.Config.SIGNS.indexOf(rightSign);
        int delta = Math.floorMod(rightIndex - leftIndex, 12);
        return switch (delta) {
            case 0 -> "Conjunction";
            case 2, 10 -> "Sextile";
            case 3, 9 -> "Square";
            case 4, 8 -> "Trine";
            case 6 -> "Opposition";
            default -> null;
        };
    }

    private record AspectDefinition(String name, double angle, double maxOrb) {
    }

    private record AspectMatch(String name, double orb, String motion) {
    }
}
