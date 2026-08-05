package app.reading.description.common.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.reading.description.common.data.SyzygyType;
import app.reading.description.common.model.PrenatalSyzygyEntry;

/**
 * End-to-end prenatal-lunation regression tests against values emitted by the official Swiss
 * Ephemeris {@code swetest64} v2.10.03 executable.
 */
class SyzygyCalculatorOracleTest {

    private static final String ORACLE_RESOURCE = "oracles/swisseph-syzygy-2.10.03.csv";
    private static final double PRINTED_JULIAN_DAY_TOLERANCE = 1.0e-5;
    private static final double PRINTED_ANGLE_TOLERANCE = 1.0e-6;
    private static final Duration EVENT_TIME_TOLERANCE = Duration.ofMillis(2);
    private static final CoreDoctrineInfo CONVENTIONS = new CoreDoctrineInfo(
            "swetest-syzygy-oracle",
            "Swiss Ephemeris syzygy oracle",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN);

    private final SyzygyCalculator calculator = new SyzygyCalculator();

    @TestFactory
    Stream<DynamicTest> matchesOfficialSwissEphemerisSyzygyOracle() throws IOException {
        return readOracleRows().stream()
                .map(row -> dynamicTest(row.caseId(), () -> assertMatchesOracle(row)));
    }

    private void assertMatchesOracle(OracleRow row) {
        OffsetDateTime birthUtc = OffsetDateTime.ofInstant(row.birthInstant(), ZoneOffset.UTC);
        Subject subject = new Subject(
                "swetest-syzygy-" + row.caseId(),
                birthUtc,
                row.birthInstant(),
                row.latitude(),
                row.longitude(),
                row.elevationMeters());
        PrenatalSyzygyEntry actual = calculator.calculate(new CalculationContext(subject, CONVENTIONS));

        assertEquals(row.expectedType(), actual.type(), row.caseId() + " type");
        assertEquals(row.eventJulianDayUt(), actual.julianDay(), PRINTED_JULIAN_DAY_TOLERANCE,
                row.caseId() + " Julian day UT");
        assertInstantClose(row.eventInstant(), actual.approximateUtcInstant(), row.caseId() + " UTC instant");
        assertEquals(row.sunLongitude(), actual.sunLongitude(), PRINTED_ANGLE_TOLERANCE,
                row.caseId() + " Sun longitude");
        assertEquals(row.moonLongitude(), actual.moonLongitude(), PRINTED_ANGLE_TOLERANCE,
                row.caseId() + " Moon longitude");
        assertEquals(row.syzygyLongitude(), actual.longitude(), PRINTED_ANGLE_TOLERANCE,
                row.caseId() + " selected syzygy longitude");
        assertEquals(row.house(), actual.house(), row.caseId() + " natal house");
        assertTrue(actual.approximateUtcInstant().isBefore(row.birthInstant()),
                row.caseId() + " must select a strictly prenatal event");

        double expectedSeparation = row.expectedType() == SyzygyType.NEW_MOON ? 0.0 : 180.0;
        assertEquals(expectedSeparation, actual.angularSeparation(), 1.0e-8,
                row.caseId() + " Sun-Moon separation");
    }

    private static void assertInstantClose(Instant expected, Instant actual, String label) {
        Duration difference = Duration.between(expected, actual).abs();
        assertTrue(difference.compareTo(EVENT_TIME_TOLERANCE) <= 0,
                () -> label + " expected " + expected + " but was " + actual
                        + " (difference " + difference.toNanos() / 1_000_000.0 + " ms)");
    }

    private static List<OracleRow> readOracleRows() throws IOException {
        InputStream input = SyzygyCalculatorOracleTest.class
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
                if (line.isBlank() || line.startsWith("#") || line.startsWith("case_id,")) {
                    continue;
                }
                rows.add(parseRow(line, lineNumber));
            }
        }
        return List.copyOf(rows);
    }

    private static OracleRow parseRow(String line, int lineNumber) {
        String[] columns = line.split(",", -1);
        if (columns.length != 13) {
            throw new IllegalArgumentException(
                    "Expected 13 oracle columns at line " + lineNumber + " but found " + columns.length);
        }
        HouseSystem houseSystem = HouseSystem.valueOf(columns[5]);
        if (houseSystem != HouseSystem.WHOLE_SIGN) {
            throw new IllegalArgumentException("Unsupported syzygy oracle house system at line " + lineNumber);
        }
        return new OracleRow(
                columns[0],
                Instant.parse(columns[1]),
                Double.parseDouble(columns[2]),
                Double.parseDouble(columns[3]),
                Double.parseDouble(columns[4]),
                SyzygyType.valueOf(columns[6]),
                Double.parseDouble(columns[7]),
                Instant.parse(columns[8]),
                Double.parseDouble(columns[9]),
                Double.parseDouble(columns[10]),
                Double.parseDouble(columns[11]),
                Integer.parseInt(columns[12]));
    }

    private record OracleRow(
            String caseId,
            Instant birthInstant,
            double longitude,
            double latitude,
            double elevationMeters,
            SyzygyType expectedType,
            double eventJulianDayUt,
            Instant eventInstant,
            double sunLongitude,
            double moonLongitude,
            double syzygyLongitude,
            int house) {
    }
}
