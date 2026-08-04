package app.reading.lifearc.zodiacalreleasing;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import app.chart.data.ZodiacSign;

public final class ZodiacalReleasingCalculator {
    public static final String METHOD_ID = "HELLENISTIC_ZODIACAL_RELEASING_EGYPTIAN_YEARS_V1";
    public static final int MAX_LEVEL = 4;

    public ZodiacalReleasingTimeline calculate(ZodiacSign startSign, OffsetDateTime startDateTime, OffsetDateTime endDateTimeExclusive) {
        if (startSign == null) {
            throw new IllegalArgumentException("startSign is required");
        }
        if (startDateTime == null || endDateTimeExclusive == null) {
            throw new IllegalArgumentException("startDateTime and endDateTimeExclusive are required");
        }
        if (!endDateTimeExclusive.isAfter(startDateTime)) {
            throw new IllegalArgumentException("endDateTimeExclusive must be after startDateTime");
        }
        return new ZodiacalReleasingTimeline(
                METHOD_ID,
                startSign,
                startDateTime,
                endDateTimeExclusive,
                periods(startSign, startDateTime, endDateTimeExclusive, 1)
        );
    }

    private List<ZodiacalReleasingPeriod> periods(ZodiacSign startSign, OffsetDateTime startDateTime,
                                                  OffsetDateTime endDateTimeExclusive, int level) {
        List<ZodiacalReleasingPeriod> periods = new ArrayList<>();
        OffsetDateTime cursor = startDateTime;
        int sequenceIndex = 0;
        while (cursor.isBefore(endDateTimeExclusive)) {
            SignStep step = signStep(startSign, sequenceIndex);
            OffsetDateTime naturalEnd = cursor.plus(duration(level, step.sign()));
            OffsetDateTime periodEnd = naturalEnd.isAfter(endDateTimeExclusive) ? endDateTimeExclusive : naturalEnd;
            List<ZodiacalReleasingPeriod> subPeriods = level < MAX_LEVEL
                    ? periods(step.sign(), cursor, periodEnd, level + 1)
                    : List.of();
            periods.add(new ZodiacalReleasingPeriod(
                    level,
                    step.sign(),
                    cursor,
                    periodEnd,
                    sequenceIndex,
                    step.markers(),
                    subPeriods
            ));
            cursor = periodEnd;
            sequenceIndex++;
        }
        return List.copyOf(periods);
    }

    private SignStep signStep(ZodiacSign startSign, int sequenceIndex) {
        ZodiacSign sign;
        List<ZodiacalReleasingMarker> markers = new ArrayList<>();
        if (sequenceIndex < 12) {
            sign = advance(startSign, sequenceIndex);
            if (sequenceIndex == 6) {
                markers.add(ZodiacalReleasingMarker.PREPARATORY_LOOSING_OF_BOND);
            }
        } else {
            sign = advance(opposite(startSign), sequenceIndex - 12);
            if (sequenceIndex == 12) {
                markers.add(ZodiacalReleasingMarker.LOOSING_OF_BOND);
            }
            if (sign == startSign) {
                markers.add(ZodiacalReleasingMarker.COMPLETION);
            }
        }
        if (sign == culmination(startSign)) {
            markers.add(ZodiacalReleasingMarker.CULMINATION);
        }
        return new SignStep(sign, markers);
    }

    private Duration duration(int level, ZodiacSign sign) {
        int years = periodYears(sign);
        return switch (level) {
            case 1 -> Duration.ofDays(360L * years);
            case 2 -> Duration.ofDays(30L * years);
            case 3 -> Duration.ofHours(60L * years);
            case 4 -> Duration.ofHours(5L * years);
            default -> throw new IllegalArgumentException("Unsupported zodiacal releasing level " + level);
        };
    }

    public int periodYears(ZodiacSign sign) {
        return switch (sign) {
            case ARIES -> 15;
            case TAURUS -> 8;
            case GEMINI -> 20;
            case CANCER -> 25;
            case LEO -> 19;
            case VIRGO -> 20;
            case LIBRA -> 8;
            case SCORPIO -> 15;
            case SAGITTARIUS -> 12;
            case CAPRICORN -> 27;
            case AQUARIUS -> 30;
            case PISCES -> 12;
        };
    }

    private ZodiacSign opposite(ZodiacSign sign) {
        return advance(sign, 6);
    }

    private ZodiacSign culmination(ZodiacSign sign) {
        return advance(sign, 9);
    }

    private ZodiacSign advance(ZodiacSign sign, int signs) {
        ZodiacSign[] values = ZodiacSign.values();
        return values[Math.floorMod(sign.ordinal() + signs, values.length)];
    }

    private record SignStep(ZodiacSign sign, List<ZodiacalReleasingMarker> markers) {
        private SignStep {
            markers = List.copyOf(markers);
        }
    }
}
