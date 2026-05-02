package app.basic;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import app.basic.model.PlanetPosition;
import app.basic.data.HouseSystem;
import app.basic.data.Planet;
import app.basic.data.Terms;
import app.basic.data.Zodiac;
import app.basic.data.ZodiacSign;
import app.input.model.Input;
import app.output.Logger;
import app.swisseph.core.SweConst;
import app.swisseph.core.SwissEph;

public class BasicCalculationContext {

    private final SwissEph swissEph = new SwissEph();
    private final Input input;
    private final double fullJulianDay;
    private final double[] cusps = new double[13];
    private final double[] ascmc = new double[10];
    private final double armc;


    public BasicCalculationContext(Input input) {
        this.input = input;
        OffsetDateTime utcDateTime = input.getSubject().getLocalBirthDateTime().withOffsetSameInstant(ZoneOffset.UTC);

        double hour = utcDateTime.getHour() + utcDateTime.getMinute() / 60.0 + (utcDateTime.getSecond() + utcDateTime.getNano() / 1_000_000_000.0) / 3600.0;
        fullJulianDay = getSwissEph().swe_julday(utcDateTime.getYear(), utcDateTime.getMonthValue(), utcDateTime.getDayOfMonth(), hour, SweConst.SE_GREG_CAL);


        int result = calculateSwissHouses(fullJulianDay, cusps, ascmc);
        if (result < 0 || Double.isNaN(ascmc[0]) || Double.isNaN(ascmc[1]) || Double.isNaN(ascmc[2])) {
            Logger.instance.error(input, "Swiss Ephemeris failed to calculate houses");
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        armc = normalize(ascmc[2]);

    }

    public Input getInput() {
        return input;
    }

    public Terms getTerms() {
        return input.getDoctrine().getTerms();
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
        return cusps;
    }


    public double[] getAscmc() {
        return ascmc;
    }

    public PlanetPosition planet(List<PlanetPosition> planets, Planet planet) {
        for (PlanetPosition position : planets) {
            if (position.getPlanet() == planet) {
                return position;
            }
        }
        return null;
    }

    public double normalize(double degrees) {
        double value = degrees % 360.0;
        return value < 0 ? value + 360.0 : value;
    }

    public ZodiacSign signOf(double longitude) {
        return ZodiacSign.values()[(int) Math.floor(normalize(longitude) / 30.0)];
    }

    public double degreeInSign(double longitude) {
        return normalize(longitude) % 30.0;
    }


    public double longitudeFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = eclipticCoordinatesFor(planet, swissPlanetId, julianDay);
        return normalize(values[0]);
    }

    public double latitudeFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = eclipticCoordinatesFor(planet, swissPlanetId, julianDay);
        return values[1];
    }

    private double[] eclipticCoordinatesFor(Planet planet, int swissPlanetId, double julianDay) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDay, swissPlanetId, planetFlags(), values, error);
        if (result < 0 || Double.isNaN(values[0]) || Double.isNaN(values[1])) {
            Logger.instance.error(input, "Swiss Ephemeris failed for " + planet + " ecliptic coordinates: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return values;
    }

    public int houseOf(double longitude, double ascendant) {
        if (input.getDoctrine().getHouseSystem() == HouseSystem.WHOLE_SIGN) {
            return wholeSignHouseOf(longitude, ascendant);
        }
        return quadrantHouseOf(longitude);
    }

    public int wholeSignHouseOf(double longitude, double ascendant) {
        int ascSignIndex = (int) Math.floor(normalize(ascendant) / 30.0);
        int planetSignIndex = (int) Math.floor(normalize(longitude) / 30.0);
        return Math.floorMod(planetSignIndex - ascSignIndex, 12) + 1;
    }

    public Integer quadrantHouseOf(double longitude) {
        if (input.getDoctrine().getHouseSystem() == HouseSystem.WHOLE_SIGN) {
            return null;
        }
        double normalizedLongitude = normalize(longitude);
        for (int house = 1; house <= 12; house++) {
            double start = normalize(cusps[house]);
            double end = normalize(cusps[house == 12 ? 1 : house + 1]);
            if (isWithinZodiacalArc(normalizedLongitude, start, end)) {
                return house;
            }
        }
        Logger.instance.error(input, "Could not assign quadrant house for longitude " + normalizedLongitude);
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

    public double rawAngularSeparation(double longitudeA, double longitudeB) {
        double distance = Math.abs(normalize(longitudeA) - normalize(longitudeB));
        return distance > 180.0 ? 360.0 - distance : distance;
    }

    public double antiscia(double longitude) {
        return normalize(180.0 - longitude);
    }

    public double contraAntiscia(double longitude) {
        return normalize(360.0 - longitude);
    }

    public double horizontalAltitude(double longitude, double latitude) {
        return horizontalAltitude(fullJulianDay, longitude, latitude);
    }

    public double horizontalAltitude(double julianDay, double longitude, double latitude) {
        double[] geopos = new double[] {input.getSubject().getLongitude(), input.getSubject().getLatitude(), 0.0};
        double[] eclipticCoordinates = new double[] {longitude, latitude, 1.0};
        double[] horizontalCoordinates = new double[3];
        swissEph.swe_azalt(julianDay, SweConst.SE_ECL2HOR, geopos, 0.0, 10.0, eclipticCoordinates, horizontalCoordinates);
        if (Double.isNaN(horizontalCoordinates[1])) {
            Logger.instance.error(input, "Swiss Ephemeris failed to calculate horizontal altitude");
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return horizontalCoordinates[1];
    }

    public int planetFlags() {
        int flags = SweConst.SEFLG_SPEED;
        if (input.getDoctrine().getZodiac() == Zodiac.SIDEREAL) {
            flags |= SweConst.SEFLG_SIDEREAL;
        }
        return flags;
    }

    private int calculateSwissHouses(double julianDay, double[] cusps, double[] ascmc) {
        return swissEph.swe_houses_ex(julianDay, houseFlags(), input.getSubject().getLatitude(), input.getSubject().getLongitude(), houseSystem(input.getDoctrine().getHouseSystem()), cusps, ascmc);
    }


    private int houseFlags() {
        if (input.getDoctrine().getZodiac() == Zodiac.SIDEREAL) {
            return SweConst.SEFLG_SIDEREAL;
        }
        return 0;
    }

    private int houseSystem(HouseSystem houseSystem) {
        return switch (houseSystem) {
            case PLACIDUS -> 'P';
            case WHOLE_SIGN -> 'W';
        };
    }



}
