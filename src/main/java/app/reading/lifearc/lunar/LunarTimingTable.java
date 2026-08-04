package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;
import java.util.List;

import app.chart.data.ZodiacSign;

public record LunarTimingTable(
        String methodId,
        String primaryDoctrine,
        String lunarReturnMethod,
        String lunationMethod,
        String eclipseCandidateMethod,
        String trueEclipseMethod,
        double solarEclipseNodeOrbDegrees,
        double lunarEclipseNodeOrbDegrees,
        int ageStartYears,
        int ageEndYearsInclusive,
        OffsetDateTime coverageStartDateTime,
        OffsetDateTime coverageEndDateTimeExclusive,
        double natalMoonLongitude,
        ZodiacSign natalMoonSign,
        double natalMoonDegreeInSign,
        int natalMoonHouse,
        List<LunarReturnEntry> lunarReturns,
        List<LunationEntry> lunations,
        List<EclipseEvent> eclipseEvents
) {
    public LunarTimingTable {
        lunarReturns = lunarReturns == null ? List.of() : List.copyOf(lunarReturns);
        lunations = lunations == null ? List.of() : List.copyOf(lunations);
        eclipseEvents = eclipseEvents == null ? List.of() : List.copyOf(eclipseEvents);
    }
}
