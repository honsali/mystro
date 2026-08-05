package app.reading.lifearc.primarydirection;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;
import app.chart.data.AngleType;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.reading.description.common.model.HylegAlcocodenEntry;

public final class PrimaryDirectionCalculator {
    public static final String METHOD_ID = "PTOLEMAIC_NORMALIZED_HYLEG_AND_ANGLE_PRIMARY_DIRECTIONS_V1";
    private static final String PRIMARY_DOCTRINE = "ptolemaic_normalized";
    private static final String DIRECTION_METHOD = "DIRECT_ZODIACAL_PRIMARY_DIRECTIONS_OF_SELECTED_PTOLEMAIC_HYLEG_AND_ANGLES; HYLEG_AND_ASCENDANT_USE_OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE; MIDHEAVEN_USES_RIGHT_ASCENSION";
    private static final String DIRECT_CONVERSE_DIRECTION_METHOD = "DIRECT_AND_CONVERSE_ZODIACAL_PRIMARY_DIRECTIONS_OF_SELECTED_PTOLEMAIC_HYLEG_AND_ANGLES; DIRECT_ARC=TARGET_COORDINATE_MINUS_SIGNIFICATOR_COORDINATE_FORWARDS; CONVERSE_ARC=SIGNIFICATOR_COORDINATE_MINUS_TARGET_COORDINATE_BACKWARDS; BOTH_WRAP_TO_POSITIVE_0_360_ARCS; HYLEG_AND_ASCENDANT_USE_OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE; MIDHEAVEN_USES_RIGHT_ASCENSION";
    private static final String ARC_CONVERSION_METHOD = "ONE_EQUATORIAL_DEGREE_EQUALS_ONE_MEAN_TROPICAL_YEAR_365_2422_DAYS";
    private static final String CONTACT_METHOD = "SIGNIFICATORS_TO_NATAL_TRADITIONAL_PLANET_BODIES_AND_PTOLEMAIC_RAYS; PLANET_BODY_LATITUDE_USED_FOR_BODY_CONTACTS; ECLIPTIC_LATITUDE_ZERO_FOR_RAYS_AND_ANGLES";
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
            new RaySpec(PrimaryDirectionContactType.BODY, AspectType.CONJUNCTION, 0.0, "BODY"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.SEXTILE, 60.0, "ZODIACAL_FORWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.SEXTILE, -60.0, "ZODIACAL_BACKWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.SQUARE, 90.0, "ZODIACAL_FORWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.SQUARE, -90.0, "ZODIACAL_BACKWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.TRINE, 120.0, "ZODIACAL_FORWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.TRINE, -120.0, "ZODIACAL_BACKWARD"),
            new RaySpec(PrimaryDirectionContactType.RAY, AspectType.OPPOSITION, 180.0, "OPPOSITION")
    );

