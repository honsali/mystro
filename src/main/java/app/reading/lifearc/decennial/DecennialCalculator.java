package app.reading.lifearc.decennial;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import app.chart.TraditionalTables;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Sect;
import app.chart.model.HousePosition;
import app.chart.model.Chart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;

public final class DecennialCalculator {
    public static final String METHOD_ID = "HELLENISTIC_DECENNIAL_CHRONOCRATOR_NORMALIZED_V1";
    private static final String PRIMARY_DOCTRINE = "hellenistic_normalized";
    private static final String SEQUENCE_METHOD = "SEVEN_TRADITIONAL_PLANETS_IN_CHALDEAN_ORDER_BEGINNING_FROM_LIGHT_OF_SECT";
    private static final String SUBPERIOD_METHOD = "TEN_YEAR_MAIN_PERIODS_SPLIT_INTO_SEVEN_EQUAL_PARTNER_PERIODS_ROTATING_FROM_MAIN_RULER";
    private static final int MAIN_PERIOD_YEARS = 10;
    private static final List<Planet> DIURNAL_SEQUENCE = List.of(
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.MOON,
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS
    );
    private static final List<Planet> NOCTURNAL_SEQUENCE = List.of(
            Planet.MOON,
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS,
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY
    );

    public DecennialTable calculateTable(Subject subject, Chart chart, LocalDate inquiryDate,
                                         int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }
        if (chart.getSect() == null || chart.getSect().getSect() == null) {
            throw new IllegalArgumentException("Natal chart sect is required for decennials");
        }

        OffsetDateTime birthDateTime = subject.getUtcBirthDateTime();
        OffsetDateTime coverageStart = birthDateTime.plusYears(ageStartYears);
        OffsetDateTime coverageEnd = birthDateTime.plusYears(ageEndYearsInclusive + 1L);
        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        Sect natalSect = chart.getSect().getSect();
        List<Planet> sequence = sequence(natalSect);

        List<DecennialPeriod> periods = new ArrayList<>();
        int periodOrdinal = 0;
        int ageCursor = 0;
        OffsetDateTime cursor = birthDateTime;
        while (cursor.isBefore(coverageEnd)) {
            int sequenceIndex = Math.floorMod(periodOrdinal, sequence.size());
            int cycleNumber = (periodOrdinal / sequence.size()) + 1;
            Planet ruler = sequence.get(sequenceIndex);
            OffsetDateTime naturalEnd = cursor.plusYears(MAIN_PERIOD_YEARS);
            int naturalEndAge = ageCursor + MAIN_PERIOD_YEARS;

            OffsetDateTime periodStart = max(cursor, coverageStart);
            OffsetDateTime periodEnd = min(naturalEnd, coverageEnd);
            if (periodStart.isBefore(periodEnd)) {
                periods.add(new DecennialPeriod(
                        cycleNumber,
                        sequenceIndex + 1,
                        ruler,
                        rulerCondition(chart, ruler),
                        Math.max(ageCursor, ageStartYears),
                        Math.min(naturalEndAge, ageEndYearsInclusive + 1),
                        periodStart,
                        periodEnd,
                        active(periodStart, periodEnd, activeDateTime),
                        subperiods(chart, sequence, ruler, cursor, naturalEnd, periodStart, periodEnd, activeDateTime)
                ));
            }

            periodOrdinal++;
            cursor = naturalEnd;
            ageCursor = naturalEndAge;
        }

