package app.chart.data;

public enum AspectType {
    CONJUNCTION(0.0),
    SEXTILE(60.0),
    SQUARE(90.0),
    TRINE(120.0),
    OPPOSITION(180.0);

    private final double exactAngle;

    AspectType(double exactAngle) {
        this.exactAngle = exactAngle;
    }

    public double getExactAngle() {
        return exactAngle;
    }
}
