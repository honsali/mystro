package app.model.input;

import app.model.data.CalculationPrecision;

public final class CalculationSetting {
    private CalculationPrecision precision;
    private boolean includeReferenceTables;

    public CalculationSetting(CalculationPrecision precision) {
        this(precision, false);
    }

    public CalculationSetting(CalculationPrecision precision, boolean includeReferenceTables) {
        this.precision = precision;
        this.includeReferenceTables = includeReferenceTables;
    }

    public CalculationPrecision getPrecision() {
        return precision;
    }

    public boolean isIncludeReferenceTables() {
        return includeReferenceTables;
    }
}