        return new DecennialTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                SEQUENCE_METHOD,
                SUBPERIOD_METHOD,
                natalSect,
                ageStartYears,
                ageEndYearsInclusive,
                coverageStart,
                coverageEnd,
                sequence,
                periods
        );
    }

    private List<DecennialSubperiod> subperiods(Chart chart, List<Planet> sequence, Planet ruler,
                                                OffsetDateTime naturalPeriodStart,
                                                OffsetDateTime naturalPeriodEnd,
                                                OffsetDateTime emittedPeriodStart,
                                                OffsetDateTime emittedPeriodEnd,
                                                OffsetDateTime activeDateTime) {
        List<Planet> partners = rotatedPartners(sequence, ruler);
        Duration periodDuration = Duration.between(naturalPeriodStart, naturalPeriodEnd);
        List<DecennialSubperiod> entries = new ArrayList<>();
        for (int i = 0; i < partners.size(); i++) {
            OffsetDateTime naturalSubperiodStart = naturalPeriodStart.plus(splitDuration(periodDuration, i, partners.size()));
            OffsetDateTime naturalSubperiodEnd = i == partners.size() - 1
                    ? naturalPeriodEnd
                    : naturalPeriodStart.plus(splitDuration(periodDuration, i + 1, partners.size()));
            OffsetDateTime subperiodStart = max(naturalSubperiodStart, emittedPeriodStart);
            OffsetDateTime subperiodEnd = min(naturalSubperiodEnd, emittedPeriodEnd);
            if (subperiodStart.isBefore(subperiodEnd)) {
                Planet partner = partners.get(i);
                entries.add(new DecennialSubperiod(
                        i + 1,
                        partner,
                        rulerCondition(chart, partner),
                        subperiodStart,
                        subperiodEnd,
                        active(subperiodStart, subperiodEnd, activeDateTime)
                ));
            }
        }
        return List.copyOf(entries);
    }

    private List<Planet> rotatedPartners(List<Planet> sequence, Planet ruler) {
        int start = sequence.indexOf(ruler);
        if (start < 0) {
            throw new IllegalArgumentException("Unsupported decennial ruler " + ruler);
        }
        List<Planet> partners = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            partners.add(sequence.get(Math.floorMod(start + i, sequence.size())));
        }
        return List.copyOf(partners);
    }

    private DecennialRulerCondition rulerCondition(Chart chart, Planet planet) {
        PointEntry point = chart.getPoints().get(PointKey.of(planet));
        if (!(point instanceof PlanetPointEntry planetPoint)) {
            throw new IllegalArgumentException("Missing natal planet point " + planet);
        }
        return new DecennialRulerCondition(
                planet,
                planetPoint.sign(),
                planetPoint.degreeInSign(),
                planetPoint.house(),
                planetPoint.retrograde(),
                planetPoint.sectCondition(),
                planetPoint.solarCondition(),
                planetPoint.dignities(),
                planetPoint.debilities(),
                ruledNatalHouses(chart, planet)
        );
    }

    private List<Integer> ruledNatalHouses(Chart chart, Planet ruler) {
        return chart.getHouses().stream()
                .filter(house -> TraditionalTables.domicileRuler(house.getSign()) == ruler)
                .map(HousePosition::getHouse)
                .toList();
    }

    private List<Planet> sequence(Sect sect) {
        return sect == Sect.DIURNAL ? DIURNAL_SEQUENCE : NOCTURNAL_SEQUENCE;
    }

    private Duration splitDuration(Duration duration, int numerator, int denominator) {
        return duration.multipliedBy(numerator).dividedBy(denominator);
    }

    private OffsetDateTime activeDateTime(Subject subject, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            return null;
        }
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }
        return OffsetDateTime.of(
                inquiryDate,
                subject.getUtcBirthDateTime().toLocalTime(),
                subject.getUtcBirthDateTime().getOffset()
        );
    }

    private boolean active(OffsetDateTime startDateTime, OffsetDateTime endDateTime, OffsetDateTime activeDateTime) {
        return activeDateTime != null
                && !activeDateTime.isBefore(startDateTime)
                && activeDateTime.isBefore(endDateTime);
    }

    private OffsetDateTime min(OffsetDateTime a, OffsetDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private OffsetDateTime max(OffsetDateTime a, OffsetDateTime b) {
        return a.isAfter(b) ? a : b;
    }
}
