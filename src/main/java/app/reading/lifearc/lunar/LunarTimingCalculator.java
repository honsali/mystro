package app.reading.lifearc.lunar;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;
import app.chart.CalculationContext;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.reading.CoreDoctrineInfo;
import app.chart.data.SyzygyType;
import app.ephemeris.SweConst;
import app.ephemeris.SwissEphAdapter;
import app.chart.search.SyzygyEventSearch;
import app.chart.search.SyzygyEventSearch.SyzygyEvent;

public final class LunarTimingCalculator {
    public static final String METHOD_ID = "LUNAR_RETURNS_LUNATIONS_AND_ECLIPSES_V3";
    private static final String PRIMARY_DOCTRINE = "traditional_normalized";
    private static final String LUNAR_RETURN_METHOD = "EXACT_TROPICAL_MOON_RETURN_TO_NATAL_MOON_LONGITUDE; NATAL_LOCATION_CONTEXT_FOR_HOUSE_OVERLAY_ONLY";
    private static final String LUNATION_METHOD = "EXACT_SUN_MOON_ELONGATION_ROOTS_FOR_NEW_AND_FULL_MOONS; SYZYGY_POINT_IS_SUN_FOR_NEW_MOON_AND_MOON_FOR_FULL_MOON";
    private static final String ECLIPSE_CANDIDATE_METHOD = "MEAN_NODE_LONGITUDE_FILTER_ONLY; SOLAR_NEW_MOON_CANDIDATE_WITHIN_18_DEGREES_OF_NODE; LUNAR_FULL_MOON_CANDIDATE_WITHIN_12_DEGREES_OF_NODE; RETAINED_AS_SUPPORTING_REFERENCE_FOR_TRUE_ECLIPSE_ROWS";
    private static final String TRUE_ECLIPSE_METHOD = "SWISS_EPHEMERIS_GLOBAL_SOLAR_AND_LUNAR_ECLIPSE_SEARCH_WITH_SUBJECT_LOCATION_VISIBILITY; SOLAR_USES_SWE_SOL_ECLIPSE_WHEN_GLOB_PLUS_SWE_SOL_ECLIPSE_WHERE_AND_SWE_SOL_ECLIPSE_WHEN_LOC; LUNAR_USES_SWE_LUN_ECLIPSE_WHEN_PLUS_SWE_LUN_ECLIPSE_HOW_AND_SWE_LUN_ECLIPSE_WHEN_LOC";
    private static final double SOLAR_ECLIPSE_NODE_ORB_DEGREES = 18.0;
    private static final double LUNAR_ECLIPSE_NODE_ORB_DEGREES = 12.0;
    private static final double MEAN_TROPICAL_YEAR_DAYS = 365.2422;
    private static final double NANOS_PER_TROPICAL_YEAR = MEAN_TROPICAL_YEAR_DAYS * 86_400_000_000_000.0;
    private static final double SIDEREAL_LUNAR_MONTH_DAYS = 27.321661547;
    private static final double ROOT_DEGREE_TOLERANCE = 1.0e-9;
    private static final double SCAN_STEP_DAYS = 0.25;
    private static final double ECLIPSE_SEARCH_MARGIN_DAYS = 1.0;
    private static final double ECLIPSE_SEARCH_ADVANCE_DAYS = 20.0;
    private static final double LOCAL_ECLIPSE_MATCH_MARGIN_DAYS = 0.75;
    private static final int BISECTION_STEPS = 80;
    private static final CoreDoctrineInfo LUNAR_CONVENTIONS = new CoreDoctrineInfo(
            "lunar_timing",
            "Traditional Lunar Timing",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );
    private static final SyzygyEventSearch SYZYGY_EVENT_SEARCH = new SyzygyEventSearch();

