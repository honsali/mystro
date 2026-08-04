package app.reading.description;

import app.chart.BasicCalculator;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.description.valens.ValensNatalDescriptionSpecialist;

public final class NatalDescriptionReadingCalculator {

    private final BasicCalculator basicCalculator;
    private final ValensNatalDescriptionSpecialist coreSpecialist;

    public NatalDescriptionReadingCalculator(BasicCalculator basicCalculator, ValensNatalDescriptionSpecialist coreSpecialist) {
        this.basicCalculator = basicCalculator;
        this.coreSpecialist = coreSpecialist;
    }

    public NatalDescriptionReadingReport calculate(Subject subject) {
        NatalChart natalChart = coreSpecialist.calculate(subject, basicCalculator);
        return new NatalDescriptionReadingReport(coreSpecialist.getCoreDoctrineInfo(), natalChart);
    }
}
