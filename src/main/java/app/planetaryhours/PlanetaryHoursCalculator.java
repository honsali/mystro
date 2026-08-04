package app.planetaryhours;

import app.chart.AstroMath;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.ephemeris.DoubleRef;
import app.ephemeris.SweConst;
import app.ephemeris.SwissEphAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PlanetaryHoursCalculator {

    private record PlanetaryDay(
            LocalDate date,
            Planet dayRuler,
            double sunriseJulianDay,
            double sunsetJulianDay,
            double nextSunriseJulianDay,
            double dayHourLength,
            double nightHourLength,
            List<FullPlanetaryHour> hours) {
    }

    private record FullPlanetaryHour(
            LocalDate planetaryDayDate,
            int hour,
            PlanetaryHourPeriod period,
            Planet ruler,
            double startJulianDay,
            double endJulianDay) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(PlanetaryHoursCalculator.class);
    private static final String EPHEMERIS_PATH = "ephe";
    private static final double JULIAN_DAY_UNIX_EPOCH = 2440587.5;
    private static final double SECONDS_PER_DAY = 86_400.0;
    private static final double ONE_SECOND_IN_DAYS = 1.0 / SECONDS_PER_DAY;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<Planet> CHALDEAN_ORDER = List.of(
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS,
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.MOON);
    private static final List<Planet> TABLE_PLANETS = List.of(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN);

    public PlanetaryHoursCalculation calculate(PlanetaryHoursInput input) {
        SwissEphAdapter swissEph = new SwissEphAdapter();
        configureEphemerisPath(swissEph, input);

        double[] geopos = new double[] {input.longitude(), input.latitude(), 0.0};
        PlanetaryDay previousPlanetaryDay = planetaryDay(swissEph, input, input.birthDate().minusDays(1), geopos);
        PlanetaryDay currentPlanetaryDay = planetaryDay(swissEph, input, input.birthDate(), geopos);

        double coverageStartJulianDay = julianDayFromLocalMidnight(input.birthDate(), input.utcOffset());
        double coverageEndJulianDay = julianDayFromLocalMidnight(input.birthDate().plusDays(1), input.utcOffset());
        List<PlanetaryHourEntry> hours = clippedCivilDayHours(
                swissEph,
                input,
                coverageStartJulianDay,
                coverageEndJulianDay,
                List.of(previousPlanetaryDay, currentPlanetaryDay));

        return new PlanetaryHoursCalculation(
                currentPlanetaryDay.dayRuler(),
                boundary(coverageStartJulianDay, input.utcOffset()),
                boundary(coverageEndJulianDay, input.utcOffset()),
                boundary(currentPlanetaryDay.sunriseJulianDay(), input.utcOffset()),
                boundary(currentPlanetaryDay.sunsetJulianDay(), input.utcOffset()),
                boundary(currentPlanetaryDay.nextSunriseJulianDay(), input.utcOffset()),
                currentPlanetaryDay.dayHourLength() * SECONDS_PER_DAY / 60.0,
                currentPlanetaryDay.nightHourLength() * SECONDS_PER_DAY / 60.0,
                hours);
    }

    public PlanetaryHoursCalculation calculateFullPlanetaryDay(PlanetaryHoursInput input) {
        SwissEphAdapter swissEph = new SwissEphAdapter();
        configureEphemerisPath(swissEph, input);

        double[] geopos = new double[] {input.longitude(), input.latitude(), 0.0};
        PlanetaryDay planetaryDay = planetaryDay(swissEph, input, input.birthDate(), geopos);
        List<PlanetaryHourEntry> hours = fullPlanetaryDayHours(swissEph, input, planetaryDay);

        return new PlanetaryHoursCalculation(
                planetaryDay.dayRuler(),
                boundary(planetaryDay.sunriseJulianDay(), input.utcOffset()),
                boundary(planetaryDay.nextSunriseJulianDay(), input.utcOffset()),
                boundary(planetaryDay.sunriseJulianDay(), input.utcOffset()),
                boundary(planetaryDay.sunsetJulianDay(), input.utcOffset()),
                boundary(planetaryDay.nextSunriseJulianDay(), input.utcOffset()),
                planetaryDay.dayHourLength() * SECONDS_PER_DAY / 60.0,
                planetaryDay.nightHourLength() * SECONDS_PER_DAY / 60.0,
                hours);
    }

    private PlanetaryDay planetaryDay(SwissEphAdapter swissEph,
                                      PlanetaryHoursInput input,
                                      LocalDate localDate,
                                      double[] geopos) {
        double localMidnightJulianDay = julianDayFromLocalMidnight(localDate, input.utcOffset());
        double sunriseJulianDay = riseSetJulianDay(swissEph, input, localMidnightJulianDay, geopos, SweConst.SE_CALC_RISE, localDate + " sunrise");
        double sunsetJulianDay = riseSetJulianDay(swissEph, input, localMidnightJulianDay, geopos, SweConst.SE_CALC_SET, localDate + " sunset");
        if (sunsetJulianDay <= sunriseJulianDay) {
            LOG.error("subject={} Invalid planetary-hours day interval for {}: sunrise={} sunset={}", input.id(), localDate, sunriseJulianDay, sunsetJulianDay);
            throw new IllegalArgumentException("Sunrise/sunset unavailable for the requested date and location");
        }

        double nextSunriseJulianDay = riseSetJulianDay(
                swissEph,
                input,
                sunsetJulianDay + ONE_SECOND_IN_DAYS,
                geopos,
                SweConst.SE_CALC_RISE,
                localDate + " next sunrise");
        if (nextSunriseJulianDay <= sunsetJulianDay) {
            LOG.error("subject={} Invalid planetary-hours night interval for {}: sunset={} nextSunrise={}", input.id(), localDate, sunsetJulianDay, nextSunriseJulianDay);
            throw new IllegalArgumentException("Sunrise/sunset unavailable for the requested date and location");
        }

        Planet dayRuler = dayRuler(localDate.getDayOfWeek());
        int firstRulerIndex = CHALDEAN_ORDER.indexOf(dayRuler);
        double dayHourLength = (sunsetJulianDay - sunriseJulianDay) / 12.0;
        double nightHourLength = (nextSunriseJulianDay - sunsetJulianDay) / 12.0;

        List<FullPlanetaryHour> fullHours = new ArrayList<>(24);
        for (int i = 0; i < 12; i++) {
            fullHours.add(new FullPlanetaryHour(
                    localDate,
                    i + 1,
                    PlanetaryHourPeriod.DAY,
                    CHALDEAN_ORDER.get((firstRulerIndex + i) % CHALDEAN_ORDER.size()),
                    sunriseJulianDay + dayHourLength * i,
                    sunriseJulianDay + dayHourLength * (i + 1)));
        }
        for (int i = 0; i < 12; i++) {
            fullHours.add(new FullPlanetaryHour(
                    localDate,
                    i + 13,
                    PlanetaryHourPeriod.NIGHT,
                    CHALDEAN_ORDER.get((firstRulerIndex + 12 + i) % CHALDEAN_ORDER.size()),
                    sunsetJulianDay + nightHourLength * i,
                    sunsetJulianDay + nightHourLength * (i + 1)));
        }

        return new PlanetaryDay(
                localDate,
                dayRuler,
                sunriseJulianDay,
                sunsetJulianDay,
                nextSunriseJulianDay,
                dayHourLength,
                nightHourLength,
                List.copyOf(fullHours));
    }

    private List<PlanetaryHourEntry> fullPlanetaryDayHours(SwissEphAdapter swissEph,
                                                           PlanetaryHoursInput input,
                                                           PlanetaryDay planetaryDay) {
        List<PlanetaryHourEntry> hours = new ArrayList<>(24);
        int sequence = 1;
        for (FullPlanetaryHour fullHour : planetaryDay.hours()) {
            hours.add(hourEntry(sequence++, fullHour, fullHour.startJulianDay(), fullHour.endJulianDay(), swissEph, input));
        }
        return List.copyOf(hours);
    }

    private List<PlanetaryHourEntry> clippedCivilDayHours(SwissEphAdapter swissEph,
                                                          PlanetaryHoursInput input,
                                                          double coverageStartJulianDay,
                                                          double coverageEndJulianDay,
                                                          List<PlanetaryDay> planetaryDays) {
        List<PlanetaryHourEntry> hours = new ArrayList<>(25);
        int sequence = 1;
        for (PlanetaryDay planetaryDay : planetaryDays) {
            for (FullPlanetaryHour fullHour : planetaryDay.hours()) {
                double displayStartJulianDay = Math.max(fullHour.startJulianDay(), coverageStartJulianDay);
                double displayEndJulianDay = Math.min(fullHour.endJulianDay(), coverageEndJulianDay);
                if (displayEndJulianDay > displayStartJulianDay) {
                    hours.add(hourEntry(sequence++, fullHour, displayStartJulianDay, displayEndJulianDay, swissEph, input));
                }
            }
        }
        return hours;
    }

    private PlanetaryHourEntry hourEntry(int sequence,
                                         FullPlanetaryHour fullHour,
                                         double displayStartJulianDay,
                                         double displayEndJulianDay,
                                         SwissEphAdapter swissEph,
                                         PlanetaryHoursInput input) {
        ZoneOffset offset = input.utcOffset();
        double midpointJulianDay = (displayStartJulianDay + displayEndJulianDay) / 2.0;
        OffsetDateTime start = offsetDateTimeFromJulianDay(displayStartJulianDay, offset);
        OffsetDateTime end = offsetDateTimeFromJulianDay(displayEndJulianDay, offset);
        return new PlanetaryHourEntry(
                sequence,
                fullHour.hour(),
                fullHour.planetaryDayDate(),
                fullHour.period(),
                fullHour.ruler(),
                glyphFor(fullHour.ruler()),
                start.toLocalDate(),
                TIME_FORMATTER.format(start),
                end.toLocalDate(),
                TIME_FORMATTER.format(end),
                (displayEndJulianDay - displayStartJulianDay) * SECONDS_PER_DAY / 60.0,
                boundary(fullHour.startJulianDay(), offset),
                boundary(fullHour.endJulianDay(), offset),
                (fullHour.endJulianDay() - fullHour.startJulianDay()) * SECONDS_PER_DAY / 60.0,
                boundary(midpointJulianDay, offset),
                midpointChart(swissEph, input, midpointJulianDay));
    }

    private PlanetaryHoursBoundary boundary(double julianDay, ZoneOffset offset) {
        OffsetDateTime local = offsetDateTimeFromJulianDay(julianDay, offset);
        return new PlanetaryHoursBoundary(local.toLocalDate(), TIME_FORMATTER.format(local));
    }

    private PlanetaryHourChartSnapshot midpointChart(SwissEphAdapter swissEph,
                                                     PlanetaryHoursInput input,
                                                     double midpointJulianDay) {
        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        int houseResult = swissEph.swe_houses_ex(
                midpointJulianDay,
                0,
                input.latitude(),
                input.longitude(),
                'W',
                cusps,
                ascmc);
        if (houseResult < 0 || !Double.isFinite(ascmc[0])) {
            LOG.error("subject={} Swiss Ephemeris failed to calculate midpoint whole-sign houses", input.id());
            throw new IllegalStateException("Calculation failed. See application logs.");
        }

        double ascendantLongitude = AstroMath.normalize(ascmc[0]);
        ZodiacSign ascendantSign = AstroMath.signOf(ascendantLongitude);
        int ascendantSignIndex = ascendantSign.ordinal();

        List<List<PlanetaryHourPlanetPlacement>> planetsByHouse = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            planetsByHouse.add(new ArrayList<>());
        }

        for (Planet planet : TABLE_PLANETS) {
            PlanetaryHourPlanetPlacement placement = planetPlacement(swissEph, input, midpointJulianDay, planet);
            int house = Math.floorMod(placement.getSign().ordinal() - ascendantSignIndex, 12) + 1;
            planetsByHouse.get(house - 1).add(placement);
        }

        List<PlanetaryHourHouseSignPlanetRow> rows = new ArrayList<>(12);
        ZodiacSign[] signs = ZodiacSign.values();
        for (int house = 1; house <= 12; house++) {
            ZodiacSign houseSign = signs[Math.floorMod(ascendantSignIndex + house - 1, 12)];
            rows.add(new PlanetaryHourHouseSignPlanetRow(house, houseSign, planetsByHouse.get(house - 1)));
        }

        return new PlanetaryHourChartSnapshot(HouseSystem.WHOLE_SIGN, ascendantLongitude, ascendantSign, rows);
    }

    private PlanetaryHourPlanetPlacement planetPlacement(SwissEphAdapter swissEph,
                                                         PlanetaryHoursInput input,
                                                         double julianDay,
                                                         Planet planet) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDay, swissPlanetId(planet), SweConst.SEFLG_SPEED | SweConst.SEFLG_SWIEPH, values, error);
        requireSwissEphemerisResult(input, planet, result, error);
        if (!Double.isFinite(values[0])) {
            LOG.error("subject={} Swiss Ephemeris returned invalid midpoint longitude for {}: {}", input.id(), planet, error);
            throw new IllegalStateException("Calculation failed. See application logs.");
        }
        double longitude = AstroMath.normalize(values[0]);
        return new PlanetaryHourPlanetPlacement(
                planet,
                glyphFor(planet),
                longitude,
                AstroMath.signOf(longitude),
                AstroMath.degreeInSign(longitude));
    }

    private int swissPlanetId(Planet planet) {
        return switch (planet) {
            case SUN -> SweConst.SE_SUN;
            case MOON -> SweConst.SE_MOON;
            case MERCURY -> SweConst.SE_MERCURY;
            case VENUS -> SweConst.SE_VENUS;
            case MARS -> SweConst.SE_MARS;
            case JUPITER -> SweConst.SE_JUPITER;
            case SATURN -> SweConst.SE_SATURN;
            case NORTH_NODE, SOUTH_NODE -> SweConst.SE_MEAN_NODE;
        };
    }

    private void requireSwissEphemerisResult(PlanetaryHoursInput input, Planet planet, int result, StringBuilder error) {
        if (result < 0) {
            LOG.error("subject={} Swiss Ephemeris failed for {} midpoint position: {}", input.id(), planet, error);
            throw new IllegalStateException("Calculation failed. See application logs.");
        }
        if ((result & SweConst.SEFLG_MOSEPH) != 0 || (result & SweConst.SEFLG_SWIEPH) == 0) {
            LOG.error("subject={} Swiss Ephemeris did not use required file-backed ephemeris for {} midpoint position (flags={}): {}", input.id(), planet, result, error);
            throw new IllegalStateException("Calculation failed. See application logs.");
        }
    }

    private double riseSetJulianDay(SwissEphAdapter swissEph,
                                    PlanetaryHoursInput input,
                                    double startJulianDay,
                                    double[] geopos,
                                    int riseSetFlag,
                                    String label) {
        DoubleRef result = new DoubleRef();
        StringBuilder error = new StringBuilder();
        int returnCode = swissEph.swe_rise_trans(
                startJulianDay,
                SweConst.SE_SUN,
                null,
                SweConst.SEFLG_SWIEPH,
                riseSetFlag,
                geopos,
                0.0,
                10.0,
                result,
                error);
        if (returnCode != SweConst.OK || !Double.isFinite(result.value)) {
            LOG.error("subject={} Swiss Ephemeris failed to calculate {} for planetary hours: {}", input.id(), label, error);
            throw new IllegalArgumentException("Sunrise/sunset unavailable for the requested date and location");
        }
        return result.value;
    }

    private void configureEphemerisPath(SwissEphAdapter swissEph, PlanetaryHoursInput input) {
        if (!Files.isDirectory(Path.of(EPHEMERIS_PATH))) {
            LOG.error("subject={} Required Swiss Ephemeris directory not found: {}", input.id(), EPHEMERIS_PATH);
            throw new IllegalStateException("Calculation failed. See application logs.");
        }
        swissEph.swe_set_ephe_path(EPHEMERIS_PATH);
    }

    private double julianDayFromLocalMidnight(LocalDate date, ZoneOffset offset) {
        return julianDayFromInstant(date.atStartOfDay().atOffset(offset).toInstant());
    }

    private double julianDayFromInstant(Instant instant) {
        return JULIAN_DAY_UNIX_EPOCH
                + instant.getEpochSecond() / SECONDS_PER_DAY
                + instant.getNano() / (SECONDS_PER_DAY * 1_000_000_000.0);
    }

    private OffsetDateTime offsetDateTimeFromJulianDay(double julianDay, ZoneOffset offset) {
        double epochSeconds = (julianDay - JULIAN_DAY_UNIX_EPOCH) * SECONDS_PER_DAY;
        long seconds = (long) Math.floor(epochSeconds);
        long nanos = Math.round((epochSeconds - seconds) * 1_000_000_000.0);
        if (nanos >= 1_000_000_000L) {
            seconds += 1;
            nanos -= 1_000_000_000L;
        } else if (nanos < 0L) {
            seconds -= 1;
            nanos += 1_000_000_000L;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), offset);
    }

    private Planet dayRuler(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> Planet.SUN;
            case MONDAY -> Planet.MOON;
            case TUESDAY -> Planet.MARS;
            case WEDNESDAY -> Planet.MERCURY;
            case THURSDAY -> Planet.JUPITER;
            case FRIDAY -> Planet.VENUS;
            case SATURDAY -> Planet.SATURN;
        };
    }

    private String glyphFor(Planet planet) {
        return switch (planet) {
            case SUN -> "☉";
            case MOON -> "☽";
            case MERCURY -> "☿";
            case VENUS -> "♀";
            case MARS -> "♂";
            case JUPITER -> "♃";
            case SATURN -> "♄";
            case NORTH_NODE -> "☊";
            case SOUTH_NODE -> "☋";
        };
    }
}
