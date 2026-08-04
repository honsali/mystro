package app.reading.description.common.calculator;

import app.chart.AstroMath;

public final class DoctrineLotMath {
    public double normalize360(double degrees) {
        return AstroMath.normalize(degrees);
    }

    public double forwardArc(double from, double to) {
        return normalize360(to - from);
    }

    public double lot(double ascendant, double from, double to) {
        return normalize360(ascendant + forwardArc(from, to));
    }
}
