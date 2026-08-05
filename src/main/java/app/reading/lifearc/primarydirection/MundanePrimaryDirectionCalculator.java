package app.reading.lifearc.primarydirection;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.chart.AstroMath;
import app.chart.data.AngleType;
import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.model.ChartAngle;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPosition;
import app.chart.model.Subject;
import app.reading.description.common.model.HylegAlcocodenEntry;

/**
 * Local/research mundane/semi-arc primary-direction prototype.
 *
 * <p>This intentionally does not replace {@link PrimaryDirectionCalculator}. It is a small,
 * separately labelled prototype for Session 15: selected Ptolemaic hyleg, Ascendant, and Midheaven
 * significators are compared to natal traditional planet body promissors only. Rays, converse
 * motion, anaereta selection, and deterministic lifespan timing are explicitly deferred.</p>
 *
 * <p>Prototype geometry: each point is placed on a normalized 0-360 mundane semi-arc circle at the
 * birth latitude: Ascendant = 0, MC = 90, Descendant = 180, IC = 270. Planet/longevity points use
 * right ascension, declination, the natal ARMC, and diurnal/nocturnal semi-arcs. A direct event is
 * the forward ARMC rotation needed for a promissor body to occupy the significator's natal mundane
 * semi-arc position. One equatorial degree maps to one mean tropical year.</p>
 */
public final class MundanePrimaryDirectionCalculator {
    public static final String METHOD_ID = PrimaryDirectionExpansionDesign.MUNDANE_SEMI_ARC_PROTOTYPE_METHOD_ID;
    private static final String PRIMARY_DOCTRINE = "ptolemaic_normalized_prototype";
    private static final String DIRECTION_METHOD = "DIRECT_MUNDANE_SEMI_ARC_BODY_CONTACT_PROTOTYPE; SIGNIFICATORS=SELECTED_PTOLEMAIC_HYLEG_ASCENDANT_MIDHEAVEN; PROMISSORS=SEVEN_TRADITIONAL_PLANET_BODIES_ONLY; ROTATE_ARMC_FORWARD_UNTIL_PROMISSOR_BODY_MATCHES_SIGNIFICATOR_NATAL_MUNDANE_POSITION; ASCENDANT_POSITION=0_MC_POSITION=90_DESCENDANT_POSITION=180_IC_POSITION=270; PLANET_POSITIONS_USE_RA_DECLINATION_AND_DIURNAL_NOCTURNAL_SEMI_ARCS_AT_BIRTH_LATITUDE";
    private static final String ARC_CONVERSION_METHOD = "ONE_EQUATORIAL_DEGREE_EQUALS_ONE_MEAN_TROPICAL_YEAR_365_2422_DAYS";
    private static final String CONTACT_METHOD = "MUNDANE_SEMI_ARC_BODY_CONTACTS_ONLY; NO_RAYS; NO_CONVERSE; NO_ANAERETA_SELECTION";
    private static final String PROTOTYPE_CAVEAT = "LOCAL_RESEARCH_PROTOTYPE_NOT_FINAL_HISTORICAL_AUTHORITY; USE_AS_EVIDENCE_ONLY; DOES_NOT_REPLACE_NORMALIZED_ZODIACAL_PRIMARY_DIRECTIONS; NO_DEATH_TIMING_OR_DETERMINISTIC_LIFESPAN_CLAIMS";
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

