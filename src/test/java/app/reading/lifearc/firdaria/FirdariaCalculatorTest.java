package app.reading.lifearc.firdaria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.model.BasicSect;
import app.chart.model.NatalChart;
import app.chart.model.Subject;

class FirdariaCalculatorTest {

    private final FirdariaCalculator calculator = new FirdariaCalculator();

    @Test
    void nocturnalChartStartsWithMoonAndMarksActiveVenusPeriod() {
        Subject subject = subject();

        FirdariaTable table = calculator.calculateTable(subject, chart(Sect.NOCTURNAL), LocalDate.of(2050, 1, 1), 0, 100);

        assertEquals(FirdariaCalculator.METHOD_ID, table.methodId());
        assertEquals(Sect.NOCTURNAL, table.natalSect());
        assertEquals(List.of(
                Planet.MOON,
                Planet.SATURN,
                Planet.JUPITER,
                Planet.MARS,
                Planet.SUN,
                Planet.VENUS,
                Planet.MERCURY,
                Planet.NORTH_NODE,
                Planet.SOUTH_NODE
        ), table.mainPeriodSequence());

        FirdariaPeriod moon = table.periods().get(0);
        assertEquals(Planet.MOON, moon.ruler());
        assertEquals(9, moon.nominalYears());
        assertEquals(subject.getUtcBirthDateTime(), moon.startDateTime());
        assertEquals(subject.getUtcBirthDateTime().plusYears(9), moon.endDateTimeExclusive());
        assertEquals(List.of(Planet.MOON, Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN, Planet.VENUS, Planet.MERCURY),
                moon.subperiods().stream().map(FirdariaSubperiod::partner).toList());

        FirdariaPeriod active = table.periods().stream()
                .filter(FirdariaPeriod::activeForInquiry)
                .findFirst()
                .orElseThrow();
        assertEquals(Planet.VENUS, active.ruler());
        assertEquals(49, active.startAgeYears());
        assertEquals(57, active.endAgeYearsExclusive());
        assertEquals(1, active.subperiods().stream().filter(FirdariaSubperiod::activeForInquiry).count());
        assertEquals(Planet.VENUS, active.subperiods().stream()
                .filter(FirdariaSubperiod::activeForInquiry)
                .findFirst()
                .orElseThrow()
                .partner());
    }

    @Test
    void diurnalChartStartsWithSunAndCyclesAfterSeventyFiveYears() {
        Subject subject = subject();

        FirdariaTable table = calculator.calculateTable(subject, chart(Sect.DIURNAL), null, 0, 100);

        assertEquals(Sect.DIURNAL, table.natalSect());
        assertEquals(Planet.SUN, table.periods().get(0).ruler());
        assertEquals(Planet.VENUS, table.periods().get(1).ruler());
        assertEquals(List.of(Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON, Planet.SATURN, Planet.JUPITER, Planet.MARS),
                table.periods().get(0).subperiods().stream().map(FirdariaSubperiod::partner).toList());

        FirdariaPeriod secondCycleSun = table.periods().stream()
                .filter(period -> period.cycleNumber() == 2 && period.sequenceIndex() == 1)
                .findFirst()
                .orElseThrow();
        assertEquals(Planet.SUN, secondCycleSun.ruler());
        assertEquals(75, secondCycleSun.startAgeYears());
        assertEquals(85, secondCycleSun.endAgeYearsExclusive());
    }

    @Test
    void nodePeriodsAreEmittedAsUndividedSelfPartnerPeriods() {
        Subject subject = subject();

        FirdariaTable table = calculator.calculateTable(subject, chart(Sect.DIURNAL), LocalDate.of(2071, 1, 1), 0, 100);

        FirdariaPeriod northNode = table.periods().stream()
                .filter(period -> period.ruler() == Planet.NORTH_NODE)
                .findFirst()
                .orElseThrow();
        assertEquals(1, northNode.subperiods().size());
        assertEquals(Planet.NORTH_NODE, northNode.subperiods().get(0).partner());
    }

    @Test
    void calculateTableRejectsInvalidInputs() {
        Subject subject = subject();
        NatalChart chart = chart(Sect.NOCTURNAL);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, null, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, chart, LocalDate.of(1999, 12, 31), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTable(subject, new NatalChart(), null, 0, 1));
    }

    private Subject subject() {
        return new Subject(
                "test",
                OffsetDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                51.5,
                0.0
        );
    }

    private NatalChart chart(Sect sect) {
        NatalChart chart = new NatalChart();
        chart.setSect(new BasicSect(
                sect,
                sect == Sect.DIURNAL ? Planet.SUN : Planet.MOON,
                sect == Sect.DIURNAL ? Planet.MOON : Planet.SUN,
                sect == Sect.DIURNAL ? Planet.JUPITER : Planet.VENUS,
                sect == Sect.DIURNAL ? Planet.VENUS : Planet.JUPITER,
                sect == Sect.DIURNAL ? Planet.SATURN : Planet.MARS,
                sect == Sect.DIURNAL ? Planet.MARS : Planet.SATURN,
                sect == Sect.DIURNAL,
                sect == Sect.NOCTURNAL,
                1.0,
                -1.0,
                Map.of()
        ));
        return chart;
    }
}
