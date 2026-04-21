package app.common.model;

public record ChartPoint(String id, String sign, double signLon, double absoluteLon, int wholeSignHouse, double speed, boolean retrograde) {
}