    public LunarTimingTable calculateTable(Subject subject, NatalChart natalChart, LocalDate inquiryDate,
                                           int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }

        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        double coverageStartAgeYears = ageStartYears;
        double coverageEndAgeYears = ageEndYearsInclusive + 1.0;
        OffsetDateTime coverageStart = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageStartAgeYears);
        OffsetDateTime coverageEnd = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageEndAgeYears);
        double coverageStartJulianDay = julianDayFromInstant(coverageStart.toInstant());
        double coverageEndJulianDay = julianDayFromInstant(coverageEnd.toInstant());

        PlanetPosition natalMoon = natalChart.requirePlanet(Planet.MOON);
        CalculationContext ephemerisContext = new CalculationContext(subject, LUNAR_CONVENTIONS);
        List<LunarReturnEntry> lunarReturns = lunarReturns(subject, natalChart, ephemerisContext, natalMoon, activeDateTime, coverageStartJulianDay, coverageEndJulianDay);
        List<LunationEntry> lunations = lunations(subject, natalChart, ephemerisContext, activeDateTime, coverageStartJulianDay, coverageEndJulianDay);
        List<EclipseEvent> eclipseEvents = trueEclipseEvents(subject, natalChart, ephemerisContext, coverageStartJulianDay, coverageEndJulianDay);

        return new LunarTimingTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                LUNAR_RETURN_METHOD,
                LUNATION_METHOD,
                ECLIPSE_CANDIDATE_METHOD,
                TRUE_ECLIPSE_METHOD,
                SOLAR_ECLIPSE_NODE_ORB_DEGREES,
                LUNAR_ECLIPSE_NODE_ORB_DEGREES,
                ageStartYears,
                ageEndYearsInclusive,
                coverageStart,
                coverageEnd,
                natalMoon.getLongitude(),
                natalMoon.getSign(),
                natalMoon.getDegreeInSign(),
                natalMoon.getHouse(),
                lunarReturns,
                lunations,
                eclipseEvents
        );
    }

    private List<LunarReturnEntry> lunarReturns(Subject subject, NatalChart natalChart, CalculationContext ephemerisContext,
                                                PlanetPosition natalMoon, OffsetDateTime activeDateTime,
                                                double coverageStartJulianDay, double coverageEndJulianDay) {
        double birthJulianDay = julianDayFromInstant(subject.getResolvedUtcInstant());
        double generationEndJulianDay = coverageEndJulianDay + SIDEREAL_LUNAR_MONTH_DAYS + 2.0;
        int maxReturnNumber = Math.max(1, (int) Math.ceil((generationEndJulianDay - birthJulianDay) / SIDEREAL_LUNAR_MONTH_DAYS) + 2);
        ZoneOffset outputOffset = subject.getUtcBirthDateTime().getOffset();
        List<ReturnInstant> instants = new ArrayList<>();
        for (int returnNumber = 0; returnNumber <= maxReturnNumber; returnNumber++) {
            ReturnInstant instant = lunarReturnInstant(subject, ephemerisContext, natalMoon.getLongitude(), outputOffset, returnNumber);
            instants.add(instant);
            if (instant.julianDay() > generationEndJulianDay) {
                break;
            }
        }

        List<LunarReturnEntry> entries = new ArrayList<>();
        int sequenceIndex = 1;
        for (int i = 0; i < instants.size() - 1; i++) {
            ReturnInstant start = instants.get(i);
            if (start.julianDay() < coverageStartJulianDay - ROOT_DEGREE_TOLERANCE) {
                continue;
            }
            if (start.julianDay() >= coverageEndJulianDay - ROOT_DEGREE_TOLERANCE) {
                break;
            }
            entries.add(lunarReturnEntry(sequenceIndex, subject, natalChart, ephemerisContext, natalMoon, activeDateTime, start, instants.get(i + 1)));
            sequenceIndex++;
        }
        return List.copyOf(entries);
    }

    private ReturnInstant lunarReturnInstant(Subject subject, CalculationContext ephemerisContext, double natalMoonLongitude,
                                             ZoneOffset outputOffset, int returnNumber) {
        if (returnNumber == 0) {
            Instant birthInstant = subject.getResolvedUtcInstant();
            return new ReturnInstant(returnNumber, julianDayFromInstant(birthInstant), birthInstant, birthInstant.atOffset(outputOffset));
        }
        double julianDay = findLunarReturnJulianDay(subject, ephemerisContext, natalMoonLongitude, returnNumber);
        Instant instant = instantFromJulianDay(julianDay);
        return new ReturnInstant(returnNumber, julianDay, instant, instant.atOffset(outputOffset));
    }

    private double findLunarReturnJulianDay(Subject subject, CalculationContext ephemerisContext,
                                            double natalMoonLongitude, int returnNumber) {
        double birthJulianDay = julianDayFromInstant(subject.getResolvedUtcInstant());
        double approximateJulianDay = birthJulianDay + returnNumber * SIDEREAL_LUNAR_MONTH_DAYS;
        for (double radiusDays : List.of(2.0, 4.0, 8.0)) {
            Double root = scanForRoot(
                    julianDay -> lunarReturnDifference(ephemerisContext, natalMoonLongitude, julianDay),
                    approximateJulianDay - radiusDays,
                    approximateJulianDay + radiusDays
            );
            if (root != null) {
                return root;
            }
        }
        throw new IllegalArgumentException("Could not bracket lunar return number " + returnNumber);
    }

    private double lunarReturnDifference(CalculationContext ephemerisContext, double natalMoonLongitude, double julianDay) {
        double moonLongitude = ephemerisContext.longitudeFor(Planet.MOON, SweConst.SE_MOON, julianDay);
        double difference = AstroMath.normalize(moonLongitude - natalMoonLongitude);
        return difference > 180.0 ? difference - 360.0 : difference;
    }

    private LunarReturnEntry lunarReturnEntry(int sequenceIndex, Subject subject, NatalChart natalChart,
                                              CalculationContext ephemerisContext, PlanetPosition natalMoon,
                                              OffsetDateTime activeDateTime, ReturnInstant start, ReturnInstant end) {
        double moonLongitude = ephemerisContext.longitudeFor(Planet.MOON, SweConst.SE_MOON, start.julianDay());
        double moonLatitude = ephemerisContext.latitudeFor(Planet.MOON, SweConst.SE_MOON, start.julianDay());
        double sunLongitude = ephemerisContext.longitudeFor(Planet.SUN, SweConst.SE_SUN, start.julianDay());
        NodeDistance nodeDistance = nodeDistance(ephemerisContext, moonLongitude, start.julianDay());
        return new LunarReturnEntry(
                sequenceIndex,
                start.returnNumber(),
                active(start.dateTime(), end.dateTime(), activeDateTime),
                start.dateTime(),
                end.dateTime(),
                start.julianDay(),
                ageYears(subject, start.julianDay()),
                moonLongitude,
                AstroMath.signOf(moonLongitude),
                AstroMath.degreeInSign(moonLongitude),
                moonLatitude,
                natalMoon.getHouse(),
                sunLongitude,
                AstroMath.signOf(sunLongitude),
                AstroMath.degreeInSign(sunLongitude),
                AstroMath.normalize(moonLongitude - sunLongitude),
                nodeDistance.nearestNode(),
                nodeDistance.nearestNodeLongitude(),
                nodeDistance.orbDegrees()
        );
    }

    private List<LunationEntry> lunations(Subject subject, NatalChart natalChart, CalculationContext ephemerisContext,
                                          OffsetDateTime activeDateTime,
                                          double coverageStartJulianDay, double coverageEndJulianDay) {
        List<SyzygyEvent> instants = syzygyInstants(ephemerisContext, coverageStartJulianDay, coverageEndJulianDay);
        List<LunationEntry> entries = new ArrayList<>();
        int sequenceIndex = 1;
        for (int i = 0; i < instants.size() - 1; i++) {
            SyzygyEvent current = instants.get(i);
            if (current.julianDay() < coverageStartJulianDay - ROOT_DEGREE_TOLERANCE) {
                continue;
            }
            if (current.julianDay() >= coverageEndJulianDay - ROOT_DEGREE_TOLERANCE) {
                break;
            }
            entries.add(lunationEntry(sequenceIndex, subject, natalChart, ephemerisContext, activeDateTime, current, instants.get(i + 1)));
            sequenceIndex++;
        }
        return List.copyOf(entries);
    }

    private List<SyzygyEvent> syzygyInstants(CalculationContext ephemerisContext,
                                             double coverageStartJulianDay, double coverageEndJulianDay) {
        SyzygyEvent previous = SYZYGY_EVENT_SEARCH.previous(coverageStartJulianDay, ephemerisContext);
        List<SyzygyEvent> instants = new ArrayList<>();
        instants.add(previous);
        SyzygyEvent current = previous;
        while (current.julianDay() < coverageEndJulianDay) {
            current = SYZYGY_EVENT_SEARCH.next(current, ephemerisContext);
            instants.add(current);
        }
        return List.copyOf(instants);
    }

    private LunationEntry lunationEntry(int sequenceIndex, Subject subject, NatalChart natalChart,
                                        CalculationContext ephemerisContext, OffsetDateTime activeDateTime,
                                        SyzygyEvent current, SyzygyEvent next) {
        double sunLongitude = ephemerisContext.longitudeFor(Planet.SUN, SweConst.SE_SUN, current.julianDay());
        double moonLongitude = ephemerisContext.longitudeFor(Planet.MOON, SweConst.SE_MOON, current.julianDay());
        double moonLatitude = ephemerisContext.latitudeFor(Planet.MOON, SweConst.SE_MOON, current.julianDay());
        double syzygyLongitude = current.type() == SyzygyType.FULL_MOON ? moonLongitude : sunLongitude;
        NodeDistance nodeDistance = nodeDistance(ephemerisContext, syzygyLongitude, current.julianDay());
        EclipseCandidateType eclipseType = eclipseType(current.type(), nodeDistance.orbDegrees());
        ZodiacSign syzygySign = AstroMath.signOf(syzygyLongitude);
        OffsetDateTime dateTime = dateTime(current.julianDay(), subject.getUtcBirthDateTime().getOffset());
        OffsetDateTime periodEndDateTime = dateTime(next.julianDay(), subject.getUtcBirthDateTime().getOffset());
        return new LunationEntry(
                sequenceIndex,
                current.type(),
                active(dateTime, periodEndDateTime, activeDateTime),
                dateTime,
                periodEndDateTime,
                current.julianDay(),
                ageYears(subject, current.julianDay()),
                syzygyLongitude,
                syzygySign,
                AstroMath.degreeInSign(syzygyLongitude),
                houseForSign(natalChart, syzygySign),
                sunLongitude,
                AstroMath.signOf(sunLongitude),
                AstroMath.degreeInSign(sunLongitude),
                moonLongitude,
                AstroMath.signOf(moonLongitude),
                AstroMath.degreeInSign(moonLongitude),
                moonLatitude,
                AstroMath.rawAngularSeparation(sunLongitude, moonLongitude),
                nodeDistance.nearestNode(),
                nodeDistance.nearestNodeLongitude(),
                nodeDistance.orbDegrees(),
                eclipseType
        );
    }

    private List<EclipseEvent> trueEclipseEvents(Subject subject, NatalChart natalChart, CalculationContext ephemerisContext,
                                                  double coverageStartJulianDay, double coverageEndJulianDay) {
        List<EclipseEvent> events = new ArrayList<>();
        events.addAll(solarEclipseEvents(subject, natalChart, ephemerisContext, coverageStartJulianDay, coverageEndJulianDay));
        events.addAll(lunarEclipseEvents(subject, natalChart, ephemerisContext, coverageStartJulianDay, coverageEndJulianDay));
        events.sort(Comparator
                .comparingDouble(EclipseEvent::maximumJulianDayUt)
                .thenComparing(event -> event.kind().ordinal()));

        List<EclipseEvent> resequenced = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            resequenced.add(withSequence(i + 1, events.get(i)));
        }
        return List.copyOf(resequenced);
    }

    private List<EclipseEvent> solarEclipseEvents(Subject subject, NatalChart natalChart,
                                                  CalculationContext ephemerisContext,
                                                  double coverageStartJulianDay, double coverageEndJulianDay) {
        List<EclipseEvent> events = new ArrayList<>();
        LocalEclipseSearchResult localVisibility = solarLocalVisibilityEvents(subject, ephemerisContext, coverageStartJulianDay, coverageEndJulianDay);
        double searchStart = coverageStartJulianDay - ECLIPSE_SEARCH_MARGIN_DAYS;
        while (searchStart < coverageEndJulianDay) {
            double[] contacts = new double[10];
            StringBuilder error = new StringBuilder();
            int result = ephemerisContext.getSwissEph().swe_sol_eclipse_when_glob(
                    searchStart,
                    SweConst.SEFLG_SWIEPH,
                    0,
                    contacts,
                    0,
                    error
            );
            requireEclipseSearchResult(result, "global solar eclipse search", error);
            double maximumJulianDay = contacts[0];
            if (!isJulianDay(maximumJulianDay) || maximumJulianDay >= coverageEndJulianDay - ROOT_DEGREE_TOLERANCE) {
                break;
            }
            if (maximumJulianDay >= coverageStartJulianDay - ROOT_DEGREE_TOLERANCE) {
                events.add(solarEclipseEvent(events.size() + 1, subject, natalChart, ephemerisContext, result, contacts, localVisibility));
            }
            searchStart = Math.max(searchStart + ECLIPSE_SEARCH_ADVANCE_DAYS, maximumJulianDay + ECLIPSE_SEARCH_ADVANCE_DAYS);
        }
        return List.copyOf(events);
    }

    private EclipseEvent solarEclipseEvent(int sequenceIndex, Subject subject, NatalChart natalChart,
                                           CalculationContext ephemerisContext, int result, double[] contacts,
                                           LocalEclipseSearchResult localVisibilitySearch) {
        double maximumJulianDay = contacts[0];
        double[] maximumGeopos = new double[10];
        double[] attributes = new double[20];
        StringBuilder error = new StringBuilder();
        int attributeResult = ephemerisContext.getSwissEph().swe_sol_eclipse_where(
                maximumJulianDay,
                SweConst.SEFLG_SWIEPH,
                maximumGeopos,
                attributes,
                error
        );
        requireEclipseAttributeResult(attributeResult, "global solar eclipse attributes", error);

        double syzygyLongitude = ephemerisContext.longitudeFor(Planet.SUN, SweConst.SE_SUN, maximumJulianDay);
        ZodiacSign syzygySign = AstroMath.signOf(syzygyLongitude);
        NodeDistance nodeDistance = nodeDistance(ephemerisContext, syzygyLongitude, maximumJulianDay);
        List<EclipseContact> globalContacts = eclipseContacts(contacts, solarContactSpecs(subject.getUtcBirthDateTime().getOffset()));
        ResolvedLocalVisibility resolvedVisibility = resolveLocalVisibility(
                EclipseEventKind.SOLAR,
                maximumJulianDay,
                globalContacts,
                localVisibilitySearch,
                subject
        );
        return new EclipseEvent(
                sequenceIndex,
                TrueEclipseCalculationDesign.METHOD_ID,
                EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY,
                EclipseEventKind.SOLAR,
                SyzygyType.NEW_MOON,
                solarEclipseType(result != 0 ? result : attributeResult),
                eclipseType(SyzygyType.NEW_MOON, nodeDistance.orbDegrees()),
                dateTime(maximumJulianDay, subject.getUtcBirthDateTime().getOffset()),
                maximumJulianDay,
                syzygyLongitude,
                syzygySign,
                AstroMath.degreeInSign(syzygyLongitude),
                houseForSign(natalChart, syzygySign),
                nodeDistance.nearestNode(),
                nodeDistance.nearestNodeLongitude(),
                nodeDistance.orbDegrees(),
                attributeResult == 0 ? null : nonNegative(attributes[0]),
                attributeResult == 0 ? null : nonNegative(attributes[2]),
                null,
                attributeResult == 0 ? null : positiveInteger(attributes[9]),
                attributeResult == 0 ? null : positiveInteger(attributes[10]),
                contactsWithLocalVisibility(globalContacts, resolvedVisibility.visibility(), EclipseEventKind.SOLAR),
                resolvedVisibility.visibility()
        );
    }

    private List<EclipseEvent> lunarEclipseEvents(Subject subject, NatalChart natalChart,
                                                  CalculationContext ephemerisContext,
                                                  double coverageStartJulianDay, double coverageEndJulianDay) {
        List<EclipseEvent> events = new ArrayList<>();
        LocalEclipseSearchResult localVisibility = lunarLocalVisibilityEvents(subject, ephemerisContext, coverageStartJulianDay, coverageEndJulianDay);
        double searchStart = coverageStartJulianDay - ECLIPSE_SEARCH_MARGIN_DAYS;
        while (searchStart < coverageEndJulianDay) {
            double[] contacts = new double[10];
            StringBuilder error = new StringBuilder();
            int result = ephemerisContext.getSwissEph().swe_lun_eclipse_when(
                    searchStart,
                    SweConst.SEFLG_SWIEPH,
                    0,
                    contacts,
                    0,
                    error
            );
            requireEclipseSearchResult(result, "global lunar eclipse search", error);
            double maximumJulianDay = contacts[0];
            if (!isJulianDay(maximumJulianDay) || maximumJulianDay >= coverageEndJulianDay - ROOT_DEGREE_TOLERANCE) {
                break;
            }
            if (maximumJulianDay >= coverageStartJulianDay - ROOT_DEGREE_TOLERANCE) {
                events.add(lunarEclipseEvent(events.size() + 1, subject, natalChart, ephemerisContext, result, contacts, localVisibility));
            }
            searchStart = Math.max(searchStart + ECLIPSE_SEARCH_ADVANCE_DAYS, maximumJulianDay + ECLIPSE_SEARCH_ADVANCE_DAYS);
        }
        return List.copyOf(events);
    }

    private EclipseEvent lunarEclipseEvent(int sequenceIndex, Subject subject, NatalChart natalChart,
                                           CalculationContext ephemerisContext, int result, double[] contacts,
                                           LocalEclipseSearchResult localVisibilitySearch) {
        double maximumJulianDay = contacts[0];
        double[] attributes = new double[20];
        StringBuilder error = new StringBuilder();
        int attributeResult = ephemerisContext.getSwissEph().swe_lun_eclipse_how(
                maximumJulianDay,
                SweConst.SEFLG_SWIEPH,
                null,
                attributes,
                error
        );
        requireEclipseAttributeResult(attributeResult, "global lunar eclipse attributes", error);

        double syzygyLongitude = ephemerisContext.longitudeFor(Planet.MOON, SweConst.SE_MOON, maximumJulianDay);
        ZodiacSign syzygySign = AstroMath.signOf(syzygyLongitude);
        NodeDistance nodeDistance = nodeDistance(ephemerisContext, syzygyLongitude, maximumJulianDay);
        List<EclipseContact> globalContacts = eclipseContacts(contacts, lunarContactSpecs(subject.getUtcBirthDateTime().getOffset()));
        ResolvedLocalVisibility resolvedVisibility = resolveLocalVisibility(
                EclipseEventKind.LUNAR,
                maximumJulianDay,
                globalContacts,
                localVisibilitySearch,
                subject
        );
        return new EclipseEvent(
                sequenceIndex,
                TrueEclipseCalculationDesign.METHOD_ID,
                EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY,
                EclipseEventKind.LUNAR,
                SyzygyType.FULL_MOON,
                lunarEclipseType(result != 0 ? result : attributeResult),
                eclipseType(SyzygyType.FULL_MOON, nodeDistance.orbDegrees()),
                dateTime(maximumJulianDay, subject.getUtcBirthDateTime().getOffset()),
                maximumJulianDay,
                syzygyLongitude,
                syzygySign,
                AstroMath.degreeInSign(syzygyLongitude),
                houseForSign(natalChart, syzygySign),
                nodeDistance.nearestNode(),
                nodeDistance.nearestNodeLongitude(),
                nodeDistance.orbDegrees(),
                attributeResult == 0 ? null : nonNegative(attributes[0]),
                null,
                attributeResult == 0 ? null : nonNegative(attributes[1]),
                attributeResult == 0 ? null : positiveInteger(attributes[9]),
                attributeResult == 0 ? null : positiveInteger(attributes[10]),
                contactsWithLocalVisibility(globalContacts, resolvedVisibility.visibility(), EclipseEventKind.LUNAR),
                resolvedVisibility.visibility()
        );
    }

    private EclipseEvent withSequence(int sequenceIndex, EclipseEvent event) {
        return new EclipseEvent(
                sequenceIndex,
                event.methodId(),
                event.calculationScope(),
                event.kind(),
                event.syzygy(),
                event.eclipseType(),
                event.candidateReference(),
                event.maximumDateTime(),
                event.maximumJulianDayUt(),
                event.syzygyLongitude(),
                event.syzygySign(),
                event.syzygyDegreeInSign(),
                event.natalHouseOverlay(),
                event.nearestNode(),
                event.nearestNodeLongitude(),
                event.nearestNodeOrbDegrees(),
                event.magnitude(),
                event.obscuration(),
                event.penumbralMagnitude(),
                event.sarosSeries(),
                event.sarosMember(),
                event.contacts(),
                event.visibility()
        );
    }

    private LocalEclipseSearchResult solarLocalVisibilityEvents(Subject subject, CalculationContext ephemerisContext,
                                                                double coverageStartJulianDay, double coverageEndJulianDay) {
        List<LocalEclipseVisibilityRow> rows = new ArrayList<>();
        double searchStart = coverageStartJulianDay - ECLIPSE_SEARCH_MARGIN_DAYS;
        double searchEnd = coverageEndJulianDay + ECLIPSE_SEARCH_MARGIN_DAYS;
        double[] geopos = eclipseGeopos(subject);
        while (searchStart < searchEnd) {
            double[] localContacts = new double[10];
            double[] attributes = new double[20];
            StringBuilder error = new StringBuilder();
            int result = ephemerisContext.getSwissEph().swe_sol_eclipse_when_loc(
                    searchStart,
                    SweConst.SEFLG_SWIEPH,
                    geopos,
                    localContacts,
                    attributes,
                    0,
                    error
            );
            if (result == SweConst.ERR) {
                return LocalEclipseSearchResult.unsupported("Swiss Ephemeris failed for local solar eclipse visibility at subject location: " + error);
            }
            if (result <= 0) {
                return LocalEclipseSearchResult.unsupported("Swiss Ephemeris returned no local solar eclipse visibility row from search start " + searchStart + ": " + error);
            }
            double localMaximum = localContacts[0];
            if (!isJulianDay(localMaximum) || localMaximum >= searchEnd + ROOT_DEGREE_TOLERANCE) {
                break;
            }
            if (localMaximum >= coverageStartJulianDay - LOCAL_ECLIPSE_MATCH_MARGIN_DAYS) {
                rows.add(new LocalEclipseVisibilityRow(
                        result,
                        localMaximum,
                        dateTime(localMaximum, subject.getUtcBirthDateTime().getOffset()),
                        visibleSolarContactPhases(result),
                        (result & SweConst.SE_ECL_MAX_VISIBLE) != 0,
                        nonNegative(attributes[0]),
                        nonNegative(attributes[2]),
                        null
                ));
            }
            searchStart = nextEclipseSearchStart(searchStart, localMaximum);
        }
        return LocalEclipseSearchResult.supported("Local solar visibility calculated with swe_sol_eclipse_when_loc", rows);
    }

    private LocalEclipseSearchResult lunarLocalVisibilityEvents(Subject subject, CalculationContext ephemerisContext,
                                                                double coverageStartJulianDay, double coverageEndJulianDay) {
        List<LocalEclipseVisibilityRow> rows = new ArrayList<>();
        double searchStart = coverageStartJulianDay - ECLIPSE_SEARCH_MARGIN_DAYS;
        double searchEnd = coverageEndJulianDay + ECLIPSE_SEARCH_MARGIN_DAYS;
        double[] geopos = eclipseGeopos(subject);
        while (searchStart < searchEnd) {
            double[] localContacts = new double[10];
            double[] attributes = new double[20];
            StringBuilder error = new StringBuilder();
            int result = ephemerisContext.getSwissEph().swe_lun_eclipse_when_loc(
                    searchStart,
                    SweConst.SEFLG_SWIEPH,
                    geopos,
                    localContacts,
                    attributes,
                    0,
                    error
            );
            if (result == SweConst.ERR) {
                return LocalEclipseSearchResult.unsupported("Swiss Ephemeris failed for local lunar eclipse visibility at subject location: " + error);
            }
            if (result <= 0) {
                return LocalEclipseSearchResult.unsupported("Swiss Ephemeris returned no local lunar eclipse visibility row from search start " + searchStart + ": " + error);
            }
            double localMaximum = localContacts[0];
            if (!isJulianDay(localMaximum) || localMaximum >= searchEnd + ROOT_DEGREE_TOLERANCE) {
                break;
            }
            if (localMaximum >= coverageStartJulianDay - LOCAL_ECLIPSE_MATCH_MARGIN_DAYS) {
                rows.add(new LocalEclipseVisibilityRow(
                        result,
                        localMaximum,
                        dateTime(localMaximum, subject.getUtcBirthDateTime().getOffset()),
                        visibleLunarContactPhases(result),
                        (result & SweConst.SE_ECL_MAX_VISIBLE) != 0,
                        nonNegative(attributes[0]),
                        null,
                        nonNegative(attributes[1])
                ));
            }
            searchStart = nextEclipseSearchStart(searchStart, localMaximum);
        }
        return LocalEclipseSearchResult.supported("Local lunar visibility calculated with swe_lun_eclipse_when_loc", rows);
    }

    private double[] eclipseGeopos(Subject subject) {
        return new double[] {
                subject.getLongitude(),
                subject.getLatitude(),
                subject.getElevationMeters()
        };
    }

    private double nextEclipseSearchStart(double currentSearchStart, double eventMaximumJulianDay) {
        double nextSearchStart = Math.max(
                currentSearchStart + ECLIPSE_SEARCH_ADVANCE_DAYS,
                eventMaximumJulianDay + ECLIPSE_SEARCH_ADVANCE_DAYS
        );
        if (nextSearchStart <= currentSearchStart + ROOT_DEGREE_TOLERANCE) {
            return currentSearchStart + ECLIPSE_SEARCH_ADVANCE_DAYS;
        }
        return nextSearchStart;
    }

    private ResolvedLocalVisibility resolveLocalVisibility(EclipseEventKind kind, double globalMaximumJulianDay,
                                                           List<EclipseContact> globalContacts,
                                                           LocalEclipseSearchResult localSearch,
                                                           Subject subject) {
        if (!localSearch.supported()) {
            return new ResolvedLocalVisibility(EclipseVisibility.unknown(true, localSearch.reason()));
        }
        LocalEclipseVisibilityRow match = matchingLocalVisibility(globalMaximumJulianDay, globalContacts, localSearch.rows());
        String location = subjectLocation(subject);
        String api = kind == EclipseEventKind.SOLAR ? "swe_sol_eclipse_when_loc" : "swe_lun_eclipse_when_loc";
        if (match == null) {
            return new ResolvedLocalVisibility(EclipseVisibility.notVisible(
                    "No local " + kind.name().toLowerCase() + " eclipse visibility matched this global event for subject location "
                            + location + " using " + api
            ));
        }
        if (!match.visible() || (!match.maximumVisibleAtLocation() && match.visibleContactPhases().isEmpty())) {
            return new ResolvedLocalVisibility(EclipseVisibility.unknown(
                    true,
                    "Local " + kind.name().toLowerCase() + " eclipse search matched the global event at "
                            + match.maximumDateTime() + " but returned no visible phase flags"
            ));
        }
        return new ResolvedLocalVisibility(EclipseVisibility.visible(
                match.maximumVisibleAtLocation(),
                match.visibleContactPhases(),
                "Visible at subject location " + location + " using " + api
                        + "; local visibility maximum " + match.maximumDateTime()
                        + "; visible phases " + match.visibleContactPhases()
                        + localMagnitudeNote(match)
        ));
    }

    private LocalEclipseVisibilityRow matchingLocalVisibility(double globalMaximumJulianDay, List<EclipseContact> globalContacts,
                                                              List<LocalEclipseVisibilityRow> localRows) {
        double start = globalContacts.stream()
                .mapToDouble(EclipseContact::julianDayUt)
                .min()
                .orElse(globalMaximumJulianDay - LOCAL_ECLIPSE_MATCH_MARGIN_DAYS);
        double end = globalContacts.stream()
                .mapToDouble(EclipseContact::julianDayUt)
                .max()
                .orElse(globalMaximumJulianDay + LOCAL_ECLIPSE_MATCH_MARGIN_DAYS);
        return localRows.stream()
                .filter(row -> row.maximumJulianDayUt() >= start - LOCAL_ECLIPSE_MATCH_MARGIN_DAYS
                        && row.maximumJulianDayUt() <= end + LOCAL_ECLIPSE_MATCH_MARGIN_DAYS)
                .min(Comparator.comparingDouble(row -> Math.abs(row.maximumJulianDayUt() - globalMaximumJulianDay)))
                .orElse(null);
    }

    private List<EclipseContact> contactsWithLocalVisibility(List<EclipseContact> contacts, EclipseVisibility visibility,
                                                             EclipseEventKind kind) {
        List<EclipseContact> resolved = new ArrayList<>();
        for (EclipseContact contact : contacts) {
            EclipseVisibilityStatus status = contactVisibilityStatus(contact.phase(), visibility, kind);
            resolved.add(new EclipseContact(
                    contact.phase(),
                    contact.dateTime(),
                    contact.julianDayUt(),
                    status,
                    contactVisibilityNote(contact.phase(), status, visibility, kind)
            ));
        }
        return List.copyOf(resolved);
    }

    private EclipseVisibilityStatus contactVisibilityStatus(EclipseContactPhase phase, EclipseVisibility visibility,
                                                            EclipseEventKind kind) {
        if (visibility.localVisibility() == EclipseVisibilityStatus.UNKNOWN) {
            return EclipseVisibilityStatus.UNKNOWN;
        }
        if (visibility.localVisibility() == EclipseVisibilityStatus.NOT_VISIBLE) {
            return EclipseVisibilityStatus.NOT_VISIBLE;
        }
        if (kind == EclipseEventKind.SOLAR) {
            return EclipseVisibilityStatus.UNKNOWN;
        }
        if (!localVisibilityPhaseSupported(phase)) {
            return EclipseVisibilityStatus.UNKNOWN;
        }
        return visibility.visibleContactPhases().contains(phase)
                ? EclipseVisibilityStatus.VISIBLE
                : EclipseVisibilityStatus.NOT_VISIBLE;
    }

    private String contactVisibilityNote(EclipseContactPhase phase, EclipseVisibilityStatus status, EclipseVisibility visibility,
                                         EclipseEventKind kind) {
        if (status == EclipseVisibilityStatus.UNKNOWN && visibility.localVisibility() == EclipseVisibilityStatus.VISIBLE
                && kind == EclipseEventKind.SOLAR) {
            return "Solar local visibility is summarized in visibleContactPhases; this global contact instant is not visibility-classified because swe_sol_eclipse_when_loc returns local contact times; "
                    + visibility.reason();
        }
        if (status == EclipseVisibilityStatus.UNKNOWN && visibility.localVisibility() == EclipseVisibilityStatus.VISIBLE
                && !localVisibilityPhaseSupported(phase)) {
            return "Local API does not expose a safe visibility flag for this global contact phase; " + visibility.reason();
        }
        return visibility.reason();
    }

    private boolean localVisibilityPhaseSupported(EclipseContactPhase phase) {
        return phase == EclipseContactPhase.MAXIMUM
                || phase == EclipseContactPhase.ECLIPSE_BEGIN
                || phase == EclipseContactPhase.ECLIPSE_END
                || phase == EclipseContactPhase.PARTIAL_BEGIN
                || phase == EclipseContactPhase.PARTIAL_END
                || phase == EclipseContactPhase.TOTALITY_BEGIN
                || phase == EclipseContactPhase.TOTALITY_END
                || phase == EclipseContactPhase.PENUMBRAL_BEGIN
                || phase == EclipseContactPhase.PENUMBRAL_END;
    }

    private List<EclipseContactPhase> visibleSolarContactPhases(int flags) {
        List<EclipseContactPhase> phases = new ArrayList<>();
        addVisiblePhase(phases, flags, SweConst.SE_ECL_MAX_VISIBLE, EclipseContactPhase.MAXIMUM);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_1ST_VISIBLE, EclipseContactPhase.ECLIPSE_BEGIN);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_2ND_VISIBLE, EclipseContactPhase.TOTALITY_BEGIN);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_3RD_VISIBLE, EclipseContactPhase.TOTALITY_END);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_4TH_VISIBLE, EclipseContactPhase.ECLIPSE_END);
        return List.copyOf(phases);
    }

    private List<EclipseContactPhase> visibleLunarContactPhases(int flags) {
        List<EclipseContactPhase> phases = new ArrayList<>();
        addVisiblePhase(phases, flags, SweConst.SE_ECL_MAX_VISIBLE, EclipseContactPhase.MAXIMUM);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_PARTBEG_VISIBLE, EclipseContactPhase.PARTIAL_BEGIN);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_PARTEND_VISIBLE, EclipseContactPhase.PARTIAL_END);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_TOTBEG_VISIBLE, EclipseContactPhase.TOTALITY_BEGIN);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_TOTEND_VISIBLE, EclipseContactPhase.TOTALITY_END);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_PENUMBBEG_VISIBLE, EclipseContactPhase.PENUMBRAL_BEGIN);
        addVisiblePhase(phases, flags, SweConst.SE_ECL_PENUMBEND_VISIBLE, EclipseContactPhase.PENUMBRAL_END);
        return List.copyOf(phases);
    }

    private void addVisiblePhase(List<EclipseContactPhase> phases, int flags, int visibilityFlag, EclipseContactPhase phase) {
        if ((flags & visibilityFlag) != 0) {
            phases.add(phase);
        }
    }

    private String localMagnitudeNote(LocalEclipseVisibilityRow match) {
        List<String> parts = new ArrayList<>();
        if (match.magnitude() != null) {
            parts.add("magnitude=" + formatLocalVisibilityNumber(match.magnitude()));
        }
        if (match.obscuration() != null) {
            parts.add("obscuration=" + formatLocalVisibilityNumber(match.obscuration()));
        }
        if (match.penumbralMagnitude() != null) {
            parts.add("penumbralMagnitude=" + formatLocalVisibilityNumber(match.penumbralMagnitude()));
        }
        return parts.isEmpty() ? "" : "; local " + String.join(", ", parts);
    }

    private String formatLocalVisibilityNumber(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    private String subjectLocation(Subject subject) {
        return "lat=" + subject.getLatitude() + ", lon=" + subject.getLongitude();
    }

    private List<EclipseContact> eclipseContacts(double[] contactJulianDays, List<EclipseContactSpec> specs) {
        List<EclipseContact> contacts = new ArrayList<>();
        for (EclipseContactSpec spec : specs) {
            double julianDay = contactJulianDays[spec.index()];
            if (!isJulianDay(julianDay)) {
                continue;
            }
            contacts.add(new EclipseContact(
                    spec.phase(),
                    dateTime(julianDay, spec.outputOffset()),
                    julianDay,
                    EclipseVisibilityStatus.UNKNOWN,
                    "Global contact instant; local visibility resolved after event matching"
            ));
        }
        contacts.sort(Comparator.comparingDouble(EclipseContact::julianDayUt));
        return List.copyOf(contacts);
    }

    private List<EclipseContactSpec> solarContactSpecs(ZoneOffset outputOffset) {
        return List.of(
                new EclipseContactSpec(0, EclipseContactPhase.MAXIMUM, outputOffset),
                new EclipseContactSpec(1, EclipseContactPhase.LOCAL_APPARENT_NOON, outputOffset),
                new EclipseContactSpec(2, EclipseContactPhase.ECLIPSE_BEGIN, outputOffset),
                new EclipseContactSpec(3, EclipseContactPhase.ECLIPSE_END, outputOffset),
                new EclipseContactSpec(4, EclipseContactPhase.TOTALITY_BEGIN, outputOffset),
                new EclipseContactSpec(5, EclipseContactPhase.TOTALITY_END, outputOffset),
                new EclipseContactSpec(6, EclipseContactPhase.CENTER_LINE_BEGIN, outputOffset),
                new EclipseContactSpec(7, EclipseContactPhase.CENTER_LINE_END, outputOffset),
                new EclipseContactSpec(8, EclipseContactPhase.HYBRID_TOTAL_BEGIN, outputOffset),
                new EclipseContactSpec(9, EclipseContactPhase.HYBRID_ANNULAR_RESUME, outputOffset)
        );
    }

    private List<EclipseContactSpec> lunarContactSpecs(ZoneOffset outputOffset) {
        return List.of(
                new EclipseContactSpec(0, EclipseContactPhase.MAXIMUM, outputOffset),
                new EclipseContactSpec(2, EclipseContactPhase.PARTIAL_BEGIN, outputOffset),
                new EclipseContactSpec(3, EclipseContactPhase.PARTIAL_END, outputOffset),
                new EclipseContactSpec(4, EclipseContactPhase.TOTALITY_BEGIN, outputOffset),
                new EclipseContactSpec(5, EclipseContactPhase.TOTALITY_END, outputOffset),
                new EclipseContactSpec(6, EclipseContactPhase.PENUMBRAL_BEGIN, outputOffset),
                new EclipseContactSpec(7, EclipseContactPhase.PENUMBRAL_END, outputOffset)
        );
    }

    private EclipseEventType solarEclipseType(int flags) {
        if ((flags & SweConst.SE_ECL_ANNULAR_TOTAL) != 0) {
            return EclipseEventType.SOLAR_HYBRID;
        }
        if ((flags & SweConst.SE_ECL_TOTAL) != 0) {
            return EclipseEventType.SOLAR_TOTAL;
        }
        if ((flags & SweConst.SE_ECL_ANNULAR) != 0) {
            return EclipseEventType.SOLAR_ANNULAR;
        }
        if ((flags & SweConst.SE_ECL_PARTIAL) != 0) {
            return EclipseEventType.SOLAR_PARTIAL;
        }
        return EclipseEventType.UNKNOWN;
    }

    private EclipseEventType lunarEclipseType(int flags) {
        if ((flags & SweConst.SE_ECL_TOTAL) != 0) {
            return EclipseEventType.LUNAR_TOTAL;
        }
        if ((flags & SweConst.SE_ECL_PARTIAL) != 0) {
            return EclipseEventType.LUNAR_PARTIAL;
        }
        if ((flags & SweConst.SE_ECL_PENUMBRAL) != 0) {
            return EclipseEventType.LUNAR_PENUMBRAL;
        }
        return EclipseEventType.UNKNOWN;
    }

    private void requireEclipseSearchResult(int result, String calculation, StringBuilder error) {
        if (result == SweConst.ERR) {
            throw new IllegalArgumentException("Swiss Ephemeris failed for " + calculation + ": " + error);
        }
    }

    private void requireEclipseAttributeResult(int result, String calculation, StringBuilder error) {
        if (result == SweConst.ERR) {
            throw new IllegalArgumentException("Swiss Ephemeris failed for " + calculation + ": " + error);
        }
    }

    private boolean isJulianDay(double value) {
        return Double.isFinite(value) && value > 0.0;
    }

    private Double nonNegative(double value) {
        return Double.isFinite(value) && value >= 0.0 ? value : null;
    }

    private Integer positiveInteger(double value) {
        if (!Double.isFinite(value) || value <= 0.0 || value > Integer.MAX_VALUE) {
            return null;
        }
        return (int) Math.round(value);
    }

    private EclipseCandidateType eclipseType(SyzygyType syzygyType, double nodeOrbDegrees) {
        if (syzygyType == SyzygyType.NEW_MOON && nodeOrbDegrees <= SOLAR_ECLIPSE_NODE_ORB_DEGREES) {
            return EclipseCandidateType.SOLAR_ECLIPSE_CANDIDATE;
        }
        if (syzygyType == SyzygyType.FULL_MOON && nodeOrbDegrees <= LUNAR_ECLIPSE_NODE_ORB_DEGREES) {
            return EclipseCandidateType.LUNAR_ECLIPSE_CANDIDATE;
        }
        return EclipseCandidateType.NONE;
    }

    private NodeDistance nodeDistance(CalculationContext ephemerisContext, double longitude, double julianDay) {
        double northNodeLongitude = ephemerisContext.longitudeFor(Planet.NORTH_NODE, SweConst.SE_MEAN_NODE, julianDay);
        double southNodeLongitude = AstroMath.normalize(northNodeLongitude + 180.0);
        double northOrb = AstroMath.rawAngularSeparation(longitude, northNodeLongitude);
        double southOrb = AstroMath.rawAngularSeparation(longitude, southNodeLongitude);
        if (northOrb <= southOrb) {
            return new NodeDistance(Planet.NORTH_NODE, northNodeLongitude, northOrb);
        }
        return new NodeDistance(Planet.SOUTH_NODE, southNodeLongitude, southOrb);
    }

    private Double scanForRoot(DifferenceFunction differenceFunction, double startJulianDay, double endJulianDay) {
        double leftJulianDay = startJulianDay;
        double leftDifference = differenceFunction.value(leftJulianDay);
        if (Math.abs(leftDifference) <= ROOT_DEGREE_TOLERANCE) {
            return leftJulianDay;
        }

        for (double rightJulianDay = startJulianDay + SCAN_STEP_DAYS;
             rightJulianDay <= endJulianDay + 1.0e-9;
             rightJulianDay += SCAN_STEP_DAYS) {
            double rightDifference = differenceFunction.value(rightJulianDay);
            if (Math.abs(rightDifference) <= ROOT_DEGREE_TOLERANCE) {
                return rightJulianDay;
            }
            if (bracketsRoot(leftDifference, rightDifference)) {
                return bisectRoot(differenceFunction, leftJulianDay, rightJulianDay, leftDifference, rightDifference);
            }
            leftJulianDay = rightJulianDay;
            leftDifference = rightDifference;
        }
        return null;
    }

    private double bisectRoot(DifferenceFunction differenceFunction, double leftJulianDay, double rightJulianDay,
                              double leftDifference, double rightDifference) {
        double left = leftJulianDay;
        double right = rightJulianDay;
        double leftDiff = leftDifference;
        double rightDiff = rightDifference;
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double mid = (left + right) / 2.0;
            double midDiff = differenceFunction.value(mid);
            if (Math.abs(midDiff) <= ROOT_DEGREE_TOLERANCE) {
                return mid;
            }
            if (bracketsRoot(leftDiff, midDiff)) {
                right = mid;
                rightDiff = midDiff;
            } else if (bracketsRoot(midDiff, rightDiff)) {
                left = mid;
                leftDiff = midDiff;
            } else if (Math.abs(leftDiff) < Math.abs(rightDiff)) {
                right = mid;
                rightDiff = midDiff;
            } else {
                left = mid;
                leftDiff = midDiff;
            }
        }
        return (left + right) / 2.0;
    }

    private boolean bracketsRoot(double leftDifference, double rightDifference) {
        return (leftDifference <= 0.0 && rightDifference >= 0.0)
                || (leftDifference >= 0.0 && rightDifference <= 0.0);
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

    private double ageYears(Subject subject, double julianDay) {
        return (julianDay - julianDayFromInstant(subject.getResolvedUtcInstant())) / MEAN_TROPICAL_YEAR_DAYS;
    }

    private OffsetDateTime dateTimeAtAgeYears(OffsetDateTime birthDateTime, double ageYears) {
        long nanos = Math.round(ageYears * NANOS_PER_TROPICAL_YEAR);
        return birthDateTime.plus(Duration.ofNanos(nanos));
    }

    private OffsetDateTime dateTime(double julianDay, ZoneOffset outputOffset) {
        return instantFromJulianDay(julianDay).atOffset(outputOffset);
    }

    private double julianDayFromInstant(Instant instant) {
        return SwissEphAdapter.utcToJulianDayUt(instant);
    }

    private Instant instantFromJulianDay(double julianDay) {
        return SwissEphAdapter.julianDayUtToUtc(julianDay);
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    @FunctionalInterface
    private interface DifferenceFunction {
        double value(double julianDay);
    }

    private record ReturnInstant(int returnNumber, double julianDay, Instant instant, OffsetDateTime dateTime) {}

    private record EclipseContactSpec(int index, EclipseContactPhase phase, ZoneOffset outputOffset) {}

    private record LocalEclipseSearchResult(boolean supported, String reason, List<LocalEclipseVisibilityRow> rows) {
        private LocalEclipseSearchResult {
            reason = reason == null ? "" : reason;
            rows = rows == null ? List.of() : List.copyOf(rows);
        }

        private static LocalEclipseSearchResult supported(String reason, List<LocalEclipseVisibilityRow> rows) {
            return new LocalEclipseSearchResult(true, reason, rows);
        }

        private static LocalEclipseSearchResult unsupported(String reason) {
            return new LocalEclipseSearchResult(false, reason, List.of());
        }
    }

    private record LocalEclipseVisibilityRow(
            int resultFlags,
            double maximumJulianDayUt,
            OffsetDateTime maximumDateTime,
            List<EclipseContactPhase> visibleContactPhases,
            boolean maximumVisibleAtLocation,
            Double magnitude,
            Double obscuration,
            Double penumbralMagnitude
    ) {
        private LocalEclipseVisibilityRow {
            if (!Double.isFinite(maximumJulianDayUt)) {
                throw new IllegalArgumentException("maximumJulianDayUt must be finite");
            }
            if (maximumDateTime == null) {
                throw new IllegalArgumentException("maximumDateTime is required");
            }
            visibleContactPhases = visibleContactPhases == null ? List.of() : List.copyOf(visibleContactPhases);
        }

        private boolean visible() {
            return (resultFlags & SweConst.SE_ECL_VISIBLE) != 0
                    || maximumVisibleAtLocation
                    || !visibleContactPhases.isEmpty();
        }
    }

    private record ResolvedLocalVisibility(EclipseVisibility visibility) {}

    private record NodeDistance(Planet nearestNode, double nearestNodeLongitude, double orbDegrees) {}
}