    public MundanePrimaryDirectionTable calculateTable(Subject subject, NatalChart chart, LocalDate inquiryDate,
                                                       int ageStartYears, int ageEndYearsInclusive) {
        validateAgeRange(ageStartYears, ageEndYearsInclusive);

        ActiveWindow activeWindow = activeWindow(subject, inquiryDate);
        double coverageStartAgeYears = ageStartYears;
        double coverageEndAgeYears = ageEndYearsInclusive + 1.0;
        OffsetDateTime coverageStart = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageStartAgeYears);
        OffsetDateTime coverageEnd = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), coverageEndAgeYears);
        List<MundanePrimaryDirectionSignificator> significators = significators(subject, chart);
        List<MundanePrimaryDirectionEvent> events = events(subject, chart, significators, activeWindow,
                coverageStartAgeYears, coverageEndAgeYears);

        return new MundanePrimaryDirectionTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                DIRECTION_METHOD,
                ARC_CONVERSION_METHOD,
                CONTACT_METHOD,
                PROTOTYPE_CAVEAT,
                subject.getLatitude(),
                chart.getArmc(),
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

    private List<MundanePrimaryDirectionSignificator> significators(Subject subject, NatalChart chart) {
        List<SignificatorSpec> specs = new ArrayList<>();
        HylegAlcocodenEntry.HylegPoint hyleg = chart.getPtolemaicHylegAlcocoden() == null
                ? null
                : chart.getPtolemaicHylegAlcocoden().hyleg();
        if (hyleg == null) {
            specs.add(angleSpec(chart, "HYLEG", "ASCENDANT", true, AngleType.ASCENDANT, 0.0, 1));
        } else {
            specs.add(hylegSpec(chart, hyleg));
        }

        if (specs.stream().noneMatch(spec -> "ASCENDANT".equals(spec.point()))) {
            specs.add(angleSpec(chart, "ASCENDANT_ANGLE", "ASCENDANT", false, AngleType.ASCENDANT, 0.0, 1));
        }
        specs.add(angleSpec(chart, "MIDHEAVEN_ANGLE", "MIDHEAVEN", false, AngleType.MIDHEAVEN, 90.0,
                houseForSign(chart, chart.requireAngle(AngleType.MIDHEAVEN).getSign())));

        return specs.stream()
                .map(spec -> significator(subject, chart, spec))
                .toList();
    }

    private SignificatorSpec hylegSpec(NatalChart chart, HylegAlcocodenEntry.HylegPoint hyleg) {
        if ("ASCENDANT".equals(hyleg.point())) {
            return angleSpec(chart, "HYLEG", "ASCENDANT", true, AngleType.ASCENDANT, 0.0, 1);
        }
        if ("MIDHEAVEN".equals(hyleg.point())) {
            return angleSpec(chart, "HYLEG", "MIDHEAVEN", true, AngleType.MIDHEAVEN, 90.0,
                    houseForSign(chart, chart.requireAngle(AngleType.MIDHEAVEN).getSign()));
        }
        Planet planet = planetForPoint(hyleg.point());
        if (planet != null) {
            PlanetPosition position = chart.requirePlanet(planet);
            return new SignificatorSpec(
                    "HYLEG",
                    hyleg.point(),
                    true,
                    position.getLongitude(),
                    position.getSign(),
                    position.getDegreeInSign(),
                    position.getHouse(),
                    position.getLatitude(),
                    null
            );
        }
        return new SignificatorSpec(
                "HYLEG",
                hyleg.point(),
                true,
                hyleg.longitude(),
                hyleg.sign(),
                hyleg.degreeInSign(),
                hyleg.house(),
                0.0,
                null
        );
    }

    private SignificatorSpec angleSpec(NatalChart chart, String role, String point, boolean selectedHyleg,
                                       AngleType angleType, double fixedMundanePositionDegrees, int house) {
        ChartAngle angle = chart.requireAngle(angleType);
        return new SignificatorSpec(
                role,
                point,
                selectedHyleg,
                angle.getLongitude(),
                angle.getSign(),
                angle.getDegreeInSign(),
                house,
                0.0,
                fixedMundanePositionDegrees
        );
    }

    private MundanePrimaryDirectionSignificator significator(Subject subject, NatalChart chart, SignificatorSpec spec) {
        EquatorialPosition equatorial = equatorial(spec.longitude(), spec.eclipticLatitude(), chart.getTrueObliquity());
        SemiArcGeometry geometry = semiArcGeometry(equatorial.declination(), subject.getLatitude());
        MundanePosition position = spec.fixedMundanePositionDegrees() == null
                ? mundanePosition(equatorial.rightAscension(), equatorial.declination(), chart.getArmc(), subject.getLatitude())
                : fixedMundanePosition(spec.fixedMundanePositionDegrees(), geometry);
        return new MundanePrimaryDirectionSignificator(
                spec.role(),
                spec.point(),
                spec.selectedHyleg(),
                AstroMath.normalize(spec.longitude()),
                spec.sign(),
                spec.degreeInSign(),
                spec.house(),
                spec.eclipticLatitude(),
                equatorial.rightAscension(),
                equatorial.declination(),
                geometry.diurnalSemiArcDegrees(),
                geometry.nocturnalSemiArcDegrees(),
                position.hourAngleDegrees(),
                position.mundanePositionDegrees(),
                position.segment()
        );
    }

    private List<MundanePrimaryDirectionEvent> events(Subject subject, NatalChart chart,
                                                      List<MundanePrimaryDirectionSignificator> significators,
                                                      ActiveWindow activeWindow,
                                                      double coverageStartAgeYears,
                                                      double coverageEndAgeYears) {
        List<RawEvent> rawEvents = new ArrayList<>();
        for (MundanePrimaryDirectionSignificator significator : significators) {
            for (Planet planet : TRADITIONAL_PLANETS) {
                PlanetPosition promissor = chart.requirePlanet(planet);
                SemiArcGeometry geometry = semiArcGeometry(promissor.getDeclination(), subject.getLatitude());
                double directedHourAngle = hourAngleForMundanePosition(significator.mundanePositionDegrees(), geometry);
                double directedArmc = AstroMath.normalize(promissor.getRightAscension() + directedHourAngle);
                double arcDegrees = arcDegrees(chart.getArmc(), directedArmc);
                while (arcDegrees < coverageStartAgeYears - EPSILON) {
                    arcDegrees += 360.0;
                }
                while (arcDegrees < coverageEndAgeYears - EPSILON) {
                    OffsetDateTime dateTime = dateTimeAtAgeYears(subject.getUtcBirthDateTime(), arcDegrees);
                    rawEvents.add(new RawEvent(
                            significator,
                            promissor,
                            geometry,
                            directedHourAngle,
                            directedArmc,
                            arcDegrees,
                            dateTime,
                            active(activeWindow, dateTime)
                    ));
                    arcDegrees += 360.0;
                }
            }
        }

        List<RawEvent> sorted = rawEvents.stream()
                .sorted(Comparator
                        .comparingDouble(RawEvent::arcDegrees)
                        .thenComparing(event -> roleOrder(event.significator().role()))
                        .thenComparing(event -> event.promissor().getPlanet().ordinal()))
                .toList();

        List<MundanePrimaryDirectionEvent> events = new ArrayList<>();
        int sequenceIndex = 1;
        for (RawEvent raw : sorted) {
            MundanePrimaryDirectionSignificator significator = raw.significator();
            PlanetPosition promissor = raw.promissor();
            events.add(new MundanePrimaryDirectionEvent(
                    sequenceIndex,
                    raw.activeForInquiryYear(),
                    significator.role(),
                    significator.point(),
                    promissor.getPlanet(),
                    PrimaryDirectionContactType.BODY,
                    significator.mundanePositionDegrees(),
                    significator.mundanePositionSegment(),
                    promissor.getRightAscension(),
                    promissor.getDeclination(),
                    raw.promissorSemiArc().diurnalSemiArcDegrees(),
                    raw.promissorSemiArc().nocturnalSemiArcDegrees(),
                    raw.directedHourAngleDegrees(),
                    raw.directedArmcDegrees(),
                    raw.arcDegrees(),
                    raw.arcDegrees(),
                    raw.dateTime(),
                    promissor.getLongitude(),
                    promissor.getSign(),
                    promissor.getDegreeInSign(),
                    promissor.getHouse()
            ));
            sequenceIndex++;
        }
        return List.copyOf(events);
    }

    private MundanePosition mundanePosition(double rightAscension, double declination, double armc, double birthLatitude) {
        SemiArcGeometry geometry = semiArcGeometry(declination, birthLatitude);
        double hourAngle = signedAngle(armc - rightAscension);
        double position;
        if (hourAngle >= -geometry.diurnalSemiArcDegrees() - EPSILON
                && hourAngle <= geometry.diurnalSemiArcDegrees() + EPSILON) {
            position = ((hourAngle + geometry.diurnalSemiArcDegrees()) / (2.0 * geometry.diurnalSemiArcDegrees())) * 180.0;
        } else if (hourAngle > geometry.diurnalSemiArcDegrees()) {
            position = 180.0 + ((hourAngle - geometry.diurnalSemiArcDegrees()) / geometry.nocturnalSemiArcDegrees()) * 90.0;
        } else {
            position = 270.0 + ((hourAngle + 180.0) / geometry.nocturnalSemiArcDegrees()) * 90.0;
        }
        double normalizedPosition = normalizePosition(position);
        return new MundanePosition(hourAngle, normalizedPosition, segmentForPosition(normalizedPosition));
    }

    private MundanePosition fixedMundanePosition(double mundanePositionDegrees, SemiArcGeometry geometry) {
        double normalized = normalizePosition(mundanePositionDegrees);
        double hourAngle = hourAngleForMundanePosition(normalized, geometry);
        return new MundanePosition(hourAngle, normalized, segmentForPosition(normalized));
    }

    private double hourAngleForMundanePosition(double mundanePositionDegrees, SemiArcGeometry geometry) {
        double position = normalizePosition(mundanePositionDegrees);
        double hourAngle;
        if (position < 180.0 - EPSILON) {
            hourAngle = (position / 180.0) * (2.0 * geometry.diurnalSemiArcDegrees()) - geometry.diurnalSemiArcDegrees();
        } else if (Math.abs(position - 180.0) <= EPSILON) {
            hourAngle = geometry.diurnalSemiArcDegrees();
        } else if (position < 270.0 - EPSILON) {
            hourAngle = geometry.diurnalSemiArcDegrees()
                    + ((position - 180.0) / 90.0) * geometry.nocturnalSemiArcDegrees();
        } else if (Math.abs(position - 270.0) <= EPSILON) {
            hourAngle = 180.0;
        } else {
            hourAngle = -180.0 + ((position - 270.0) / 90.0) * geometry.nocturnalSemiArcDegrees();
        }
        if (!Double.isFinite(hourAngle)) {
            throw new IllegalArgumentException("Mundane semi-arc hour angle is not finite for position " + mundanePositionDegrees);
        }
        return hourAngle;
    }

    private SemiArcGeometry semiArcGeometry(double declination, double birthLatitude) {
        double product = Math.tan(Math.toRadians(birthLatitude)) * Math.tan(Math.toRadians(declination));
        if (product > 1.0 && product < 1.0 + 1.0e-12) {
            product = 1.0;
        } else if (product < -1.0 && product > -1.0 - 1.0e-12) {
            product = -1.0;
        }
        if (product < -1.0 || product > 1.0) {
            throw new IllegalArgumentException("Mundane semi-arc is undefined for declination " + declination + " at latitude " + birthLatitude);
        }
        double ascensionalDifference = Math.toDegrees(Math.asin(product));
        double diurnalSemiArc = 90.0 + ascensionalDifference;
        double nocturnalSemiArc = 90.0 - ascensionalDifference;
        if (!Double.isFinite(diurnalSemiArc) || !Double.isFinite(nocturnalSemiArc)
                || diurnalSemiArc <= EPSILON || nocturnalSemiArc <= EPSILON) {
            throw new IllegalArgumentException("Mundane semi-arc is not finite for declination " + declination + " at latitude " + birthLatitude);
        }
        return new SemiArcGeometry(diurnalSemiArc, nocturnalSemiArc);
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

    private double arcDegrees(double natalArmc, double directedArmc) {
        double arc = AstroMath.normalize(directedArmc - natalArmc);
        return Math.abs(arc - 360.0) <= EPSILON ? 0.0 : arc;
    }

    private double signedAngle(double degrees) {
        double normalized = AstroMath.normalize(degrees);
        return normalized > 180.0 ? normalized - 360.0 : normalized;
    }

    private double normalizePosition(double degrees) {
        double normalized = AstroMath.normalize(degrees);
        return Math.abs(normalized - 360.0) <= EPSILON ? 0.0 : normalized;
    }

    private String segmentForPosition(double position) {
        double normalized = normalizePosition(position);
        if (Math.abs(normalized) <= EPSILON) {
            return "ASC";
        }
        if (normalized < 90.0 - EPSILON) {
            return "ASC_TO_MC";
        }
        if (Math.abs(normalized - 90.0) <= EPSILON) {
            return "MC";
        }
        if (normalized < 180.0 - EPSILON) {
            return "MC_TO_DESC";
        }
        if (Math.abs(normalized - 180.0) <= EPSILON) {
            return "DESC";
        }
        if (normalized < 270.0 - EPSILON) {
            return "DESC_TO_IC";
        }
        if (Math.abs(normalized - 270.0) <= EPSILON) {
            return "IC";
        }
        return "IC_TO_ASC";
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

    private int roleOrder(String role) {
        return switch (role) {
            case "HYLEG" -> 0;
            case "ASCENDANT_ANGLE" -> 1;
            case "MIDHEAVEN_ANGLE" -> 2;
            default -> 99;
        };
    }

    private void validateAgeRange(int ageStartYears, int ageEndYearsInclusive) {
        if (ageStartYears < 0) {
            throw new IllegalArgumentException("ageStartYears must be zero or greater");
        }
        if (ageEndYearsInclusive < ageStartYears) {
            throw new IllegalArgumentException("ageEndYearsInclusive must be greater than or equal to ageStartYears");
        }
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

    private record SignificatorSpec(String role, String point, boolean selectedHyleg,
                                    double longitude, ZodiacSign sign, double degreeInSign, int house,
                                    double eclipticLatitude, Double fixedMundanePositionDegrees) {}

    private record EquatorialPosition(double rightAscension, double declination) {}

    private record SemiArcGeometry(double diurnalSemiArcDegrees, double nocturnalSemiArcDegrees) {}

    private record MundanePosition(double hourAngleDegrees, double mundanePositionDegrees, String segment) {}

    private record RawEvent(MundanePrimaryDirectionSignificator significator, PlanetPosition promissor,
                            SemiArcGeometry promissorSemiArc, double directedHourAngleDegrees,
                            double directedArmcDegrees, double arcDegrees, OffsetDateTime dateTime,
                            boolean activeForInquiryYear) {}

    private record ActiveWindow(OffsetDateTime start, OffsetDateTime endExclusive) {}
}
