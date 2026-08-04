package app.ephemeris;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.swisseph.ffm.CalculationFlag;
import org.swisseph.ffm.EclipseResult;
import org.swisseph.ffm.EphemerisPosition;
import org.swisseph.ffm.FixedStarPosition;
import org.swisseph.ffm.GeographicPosition;
import org.swisseph.ffm.HorizontalCoordinateType;
import org.swisseph.ffm.HorizontalCoordinates;
import org.swisseph.ffm.HouseCusps;
import org.swisseph.ffm.HouseSystem;
import org.swisseph.ffm.RiseTransitResult;
import org.swisseph.ffm.SwissEph;
import org.swisseph.ffm.SwissEphException;

/**
 * Transitional Mystro adapter over {@code swisseph-java-ffm}.
 *
 * <p>It preserves the raw array-oriented call shape currently used by the
 * calculation layer while delegating every astronomical operation to Swiss
 * Ephemeris C 2.10.03 through the FFM binding.</p>
 */
public final class SwissEphAdapter {
    private static final SwissEph NATIVE = loadNativeLibrary();

    public int swe_calc_ut(double julianDayUt, int bodyId, int flags,
                           double[] values, StringBuilder error) {
        try {
            EphemerisPosition position = NATIVE.calculateUt(
                    julianDayUt, bodyId, calculationFlags(flags));
            copyPosition(position, values);
            append(error, position.warning());
            return position.returnedFlags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public void swe_set_ephe_path(String path) {
        NATIVE.setEphemerisPath(path);
    }

    public double swe_deltat(double julianDayUt) {
        return NATIVE.deltaT(julianDayUt);
    }

    public int swe_houses_ex(double julianDayUt, int flags, double latitude,
                             double longitude, int houseSystemCode,
                             double[] cusps, double[] additionalPoints) {
        try {
            HouseCusps result = NATIVE.housesEx(
                    julianDayUt, flags, latitude, longitude, houseSystem(houseSystemCode));
            copy(result.cusps(), cusps);
            copy(result.additionalPoints(), additionalPoints);
            return SweConstants.OK;
        } catch (SwissEphException | IllegalArgumentException exception) {
            return SweConstants.ERR;
        }
    }

    public void swe_azalt(double julianDayUt, int coordinateType, double[] geographicPosition,
                          double atmosphericPressure, double atmosphericTemperature,
                          double[] input, double[] output) {
        HorizontalCoordinates result = NATIVE.azimuthAltitude(
                julianDayUt,
                coordinateType == SweConstants.SE_ECL2HOR
                        ? HorizontalCoordinateType.ECLIPTIC
                        : HorizontalCoordinateType.EQUATORIAL,
                geographicPosition(geographicPosition),
                atmosphericPressure,
                atmosphericTemperature,
                input[0], input[1], input[2]);
        output[0] = result.azimuth();
        output[1] = result.trueAltitude();
        output[2] = result.apparentAltitude();
    }

    public int swe_fixstar_ut(StringBuilder star, double julianDayUt, int flags,
                              double[] values, StringBuilder error) {
        try {
            FixedStarPosition result = NATIVE.fixedStarUt(julianDayUt, star.toString(), flags);
            star.setLength(0);
            star.append(result.name());
            copyPosition(result.position(), values);
            append(error, result.position().warning());
            return result.position().returnedFlags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_rise_trans(double startJulianDayUt, int bodyId, StringBuilder star,
                              int ephemerisFlags, int eventFlags, double[] geographicPosition,
                              double atmosphericPressure, double atmosphericTemperature,
                              DoubleRef result, StringBuilder error) {
        try {
            RiseTransitResult nativeResult = star == null || star.isEmpty()
                    ? NATIVE.riseTransit(
                            startJulianDayUt, bodyId, ephemerisFlags, eventFlags,
                            geographicPosition(geographicPosition), atmosphericPressure,
                            atmosphericTemperature)
                    : NATIVE.riseTransit(
                            startJulianDayUt, star.toString(), ephemerisFlags, eventFlags,
                            geographicPosition(geographicPosition), atmosphericPressure,
                            atmosphericTemperature);
            append(error, nativeResult.message());
            result.value = nativeResult.julianDayUt();
            return nativeResult.found() ? SweConstants.OK : -2;
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_sol_eclipse_when_glob(double startJulianDayUt, int ephemerisFlags,
                                          int eclipseTypeFlags, double[] times, int backward,
                                          StringBuilder error) {
        try {
            EclipseResult result = NATIVE.solarEclipseWhenGlobal(
                    startJulianDayUt, ephemerisFlags, eclipseTypeFlags, backward != 0);
            copyEclipse(result, times, null, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_sol_eclipse_when_loc(double startJulianDayUt, int ephemerisFlags,
                                         double[] geographicPosition, double[] times,
                                         double[] attributes, int backward, StringBuilder error) {
        try {
            EclipseResult result = NATIVE.solarEclipseWhenLocal(
                    startJulianDayUt, ephemerisFlags,
                    geographicPosition(geographicPosition), backward != 0);
            copyEclipse(result, times, attributes, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_sol_eclipse_where(double julianDayUt, int ephemerisFlags,
                                      double[] geographicPositions, double[] attributes,
                                      StringBuilder error) {
        try {
            EclipseResult result = NATIVE.solarEclipseWhere(julianDayUt, ephemerisFlags);
            copyEclipse(result, null, attributes, geographicPositions, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_sol_eclipse_how(double julianDayUt, int ephemerisFlags,
                                    double[] geographicPosition, double[] attributes,
                                    StringBuilder error) {
        try {
            EclipseResult result = NATIVE.solarEclipseHow(
                    julianDayUt, ephemerisFlags, geographicPosition(geographicPosition));
            copyEclipse(result, null, attributes, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_lun_eclipse_when(double startJulianDayUt, int ephemerisFlags,
                                     int eclipseTypeFlags, double[] times, int backward,
                                     StringBuilder error) {
        try {
            EclipseResult result = NATIVE.lunarEclipseWhen(
                    startJulianDayUt, ephemerisFlags, eclipseTypeFlags, backward != 0);
            copyEclipse(result, times, null, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_lun_eclipse_when_loc(double startJulianDayUt, int ephemerisFlags,
                                         double[] geographicPosition, double[] times,
                                         double[] attributes, int backward, StringBuilder error) {
        try {
            EclipseResult result = NATIVE.lunarEclipseWhenLocal(
                    startJulianDayUt, ephemerisFlags,
                    geographicPosition(geographicPosition), backward != 0);
            copyEclipse(result, times, attributes, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public int swe_lun_eclipse_how(double julianDayUt, int ephemerisFlags,
                                    double[] geographicPosition, double[] attributes,
                                    StringBuilder error) {
        try {
            EclipseResult result = NATIVE.lunarEclipseHow(
                    julianDayUt, ephemerisFlags, geographicPosition(geographicPosition));
            copyEclipse(result, null, attributes, null, error);
            return result.flags();
        } catch (SwissEphException exception) {
            return failure(error, exception);
        }
    }

    public String version() {
        return NATIVE.version();
    }

    private static SwissEph loadNativeLibrary() {
        String configuredPath = System.getProperty(SwissEph.LIBRARY_PATH_PROPERTY);
        String configuredEnvironment = System.getenv(SwissEph.LIBRARY_PATH_ENVIRONMENT);
        if ((configuredPath != null && !configuredPath.isBlank())
                || (configuredEnvironment != null && !configuredEnvironment.isBlank())) {
            return verifyNativeVersion(SwissEph.loadConfigured());
        }

        Path[] conventionalPaths = {
                Path.of("dll", nativeLibraryFileName()).toAbsolutePath().normalize(),
                Path.of("libs", nativeLibraryFileName()).toAbsolutePath().normalize()
        };
        for (Path conventionalPath : conventionalPaths) {
            if (Files.isRegularFile(conventionalPath)) {
                return verifyNativeVersion(SwissEph.load(conventionalPath));
            }
        }
        throw new IllegalStateException(
                "Swiss Ephemeris native library not found. Set -D"
                        + SwissEph.LIBRARY_PATH_PROPERTY + "=<library> or "
                        + SwissEph.LIBRARY_PATH_ENVIRONMENT + ", or place it at one of "
                        + Arrays.toString(conventionalPaths));
    }

    private static SwissEph verifyNativeVersion(SwissEph nativeEngine) {
        String actualVersion = nativeEngine.version();
        if (!actualVersion.startsWith(SwissEph.EXPECTED_NATIVE_VERSION)) {
            nativeEngine.close();
            throw new IllegalStateException(
                    "Expected Swiss Ephemeris " + SwissEph.EXPECTED_NATIVE_VERSION
                            + " but native library reports " + actualVersion);
        }
        return nativeEngine;
    }

    private static String nativeLibraryFileName() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return "swedll64.dll";
        }
        if (osName.contains("mac")) {
            return "libswe.dylib";
        }
        return "libswe.so";
    }

    private static CalculationFlag[] calculationFlags(int mask) {
        return Arrays.stream(CalculationFlag.values())
                .filter(flag -> (mask & flag.value()) != 0)
                .toArray(CalculationFlag[]::new);
    }

    private static HouseSystem houseSystem(int code) {
        for (HouseSystem system : HouseSystem.values()) {
            if (system.code() == (char) code) {
                return system;
            }
        }
        throw new IllegalArgumentException("Unsupported house system code: " + (char) code);
    }

    private static GeographicPosition geographicPosition(double[] values) {
        // swe_lun_eclipse_how() explicitly permits a null location when only
        // global attributes are requested. The typed binding requires a value;
        // a zeroed observer preserves the global magnitude fields consumed by
        // Mystro while leaving location-specific fields irrelevant.
        if (values == null) {
            return new GeographicPosition(0.0, 0.0, 0.0);
        }
        return new GeographicPosition(values[0], values[1], values[2]);
    }

    private static void copyPosition(EphemerisPosition position, double[] target) {
        target[0] = position.firstCoordinate();
        target[1] = position.secondCoordinate();
        target[2] = position.thirdCoordinate();
        target[3] = position.firstCoordinateSpeed();
        target[4] = position.secondCoordinateSpeed();
        target[5] = position.thirdCoordinateSpeed();
    }

    private static void copyEclipse(EclipseResult result, double[] times,
                                    double[] attributes, double[] geographicPositions,
                                    StringBuilder error) {
        copy(result.times(), times);
        copy(result.attributes(), attributes);
        copy(result.geographicPositions(), geographicPositions);
        append(error, result.warning());
    }

    private static void copy(double[] source, double[] target) {
        if (target != null) {
            System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
        }
    }

    private static int failure(StringBuilder error, SwissEphException exception) {
        append(error, exception.getMessage());
        return exception.returnCode() == null ? SweConstants.ERR : exception.returnCode();
    }

    private static void append(StringBuilder target, String message) {
        if (target != null && message != null && !message.isBlank()) {
            target.append(message);
        }
    }
}
