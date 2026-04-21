package app.mystro.processor.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.common.NativeReportBuilder;
import app.common.model.ChartPoint;
import app.mystro.processor.MystroProcessor;

public final class DerivedChartsProcessor extends MystroProcessor {
    private static final List<String> IDS = List.of("AC", "MC", "Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Node", "Fortune", "Spirit", "Syzygy");

    @Override
    public void populate(NativeReportBuilder builder) {
        Map<String, ChartPoint> source = sourcePoints(builder);
        builder.dodecatemoria(transform(source, 12.0));
        builder.novenaria(transformNovenaria(source));
        builder.antiscia(transformAntiscia(source, false));
        builder.contraAntiscia(transformAntiscia(source, true));
    }

    private Map<String, ChartPoint> sourcePoints(NativeReportBuilder builder) {
        Map<String, ChartPoint> points = new LinkedHashMap<>();
        ChartPoint asc = builder.houses().get("AC");
        ChartPoint mc = builder.houses().get("MC");
        if (asc == null || mc == null) {
            throw new IllegalArgumentException("Houses must be populated before derived charts");
        }
        points.put("AC", asc);
        points.put("MC", mc);
        for (String id : IDS) {
            if (id.equals("AC") || id.equals("MC")) {
                continue;
            }
            ChartPoint point = builder.planets().get(id);
            if (point == null) {
                throw new IllegalArgumentException("Missing source point for derived charts: " + id);
            }
            points.put(id, point);
        }
        return points;
    }

    private Map<String, ChartPoint> transform(Map<String, ChartPoint> source, double factor) {
        Map<String, ChartPoint> result = new LinkedHashMap<>();
        for (Map.Entry<String, ChartPoint> entry : source.entrySet()) {
            ChartPoint point = entry.getValue();
            double lon = normalize(point.absoluteLon() + point.signLon() * (factor - 1.0));
            result.put(entry.getKey(), remap(entry.getKey(), lon));
        }
        return result;
    }

    private Map<String, ChartPoint> transformNovenaria(Map<String, ChartPoint> source) {
        Map<String, ChartPoint> result = new LinkedHashMap<>();
        for (Map.Entry<String, ChartPoint> entry : source.entrySet()) {
            ChartPoint point = entry.getValue();
            double lon = novenariaBase(point.sign()) + point.signLon() * 9.0;
            result.put(entry.getKey(), remap(entry.getKey(), normalize(lon)));
        }
        return result;
    }

    private Map<String, ChartPoint> transformAntiscia(Map<String, ChartPoint> source, boolean contra) {
        Map<String, ChartPoint> result = new LinkedHashMap<>();
        for (Map.Entry<String, ChartPoint> entry : source.entrySet()) {
            double lon = normalize(180.0 - entry.getValue().absoluteLon());
            if (contra) {
                lon = normalize(lon + 180.0);
            }
            result.put(entry.getKey(), remap(entry.getKey(), lon));
        }
        return result;
    }

    private double novenariaBase(String sign) {
        return switch (sign) {
            case "Aries", "Leo", "Sagittarius" -> 0.0;
            case "Taurus", "Virgo", "Capricorn" -> 270.0;
            case "Gemini", "Libra", "Aquarius" -> 180.0;
            case "Cancer", "Scorpio", "Pisces" -> 90.0;
            default -> throw new IllegalArgumentException("Unsupported sign for novenaria: " + sign);
        };
    }

    private ChartPoint remap(String id, double lon) {
        return new ChartPoint(id, signOf(lon), signLon(lon), lon, 0, 0.0, false);
    }
}
