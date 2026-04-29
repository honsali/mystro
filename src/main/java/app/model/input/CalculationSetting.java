package app.model.input;

import app.model.data.CalculationPrecision;
import app.model.data.NodeType;
import app.model.data.PositionType;
import app.model.data.ReferenceFrame;
import app.model.data.RoundingPolicy;

public final class CalculationSetting {
    private CalculationPrecision precision;
    private RoundingPolicy roundingPolicy;
    private NodeType nodeType;
    private PositionType positionType;
    private ReferenceFrame frame;
    private String ephemerisVersion;
    private boolean includeReferenceTables;

    public CalculationSetting(CalculationPrecision precision, RoundingPolicy roundingPolicy) {
        this(precision, roundingPolicy, false);
    }

    public CalculationSetting(CalculationPrecision precision, RoundingPolicy roundingPolicy, boolean includeReferenceTables) {
        this.precision = precision;
        this.roundingPolicy = roundingPolicy;
        this.nodeType = NodeType.MEAN;
        this.positionType = PositionType.APPARENT;
        this.frame = ReferenceFrame.GEOCENTRIC;
        this.ephemerisVersion = "swisseph-2.10.03";
        this.includeReferenceTables = includeReferenceTables;
    }

    public CalculationPrecision getPrecision() {
        return precision;
    }

    public RoundingPolicy getRoundingPolicy() {
        return roundingPolicy;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public ReferenceFrame getFrame() {
        return frame;
    }

    public String getEphemerisVersion() {
        return ephemerisVersion;
    }

    public boolean isIncludeReferenceTables() {
        return includeReferenceTables;
    }
}
