package app.chart;

import java.nio.file.Files;
import java.nio.file.Path;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.reading.CoreDoctrineInfo;
import app.chart.model.Subject;
import app.ephemeris.SweConst;
import app.ephemeris.SwissEphAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculationContext {

    private static final Logger LOG = LoggerFactory.getLogger(CalculationContext.class);
    private static final String EPHEMERIS_PATH = "ephe";

    private final SwissEphAdapter swissEph = new SwissEphAdapter();
    private final Subject subject;
    private final String doctrineId;
    private final HouseSystem houseSystem;
    private final Terms terms;
    private final Triplicity triplicity;
    private final double fullJulianDay;
    private final double[] cusps = new double[13];
    private final double[] ascmc = new double[10];
    private final double armc;

    public CalculationContext(Subject subject, CoreDoctrineInfo coreDoctrineInfo) {
        this.subject = subject;
        configureEphemerisPath(subject);
        this.doctrineId = coreDoctrineInfo.getId();
        this.houseSystem = coreDoctrineInfo.getHouseSystem();
        this.terms = coreDoctrineInfo.getTerms();
        this.triplicity = coreDoctrineInfo.getTriplicity();

        fullJulianDay = SwissEphAdapter.utcToJulianDayUt(subject.getResolvedUtcInstant());

        int result = calculateSwissHouses(fullJulianDay, cusps, ascmc);
        if (result < 0 || hasInvalidHouseOutput(cusps, ascmc)) {
            LOG.error("subject={} Swiss Ephemeris failed to calculate houses", subject.getId());
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        armc = AstroMath.normalize(ascmc[2]);
    }

    public Subject getSubject() {
        return subject;
    }

    public String getDoctrineId() {
        return doctrineId;
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

    public double getArmc() {
        return armc;
    }

    public SwissEphAdapter getSwissEph() {
        return swissEph;
    }

    public double getFullJulianDay() {
        return fullJulianDay;
    }

    public double[] getCusps() {
        return cusps.clone();
    }

    public double[] getAscmc() {
        return ascmc.clone();
    }

    public double longitudeFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = eclipticCoordinatesFor(planet, swissPlanetId, julianDay);
        return AstroMath.normalize(values[0]);
    }

    public double latitudeFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = eclipticCoordinatesFor(planet, swissPlanetId, julianDay);
        return values[1];
    }

    public void requireSwissEphemerisResult(int result, Planet planet, String calculation, StringBuilder error) {
        if (result < 0) {
            LOG.error("subject={} Swiss Ephemeris failed for {} {}: {}", subject.getId(), planet, calculation, error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        if ((result & SweConst.SEFLG_MOSEPH) != 0 || (result & SweConst.SEFLG_SWIEPH) == 0) {
            LOG.error("subject={} Swiss Ephemeris did not use required file-backed ephemeris for {} {} (flags={}): {}", subject.getId(), planet, calculation, result, error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
    }

    public int houseOf(double longitude, double ascendant) {
        if (houseSystem == HouseSystem.WHOLE_SIGN) {
            return wholeSignHouseOf(longitude, ascendant);
        }
        return quadrantHouseOf(longitude);
    }

    public int wholeSignHouseOf(double longitude, double ascendant) {
        int ascSignIndex = (int) Math.floor(AstroMath.normalize(ascendant) / 30.0);
        int planetSignIndex = (int) Math.floor(AstroMath.normalize(longitude) / 30.0);
        return Math.floorMod(planetSignIndex - ascSignIndex, 12) + 1;
    }

    public Integer quadrantHouseOf(double longitude) {
        if (houseSystem == HouseSystem.WHOLE_SIGN) {
            return null;
        }
        double normalizedLongitude = AstroMath.normalize(longitude);
        for (int house = 1; house <= 12; house++) {
            double start = AstroMath.normalize(cusps[house]);
            double end = AstroMath.normalize(cusps[house == 12 ? 1 : house + 1]);
            if (isWithinZodiacalArc(normalizedLongitude, start, end)) {
                return house;
            }
        }
        LOG.error("subject={} Could not assign quadrant house for longitude {}", subject.getId(), normalizedLongitude);
        throw new IllegalArgumentException("Calculation failed. See application logs.");
    }

    public Planet termRuler(double longitude, Terms terms) {
        return TraditionalTables.termRuler(longitude, terms);
    }

    public double antiscia(double longitude) {
        return AstroMath.normalize(180.0 - longitude);
    }

    public double contraAntiscia(double longitude) {
        return AstroMath.normalize(360.0 - longitude);
    }

    public double horizontalAltitude(double longitude, double latitude) {
        return horizontalAltitude(fullJulianDay, longitude, latitude);
    }

    public double horizontalAltitude(double julianDay, double longitude, double latitude) {
        double[] geopos = new double[] {
                subject.getLongitude(),
                subject.getLatitude(),
                subject.getElevationMeters()
        };
        double[] eclipticCoordinates = new double[] {longitude, latitude, 1.0};
        double[] horizontalCoordinates = new double[3];
        // swe_azalt returns true altitude in [1] and refracted apparent altitude in [2].
        // Mystro's shared sect baseline intentionally uses true altitude with altitude >= 0.0.
        swissEph.swe_azalt(julianDay, SweConst.SE_ECL2HOR, geopos, 0.0, 10.0, eclipticCoordinates, horizontalCoordinates);
        if (!Double.isFinite(horizontalCoordinates[1])) {
            LOG.error("subject={} Swiss Ephemeris failed to calculate horizontal altitude", subject.getId());
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        return horizontalCoordinates[1];
    }

    public double topocentricHorizontalAltitude(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut_topocentric(
                julianDay,
                swissPlanetId,
                planetFlags(),
                subject.getLongitude(),
                subject.getLatitude(),
                subject.getElevationMeters(),
                values,
                error);
        requireSwissEphemerisResult(result, planet, "topocentric position", error);
        if (!Double.isFinite(values[0]) || !Double.isFinite(values[1])) {
            LOG.error(
                    "subject={} Swiss Ephemeris returned invalid values for {} topocentric position: {}",
                    subject.getId(),
                    planet,
                    error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        return horizontalAltitude(julianDay, AstroMath.normalize(values[0]), values[1]);
    }

    /**
     * Swiss Ephemeris flags for geocentric apparent positions with speed output. File-backed Swiss
     * Ephemeris data is required; Moshier fallback is rejected by callers.
     */
    public int planetFlags() {
        return SweConst.SEFLG_SPEED | SweConst.SEFLG_SWIEPH;
    }

    private void configureEphemerisPath(Subject subject) {
        if (!Files.isDirectory(Path.of(EPHEMERIS_PATH))) {
            LOG.error("subject={} Required Swiss Ephemeris directory not found: {}", subject.getId(), EPHEMERIS_PATH);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        swissEph.swe_set_ephe_path(EPHEMERIS_PATH);
    }

    private double[] eclipticCoordinatesFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDay, swissPlanetId, planetFlags(), values, error);
        requireSwissEphemerisResult(result, planet, "ecliptic coordinates", error);
        if (!Double.isFinite(values[0]) || !Double.isFinite(values[1])) {
            LOG.error("subject={} Swiss Ephemeris returned invalid values for {} ecliptic coordinates: {}", subject.getId(), planet, error);
            throw new IllegalArgumentException("Calculation failed. See application logs.");
        }
        return values;
    }

    private boolean hasInvalidHouseOutput(double[] cusps, double[] ascmc) {
        for (int house = 1; house <= 12; house++) {
            if (!Double.isFinite(cusps[house])) {
                return true;
            }
        }
        return !Double.isFinite(ascmc[0]) || !Double.isFinite(ascmc[1]) || !Double.isFinite(ascmc[2]);
    }

    private boolean isWithinZodiacalArc(double longitude, double start, double end) {
        if (start <= end) {
            return longitude >= start && longitude < end;
        }
        return longitude >= start || longitude < end;
    }

    private int calculateSwissHouses(double julianDay, double[] cusps, double[] ascmc) {
        return swissEph.swe_houses_ex(julianDay, houseFlags(), subject.getLatitude(), subject.getLongitude(), houseSystem(houseSystem), cusps, ascmc);
    }

    private int houseFlags() {
        return 0;
    }

    private int houseSystem(HouseSystem houseSystem) {
        return switch (houseSystem) {
            case WHOLE_SIGN -> 'W';
            case EQUAL -> 'E';
            case ALCABITIUS -> 'B';
            case CAMPANUS -> 'C';
            case REGIOMONTANUS -> 'R';
            case PLACIDUS -> 'P';
        };
    }
}