    public PrimaryDirectionTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                int ageStartYears, int ageEndYearsInclusive) {
        return calculateTable(
                subject,
                chart,
                inquiryDate,
                ageStartYears,
                ageEndYearsInclusive,
                METHOD_ID,
                DIRECTION_METHOD,
                List.of(PrimaryDirectionPolarity.DIRECT)
        );
    }

    public PrimaryDirectionTable calculateDirectConverseTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                              int ageStartYears, int ageEndYearsInclusive) {
        return calculateTable(
                subject,
                chart,
                inquiryDate,
                ageStartYears,
                ageEndYearsInclusive,
                PrimaryDirectionExpansionDesign.CONVERSE_ZODIACAL_METHOD_ID,
                DIRECT_CONVERSE_DIRECTION_METHOD,
                List.of(PrimaryDirectionPolarity.DIRECT, PrimaryDirectionPolarity.CONVERSE)
        );
    }

    private PrimaryDirectionTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                 int ageStartYears, int ageEndYearsInclusive,
                                                 String methodId, String directionMethod,
                                                 List<PrimaryDirectionPolarity> directions) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }
        if (directions == null || directions.isEmpty()) {
            throw new IllegalArgumentException("At least one primary-direction polarity is required");
        }

        ActiveWindow activeWindow = activeWindow(subject, inquiryDate);
        double coverageStartAgeYears = ageStartYears;
        double coverageEndAgeYears = ageEndYearsInclusive + 1.0;
        OffsetDateTime coverageStart = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageStartAgeYears);
        OffsetDateTime coverageEnd = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageEndAgeYears);
        List<PrimaryDirectionSignificator> significators = significators(subject, chart);
        List<PrimaryDirectionEvent> events = events(subject, chart, significators, directions, activeWindow, coverageStartAgeYears, coverageEndAgeYears);

        return new PrimaryDirectionTable(
                methodId,
                PRIMARY_DOCTRINE,
                directionMethod,
                ARC_CONVERSION_METHOD,
                CONTACT_METHOD,
                subject.getLatitude(),
                ageStartYears,
                ageEndYearsInclusive,
                coverageStart,
                coverageEnd,
                activeWindow == null ? null : activeWindow.start(),
                activeWindow == null ? null : activeWindow.endExclusive(),
                significators,
                events
        );
    }

    private List<PrimaryDirectionSignificator> significators(Subject subject, NatalChart chart) {
        List<PrimaryDirectionSignificator> significators = new ArrayList<>();
        HylegAlcocodenEntry.HylegPoint hyleg = chart.getPtolemaicHylegAlcocoden() == null
                ? null
                : chart.getPtolemaicHylegAlcocoden().hyleg();
        if (hyleg == null) {
            ChartAngle ascendant = chart.requireAngle(AngleType.ASCENDANT);
            significators.add(significator(subject, chart, "HYLEG", "ASCENDANT", true,
                    PrimaryDirectionCoordinate.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                    ascendant.getLongitude(), ascendant.getSign(), ascendant.getDegreeInSign(), 1, 0.0));
        } else {
            significators.add(significator(subject, chart, "HYLEG", hyleg.point(), true,
                    coordinateForHyleg(hyleg.point()),
                    hyleg.longitude(), hyleg.sign(), hyleg.degreeInSign(), hyleg.house(), latitudeForPoint(chart, hyleg.point())));
        }

        if (significators.stream().noneMatch(significator -> "ASCENDANT".equals(significator.point()))) {
            ChartAngle ascendant = chart.requireAngle(AngleType.ASCENDANT);
            significators.add(significator(subject, chart, "ASCENDANT_ANGLE", "ASCENDANT", false,
                    PrimaryDirectionCoordinate.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE,
                    ascendant.getLongitude(), ascendant.getSign(), ascendant.getDegreeInSign(), 1, 0.0));
        }

        ChartAngle midheaven = chart.requireAngle(AngleType.MIDHEAVEN);
        significators.add(significator(subject, chart, "MIDHEAVEN_ANGLE", "MIDHEAVEN", false,
                PrimaryDirectionCoordinate.RIGHT_ASCENSION,
                midheaven.getLongitude(), midheaven.getSign(), midheaven.getDegreeInSign(), houseForSign(chart, midheaven.getSign()), 0.0));

        return List.copyOf(significators);
    }

    private PrimaryDirectionCoordinate coordinateForHyleg(String point) {
        if ("MIDHEAVEN".equals(point)) {
            return PrimaryDirectionCoordinate.RIGHT_ASCENSION;
        }
        return PrimaryDirectionCoordinate.OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE;
    }

    private PrimaryDirectionSignificator significator(Subject subject, NatalChart chart, String role, String point,
                                                      boolean selectedHyleg, PrimaryDirectionCoordinate coordinate,
                                                      double longitude, ZodiacSign sign, double degreeInSign,
                                                      int house, double eclipticLatitude) {
        EquatorialPosition equatorial = equatorial(longitude, eclipticLatitude, chart.getTrueObliquity());
        double directionCoordinate = directionCoordinate(coordinate, equatorial, subject.getLatitude());
        return new PrimaryDirectionSignificator(
                role,
                point,
                selectedHyleg,
                coordinate,
                AstroMath.normalize(longitude),
                sign,
                degreeInSign,
                house,
                eclipticLatitude,
                equatorial.rightAscension(),
                equatorial.declination(),
                directionCoordinate
        );
    }

    private List<PrimaryDirectionEvent> events(Subject subject, NatalChart chart,
                                               List<PrimaryDirectionSignificator> significators,
                                               List<PrimaryDirectionPolarity> directions,
                                               ActiveWindow activeWindow,
                                               double coverageStartAgeYears,
                                               double coverageEndAgeYears) {
        List<RawEvent> rawEvents = new ArrayList<>();
        List<PromissorSpec> promissors = promissors(chart);
        for (PrimaryDirectionSignificator significator : significators) {
            for (PromissorSpec promissor : promissors) {
                double targetCoordinate = directionCoordinate(significator.coordinate(), promissor.equatorial(), subject.getLatitude());
                for (PrimaryDirectionPolarity direction : directions) {
                    double arcDegrees = arcDegrees(significator.directionCoordinateDegrees(), targetCoordinate, direction);
                    while (arcDegrees < coverageStartAgeYears - EPSILON) {
                        arcDegrees += 360.0;
                    }
                    while (arcDegrees < coverageEndAgeYears - EPSILON) {
                        OffsetDateTime dateTime = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), arcDegrees);
                        rawEvents.add(new RawEvent(
                                significator,
                                promissor,
                                direction,
                                targetCoordinate,
                                arcDegrees,
                                arcDegrees,
                                dateTime,
                                active(activeWindow, dateTime)
                        ));
                        arcDegrees += 360.0;
                    }
                }
            }
        }

        List<RawEvent> sorted = rawEvents.stream()
                .sorted(Comparator
                        .comparingDouble(RawEvent::ageYears)
                        .thenComparing(event -> event.direction().ordinal())
                        .thenComparing(event -> roleOrder(event.significator().role()))
                        .thenComparing(event -> event.promissor().sourcePlanet().ordinal())
                        .thenComparing(event -> event.promissor().aspect().ordinal())
                        .thenComparing(event -> event.promissor().rayDirection()))
                .toList();

        List<PrimaryDirectionEvent> events = new ArrayList<>();
        int sequenceIndex = 1;
        for (RawEvent raw : sorted) {
            PromissorSpec promissor = raw.promissor();
            PrimaryDirectionSignificator significator = raw.significator();
            events.add(new PrimaryDirectionEvent(
                    sequenceIndex,
                    raw.activeForInquiryYear(),
                    raw.direction(),
                    significator.role(),
                    significator.point(),
                    significator.coordinate(),
                    promissor.sourcePlanet(),
                    promissor.contactType(),
                    promissor.aspect(),
                    promissor.rayDirection(),
                    promissor.targetLongitude(),
                    promissor.targetSign(),
                    promissor.targetDegreeInSign(),
                    promissor.targetLatitude(),
                    promissor.equatorial().rightAscension(),
                    promissor.equatorial().declination(),
                    raw.targetDirectionCoordinateDegrees(),
                    raw.arcDegrees(),
                    raw.ageYears(),
                    raw.dateTime(),
                    promissor.sourceNatalLongitude(),
                    promissor.sourceNatalSign(),
                    promissor.sourceNatalDegreeInSign(),
                    promissor.sourceNatalHouse()
            ));
            sequenceIndex++;
        }
        return List.copyOf(events);
    }

    private int roleOrder(String role) {
        return switch (role) {
            case "HYLEG" -> 0;
            case "ASCENDANT_ANGLE" -> 1;
            case "MIDHEAVEN_ANGLE" -> 2;
            default -> 99;
        };
    }

    private List<PromissorSpec> promissors(NatalChart chart) {
        List<PromissorSpec> promissors = new ArrayList<>();
        for (Planet planet : TRADITIONAL_PLANETS) {
            PlanetPosition source = chart.requirePlanet(planet);
            for (RaySpec ray : RAY_SPECS) {
                double targetLongitude = AstroMath.normalize(source.getLongitude() + ray.offsetDegrees());
                double targetLatitude = ray.type() == PrimaryDirectionContactType.BODY ? source.getLatitude() : 0.0;
                EquatorialPosition equatorial = equatorial(targetLongitude, targetLatitude, chart.getTrueObliquity());
                promissors.add(new PromissorSpec(
                        planet,
                        ray.type(),
                        ray.aspect(),
                        ray.rayDirection(),
                        targetLongitude,
                        AstroMath.signOf(targetLongitude),
                        AstroMath.degreeInSign(targetLongitude),
                        targetLatitude,
                        equatorial,
                        source.getLongitude(),
                        source.getSign(),
                        source.getDegreeInSign(),
                        source.getHouse()
                ));
            }
        }
        return List.copyOf(promissors);
    }

    private double arcDegrees(double significatorCoordinate, double targetCoordinate, PrimaryDirectionPolarity direction) {
        double rawArc = switch (direction) {
            case DIRECT -> targetCoordinate - significatorCoordinate;
            case CONVERSE -> significatorCoordinate - targetCoordinate;
        };
        if (Math.abs(rawArc) <= EPSILON) {
            return 0.0;
        }
        double normalized = AstroMath.normalize(rawArc);
        return Math.abs(normalized - 360.0) <= EPSILON ? 0.0 : normalized;
    }

    private EquatorialPosition equatorial(double longitude, double latitude, double obliquity) {
        double lambda = Math.toRadians(AstroMath.normalize(longitude));
        double beta = Math.toRadians(latitude);
        double epsilon = Math.toRadians(obliquity);
        double cosBeta = Math.cos(beta);
        double x = cosBeta * Math.cos(lambda);
        double y = cosBeta * Math.sin(lambda) * Math.cos(epsilon) - Math.sin(beta) * Math.sin(epsilon);
        double z = cosBeta * Math.sin(lambda) * Math.sin(epsilon) + Math.sin(beta) * Math.cos(epsilon);
        double rightAscension = AstroMath.normalize(Math.toDegrees(Math.atan2(y, x)));
        double declination = Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, z))));
        return new EquatorialPosition(rightAscension, declination);
    }

    private double directionCoordinate(PrimaryDirectionCoordinate coordinate, EquatorialPosition equatorial, double birthLatitude) {
        return switch (coordinate) {
            case RIGHT_ASCENSION -> equatorial.rightAscension();
            case OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE -> obliqueAscension(equatorial.rightAscension(), equatorial.declination(), birthLatitude);
        };
    }

    private double obliqueAscension(double rightAscension, double declination, double birthLatitude) {
        double product = Math.tan(Math.toRadians(birthLatitude)) * Math.tan(Math.toRadians(declination));
        if (product > 1.0 && product < 1.0 + 1.0e-12) {
            product = 1.0;
        } else if (product < -1.0 && product > -1.0 - 1.0e-12) {
            product = -1.0;
        }
        if (product < -1.0 || product > 1.0) {
            throw new IllegalArgumentException("Oblique ascension is undefined for declination " + declination + " at latitude " + birthLatitude);
        }
        double ascensionalDifference = Math.toDegrees(Math.asin(product));
        return AstroMath.normalize(rightAscension - ascensionalDifference);
    }

    private double latitudeForPoint(NatalChart chart, String point) {
        Planet planet = planetForPoint(point);
        if (planet == null) {
            return 0.0;
        }
        return chart.requirePlanet(planet).getLatitude();
    }

    private Planet planetForPoint(String point) {
        try {
            Planet planet = Planet.valueOf(point);
            return TRADITIONAL_PLANETS.contains(planet) ? planet : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(candidate -> candidate.getHouse())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    private ActiveWindow activeWindow(Subject subject, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            return null;
        }
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }
        OffsetDateTime activeDateTime = OffsetDateTime.of(
                inquiryDate,
                subject.getUtcBirthDateTime().toLocalTime(),
                subject.getUtcBirthDateTime().getOffset()
        );
        int years = inquiryDate.getYear() - birthDate.getYear();
        OffsetDateTime start = subject.getUtcBirthDateTime().plusYears(years);
        if (start.isAfter(activeDateTime)) {
            start = start.minusYears(1);
        }
        return new ActiveWindow(start, start.plusYears(1));
    }

    private boolean active(ActiveWindow activeWindow, OffsetDateTime dateTime) {
        return activeWindow != null
                && !dateTime.isBefore(activeWindow.start())
                && dateTime.isBefore(activeWindow.endExclusive());
    }

    private OffsetDateTime dateTimeAtAgeYears(OffsetDateTime birthDateTime, double ageYears) {
        long nanos = Math.round(ageYears * NANOS_PER_TROPICAL_YEAR);
        return birthDateTime.plus(Duration.ofNanos(nanos));
    }

    private record EquatorialPosition(double rightAscension, double declination) {}

    private record RaySpec(PrimaryDirectionContactType type, AspectType aspect, double offsetDegrees, String rayDirection) {}

    private record PromissorSpec(Planet sourcePlanet, PrimaryDirectionContactType contactType, AspectType aspect,
                                 String rayDirection, double targetLongitude, ZodiacSign targetSign,
                                 double targetDegreeInSign, double targetLatitude, EquatorialPosition equatorial,
                                 double sourceNatalLongitude, ZodiacSign sourceNatalSign,
                                 double sourceNatalDegreeInSign, int sourceNatalHouse) {}

    private record RawEvent(PrimaryDirectionSignificator significator, PromissorSpec promissor,
                            PrimaryDirectionPolarity direction, double targetDirectionCoordinateDegrees,
                            double arcDegrees, double ageYears, OffsetDateTime dateTime,
                            boolean activeForInquiryYear) {}

    private record ActiveWindow(OffsetDateTime start, OffsetDateTime endExclusive) {}
}
