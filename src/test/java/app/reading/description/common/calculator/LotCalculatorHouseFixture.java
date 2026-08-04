package app.reading.description.common.calculator;

import app.chart.CalculationContext;
import app.chart.model.NatalChart;
import app.reading.CoreDoctrineInfo;

public final class LotCalculatorHouseFixture {
    public static CalculationContext ctx(CoreDoctrineInfo coreDoctrineInfo) {
        return new CalculationContext(app.testing.SyntheticTestData.subject(), coreDoctrineInfo);
    }

    public static NatalChart chart(double ascendant, app.chart.data.Sect sectValue) {
        NatalChart chart = new NatalChart();
        chart.setAngles(java.util.List.of(new app.chart.model.ChartAngle(app.chart.data.AngleType.ASCENDANT, ascendant, app.chart.data.ZodiacSign.CANCER, ascendant % 30.0)));
        chart.setSect(new app.chart.model.BasicSect(sectValue, app.chart.data.Planet.SUN, app.chart.data.Planet.MOON, app.chart.data.Planet.JUPITER, app.chart.data.Planet.VENUS, app.chart.data.Planet.SATURN, app.chart.data.Planet.MARS, sectValue == app.chart.data.Sect.DIURNAL,
                sectValue != app.chart.data.Sect.DIURNAL, 1.0, -1.0, java.util.Map.of()));
        return chart;
    }

    private LotCalculatorHouseFixture() {}
}
