package app.reading.lifearc.distribution;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import app.chart.AstroMath;
import app.chart.TraditionalTables;
import app.chart.data.AngleType;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.ZodiacSign;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PlanetPosition;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.reading.description.common.model.HylegAlcocodenEntry;

/**
 * Local/research distributions-through-bounds calculator.
 *
 * <p>The public {@link #calculateTable(Subject, NatalChart, LocalDate, int, int)} method keeps the
 * existing Ascendant-only output and method id unchanged. Session 11 adds a separate extended table
 * builder for the first non-Ascendant slice declared by {@link DistributionDirectedPoint}: selected
 * hyleg, Midheaven, Valens Fortune, Valens Spirit, Sun, and Moon.</p>
 *
 * <p>All tables use zodiacal Egyptian bounds. Timing arcs are normalized research choices: the
 * Ascendant, lots, luminaries, and non-MC hyleg use oblique ascension at the native's birth
 * latitude; the Midheaven uses right ascension. The selected hyleg uses the already enriched
 * {@code natalChart.ptolemaicHylegAlcocoden.hyleg} point and resolves its coordinate method from the
 * actual selected point.</p>
 */
public final class DistributionThroughBoundsCalculator {
    public static final String METHOD_ID = "HELLENISTIC_ASCENDANT_DISTRIBUTIONS_THROUGH_EGYPTIAN_BOUNDS_V1";
    public static final String EXTENDED_METHOD_ID = "HELLENISTIC_EXTENDED_DISTRIBUTIONS_THROUGH_EGYPTIAN_BOUNDS_V1";
    private static final String PRIMARY_DOCTRINE = "hellenistic_normalized";
    private static final Terms TERMS = Terms.EGYPTIAN;
    private static final String TIMING_METHOD = "ASCENDANT_DIRECT_OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE; ECLIPTIC_LATITUDE_ZERO; ONE_OBLIQUE_ASCENSION_DEGREE_EQUALS_ONE_MEAN_TROPICAL_YEAR_365_2422_DAYS";
    private static final String CONTACT_METHOD = "DIRECTED_ASCENDANT_TO_NATAL_TRADITIONAL_PLANET_BODIES_AND_PTOLEMAIC_RAYS_BY_OBLIQUE_ASCENSION";
    private static final double MEAN_TROPICAL_YEAR_DAYS = 365.2422;
    private static final double NANOS_PER_TROPICAL_YEAR = MEAN_TROPICAL_YEAR_DAYS * 86_400_000_000_000.0;
    private static final double EPSILON = 1.0e-9;
    private static final List<Planet> TRADITIONAL_PLANETS = List.of(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN
    );
    private static final List<RaySpec> RAY_SPECS = List.of(
            new RaySpec(DistributionContactType.BODY, AspectType.CONJUNCTION, 0.0, "BODY"),
            new RaySpec(DistributionContactType.RAY, AspectType.SEXTILE, 60.0, "ZODIACAL_FORWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.SEXTILE, -60.0, "ZODIACAL_BACKWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.SQUARE, 90.0, "ZODIACAL_FORWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.SQUARE, -90.0, "ZODIACAL_BACKWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.TRINE, 120.0, "ZODIACAL_FORWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.TRINE, -120.0, "ZODIACAL_BACKWARD"),
            new RaySpec(DistributionContactType.RAY, AspectType.OPPOSITION, 180.0, "OPPOSITION")
    );

    public DistributionThroughBoundsTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                          int ageStartYears, int ageEndYearsInclusive) {
        validateAgeRange(ageStartYears, ageEndYearsInclusive);

        ChartAngle ascendant = chart.requireAngle(AngleType.ASCENDANT);
        DirectedPointSpec directedPoint = new DirectedPointSpec(
                DistributionDirectedPoint.ASCENDANT,
                "ASCENDANT",
                "ASCENDANT",
                ascendant.getLongitude(),
                ascendant.getSign(),
                ascendant.getDegreeInSign(),
                1,
                DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                TIMING_METHOD,
                CONTACT_METHOD
        );
        return calculateTable(subject, chart, inquiryDate, ageStartYears, ageEndYearsInclusive, METHOD_ID, directedPoint);
    }

