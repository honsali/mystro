package app.basic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import app.chart.data.HouseSystem;
import app.chart.data.NodeType;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.Zodiac;
import app.chart.model.CalculationDefinition;
import app.input.model.Subject;
import app.output.Logger;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;

public class CalculationContext {

    private static final String EPHEMERIS_PATH = "ephe";

    private final SwissEph swissEph = new SwissEph();
    private final Subject subject;
    private final String doctrineId;
    private final HouseSystem houseSystem;
    private final Zodiac zodiac;
    private final Terms terms;
    private final Triplicity triplicity;
    private final NodeType nodeType;
    private final double fullJulianDay;
    private final double[] cusps = new double[13];
    private final double[] ascmc = new double[10];
    private final double armc;

    public CalculationContext(Subject subject, CalculationDefinition definition) {
        this.subject = subject;
        configureEphemerisPath(subject);
        this.doctrineId = definition.getId();
        this.houseSystem = definition.getHouseSystem();
        this.zodiac = definition.getZodiac();
        this.terms = definition.getTerms();
        this.triplicity = definition.getTriplicity();
        this.nodeType = definition.getNodeType();

        fullJulianDay = julianDayFromInstant(subject.getResolvedUtcInstant());

        int result = calculateSwissHouses(fullJulianDay, cusps, ascmc);
        if (result < 0 || Double.isNaN(ascmc[0]) || Double.isNaN(ascmc[1]) || Double.isNaN(ascmc[2])) {
            Logger.instance.error(subject.getId(), "Swiss Ephemeris failed to calculate houses");
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        armc = AstroMath.normalize(ascmc[2]);
    }

    private void configureEphemerisPath(Subject subject) {
        if (!Files.isDirectory(Path.of(EPHEMERIS_PATH))) {
            Logger.instance.error(subject.getId(), "Required Swiss Ephemeris directory not found: " + EPHEMERIS_PATH);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        swissEph.swe_set_ephe_path(EPHEMERIS_PATH);
    }

    private double julianDayFromInstant(Instant instant) {
        return 2440587.5 + instant.getEpochSecond() / 86400.0 + instant.getNano() / 86_400_000_000_000.0;
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

    public Zodiac getZodiac() {
        return zodiac;
    }

    public Terms getTerms() {
        return terms;
    }

    public Triplicity getTriplicity() {
        return triplicity;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public double getArmc() {
        return armc;
    }

    public SwissEph getSwissEph() {
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

    private double[] eclipticCoordinatesFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDay, swissPlanetId, planetFlags(), values, error);
        requireSwissEphemerisResult(result, planet, "ecliptic coordinates", error);
        if (Double.isNaN(values[0]) || Double.isNaN(values[1])) {
            Logger.instance.error(subject.getId(), "Swiss Ephemeris returned invalid values for " + planet + " ecliptic coordinates: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return values;
    }

    public void requireSwissEphemerisResult(int result, Planet planet, String calculation, StringBuilder error) {
        if (result < 0) {
            Logger.instance.error(subject.getId(), "Swiss Ephemeris failed for " + planet + " " + calculation + ": " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        if ((result & SweConst.SEFLG_MOSEPH) != 0 || (result & SweConst.SEFLG_SWIEPH) == 0) {
            Logger.instance.error(subject.getId(), "Swiss Ephemeris did not use required file-backed ephemeris for " + planet + " " + calculation + " (flags=" + result + "): " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
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
        Logger.instance.error(subject.getId(), "Could not assign quadrant house for longitude " + normalizedLongitude);
        throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
    }

    private boolean isWithinZodiacalArc(double longitude, double start, double end) {
        if (start <= end) {
            return longitude >= start && longitude < end;
        }
        return longitude >= start || longitude < end;
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
        double[] geopos = new double[] {subject.getLongitude(), subject.getLatitude(), 0.0};
        double[] eclipticCoordinates = new double[] {longitude, latitude, 1.0};
        double[] horizontalCoordinates = new double[3];
        // swe_azalt returns true altitude in [1] and refracted apparent altitude in [2].
        // Mystro's shared sect baseline intentionally uses true altitude with altitude >= 0.0.
        swissEph.swe_azalt(julianDay, SweConst.SE_ECL2HOR, geopos, 0.0, 10.0, eclipticCoordinates, horizontalCoordinates);
        if (Double.isNaN(horizontalCoordinates[1])) {
            Logger.instance.error(subject.getId(), "Swiss Ephemeris failed to calculate horizontal altitude");
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return horizontalCoordinates[1];
    }

    /**
     * Swiss Ephemeris flags for geocentric apparent positions with speed output.
     * File-backed Swiss Ephemeris data is required; Moshier fallback is rejected by callers.
     */
    public int planetFlags() {
        return SweConst.SEFLG_SPEED | SweConst.SEFLG_SWIEPH;
    }

    private int calculateSwissHouses(double julianDay, double[] cusps, double[] ascmc) {
        return swissEph.swe_houses_ex(julianDay, houseFlags(), subject.getLatitude(), subject.getLongitude(), houseSystem(houseSystem), cusps, ascmc);
    }

    private int houseFlags() {
        return 0;
    }

    private int houseSystem(HouseSystem houseSystem) {
        return switch (houseSystem) {
            case PLACIDUS -> 'P';
            case WHOLE_SIGN -> 'W';
        };
    }
}
