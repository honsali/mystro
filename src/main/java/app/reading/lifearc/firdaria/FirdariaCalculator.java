package app.reading.lifearc.firdaria;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import app.chart.data.Planet;
import app.chart.data.Sect;
import app.chart.model.NatalChart;
import app.chart.model.Subject;

public final class FirdariaCalculator {
    public static final String METHOD_ID = "MEDIEVAL_FIRDARIA_V1";
    private static final String PRIMARY_DOCTRINE = "medieval";
    private static final String SUBPERIOD_METHOD = "SEVEN_PLANET_PARTNERS_ROTATED_FROM_MAIN_RULER; NODE_PERIODS_UNDIVIDED";
    private static final List<Planet> DIURNAL_SEQUENCE = List.of(
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.MOON,
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS,
            Planet.NORTH_NODE,
            Planet.SOUTH_NODE
    );
    private static final List<Planet> NOCTURNAL_SEQUENCE = List.of(
            Planet.MOON,
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS,
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.NORTH_NODE,
            Planet.SOUTH_NODE
    );
    private static final List<Planet> PLANETARY_PARTNER_SEQUENCE = List.of(
            Planet.SUN,
            Planet.VENUS,
            Planet.MERCURY,
            Planet.MOON,
            Planet.SATURN,
            Planet.JUPITER,
            Planet.MARS
    );

    public FirdariaTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                        int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }
        if (chart.getSect() == null || chart.getSect().getSect() == null) {
            throw new IllegalArgumentException("Natal chart sect is required for firdaria");
        }

        OffsetDateTime birthDateTime = subject.getUtcBirthDateTime();
        OffsetDateTime coverageStart = birthDateTime.plusYears(ageStartYears);
        OffsetDateTime coverageEnd = birthDateTime.plusYears(ageEndYearsInclusive + 1L);
        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        Sect natalSect = chart.getSect().getSect();
        List<Planet> sequence = sequence(natalSect);

        List<FirdariaPeriod> periods = new ArrayList<>();
        int cycleNumber = 1;
        int ageCursor = 0;
        OffsetDateTime cursor = birthDateTime;
        while (cursor.isBefore(coverageEnd)) {
            for (int i = 0; i < sequence.size() && cursor.isBefore(coverageEnd); i++) {
                Planet ruler = sequence.get(i);
                int nominalYears = periodYears(ruler);
                OffsetDateTime naturalEnd = cursor.plusYears(nominalYears);
                int naturalEndAge = ageCursor + nominalYears;

                OffsetDateTime periodStart = max(cursor, coverageStart);
                OffsetDateTime periodEnd = min(naturalEnd, coverageEnd);
                if (periodStart.isBefore(periodEnd)) {
                    periods.add(new FirdariaPeriod(
                            cycleNumber,
                            i + 1,
                            ruler,
                            nominalYears,
                            Math.max(ageCursor, ageStartYears),
                            Math.min(naturalEndAge, ageEndYearsInclusive + 1),
                            periodStart,
                            periodEnd,
                            active(periodStart, periodEnd, activeDateTime),
                            subperiods(ruler, cursor, naturalEnd, periodStart, periodEnd, activeDateTime)
                    ));
                }

                cursor = naturalEnd;
                ageCursor = naturalEndAge;
            }
            cycleNumber++;
        }

        return new FirdariaTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
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

    public int periodYears(Planet ruler) {
        return switch (ruler) {
            case SUN -> 10;
            case VENUS -> 8;
            case MERCURY -> 13;
            case MOON -> 9;
            case SATURN -> 11;
            case JUPITER -> 12;
            case MARS -> 7;
            case NORTH_NODE -> 3;
            case SOUTH_NODE -> 2;
        };
    }

    private List<Planet> sequence(Sect sect) {
        return sect == Sect.DIURNAL ? DIURNAL_SEQUENCE : NOCTURNAL_SEQUENCE;
    }

    private List<FirdariaSubperiod> subperiods(Planet ruler,
                                               OffsetDateTime naturalPeriodStart,
                                               OffsetDateTime naturalPeriodEnd,
                                               OffsetDateTime emittedPeriodStart,
                                               OffsetDateTime emittedPeriodEnd,
                                               OffsetDateTime activeDateTime) {
        if (isNode(ruler)) {
            return List.of(new FirdariaSubperiod(
                    1,
                    ruler,
                    emittedPeriodStart,
                    emittedPeriodEnd,
                    active(emittedPeriodStart, emittedPeriodEnd, activeDateTime)
            ));
        }

        List<Planet> partners = rotatedPlanetaryPartners(ruler);
        Duration periodDuration = Duration.between(naturalPeriodStart, naturalPeriodEnd);
        List<FirdariaSubperiod> entries = new ArrayList<>();
        for (int i = 0; i < partners.size(); i++) {
            OffsetDateTime naturalSubperiodStart = naturalPeriodStart.plus(splitDuration(periodDuration, i, partners.size()));
            OffsetDateTime naturalSubperiodEnd = i == partners.size() - 1
                    ? naturalPeriodEnd
                    : naturalPeriodStart.plus(splitDuration(periodDuration, i + 1, partners.size()));
            OffsetDateTime subperiodStart = max(naturalSubperiodStart, emittedPeriodStart);
            OffsetDateTime subperiodEnd = min(naturalSubperiodEnd, emittedPeriodEnd);
            if (subperiodStart.isBefore(subperiodEnd)) {
                entries.add(new FirdariaSubperiod(
                        i + 1,
                        partners.get(i),
                        subperiodStart,
                        subperiodEnd,
                        active(subperiodStart, subperiodEnd, activeDateTime)
                ));
            }
        }
        return List.copyOf(entries);
    }

    private List<Planet> rotatedPlanetaryPartners(Planet ruler) {
        int start = PLANETARY_PARTNER_SEQUENCE.indexOf(ruler);
        if (start < 0) {
            throw new IllegalArgumentException("Unsupported firdaria partner ruler " + ruler);
        }
        List<Planet> partners = new ArrayList<>();
        for (int i = 0; i < PLANETARY_PARTNER_SEQUENCE.size(); i++) {
            partners.add(PLANETARY_PARTNER_SEQUENCE.get(Math.floorMod(start + i, PLANETARY_PARTNER_SEQUENCE.size())));
        }
        return List.copyOf(partners);
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

    private boolean isNode(Planet ruler) {
        return ruler == Planet.NORTH_NODE || ruler == Planet.SOUTH_NODE;
    }

    private OffsetDateTime min(OffsetDateTime a, OffsetDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private OffsetDateTime max(OffsetDateTime a, OffsetDateTime b) {
        return a.isAfter(b) ? a : b;
    }
}
