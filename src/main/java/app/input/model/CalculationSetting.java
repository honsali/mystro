package app.input.model;

import app.basic.data.CalculationPrecision;

public final class CalculationSetting {
    private CalculationPrecision precision;


    public CalculationSetting(CalculationPrecision precision) {
        this.precision = precision;
    }

    public CalculationPrecision getPrecision() {
        return precision;
    }

}