    public List<DistributionThroughBoundsTable> calculateExtendedTables(Subject subject, NatalChart chart,
                                                                         LocalDate inquiryDate, int ageStartYears,
                                                                         int ageEndYearsInclusive) {
        validateAgeRange(ageStartYears, ageEndYearsInclusive);

        List<DistributionThroughBoundsTable> tables = new ArrayList<>();
        for (DirectedPointSpec directedPoint : extendedDirectedPoints(chart)) {
            tables.add(calculateTable(subject, chart, inquiryDate, ageStartYears, ageEndYearsInclusive,
                    EXTENDED_METHOD_ID, directedPoint));
        }
        return List.copyOf(tables);
    }

    private DistributionThroughBoundsTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                          int ageStartYears, int ageEndYearsInclusive, String methodId,
                                                          DirectedPointSpec directedPoint) {
        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        double coverageStartAgeYears = ageStartYears;
        double coverageEndAgeYears = ageEndYearsInclusive + 1.0;
        OffsetDateTime coverageStart = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageStartAgeYears);
        OffsetDateTime coverageEnd = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageEndAgeYears);
        List<DistributionThroughBoundsContact> contacts = contacts(subject, chart, directedPoint.longitude(),
                coverageEndAgeYears, directedPoint.coordinateMethod());

        return new DistributionThroughBoundsTable(
                methodId,
                PRIMARY_DOCTRINE,
                TERMS,
                directedPoint.tableLabel(),
                directedPoint.longitude(),
                directedPoint.sign(),
                directedPoint.degreeInSign(),
                directedPoint.house(),
                directedPoint.timingMethod(),
                directedPoint.contactMethod(),
                subject.getLatitude(),
                ageStartYears,
                ageEndYearsInclusive,
                coverageStart,
                coverageEnd,
                periods(subject, chart, directedPoint.longitude(), coverageStartAgeYears, coverageEndAgeYears,
                        activeDateTime, contacts, directedPoint.coordinateMethod())
        );
    }

    private List<DirectedPointSpec> extendedDirectedPoints(NatalChart chart) {
        List<DirectedPointSpec> specs = new ArrayList<>();
        for (DistributionDirectedPoint directedPoint : DistributionDirectedPoint.firstExtendedSlicePoints()) {
            directedPointSpec(chart, directedPoint).ifPresent(specs::add);
        }
        return List.copyOf(specs);
    }

    private Optional<DirectedPointSpec> directedPointSpec(NatalChart chart, DistributionDirectedPoint directedPoint) {
        return switch (directedPoint) {
            case SELECTED_HYLEG -> selectedHylegSpec(chart);
            case MIDHEAVEN -> Optional.of(angleSpec(chart, directedPoint, AngleType.MIDHEAVEN, "MIDHEAVEN"));
            case LOT_FORTUNE -> lotSpec(chart, directedPoint, "FORTUNE");
            case LOT_SPIRIT -> lotSpec(chart, directedPoint, "SPIRIT");
            case SUN -> Optional.of(planetSpec(chart, directedPoint, Planet.SUN));
            case MOON -> Optional.of(planetSpec(chart, directedPoint, Planet.MOON));
            case ASCENDANT -> Optional.empty();
        };
    }

    private Optional<DirectedPointSpec> selectedHylegSpec(NatalChart chart) {
        HylegAlcocodenEntry entry = chart.getPtolemaicHylegAlcocoden();
        if (entry == null || entry.hyleg() == null) {
            return Optional.empty();
        }
        HylegAlcocodenEntry.HylegPoint hyleg = entry.hyleg();
        DistributionCoordinateMethod coordinateMethod = "MIDHEAVEN".equals(hyleg.point())
                ? DistributionCoordinateMethod.RIGHT_ASCENSION
                : DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE;
        String label = "HYLEG:" + hyleg.point();
        return Optional.of(spec(
                DistributionDirectedPoint.SELECTED_HYLEG,
                label,
                hyleg.point(),
                hyleg.longitude(),
                hyleg.sign(),
                hyleg.degreeInSign(),
                hyleg.house(),
                coordinateMethod,
                "SELECTED_HYLEG_POINT=" + hyleg.point() + "; " + DistributionDirectedPoint.SELECTED_HYLEG.methodNote()
        ));
    }

    private DirectedPointSpec angleSpec(NatalChart chart, DistributionDirectedPoint directedPoint, AngleType angleType,
                                        String sourcePoint) {
        ChartAngle angle = chart.requireAngle(angleType);
        return spec(
                directedPoint,
                directedPoint.outputLabel(),
                sourcePoint,
                angle.getLongitude(),
                angle.getSign(),
                angle.getDegreeInSign(),
                houseForSign(chart, angle.getSign()),
                directedPoint.coordinateMethod(),
                directedPoint.methodNote()
        );
    }

    private Optional<DirectedPointSpec> lotSpec(NatalChart chart, DistributionDirectedPoint directedPoint, String lotName) {
        if (chart.getLots() == null) {
            return Optional.empty();
        }
        return chart.getLots().stream()
                .filter(lot -> lotName.equals(lot.name()))
                .findFirst()
                .map(lot -> spec(
                        directedPoint,
                        directedPoint.outputLabel(),
                        lot.name(),
                        lot.longitude(),
                        lot.sign(),
                        lot.degreeInSign(),
                        lot.house(),
                        directedPoint.coordinateMethod(),
                        directedPoint.methodNote()
                ));
    }

    private DirectedPointSpec planetSpec(NatalChart chart, DistributionDirectedPoint directedPoint, Planet planet) {
        PlanetPosition position = chart.requirePlanet(planet);
        return spec(
                directedPoint,
                directedPoint.outputLabel(),
                planet.name(),
                position.getLongitude(),
                position.getSign(),
                position.getDegreeInSign(),
                position.getHouse(),
                directedPoint.coordinateMethod(),
                directedPoint.methodNote()
        );
    }

    private DirectedPointSpec spec(DistributionDirectedPoint directedPoint, String tableLabel, String sourcePoint,
                                   double longitude, ZodiacSign sign, double degreeInSign, int house,
                                   DistributionCoordinateMethod coordinateMethod, String specificMethodNote) {
        DistributionCoordinateMethod effectiveCoordinate = resolveCoordinateMethod(coordinateMethod, sourcePoint);
        String timingMethod = "DIRECT_" + tableLabel + "_DISTRIBUTION_THROUGH_EGYPTIAN_BOUNDS; TERMS="
                + TERMS + "; COORDINATE=" + effectiveCoordinate
                + "; " + effectiveCoordinate.description()
                + "; ECLIPTIC_LATITUDE_ZERO; ONE_COORDINATE_DEGREE_EQUALS_ONE_MEAN_TROPICAL_YEAR_365_2422_DAYS"
                + "; LONGITUDE_SOURCE=" + directedPoint.longitudeSource()
                + "; METHOD_NOTE=" + specificMethodNote
                + "; ALTERNATE_TERMS_POLICY=" + DistributionDirectedPoint.ALTERNATE_TERMS_POLICY;
        String contactMethod = "DIRECTED_" + tableLabel
                + "_TO_NATAL_TRADITIONAL_PLANET_BODIES_AND_PTOLEMAIC_RAYS_BY_" + effectiveCoordinate;
        return new DirectedPointSpec(
                directedPoint,
                tableLabel,
                sourcePoint,
                AstroMath.normalize(longitude),
                sign,
                degreeInSign,
                house,
                effectiveCoordinate,
                timingMethod,
                contactMethod
        );
    }

    private DistributionCoordinateMethod resolveCoordinateMethod(DistributionCoordinateMethod coordinateMethod, String sourcePoint) {
        if (coordinateMethod != DistributionCoordinateMethod.RESOLVED_FROM_SELECTED_HYLEG_POINT) {
            return coordinateMethod;
        }
        return "MIDHEAVEN".equals(sourcePoint)
                ? DistributionCoordinateMethod.RIGHT_ASCENSION
                : DistributionCoordinateMethod.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE;
    }

    private List<DistributionThroughBoundsPeriod> periods(Subject subject, NatalChart chart, double startLongitude,
                                                          double coverageStartAgeYears, double coverageEndAgeYears,
                                                          OffsetDateTime activeDateTime,
                                                          List<DistributionThroughBoundsContact> contacts,
                                                          DistributionCoordinateMethod coordinateMethod) {
        List<BoundSegment> segments = boundSegments();
        int startSegmentIndex = startSegmentIndex(segments, startLongitude);
        double startLongitudeAbs = AstroMath.normalize(startLongitude);
        List<DistributionThroughBoundsPeriod> periods = new ArrayList<>();

        int segmentIndex = startSegmentIndex;
        int zodiacWrapCount = 0;
        int circuitNumber = 1;
        int sequenceIndex = 1;
        while (true) {
            BoundSegment segment = segments.get(segmentIndex);
            double segmentCycleBase = zodiacWrapCount * 360.0;
            double naturalStartLongitudeAbs = segment.startLongitude() + segmentCycleBase;
            double naturalEndLongitudeAbs = segment.endLongitude() + segmentCycleBase;
            if (sequenceIndex == 1) {
                naturalStartLongitudeAbs = startLongitudeAbs;
            }
            double naturalStartAgeYears = arcYears(startLongitudeAbs, naturalStartLongitudeAbs, subject.getLatitude(),
                    chart.getTrueObliquity(), coordinateMethod);
            double naturalEndAgeYears = arcYears(startLongitudeAbs, naturalEndLongitudeAbs, subject.getLatitude(),
                    chart.getTrueObliquity(), coordinateMethod);
            if (naturalStartAgeYears >= coverageEndAgeYears - EPSILON) {
                break;
            }

            if (naturalEndAgeYears > coverageStartAgeYears + EPSILON) {
                double emittedStartAgeYears = Math.max(naturalStartAgeYears, coverageStartAgeYears);
                double emittedEndAgeYears = Math.min(naturalEndAgeYears, coverageEndAgeYears);
                double emittedStartLongitudeAbs = longitudeAtArcYears(startLongitudeAbs, naturalStartLongitudeAbs,
                        naturalEndLongitudeAbs, naturalStartAgeYears, naturalEndAgeYears, emittedStartAgeYears,
                        subject.getLatitude(), chart.getTrueObliquity(), coordinateMethod);
                double emittedEndLongitudeAbs = longitudeAtArcYears(startLongitudeAbs, naturalStartLongitudeAbs,
                        naturalEndLongitudeAbs, naturalStartAgeYears, naturalEndAgeYears, emittedEndAgeYears,
                        subject.getLatitude(), chart.getTrueObliquity(), coordinateMethod);
                OffsetDateTime startDateTime = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), emittedStartAgeYears);
                OffsetDateTime endDateTime = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), emittedEndAgeYears);

                periods.add(new DistributionThroughBoundsPeriod(
                        sequenceIndex,
                        circuitNumber,
                        segment.sign(),
                        segment.boundIndexInSign(),
                        segment.ruler(),
                        AstroMath.normalize(segment.startLongitude()),
                        AstroMath.normalize(segment.endLongitude()),
                        segment.startDegreeInSign(),
                        segment.endDegreeInSign(),
                        AstroMath.normalize(emittedStartLongitudeAbs),
                        AstroMath.normalize(emittedEndLongitudeAbs),
                        degreeInSegment(segment, emittedStartLongitudeAbs),
                        degreeInSegment(segment, emittedEndLongitudeAbs),
                        emittedStartAgeYears,
                        emittedEndAgeYears,
                        emittedStartAgeYears,
                        emittedEndAgeYears,
                        startDateTime,
                        endDateTime,
                        active(startDateTime, endDateTime, activeDateTime),
                        contactsForPeriod(contacts, emittedStartAgeYears, emittedEndAgeYears)
                ));
            }

            segmentIndex++;
            if (segmentIndex == segments.size()) {
                segmentIndex = 0;
                zodiacWrapCount++;
            }
            if (segmentIndex == startSegmentIndex) {
                circuitNumber++;
            }
            sequenceIndex++;
        }
        return List.copyOf(periods);
    }

    private List<DistributionThroughBoundsContact> contacts(Subject subject, NatalChart chart, double startLongitude,
                                                            double coverageEndAgeYears,
                                                            DistributionCoordinateMethod coordinateMethod) {
        double startLongitudeAbs = AstroMath.normalize(startLongitude);
        List<DistributionThroughBoundsContact> contacts = new ArrayList<>();
        for (Planet sourcePlanet : TRADITIONAL_PLANETS) {
            PlanetPointEntry source = planetPoint(chart, sourcePlanet);
            for (RaySpec raySpec : RAY_SPECS) {
                double rayLongitude = AstroMath.normalize(source.longitude() + raySpec.offsetDegrees());
                double rayLongitudeAbs = rayLongitude;
                while (rayLongitudeAbs < startLongitudeAbs - EPSILON) {
                    rayLongitudeAbs += 360.0;
                }
                while (true) {
                    double ageYears = arcYears(startLongitudeAbs, rayLongitudeAbs, subject.getLatitude(),
                            chart.getTrueObliquity(), coordinateMethod);
                    if (ageYears > coverageEndAgeYears + EPSILON) {
                        break;
                    }
                    contacts.add(new DistributionThroughBoundsContact(
                            sourcePlanet,
                            raySpec.type(),
                            raySpec.aspect(),
                            raySpec.rayDirection(),
                            AstroMath.normalize(rayLongitudeAbs),
                            AstroMath.signOf(rayLongitudeAbs),
                            AstroMath.degreeInSign(rayLongitudeAbs),
                            TraditionalTables.termRuler(rayLongitudeAbs, TERMS),
                            ageYears,
                            ageYears,
                            dateTimeAtAgeYears(subject.getUtcBirthDateTime(), ageYears),
                            source.longitude(),
                            source.sign(),
                            source.degreeInSign(),
                            source.house()
                    ));
                    rayLongitudeAbs += 360.0;
                }
            }
        }
        return contacts.stream()
                .sorted(Comparator
                        .comparingDouble(DistributionThroughBoundsContact::ageYears)
                        .thenComparing(contact -> contact.sourcePlanet().ordinal())
                        .thenComparing(contact -> contact.aspect().ordinal())
                        .thenComparing(DistributionThroughBoundsContact::rayDirection))
                .toList();
    }

    private List<DistributionThroughBoundsContact> contactsForPeriod(List<DistributionThroughBoundsContact> contacts,
                                                                     double startAgeYears, double endAgeYears) {
        return contacts.stream()
                .filter(contact -> contact.ageYears() >= startAgeYears - EPSILON && contact.ageYears() < endAgeYears - EPSILON)
                .toList();
    }

    private double longitudeAtArcYears(double baseLongitudeAbs, double periodStartLongitudeAbs,
                                       double periodEndLongitudeAbs, double periodStartAgeYears,
                                       double periodEndAgeYears, double targetAgeYears, double latitude,
                                       double obliquity, DistributionCoordinateMethod coordinateMethod) {
        if (Math.abs(targetAgeYears - periodStartAgeYears) <= EPSILON) {
            return periodStartLongitudeAbs;
        }
        if (Math.abs(targetAgeYears - periodEndAgeYears) <= EPSILON) {
            return periodEndLongitudeAbs;
        }
        double low = periodStartLongitudeAbs;
        double high = periodEndLongitudeAbs;
        for (int i = 0; i < 80; i++) {
            double mid = (low + high) / 2.0;
            double midAge = arcYears(baseLongitudeAbs, mid, latitude, obliquity, coordinateMethod);
            if (midAge < targetAgeYears) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2.0;
    }

    private double degreeInSegment(BoundSegment segment, double longitudeAbs) {
        double absoluteDegree = longitudeAbs - Math.floor(longitudeAbs / 360.0) * 360.0 - segment.sign().ordinal() * 30.0;
        if (Math.abs(absoluteDegree) <= EPSILON && Math.abs(segment.endDegreeInSign() - 30.0) <= EPSILON) {
            return 30.0;
        }
        if (absoluteDegree < -EPSILON) {
            absoluteDegree += 360.0;
        }
        return Math.max(0.0, Math.min(30.0, absoluteDegree));
    }

    private double arcYears(double startLongitudeAbs, double targetLongitudeAbs, double latitude, double obliquity,
                            DistributionCoordinateMethod coordinateMethod) {
        return switch (coordinateMethod) {
            case OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE -> obliqueAscensionAbsolute(targetLongitudeAbs, latitude, obliquity)
                    - obliqueAscensionAbsolute(startLongitudeAbs, latitude, obliquity);
            case RIGHT_ASCENSION -> rightAscensionAbsolute(targetLongitudeAbs, obliquity)
                    - rightAscensionAbsolute(startLongitudeAbs, obliquity);
            case RESOLVED_FROM_SELECTED_HYLEG_POINT -> throw new IllegalArgumentException("Distribution coordinate method must be resolved before timing arcs are calculated");
        };
    }

    private double obliqueAscensionAbsolute(double longitudeAbs, double latitude, double obliquity) {
        double cycle = Math.floor(longitudeAbs / 360.0);
        double normalizedLongitude = longitudeAbs - cycle * 360.0;
        return obliqueAscension(normalizedLongitude, latitude, obliquity) + cycle * 360.0;
    }

    private double obliqueAscension(double longitude, double latitude, double obliquity) {
        double lambda = Math.toRadians(AstroMath.normalize(longitude));
        double epsilon = Math.toRadians(obliquity);
        double rightAscension = Math.toDegrees(Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda)));
        rightAscension = AstroMath.normalize(rightAscension);
        double declination = Math.asin(Math.sin(lambda) * Math.sin(epsilon));
        double product = Math.tan(Math.toRadians(latitude)) * Math.tan(declination);
        if (product > 1.0 && product < 1.0 + 1.0e-12) {
            product = 1.0;
        } else if (product < -1.0 && product > -1.0 - 1.0e-12) {
            product = -1.0;
        }
        if (product < -1.0 || product > 1.0) {
            throw new IllegalArgumentException("Oblique ascension is undefined for longitude " + longitude + " at latitude " + latitude);
        }
        double ascensionalDifference = Math.toDegrees(Math.asin(product));
        return AstroMath.normalize(rightAscension - ascensionalDifference);
    }

    private double rightAscensionAbsolute(double longitudeAbs, double obliquity) {
        double cycle = Math.floor(longitudeAbs / 360.0);
        double normalizedLongitude = longitudeAbs - cycle * 360.0;
        return rightAscension(normalizedLongitude, obliquity) + cycle * 360.0;
    }

    private double rightAscension(double longitude, double obliquity) {
        double lambda = Math.toRadians(AstroMath.normalize(longitude));
        double epsilon = Math.toRadians(obliquity);
        return AstroMath.normalize(Math.toDegrees(Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda))));
    }

    private List<BoundSegment> boundSegments() {
        List<BoundSegment> segments = new ArrayList<>();
        for (ZodiacSign sign : ZodiacSign.values()) {
            double signStart = sign.ordinal() * 30.0;
            List<TraditionalTables.TermRange> ranges = TraditionalTables.termRanges(sign, TERMS);
            for (int i = 0; i < ranges.size(); i++) {
                TraditionalTables.TermRange range = ranges.get(i);
                segments.add(new BoundSegment(
                        sign,
                        i + 1,
                        range.ruler(),
                        signStart + range.startDegreeInclusive(),
                        signStart + range.endDegreeExclusive(),
                        range.startDegreeInclusive(),
                        range.endDegreeExclusive()
                ));
            }
        }
        return List.copyOf(segments);
    }

    private int startSegmentIndex(List<BoundSegment> segments, double longitude) {
        double normalized = AstroMath.normalize(longitude);
        for (int i = 0; i < segments.size(); i++) {
            BoundSegment segment = segments.get(i);
            if (normalized >= segment.startLongitude() - EPSILON && normalized < segment.endLongitude() - EPSILON) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not identify Egyptian bound for longitude " + longitude);
    }

    private PlanetPointEntry planetPoint(NatalChart chart, Planet planet) {
        PointEntry point = chart.getPoints().get(PointKey.of(planet));
        if (!(point instanceof PlanetPointEntry planetPoint)) {
            throw new IllegalArgumentException("Missing natal planet point " + planet);
        }
        return planetPoint;
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(candidate -> candidate.getHouse())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    private void validateAgeRange(int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }
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

    private OffsetDateTime dateTimeAtAgeYears(OffsetDateTime birthDateTime, double ageYears) {
        long nanos = Math.round(ageYears * NANOS_PER_TROPICAL_YEAR);
        return birthDateTime.plus(Duration.ofNanos(nanos));
    }

    private record DirectedPointSpec(DistributionDirectedPoint directedPoint, String tableLabel, String sourcePoint,
                                     double longitude, ZodiacSign sign, double degreeInSign, int house,
                                     DistributionCoordinateMethod coordinateMethod, String timingMethod,
                                     String contactMethod) {}

    private record BoundSegment(ZodiacSign sign, int boundIndexInSign, Planet ruler, double startLongitude,
                                double endLongitude, double startDegreeInSign, double endDegreeInSign) {}

    private record RaySpec(DistributionContactType type, AspectType aspect, double offsetDegrees, String rayDirection) {}
}
