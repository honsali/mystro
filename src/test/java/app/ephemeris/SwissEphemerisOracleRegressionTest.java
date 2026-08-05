package app.ephemeris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;

/**
 * End-to-end numerical regression tests against values emitted by the official Swiss Ephemeris
 * {@code swetest64} v2.10.03 executable.
 */
class SwissEphemerisOracleRegressionTest {

    private static final String ORACLE_RESOURCE = "oracles/swisseph-2.10.03.csv";
    private static final double PRINTED_ANGLE_TOLERANCE = 1.0e-6;
    private static final double PRINTED_FRACTION_TOLERANCE = 5.0e-9;

    @TestFactory
    Stream<DynamicTest> matchesOfficialSwissEphemerisOracle() throws IOException {
        List<OracleRow> rows = readOracleRows();
        Map<CaseKey, CalculationContext> contexts = new ConcurrentHashMap<>();
        Map<CaseKey, NatalChart> charts = new ConcurrentHashMap<>();

        return rows.stream().map(row -> dynamicTest(row.displayName(), () -> {
            CalculationContext context = contexts.computeIfAbsent(row.key(), ignored -> createContext(row));
            switch (row.recordType()) {
                case PLANET -> assertPlanet(row, chartFor(row, context, charts));
                case HOUSES -> assertHouses(row, context);
                case MOON_PHENOMENA -> assertMoonPhenomena(row, chartFor(row, context, charts));
                case HORIZONTAL -> assertHorizontal(row, context);
            }
        }));
    }

    private static NatalChart chartFor(
            OracleRow row,
            CalculationContext context,
            Map<CaseKey, NatalChart> charts) {
        return charts.computeIfAbsent(row.key(), ignored -> new BasicCalculator().calculate(context));
    }

    private static CalculationContext createContext(OracleRow row) {
        OffsetDateTime utcDateTime = OffsetDateTime.ofInstant(row.instant(), ZoneOffset.UTC);
        Subject subject = new Subject(
                "swetest-oracle-" + row.caseId() + "-" + row.houseSystem(),
                utcDateTime,
                row.instant(),
                row.latitude(),
                row.longitude(),
                row.elevationMeters());
        CoreDoctrineInfo conventions = new CoreDoctrineInfo(
                "swetest-oracle",
                "Swiss Ephemeris oracle",
                row.houseSystem(),
                Terms.EGYPTIAN,
                Triplicity.DOROTHEAN);
        return new CalculationContext(subject, conventions);
    }

    private static void assertPlanet(OracleRow row, NatalChart chart) {
        Planet planet = planetFor(row.objectId());
        PlanetPosition actual = chart.requirePlanet(planet);
        String prefix = row.caseId() + " " + planet;

        assertPrintedValue(row.value(0), actual.getLongitude(), prefix + " longitude");
        assertPrintedValue(row.value(1), actual.getLatitude(), prefix + " latitude");
        assertPrintedValue(row.value(2), actual.getSpeed(), prefix + " longitude speed");
        assertPrintedValue(row.value(3), actual.getRightAscension(), prefix + " right ascension");
        assertPrintedValue(row.value(4), actual.getDeclination(), prefix + " declination");
    }

    private static void assertHouses(OracleRow row, CalculationContext context) {
        double[] cusps = context.getCusps();
        for (int house = 1; house <= 12; house++) {
            assertPrintedValue(
                    row.value(house - 1),
                    cusps[house],
                    row.caseId() + " " + row.houseSystem() + " cusp " + house);
        }

        double[] ascmc = context.getAscmc();
        assertPrintedValue(row.value(12), ascmc[0], row.caseId() + " ascendant");
        assertPrintedValue(row.value(13), ascmc[1], row.caseId() + " midheaven");
        assertPrintedValue(row.value(14), context.getArmc(), row.caseId() + " ARMC");
    }

    private static void assertMoonPhenomena(OracleRow row, NatalChart chart) {
        assertEquals(
                row.value(0),
                chart.getMoonPhase().getIlluminationFraction(),
                PRINTED_FRACTION_TOLERANCE,
                row.caseId() + " Moon illuminated fraction");
    }

