package app.basic;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import app.model.basic.PlanetPosition;
import app.model.data.HouseSystem;
import app.model.data.Planet;
import app.model.data.RoundingPolicy;
import app.model.data.Terms;
import app.model.data.Zodiac;
import app.model.data.ZodiacSign;
import app.model.input.Input;
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
        if (result < 0) {
            Logger.instance.error(input, "Swiss Ephemeris failed to calculate houses");
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

    public double round(double value) {
        if (input.getCalculationSetting().getRoundingPolicy() == RoundingPolicy.DECIMAL_6) {
            return Math.round(value * 1_000_000.0) / 1_000_000.0;
        }
        return value;
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
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDay, swissPlanetId, planetFlags(), values, error);
        if (result < 0 || Double.isNaN(values[0])) {
            Logger.instance.error(input, "Swiss Ephemeris failed for " + planet + " longitude: " + error);
            throw new IllegalArgumentException("Calculation failed. See output/run-logger.json");
        }
        return normalize(values[0]);
    }

    public int houseOf(double longitude, double ascendant) {
        int ascSignIndex = (int) Math.floor(normalize(ascendant) / 30.0);
        int planetSignIndex = (int) Math.floor(normalize(longitude) / 30.0);
        return Math.floorMod(planetSignIndex - ascSignIndex, 12) + 1;
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
            case WHOLE_SIGN, UNKNOWN -> 'W';
        };
    }



}
