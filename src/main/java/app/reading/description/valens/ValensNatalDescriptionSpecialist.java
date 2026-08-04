package app.reading.description.valens;

import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.calculator.EssentialDignityCalculator;
import app.reading.description.common.calculator.SyzygyCalculator;
import app.reading.description.valens.calculator.ValensAspectCalculator;
import app.reading.description.valens.calculator.ValensBeneficMaleficAssessmentCalculator;
import app.reading.description.valens.calculator.ValensDerivedHouseFrameCalculator;
import app.reading.description.valens.calculator.ValensDodecatemoriaCalculator;
import app.reading.description.valens.calculator.ValensDoryphoryCalculator;
import app.reading.description.valens.calculator.ValensFixedStarCalculator;
import app.reading.description.valens.calculator.ValensHouseTopicRulerCalculator;
import app.reading.description.valens.calculator.ValensLotAssessmentCalculator;
import app.reading.description.valens.calculator.ValensLotCalculator;
import app.reading.description.valens.calculator.ValensMercuryConfigurationCalculator;
import app.reading.description.valens.calculator.ValensMoonConfigurationCalculator;
import app.reading.description.valens.calculator.ValensPtolemaicHylegAlcocodenCalculator;
import app.reading.description.valens.calculator.ValensSolarConditionCalculator;
import app.reading.description.valens.calculator.ValensTopicAssessmentCalculator;
import app.reading.description.valens.calculator.ValensTriplicityLifePhaseCalculator;

public final class ValensNatalDescriptionSpecialist {
    private final CoreDoctrineInfo coreDoctrineInfo = new CoreDoctrineInfo("valens", "Vettius Valens", HouseSystem.WHOLE_SIGN, Terms.EGYPTIAN, Triplicity.DOROTHEAN);
    private final SyzygyCalculator syzygyCalculator = new SyzygyCalculator();
    private final EssentialDignityCalculator dignityCalculator = new EssentialDignityCalculator(Triplicity.DOROTHEAN);
    private final ValensLotCalculator lotCalculator = new ValensLotCalculator();
    private final ValensAspectCalculator aspectCalculator = new ValensAspectCalculator();
    private final ValensSolarConditionCalculator solarConditionCalculator = new ValensSolarConditionCalculator();
    private final ValensMercuryConfigurationCalculator mercuryConfigurationCalculator = new ValensMercuryConfigurationCalculator();
    private final ValensMoonConfigurationCalculator moonConfigurationCalculator = new ValensMoonConfigurationCalculator();
    private final ValensDoryphoryCalculator doryphoryCalculator = new ValensDoryphoryCalculator();
    private final ValensDodecatemoriaCalculator dodecatemoriaCalculator = new ValensDodecatemoriaCalculator();
    private final ValensTriplicityLifePhaseCalculator triplicityLifePhaseCalculator = new ValensTriplicityLifePhaseCalculator(Triplicity.DOROTHEAN);
    private final ValensBeneficMaleficAssessmentCalculator beneficMaleficAssessmentCalculator = new ValensBeneficMaleficAssessmentCalculator();
    private final ValensPtolemaicHylegAlcocodenCalculator hylegAlcocodenCalculator = new ValensPtolemaicHylegAlcocodenCalculator();
    private final ValensFixedStarCalculator fixedStarCalculator = new ValensFixedStarCalculator();
    private final ValensHouseTopicRulerCalculator houseTopicRulerCalculator = new ValensHouseTopicRulerCalculator();
    private final ValensLotAssessmentCalculator lotAssessmentCalculator = new ValensLotAssessmentCalculator();
    private final ValensDerivedHouseFrameCalculator derivedHouseFrameCalculator = new ValensDerivedHouseFrameCalculator();
    private final ValensTopicAssessmentCalculator topicAssessmentCalculator = new ValensTopicAssessmentCalculator();

    public CoreDoctrineInfo getCoreDoctrineInfo() {
        return coreDoctrineInfo;
    }

    public NatalChart calculate(Subject subject, BasicCalculator basicCalculator) {
        CalculationContext ctx = new CalculationContext(subject, coreDoctrineInfo);
        NatalChart chart = basicCalculator.calculate(ctx);
        enrich(ctx, chart);
        return chart;
    }

    public void enrich(CalculationContext ctx, NatalChart chart) {
        chart.setSyzygy(syzygyCalculator.calculate(ctx));
        chart.setLots(lotCalculator.calculate(ctx, chart));
        chart.applyAspects(aspectCalculator.calculate(chart));
        chart.applyDignityAssessments(dignityCalculator.calculate(chart));
        chart.applySolarConditions(solarConditionCalculator.calculate(chart));
        chart.setMercuryConfiguration(mercuryConfigurationCalculator.calculate(chart));
        chart.setMoonConfiguration(moonConfigurationCalculator.calculate(chart));
        chart.setDoryphories(doryphoryCalculator.calculate(chart));
        chart.setDodecatemoria(dodecatemoriaCalculator.calculate(chart));
        chart.setPtolemaicHylegAlcocoden(hylegAlcocodenCalculator.calculate(ctx, chart));
        chart.setTriplicityLifePhases(triplicityLifePhaseCalculator.calculate(chart));
        chart.setFixedStars(fixedStarCalculator.calculate(ctx, chart));
        chart.applyBeneficMaleficAssessments(beneficMaleficAssessmentCalculator.calculate(chart));
        chart.setHouseTopicRulers(houseTopicRulerCalculator.calculate(chart));
        chart.setLotAssessments(lotAssessmentCalculator.calculate(chart));
        chart.setDerivedHouseFrames(derivedHouseFrameCalculator.calculate(chart));
        chart.setTopicAssessments(topicAssessmentCalculator.calculate(chart));
    }
}