    private static void assertHorizontal(OracleRow row, CalculationContext context) {
        double altitude = context.topocentricHorizontalAltitude(
                Planet.MOON,
                SweConst.SE_MOON,
                context.getFullJulianDay());
        assertPrintedValue(row.value(0), altitude, row.caseId() + " Moon true altitude");
    }

    private static void assertPrintedValue(double expected, double actual, String label) {
        assertEquals(expected, actual, PRINTED_ANGLE_TOLERANCE, label);
    }

    private static Planet planetFor(Integer swissObjectId) {
        if (swissObjectId == null) {
            throw new IllegalArgumentException("PLANET oracle row requires an object id");
        }
        return switch (swissObjectId) {
            case SweConst.SE_SUN -> Planet.SUN;
            case SweConst.SE_MOON -> Planet.MOON;
            case SweConst.SE_MERCURY -> Planet.MERCURY;
            case SweConst.SE_VENUS -> Planet.VENUS;
            case SweConst.SE_MARS -> Planet.MARS;
            case SweConst.SE_JUPITER -> Planet.JUPITER;
            case SweConst.SE_SATURN -> Planet.SATURN;
            case SweConst.SE_MEAN_NODE -> Planet.NORTH_NODE;
            default -> throw new IllegalArgumentException("Unsupported Swiss Ephemeris object id: " + swissObjectId);
        };
    }

    private static List<OracleRow> readOracleRows() throws IOException {
        InputStream input = SwissEphemerisOracleRegressionTest.class
                .getClassLoader()
                .getResourceAsStream(ORACLE_RESOURCE);
        if (input == null) {
            throw new IOException("Missing test oracle resource: " + ORACLE_RESOURCE);
        }

        List<OracleRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#") || line.startsWith("record_type,")) {
                    continue;
                }
                rows.add(parseRow(line, lineNumber));
            }
        }
        return List.copyOf(rows);
    }

    private static OracleRow parseRow(String line, int lineNumber) {
        String[] columns = line.split(",", -1);
        if (columns.length < 9) {
            throw new IllegalArgumentException("Incomplete oracle row at line " + lineNumber);
        }

        RecordType recordType = RecordType.valueOf(columns[0]);
        Integer objectId = columns[7].isBlank() ? null : Integer.valueOf(columns[7]);
        List<Double> values = new ArrayList<>();
        for (int column = 8; column < columns.length; column++) {
            if (!columns[column].isBlank()) {
                values.add(Double.valueOf(columns[column]));
            }
        }
        int expectedValues = switch (recordType) {
            case PLANET -> 5;
            case HOUSES -> 15;
            case MOON_PHENOMENA, HORIZONTAL -> 1;
        };
        if (values.size() != expectedValues) {
            throw new IllegalArgumentException(
                    "Expected " + expectedValues + " values for " + recordType
                            + " at line " + lineNumber + " but found " + values.size());
        }

        return new OracleRow(
                recordType,
                columns[1],
                Instant.parse(columns[2]),
                Double.parseDouble(columns[3]),
                Double.parseDouble(columns[4]),
                Double.parseDouble(columns[5]),
                HouseSystem.valueOf(columns[6]),
                objectId,
                List.copyOf(values));
    }

    private enum RecordType {
        PLANET,
        HOUSES,
        MOON_PHENOMENA,
        HORIZONTAL
    }

    private record CaseKey(
            Instant instant,
            double longitude,
            double latitude,
            double elevationMeters,
            HouseSystem houseSystem) {
    }

    private record OracleRow(
            RecordType recordType,
            String caseId,
            Instant instant,
            double longitude,
            double latitude,
            double elevationMeters,
            HouseSystem houseSystem,
            Integer objectId,
            List<Double> values) {

        CaseKey key() {
            return new CaseKey(instant, longitude, latitude, elevationMeters, houseSystem);
        }

        double value(int index) {
            return values.get(index);
        }

        String displayName() {
            String object = objectId == null ? "" : " object=" + objectId;
            return recordType + " " + caseId + " " + houseSystem + object;
        }
    }
}
