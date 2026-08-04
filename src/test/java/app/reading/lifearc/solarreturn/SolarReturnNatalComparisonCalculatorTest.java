package app.reading.lifearc.solarreturn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.TraditionalTables;
import app.chart.data.Angularity;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.PointType;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;

class SolarReturnNatalComparisonCalculatorTest {

    private final SolarReturnNatalComparisonCalculator calculator = new SolarReturnNatalComparisonCalculator();

    @Test
    void calculateOverlaysSolarReturnPointsOnNatalHousesAndFindsConjunctions() {
        Subject subject = subject();
        SolarReturnNatalComparisonTable table = calculator.calculate(
                subject,
                natalChart(),
                solarReturnTable(),
                LocalDate.of(2002, 6, 1)
        );

        assertEquals(SolarReturnNatalComparisonCalculator.METHOD_ID, table.methodId());
        assertEquals(SolarReturnCalculator.METHOD_ID, table.sourceSolarReturnMethodId());
        assertEquals(1, table.rows().size());

        SolarReturnNatalComparisonRow row = table.rows().get(0);
        assertTrue(row.activeForInquiry());
        assertEquals(2, row.ageYears());
        assertEquals(3, row.profectedHouse());
        assertEquals(ZodiacSign.TAURUS, row.profectedSign());
        assertEquals(Planet.VENUS, row.lordOfYear());
        assertEquals(PointKey.VENUS, row.lordOfYearOverlay().point());
        assertEquals(ZodiacSign.GEMINI, row.lordOfYearOverlay().sign());
        assertEquals(4, row.lordOfYearOverlay().natalHouseOverlay());
        assertEquals(4, row.ascendantNatalHouseOverlay());
        assertEquals(List.of(PointKey.MARS), row.solarReturnPointsInProfectedSign());
        assertEquals(List.of(PointKey.MARS), row.solarReturnPointsOverlayingProfectedHouse());

        assertTrue(row.conjunctions().stream().anyMatch(contact ->
                contact.solarReturnPoint() == PointKey.SUN
                        && contact.natalTargetType() == SolarReturnNatalTargetType.POINT
                        && "SUN".equals(contact.natalTargetName())
                        && Math.abs(contact.orbDegrees() - 1.0) < 1e-9));
        assertTrue(row.conjunctions().stream().anyMatch(contact ->
                contact.solarReturnPoint() == PointKey.MARS
                        && contact.natalTargetType() == SolarReturnNatalTargetType.LOT
                        && "LOT_FORTUNE".equals(contact.natalTargetName())
                        && Math.abs(contact.orbDegrees() - 1.0) < 1e-9));
    }

    @Test
    void calculateRejectsInquiryDateBeforeBirthDate() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(
                subject(),
                natalChart(),
                solarReturnTable(),
                LocalDate.of(1999, 12, 31)
        ));
    }

    private Subject subject() {
        return new Subject(
                "test",
                OffsetDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                51.5,
                0.0
        );
    }

    private NatalChart natalChart() {
        NatalChart chart = new NatalChart();
        chart.setHouses(housesFromPiscesAscendant());
        Map<PointKey, PointEntry> points = new LinkedHashMap<>();
        points.put(PointKey.SUN, planetPoint(ZodiacSign.CANCER, 10.0, 5));
        points.put(PointKey.MOON, planetPoint(ZodiacSign.TAURUS, 20.0, 3));
        points.put(PointKey.ASCENDANT, anglePoint(ZodiacSign.PISCES, 0.0));
        chart.setPoints(points);
        chart.setLots(List.of(new LotEntry(
                "FORTUNE",
                "Lot of Fortune",
                "valens",
                52.0,
                ZodiacSign.TAURUS,
                22.0,
                3,
                Planet.VENUS,
                "fixture"
        )));
        return chart;
    }

    private SolarReturnTable solarReturnTable() {
        OffsetDateTime start = OffsetDateTime.of(2002, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(2003, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        SolarReturnEntry row = new SolarReturnEntry(
                2,
                start,
                end,
                2452276.0,
                101.0,
                ZodiacSign.CANCER,
                11.0,
                60.0,
                ZodiacSign.GEMINI,
                0.0,
                270.0,
                ZodiacSign.CAPRICORN,
                0.0,
                app.chart.data.Sect.DIURNAL,
                List.of(
                        point(PointKey.SUN, PointType.PLANET, 101.0, ZodiacSign.CANCER, 11.0, 2, false),
                        point(PointKey.VENUS, PointType.PLANET, 65.0, ZodiacSign.GEMINI, 5.0, 1, false),
                        point(PointKey.MARS, PointType.PLANET, 51.0, ZodiacSign.TAURUS, 21.0, 12, false),
                        point(PointKey.ASCENDANT, PointType.ANGLE, 60.0, ZodiacSign.GEMINI, 0.0, null, null)
                )
        );
        return new SolarReturnTable(
                SolarReturnCalculator.METHOD_ID,
                "traditional",
                "NATAL_LOCATION",
                2,
                2,
                100.0,
                ZodiacSign.CANCER,
                10.0,
                List.of(row)
        );
    }

    private SolarReturnPointEntry point(PointKey point, PointType type, double longitude, ZodiacSign sign,
                                        double degreeInSign, Integer house, Boolean retrograde) {
        return new SolarReturnPointEntry(point, type, longitude, sign, degreeInSign, house, retrograde);
    }

    private List<HousePosition> housesFromPiscesAscendant() {
        return List.of(
                house(1, ZodiacSign.PISCES),
                house(2, ZodiacSign.ARIES),
                house(3, ZodiacSign.TAURUS),
                house(4, ZodiacSign.GEMINI),
                house(5, ZodiacSign.CANCER),
                house(6, ZodiacSign.LEO),
                house(7, ZodiacSign.VIRGO),
                house(8, ZodiacSign.LIBRA),
                house(9, ZodiacSign.SCORPIO),
                house(10, ZodiacSign.SAGITTARIUS),
                house(11, ZodiacSign.CAPRICORN),
                house(12, ZodiacSign.AQUARIUS)
        );
    }

    private HousePosition house(int house, ZodiacSign sign) {
        return new HousePosition(house, sign.ordinal() * 30.0, sign);
    }

    private AnglePointEntry anglePoint(ZodiacSign sign, double degreeInSign) {
        return new AnglePointEntry(sign.ordinal() * 30.0 + degreeInSign, sign, degreeInSign);
    }

    private PlanetPointEntry planetPoint(ZodiacSign sign, double degreeInSign, int house) {
        return new PlanetPointEntry(
                sign.ordinal() * 30.0 + degreeInSign,
                sign,
                TraditionalTables.element(sign),
                degreeInSign,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                1.0,
                1.0,
                1.0,
                false,
                house,
                house,
                null,
                Angularity.SUCCEDENT,
                0.0,
                0.0,
                TraditionalTables.domicileRuler(sign),
                TraditionalTables.exaltationRuler(sign),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                List.of(),
                false,
                PointType.PLANET
        );
    }
}
