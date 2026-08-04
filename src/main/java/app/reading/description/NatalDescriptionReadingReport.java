package app.reading.description;

import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.reading.CoreDoctrineInfo;

public final class NatalDescriptionReadingReport {
    private final String id;
    private final String coreDoctrine;
    private final CoreConventions coreConventions;
    private final NatalChart natalChart;

    public NatalDescriptionReadingReport(CoreDoctrineInfo coreDoctrineInfo, NatalChart natalChart) {
        this.id = "NATAL_DESCRIPTION";
        this.coreDoctrine = coreDoctrineInfo.getId().toUpperCase();
        this.coreConventions = new CoreConventions(
                coreDoctrineInfo.getHouseSystem(),
                coreDoctrineInfo.getTerms(),
                coreDoctrineInfo.getTriplicity()
        );
        this.natalChart = natalChart;
    }

    public String getId() {
        return id;
    }

    public String getCoreDoctrine() {
        return coreDoctrine;
    }

    public CoreConventions getCoreConventions() {
        return coreConventions;
    }

    public NatalChart getNatalChart() {
        return natalChart;
    }

    public static final class CoreConventions {
        private final HouseSystem houseSystem;
        private final Terms terms;
        private final Triplicity triplicity;

        public CoreConventions(HouseSystem houseSystem, Terms terms, Triplicity triplicity) {
            this.houseSystem = houseSystem;
            this.terms = terms;
            this.triplicity = triplicity;
        }

        public HouseSystem getHouseSystem() {
            return houseSystem;
        }

        public Terms getTerms() {
            return terms;
        }

        public Triplicity getTriplicity() {
            return triplicity;
        }
    }
}
