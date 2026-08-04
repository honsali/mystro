package app;

import java.util.List;

import app.chart.BasicCalculator;
import app.chart.model.Subject;
import app.input.ReadingInputMapper;
import app.reading.ReadingBundleReport;
import app.reading.description.NatalDescriptionReadingCalculator;
import app.reading.description.NatalDescriptionReadingReport;
import app.reading.description.valens.ValensNatalDescriptionSpecialist;

/**
 * Tiny non-web façade for calculating the current Mystro reading bundle.
 */
public final class ReadingBundleCalculator {
    private final NatalDescriptionReadingCalculator natalDescriptionReadingCalculator;

    public ReadingBundleCalculator() {
        this(new NatalDescriptionReadingCalculator(new BasicCalculator(), new ValensNatalDescriptionSpecialist()));
    }

    public ReadingBundleCalculator(NatalDescriptionReadingCalculator natalDescriptionReadingCalculator) {
        this.natalDescriptionReadingCalculator = natalDescriptionReadingCalculator;
    }

    public ReadingBundleReport calculate(String engineVersion, ReadingInputMapper.ResolvedBundle resolved) {
        Subject subject = resolved.subject();
        NatalDescriptionReadingReport natalDescription = natalDescriptionReadingCalculator.calculate(subject);

        return new ReadingBundleReport(engineVersion, subject, List.of(natalDescription));
    }
}
