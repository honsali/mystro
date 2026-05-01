package app.model.input;

import app.model.data.CalculationPrecision;
import app.model.data.RoundingPolicy;

public final class CalculationSetting {
    private CalculationPrecision precision;
    private RoundingPolicy roundingPolicy;
    private boolean includeReferenceTables;

    public CalculationSetting(CalculationPrecision precision, RoundingPolicy roundingPolicy) {
        this(precision, roundingPolicy, false);
    }

    public CalculationSetting(CalculationPrecision precision, RoundingPolicy roundingPolicy, boolean includeReferenceTables) {
        this.precision = precision;
        this.roundingPolicy = roundingPolicy;
        this.includeReferenceTables = includeReferenceTables;
    }

    public CalculationPrecision getPrecision() {
        return precision;
    }

    public RoundingPolicy getRoundingPolicy() {
        return roundingPolicy;
    }

    public boolean isIncludeReferenceTables() {
        return includeReferenceTables;
    }
}
