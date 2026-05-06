package app.doctrine.impl.valens;

import app.basic.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;
import app.chart.model.NatalChart;
import app.descriptive.valens.calculator.ValensDescriptiveCalculator;
import app.doctrine.Doctrine;
import app.input.model.DoctrineInfo;

public final class ValensDoctrine extends Doctrine {

    private final DoctrineInfo doctrineInfo = new DoctrineInfo("valens", "Valens", HouseSystem.WHOLE_SIGN, Zodiac.TROPICAL, Terms.EGYPTIAN, Triplicity.DOROTHEAN, NodeType.MEAN);

    private final ValensDescriptiveCalculator descriptiveCalculator = new ValensDescriptiveCalculator(Triplicity.DOROTHEAN);

    @Override
    public DoctrineInfo getDoctrineInfo() {
        return doctrineInfo;
    }



    @Override
    public void describe(CalculationContext ctx, NatalChart chart) {
        descriptiveCalculator.calculate(ctx, chart);
    }
}
