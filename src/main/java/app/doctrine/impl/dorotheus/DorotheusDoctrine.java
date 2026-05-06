package app.doctrine.impl.dorotheus;

import app.basic.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;
import app.chart.model.NatalChart;
import app.doctrine.Doctrine;
import app.input.model.DoctrineInfo;

public final class DorotheusDoctrine extends Doctrine {

    private final DoctrineInfo doctrineInfo = new DoctrineInfo("dorotheus", "Dorotheus", HouseSystem.WHOLE_SIGN, Zodiac.TROPICAL, Terms.EGYPTIAN, Triplicity.DOROTHEAN, NodeType.MEAN);

    @Override
    public DoctrineInfo getDoctrineInfo() {
        return doctrineInfo;
    }

    @Override
    public void describe(CalculationContext ctx, NatalChart chart) {}
}
