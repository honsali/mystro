package app.reading.lifearc.synthesis;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.description.common.model.LotEntry;
import app.reading.lifearc.decennial.DecennialCalculator;
import app.reading.lifearc.decennial.DecennialPeriod;
import app.reading.lifearc.decennial.DecennialSubperiod;
import app.reading.lifearc.decennial.DecennialTable;
import app.reading.lifearc.distribution.DistributionThroughBoundsCalculator;
import app.reading.lifearc.distribution.DistributionThroughBoundsContact;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.dorothean.calculator.DorotheanAnnualProfectionCalculator;
import app.reading.lifearc.dorothean.calculator.DorotheanMonthlyProfectionCalculator;
import app.reading.lifearc.firdaria.FirdariaCalculator;
import app.reading.lifearc.firdaria.FirdariaPeriod;
import app.reading.lifearc.firdaria.FirdariaSubperiod;
import app.reading.lifearc.firdaria.FirdariaTable;
import app.reading.lifearc.lunar.EclipseCandidateType;
import app.reading.lifearc.lunar.EclipseEvent;
import app.reading.lifearc.lunar.EclipseEventKind;
import app.reading.lifearc.lunar.LunarReturnEntry;
import app.reading.lifearc.lunar.LunarTimingCalculator;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.lunar.LunationEntry;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;
import app.reading.lifearc.primarydirection.PrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.PrimaryDirectionPolarity;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;
import app.reading.lifearc.solarreturn.SolarReturnCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonRow;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnNatalContact;
import app.reading.lifearc.solarreturn.SolarReturnTable;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingCalculator;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingPeriod;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

public final class LifeArcSynthesisCalculator {
    public static final String METHOD_ID = "LIFE_ARC_EVIDENCE_SYNTHESIS_V1";
    private static final String PRIMARY_DOCTRINE = "traditional_normalized";
    private static final String SYNTHESIS_METHOD = "GROUP_ACTIVE_LOCAL_LIFE_ARC_EVIDENCE_BY_SIGN_HOUSE_PLANET_POINT_LOT_AND_ASPECT; WEIGHTS_ARE_CLASS_CALIBRATED_RESEARCH_PRIORITIES_BY_CHRONOCRATOR_RETURN_CHART_DIRECTION_CONTACT_AND_LUNAR_ECLIPSE_WITH_LOCAL_VISIBILITY; NORMALIZED_CONVERSE_AND_MUNDANE_SEMI_ARC_PRIMARY_DIRECTION_VARIANTS_ARE_SEPARATELY_LABELLED_AND_LOWER_WEIGHT_UNTIL_VALIDATED; WEIGHTS_PRIORITIZE_EVIDENCE_DENSITY_NOT_JUDGMENT";
    /**
     * Weight value plus its calibration class. The value uses the shared 1-6 synthesis scale:
     * 1 = small nested/supporting testimony, 2 = ordinary supporting testimony,
     * 3 = active background timing testimony, 4 = strong active ruler/period testimony,
     * 5 = primary timing contact/master testimony, 6 = strongest annual activation.
     */
    private record EvidenceWeight(LifeArcEvidenceWeightClass weightClass, int value) {
        private EvidenceWeight {
            if (weightClass == null) {
                throw new IllegalArgumentException("weightClass is required");
            }
            if (value <= 0) {
                throw new IllegalArgumentException("weight value must be positive");
            }
        }

        private static EvidenceWeight of(LifeArcEvidenceWeightClass weightClass, int value) {
            return new EvidenceWeight(weightClass, value);
        }
    }

    /** Chronocrator weights for active time-lords and their signs, houses, lots, and rulers. */
    private static final class ChronocratorWeights {
        private static final EvidenceWeight ANNUAL_PROFECTED_SIGN_OR_HOUSE = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 5);
        private static final EvidenceWeight LORD_OF_YEAR = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 6);
        private static final EvidenceWeight MONTHLY_PROFECTED_SIGN_OR_HOUSE = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 4);
        private static final EvidenceWeight LORD_OF_MONTH = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 5);
        private static final EvidenceWeight MAIN_PERIOD_RULER = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 4);
        private static final EvidenceWeight PARTNER_PERIOD_RULER = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 2);
        private static final EvidenceWeight ZODIACAL_RELEASING_LOT_ANCHOR = EvidenceWeight.of(LifeArcEvidenceWeightClass.CHRONOCRATOR, 2);
        private static final int ZODIACAL_RELEASING_LEVEL_BASE = 5;
        private static final int ZODIACAL_RELEASING_MINIMUM = 1;

        private static EvidenceWeight zodiacalReleasingLevel(int level) {
            return EvidenceWeight.of(
                    LifeArcEvidenceWeightClass.CHRONOCRATOR,
                    Math.max(ZODIACAL_RELEASING_MINIMUM, ZODIACAL_RELEASING_LEVEL_BASE - level)
            );
        }
    }

    /** Return-chart weights for active solar-return overlays and tight return-to-natal contacts. */
    private static final class ReturnChartWeights {
        private static final EvidenceWeight SOLAR_RETURN_PROFECTION_OVERLAY = EvidenceWeight.of(LifeArcEvidenceWeightClass.RETURN_CHART, 4);
        private static final EvidenceWeight SOLAR_RETURN_LORD_OF_YEAR = EvidenceWeight.of(LifeArcEvidenceWeightClass.RETURN_CHART, 4);
        private static final EvidenceWeight SOLAR_RETURN_POINT_OVERLAY = EvidenceWeight.of(LifeArcEvidenceWeightClass.RETURN_CHART, 2);
        private static final EvidenceWeight TIGHT_SOLAR_RETURN_NATAL_CONJUNCTION = EvidenceWeight.of(LifeArcEvidenceWeightClass.RETURN_CHART, 2);
    }

    /** Direction/contact weights for distributions through bounds and normalized primary-direction contacts. */
    private static final class DirectionContactWeights {
        private static final EvidenceWeight ACTIVE_BOUND_SIGN_OR_HOUSE = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 3);
        private static final EvidenceWeight BOUND_LORD = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 4);
        private static final EvidenceWeight DIRECTED_CONTACT_PLANET = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 4);
        private static final EvidenceWeight DIRECTED_CONTACT_ASPECT = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 2);
        private static final EvidenceWeight PRIMARY_DIRECTION_PROMISSOR = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 5);
        private static final EvidenceWeight PRIMARY_DIRECTION_TARGET_SIGN_OR_HOUSE = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 2);
        private static final EvidenceWeight PRIMARY_DIRECTION_ASPECT = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 2);
        private static final EvidenceWeight PRIMARY_DIRECTION_VARIANT_PROMISSOR = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 3);
        private static final EvidenceWeight PRIMARY_DIRECTION_VARIANT_TARGET_SIGN_OR_HOUSE = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 1);
        private static final EvidenceWeight PRIMARY_DIRECTION_VARIANT_ASPECT = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 1);
        private static final EvidenceWeight MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_PROMISSOR = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 2);
        private static final EvidenceWeight MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_CONTEXT = EvidenceWeight.of(LifeArcEvidenceWeightClass.DIRECTION_CONTACT, 1);
    }

    /** Lunar/eclipse weights for active lunar returns, lunations, mean-node candidates, and true eclipse rows. */
    private static final class LunarEclipseWeights {
        private static final EvidenceWeight ACTIVE_LUNAR_RETURN = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 2);
        private static final EvidenceWeight ACTIVE_LUNATION = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 2);
        private static final EvidenceWeight ECLIPSE_CANDIDATE = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 3);
        private static final EvidenceWeight TRUE_ECLIPSE_VISIBLE = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 5);
        private static final EvidenceWeight TRUE_ECLIPSE_UNKNOWN_VISIBILITY = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 3);
        private static final EvidenceWeight TRUE_ECLIPSE_NOT_VISIBLE = EvidenceWeight.of(LifeArcEvidenceWeightClass.LUNAR_ECLIPSE, 2);
    }

    public LifeArcSynthesisTable calculate(Subject subject, NatalChart chart, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            throw new IllegalArgumentException("inquiryDate is required for life-arc synthesis");
        }
        LocalDate birthDate = subject.getLocalBirthDateTime().toLocalDate();
        if (inquiryDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("inquiryDate must be on or after birthDate");
        }

        OffsetDateTime inquiryDateTime = OffsetDateTime.of(
                inquiryDate,
                subject.getLocalBirthDateTime().toLocalTime(),
                subject.getLocalBirthDateTime().getOffset()
        );
        int completedAgeYears = completedAgeYears(subject, inquiryDate);
        OffsetDateTime activeYearStart = subject.getLocalBirthDateTime().plusYears(completedAgeYears);
        OffsetDateTime activeYearEnd = activeYearStart.plusYears(1);

        EvidenceBuilder evidence = new EvidenceBuilder();
        addProfectionEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addFirdariaEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addDecennialEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addZodiacalReleasingEvidence(evidence, subject, chart, inquiryDateTime);
        addDistributionEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addPrimaryDirectionEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addSolarReturnEvidence(evidence, subject, chart, inquiryDate, completedAgeYears);
        addLunarTimingEvidence(evidence, subject, chart, inquiryDate, completedAgeYears, activeYearStart, activeYearEnd);

        List<LifeArcSynthesisEvidence> evidenceRows = evidence.rows();
        return new LifeArcSynthesisTable(
                METHOD_ID,
                PRIMARY_DOCTRINE,
                SYNTHESIS_METHOD,
                inquiryDate,
                inquiryDateTime,
                completedAgeYears,
                activeYearStart,
                activeYearEnd,
                evidenceRows,
                groups(evidenceRows)
        );
    }

    private void addProfectionEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                       int completedAgeYears) {
        AnnualProfectionTable annualTable = new DorotheanAnnualProfectionCalculator().calculateTable(
                subject,
                chart,
                inquiryDate,
                completedAgeYears,
                completedAgeYears
        );
        AnnualProfectionTableRow annual = annualTable.rows().stream()
                .filter(AnnualProfectionTableRow::activeForInquiry)
                .findFirst()
                .orElse(annualTable.rows().get(0));
        AnnualProfectionReferenceEntry annualAsc = annual.referenceProfections().stream()
                .filter(ref -> ref.reference() == AnnualProfectionReference.ASCENDANT)
                .findFirst()
                .orElseThrow();
        OffsetDateTime annualStart = subject.getLocalBirthDateTime().plusYears(annual.ageYears());
        OffsetDateTime annualEnd = annualStart.plusYears(1);
        String annualDetail = "Annual Ascendant profection activates H" + annualAsc.profectedHouse() + " " + annualAsc.profectedSign() + " ruled by " + annualAsc.lord() + ".";
        evidence.sign("ANNUAL_PROFECTION", annualTable.methodId(), "Annual profection", annualStart, annualEnd, annualAsc.profectedSign(), ChronocratorWeights.ANNUAL_PROFECTED_SIGN_OR_HOUSE, annualDetail);
        evidence.house("ANNUAL_PROFECTION", annualTable.methodId(), "Annual profection", annualStart, annualEnd, annualAsc.profectedHouse(), ChronocratorWeights.ANNUAL_PROFECTED_SIGN_OR_HOUSE, annualDetail);
        evidence.planet("ANNUAL_PROFECTION", annualTable.methodId(), "Lord of the Year", annualStart, annualEnd, annualAsc.lord(), ChronocratorWeights.LORD_OF_YEAR, annualDetail);

        MonthlyProfectionTable monthlyTable = new DorotheanMonthlyProfectionCalculator().calculateTable(
                subject,
                chart,
                inquiryDate,
                completedAgeYears,
                completedAgeYears
        );
        MonthlyProfectionTableRow monthly = monthlyTable.rows().stream()
                .filter(MonthlyProfectionTableRow::activeForInquiry)
                .findFirst()
                .orElseThrow();
        MonthlyProfectionReferenceEntry monthlyAsc = monthly.referenceProfections().stream()
                .filter(ref -> ref.reference() == AnnualProfectionReference.ASCENDANT)
                .findFirst()
                .orElseThrow();
        String monthlyDetail = "Monthly Ascendant profection activates H" + monthlyAsc.profectedHouse() + " " + monthlyAsc.profectedSign() + " ruled by " + monthlyAsc.lord() + ".";
        evidence.sign("MONTHLY_PROFECTION", monthlyTable.methodId(), "Monthly profection", monthly.periodStartDateTime(), monthly.periodEndDateTimeExclusive(), monthlyAsc.profectedSign(), ChronocratorWeights.MONTHLY_PROFECTED_SIGN_OR_HOUSE, monthlyDetail);
        evidence.house("MONTHLY_PROFECTION", monthlyTable.methodId(), "Monthly profection", monthly.periodStartDateTime(), monthly.periodEndDateTimeExclusive(), monthlyAsc.profectedHouse(), ChronocratorWeights.MONTHLY_PROFECTED_SIGN_OR_HOUSE, monthlyDetail);
        evidence.planet("MONTHLY_PROFECTION", monthlyTable.methodId(), "Lord of the Month", monthly.periodStartDateTime(), monthly.periodEndDateTimeExclusive(), monthlyAsc.lord(), ChronocratorWeights.LORD_OF_MONTH, monthlyDetail);
    }

    private void addFirdariaEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                     int completedAgeYears) {
        FirdariaTable table = new FirdariaCalculator().calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        FirdariaPeriod active = table.periods().stream().filter(FirdariaPeriod::activeForInquiry).findFirst().orElse(null);
        if (active == null) {
            return;
        }
        String detail = "Active firdaria main period is " + active.ruler() + ".";
        evidence.planet("FIRDARIA", table.methodId(), "Firdaria main period", active.startDateTime(), active.endDateTimeExclusive(), active.ruler(), ChronocratorWeights.MAIN_PERIOD_RULER, detail);
        active.subperiods().stream()
                .filter(FirdariaSubperiod::activeForInquiry)
                .findFirst()
                .ifPresent(sub -> evidence.planet("FIRDARIA", table.methodId(), "Firdaria subperiod", sub.startDateTime(), sub.endDateTimeExclusive(), sub.partner(), ChronocratorWeights.PARTNER_PERIOD_RULER, "Active firdaria partner is " + sub.partner() + "."));
    }

    private void addDecennialEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                      int completedAgeYears) {
        DecennialTable table = new DecennialCalculator().calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        DecennialPeriod active = table.periods().stream().filter(DecennialPeriod::activeForInquiry).findFirst().orElse(null);
        if (active == null) {
            return;
        }
        String detail = "Active decennial main period is " + active.ruler() + ".";
        evidence.planet("DECENNIAL", table.methodId(), "Decennial main period", active.startDateTime(), active.endDateTimeExclusive(), active.ruler(), ChronocratorWeights.MAIN_PERIOD_RULER, detail);
        active.subperiods().stream()
                .filter(DecennialSubperiod::activeForInquiry)
                .findFirst()
                .ifPresent(sub -> evidence.planet("DECENNIAL", table.methodId(), "Decennial subperiod", sub.startDateTime(), sub.endDateTimeExclusive(), sub.partner(), ChronocratorWeights.PARTNER_PERIOD_RULER, "Active decennial partner is " + sub.partner() + "."));
    }

    private void addZodiacalReleasingEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart,
                                              OffsetDateTime inquiryDateTime) {
        if (chart.getLots() == null) {
            return;
        }
        OffsetDateTime endDateTime = subject.getLocalBirthDateTime().plusYears(100);
        ZodiacalReleasingCalculator calculator = new ZodiacalReleasingCalculator();
        for (String lotName : List.of("FORTUNE", "SPIRIT")) {
            LotEntry lot = chart.getLots().stream()
                    .filter(candidate -> lotName.equals(candidate.name()))
                    .findFirst()
                    .orElse(null);
            if (lot == null) {
                continue;
            }
            ZodiacalReleasingTimeline timeline = calculator.calculate(lot.sign(), subject.getLocalBirthDateTime(), endDateTime);
            List<ZodiacalReleasingPeriod> activePath = activeReleasingPath(timeline.periods(), inquiryDateTime);
            evidence.lot("ZODIACAL_RELEASING", timeline.methodId(), lot.name() + " releasing", subject.getLocalBirthDateTime(), endDateTime, lot.name(), ChronocratorWeights.ZODIACAL_RELEASING_LOT_ANCHOR, "Zodiacal Releasing from " + lot.name() + ".");
            for (ZodiacalReleasingPeriod period : activePath) {
                EvidenceWeight weight = ChronocratorWeights.zodiacalReleasingLevel(period.level());
                String detail = lot.name() + " releasing L" + period.level() + " is in " + period.sign() + ".";
                evidence.sign("ZODIACAL_RELEASING", timeline.methodId(), lot.name() + " ZR L" + period.level(), period.startDateTime(), period.endDateTimeExclusive(), period.sign(), weight, detail);
                evidence.house("ZODIACAL_RELEASING", timeline.methodId(), lot.name() + " ZR L" + period.level(), period.startDateTime(), period.endDateTimeExclusive(), houseForSign(chart, period.sign()), weight, detail);
            }
        }
    }

    private List<ZodiacalReleasingPeriod> activeReleasingPath(List<ZodiacalReleasingPeriod> periods, OffsetDateTime inquiryDateTime) {
        for (ZodiacalReleasingPeriod period : periods) {
            if (!inquiryDateTime.isBefore(period.startDateTime()) && inquiryDateTime.isBefore(period.endDateTimeExclusive())) {
                List<ZodiacalReleasingPeriod> path = new ArrayList<>();
                path.add(period);
                path.addAll(activeReleasingPath(period.subPeriods(), inquiryDateTime));
                return List.copyOf(path);
            }
        }
        return List.of();
    }

    private void addDistributionEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                         int completedAgeYears) {
        DistributionThroughBoundsCalculator calculator = new DistributionThroughBoundsCalculator();
        DistributionThroughBoundsTable ascendantTable = calculator.calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        addDistributionTableEvidence(evidence, chart, ascendantTable, "DISTRIBUTIONS_THROUGH_BOUNDS");
        for (DistributionThroughBoundsTable extendedTable : calculator.calculateExtendedTables(subject, chart, inquiryDate, completedAgeYears, completedAgeYears)) {
            addDistributionTableEvidence(evidence, chart, extendedTable, "DISTRIBUTIONS_EXTENDED");
        }
    }

    private void addDistributionTableEvidence(EvidenceBuilder evidence, NatalChart chart,
                                              DistributionThroughBoundsTable table, String sourceTechnique) {
        DistributionThroughBoundsPeriod active = table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (active == null) {
            return;
        }
        String detail = "Active " + table.directedPoint() + " distribution bound is " + active.sign() + " ruled by " + active.boundRuler() + ".";
        evidence.sign(sourceTechnique, table.methodId(), table.directedPoint() + " active bound", active.startDateTime(), active.endDateTimeExclusive(), active.sign(), DirectionContactWeights.ACTIVE_BOUND_SIGN_OR_HOUSE, detail);
        evidence.house(sourceTechnique, table.methodId(), table.directedPoint() + " active bound", active.startDateTime(), active.endDateTimeExclusive(), houseForSign(chart, active.sign()), DirectionContactWeights.ACTIVE_BOUND_SIGN_OR_HOUSE, detail);
        evidence.planet(sourceTechnique, table.methodId(), table.directedPoint() + " bound lord", active.startDateTime(), active.endDateTimeExclusive(), active.boundRuler(), DirectionContactWeights.BOUND_LORD, detail);
        for (DistributionThroughBoundsContact contact : active.contacts()) {
            String contactDetail = "Directed " + table.directedPoint() + " contacts " + contact.sourcePlanet() + " " + contact.aspect() + ".";
            evidence.planet(sourceTechnique, table.methodId(), table.directedPoint() + " directed contact", contact.dateTime(), contact.dateTime(), contact.sourcePlanet(), DirectionContactWeights.DIRECTED_CONTACT_PLANET, contactDetail);
            evidence.aspect(sourceTechnique, table.methodId(), table.directedPoint() + " directed contact", contact.dateTime(), contact.dateTime(), contact.aspect(), DirectionContactWeights.DIRECTED_CONTACT_ASPECT, contactDetail);
        }
    }

    private void addPrimaryDirectionEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                             int completedAgeYears) {
        PrimaryDirectionCalculator calculator = new PrimaryDirectionCalculator();
        PrimaryDirectionTable directTable = calculator.calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        for (PrimaryDirectionEvent event : directTable.events().stream().filter(PrimaryDirectionEvent::activeForInquiryYear).toList()) {
            addZodiacalPrimaryDirectionEvent(
                    evidence,
                    chart,
                    directTable.methodId(),
                    "PRIMARY_DIRECTIONS",
                    "Primary direction",
                    "Primary direction target",
                    DirectionContactWeights.PRIMARY_DIRECTION_PROMISSOR,
                    DirectionContactWeights.PRIMARY_DIRECTION_TARGET_SIGN_OR_HOUSE,
                    DirectionContactWeights.PRIMARY_DIRECTION_ASPECT,
                    event,
                    event.significatorPoint() + " direct normalized zodiacal direction to " + event.promissorPlanet() + " " + event.aspect() + "."
            );
        }

        PrimaryDirectionTable directConverseTable = calculator.calculateDirectConverseTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        for (PrimaryDirectionEvent event : directConverseTable.events().stream()
                .filter(PrimaryDirectionEvent::activeForInquiryYear)
                .filter(candidate -> candidate.direction() == PrimaryDirectionPolarity.CONVERSE)
                .toList()) {
            addZodiacalPrimaryDirectionEvent(
                    evidence,
                    chart,
                    directConverseTable.methodId(),
                    "PRIMARY_DIRECTIONS_CONVERSE",
                    "Converse primary direction variant",
                    "Converse primary direction target",
                    DirectionContactWeights.PRIMARY_DIRECTION_VARIANT_PROMISSOR,
                    DirectionContactWeights.PRIMARY_DIRECTION_VARIANT_TARGET_SIGN_OR_HOUSE,
                    DirectionContactWeights.PRIMARY_DIRECTION_VARIANT_ASPECT,
                    event,
                    event.significatorPoint() + " converse normalized zodiacal direction to " + event.promissorPlanet() + " " + event.aspect() + "; lower-weight variant evidence."
            );
        }

        MundanePrimaryDirectionTable mundaneTable = new MundanePrimaryDirectionCalculator().calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        for (MundanePrimaryDirectionEvent event : mundaneTable.events().stream().filter(MundanePrimaryDirectionEvent::activeForInquiryYear).toList()) {
            String detail = "Mundane/semi-arc prototype directs " + event.significatorPoint()
                    + " to " + event.promissorPlanet() + " body at mundane position "
                    + formatDegrees(event.targetMundanePositionDegrees())
                    + "; lower-weight prototype evidence only.";
            evidence.planet("PRIMARY_DIRECTIONS_MUNDANE_PROTOTYPE", mundaneTable.methodId(), "Mundane primary direction prototype", event.dateTime(), event.dateTime(), event.promissorPlanet(), DirectionContactWeights.MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_PROMISSOR, detail);
            evidence.point("PRIMARY_DIRECTIONS_MUNDANE_PROTOTYPE", mundaneTable.methodId(), "Mundane primary direction significator", event.dateTime(), event.dateTime(), event.significatorPoint(), DirectionContactWeights.MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_CONTEXT, detail);
            evidence.sign("PRIMARY_DIRECTIONS_MUNDANE_PROTOTYPE", mundaneTable.methodId(), "Mundane primary direction promissor sign", event.dateTime(), event.dateTime(), event.promissorNatalSign(), DirectionContactWeights.MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_CONTEXT, detail);
            evidence.house("PRIMARY_DIRECTIONS_MUNDANE_PROTOTYPE", mundaneTable.methodId(), "Mundane primary direction promissor house", event.dateTime(), event.dateTime(), event.promissorNatalHouse(), DirectionContactWeights.MUNDANE_PRIMARY_DIRECTION_PROTOTYPE_CONTEXT, detail);
        }
    }

    private void addZodiacalPrimaryDirectionEvent(EvidenceBuilder evidence, NatalChart chart, String methodId,
                                                  String sourceTechnique, String timingLabel, String targetLabel,
                                                  EvidenceWeight promissorWeight, EvidenceWeight targetWeight,
                                                  EvidenceWeight aspectWeight, PrimaryDirectionEvent event,
                                                  String detail) {
        evidence.planet(sourceTechnique, methodId, timingLabel, event.dateTime(), event.dateTime(), event.promissorPlanet(), promissorWeight, detail);
        evidence.sign(sourceTechnique, methodId, targetLabel, event.dateTime(), event.dateTime(), event.targetSign(), targetWeight, detail);
        evidence.house(sourceTechnique, methodId, targetLabel, event.dateTime(), event.dateTime(), houseForSign(chart, event.targetSign()), targetWeight, detail);
        evidence.aspect(sourceTechnique, methodId, timingLabel, event.dateTime(), event.dateTime(), event.aspect(), aspectWeight, detail);
    }

    private void addSolarReturnEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                        int completedAgeYears) {
        SolarReturnTable solarReturnTable = new SolarReturnCalculator().calculateTable(subject, chart, completedAgeYears, completedAgeYears);
        SolarReturnNatalComparisonTable comparisonTable = new SolarReturnNatalComparisonCalculator().calculate(subject, chart, solarReturnTable, inquiryDate);
        SolarReturnNatalComparisonRow active = comparisonTable.rows().stream().filter(SolarReturnNatalComparisonRow::activeForInquiry).findFirst().orElse(null);
        if (active == null) {
            return;
        }
        String detail = "Active solar return overlays profected H" + active.profectedHouse() + " " + active.profectedSign() + ".";
        evidence.sign("SOLAR_RETURN", comparisonTable.methodId(), "Solar return profection overlay", active.returnDateTime(), active.periodEndDateTimeExclusive(), active.profectedSign(), ReturnChartWeights.SOLAR_RETURN_PROFECTION_OVERLAY, detail);
        evidence.house("SOLAR_RETURN", comparisonTable.methodId(), "Solar return profection overlay", active.returnDateTime(), active.periodEndDateTimeExclusive(), active.profectedHouse(), ReturnChartWeights.SOLAR_RETURN_PROFECTION_OVERLAY, detail);
        evidence.planet("SOLAR_RETURN", comparisonTable.methodId(), "Solar return Lord of Year", active.returnDateTime(), active.periodEndDateTimeExclusive(), active.lordOfYear(), ReturnChartWeights.SOLAR_RETURN_LORD_OF_YEAR, detail);
        for (PointKey point : active.solarReturnPointsInProfectedSign()) {
            evidence.point("SOLAR_RETURN", comparisonTable.methodId(), "SR point in profected sign", active.returnDateTime(), active.periodEndDateTimeExclusive(), point.name(), ReturnChartWeights.SOLAR_RETURN_POINT_OVERLAY, point + " is in the annual profected sign in the solar return.");
        }
        for (PointKey point : active.solarReturnPointsOverlayingProfectedHouse()) {
            evidence.point("SOLAR_RETURN", comparisonTable.methodId(), "SR point overlaying profected house", active.returnDateTime(), active.periodEndDateTimeExclusive(), point.name(), ReturnChartWeights.SOLAR_RETURN_POINT_OVERLAY, point + " overlays the annual profected house in the solar return.");
        }
        for (SolarReturnNatalContact contact : active.conjunctions().stream().filter(contact -> contact.orbDegrees() <= 1.0).toList()) {
            evidence.point("SOLAR_RETURN", comparisonTable.methodId(), "Tight SR-natal conjunction", active.returnDateTime(), active.periodEndDateTimeExclusive(), contact.solarReturnPoint().name(), ReturnChartWeights.TIGHT_SOLAR_RETURN_NATAL_CONJUNCTION, contact.solarReturnPoint() + " tightly conjoins " + contact.natalTargetName() + ".");
        }
    }

    private void addLunarTimingEvidence(EvidenceBuilder evidence, Subject subject, NatalChart chart, LocalDate inquiryDate,
                                        int completedAgeYears, OffsetDateTime activeYearStart, OffsetDateTime activeYearEnd) {
        LunarTimingTable table = new LunarTimingCalculator().calculateTable(subject, chart, inquiryDate, completedAgeYears, completedAgeYears);
        table.lunarReturns().stream()
                .filter(LunarReturnEntry::activeForInquiry)
                .findFirst()
                .ifPresent(row -> {
                    String detail = "Active lunar return places the Moon in " + row.moonSign() + " over natal H" + row.natalHouseOverlay() + ".";
                    evidence.sign("LUNAR_RETURN", table.methodId(), "Active lunar return", row.returnDateTime(), row.periodEndDateTimeExclusive(), row.moonSign(), LunarEclipseWeights.ACTIVE_LUNAR_RETURN, detail);
                    evidence.house("LUNAR_RETURN", table.methodId(), "Active lunar return", row.returnDateTime(), row.periodEndDateTimeExclusive(), row.natalHouseOverlay(), LunarEclipseWeights.ACTIVE_LUNAR_RETURN, detail);
                    evidence.planet("LUNAR_RETURN", table.methodId(), "Active lunar return", row.returnDateTime(), row.periodEndDateTimeExclusive(), Planet.MOON, LunarEclipseWeights.ACTIVE_LUNAR_RETURN, detail);
                });
        table.lunations().stream()
                .filter(LunationEntry::activeForInquiry)
                .findFirst()
                .ifPresent(row -> {
                    String detail = "Active lunation is " + row.type() + " at " + row.syzygySign() + ".";
                    evidence.sign("LUNATION", table.methodId(), "Active lunation", row.dateTime(), row.periodEndDateTimeExclusive(), row.syzygySign(), LunarEclipseWeights.ACTIVE_LUNATION, detail);
                    evidence.house("LUNATION", table.methodId(), "Active lunation", row.dateTime(), row.periodEndDateTimeExclusive(), row.natalHouseOverlay(), LunarEclipseWeights.ACTIVE_LUNATION, detail);
                });
        for (LunationEntry row : table.lunations()) {
            if (row.eclipseType() == EclipseCandidateType.NONE || row.dateTime().isBefore(activeYearStart) || !row.dateTime().isBefore(activeYearEnd)) {
                continue;
            }
            String detail = row.eclipseType() + " at " + row.syzygySign() + " in active birthday year.";
            evidence.sign("ECLIPSE_CANDIDATE", table.methodId(), "Eclipse candidate", row.dateTime(), row.periodEndDateTimeExclusive(), row.syzygySign(), LunarEclipseWeights.ECLIPSE_CANDIDATE, detail);
            evidence.house("ECLIPSE_CANDIDATE", table.methodId(), "Eclipse candidate", row.dateTime(), row.periodEndDateTimeExclusive(), row.natalHouseOverlay(), LunarEclipseWeights.ECLIPSE_CANDIDATE, detail);
        }
        for (EclipseEvent event : table.eclipseEvents()) {
            if (event.maximumDateTime().isBefore(activeYearStart) || !event.maximumDateTime().isBefore(activeYearEnd)) {
                continue;
            }
            EvidenceWeight weight = trueEclipseWeight(event);
            String label = "True " + event.kind().name().toLowerCase() + " eclipse";
            String detail = event.kind() + " " + event.eclipseType() + " true eclipse at " + event.syzygySign()
                    + " over natal H" + event.natalHouseOverlay()
                    + "; localVisibility=" + event.visibility().localVisibility()
                    + "; visiblePhases=" + event.visibility().visibleContactPhases() + ".";
            evidence.sign("TRUE_ECLIPSE", event.methodId(), label, event.maximumDateTime(), event.maximumDateTime(), event.syzygySign(), weight, detail);
            evidence.house("TRUE_ECLIPSE", event.methodId(), label, event.maximumDateTime(), event.maximumDateTime(), event.natalHouseOverlay(), weight, detail);
            evidence.planet("TRUE_ECLIPSE", event.methodId(), label, event.maximumDateTime(), event.maximumDateTime(), event.kind() == EclipseEventKind.SOLAR ? Planet.SUN : Planet.MOON, weight, detail);
            evidence.planet("TRUE_ECLIPSE", event.methodId(), "True eclipse nearest node", event.maximumDateTime(), event.maximumDateTime(), event.nearestNode(), LunarEclipseWeights.ECLIPSE_CANDIDATE, detail);
        }
    }

    private EvidenceWeight trueEclipseWeight(EclipseEvent event) {
        return switch (event.visibility().localVisibility()) {
            case VISIBLE -> LunarEclipseWeights.TRUE_ECLIPSE_VISIBLE;
            case NOT_VISIBLE -> LunarEclipseWeights.TRUE_ECLIPSE_NOT_VISIBLE;
            case UNKNOWN -> LunarEclipseWeights.TRUE_ECLIPSE_UNKNOWN_VISIBILITY;
        };
    }

    private String formatDegrees(double value) {
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private List<LifeArcSynthesisGroup> groups(List<LifeArcSynthesisEvidence> evidence) {
        Map<GroupKey, List<LifeArcSynthesisEvidence>> byKey = new LinkedHashMap<>();
        for (LifeArcSynthesisEvidence item : evidence) {
            byKey.computeIfAbsent(new GroupKey(item.keyType(), item.key()), ignored -> new ArrayList<>()).add(item);
        }
        return byKey.entrySet().stream()
                .map(entry -> new LifeArcSynthesisGroup(
                        entry.getKey().keyType(),
                        entry.getKey().key(),
                        entry.getValue().stream().mapToInt(LifeArcSynthesisEvidence::weight).sum(),
                        entry.getValue().size(),
                        entry.getValue().stream().map(LifeArcSynthesisEvidence::sequenceIndex).toList()
                ))
                .sorted(Comparator
                        .comparingInt(LifeArcSynthesisGroup::totalWeight).reversed()
                        .thenComparing(group -> group.keyType().ordinal())
                        .thenComparing(LifeArcSynthesisGroup::key))
                .toList();
    }

    private int completedAgeYears(Subject subject, LocalDate inquiryDate) {
        LocalDate birthDate = subject.getLocalBirthDateTime().toLocalDate();
        int years = inquiryDate.getYear() - birthDate.getYear();
        if (inquiryDate.isBefore(birthDate.plusYears(years))) {
            years--;
        }
        return years;
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    private static final class EvidenceBuilder {
        private final List<LifeArcSynthesisEvidence> rows = new ArrayList<>();

        void sign(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                  OffsetDateTime end, ZodiacSign sign, EvidenceWeight weight, String detail) {
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.SIGN, sign.name(), sign, null, null, null, null, null, weight, detail);
        }

        void house(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                   OffsetDateTime end, Integer house, EvidenceWeight weight, String detail) {
            if (house == null) {
                return;
            }
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.HOUSE, "H" + house, null, house, null, null, null, null, weight, detail);
        }

        void planet(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                    OffsetDateTime end, Planet planet, EvidenceWeight weight, String detail) {
            if (planet == null) {
                return;
            }
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.PLANET, planet.name(), null, null, planet, null, null, null, weight, detail);
        }

        void point(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                   OffsetDateTime end, String point, EvidenceWeight weight, String detail) {
            if (point == null) {
                return;
            }
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.POINT, point, null, null, null, point, null, null, weight, detail);
        }

        void lot(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                 OffsetDateTime end, String lotName, EvidenceWeight weight, String detail) {
            if (lotName == null) {
                return;
            }
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.LOT, "LOT_" + lotName, null, null, null, null, lotName, null, weight, detail);
        }

        void aspect(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                    OffsetDateTime end, AspectType aspect, EvidenceWeight weight, String detail) {
            if (aspect == null) {
                return;
            }
            add(sourceTechnique, sourceMethodId, timingLabel, start, end, LifeArcEvidenceKeyType.ASPECT, aspect.name(), null, null, null, null, null, aspect.name(), weight, detail);
        }

        void add(String sourceTechnique, String sourceMethodId, String timingLabel, OffsetDateTime start,
                 OffsetDateTime end, LifeArcEvidenceKeyType keyType, String key, ZodiacSign sign,
                 Integer house, Planet planet, String point, String lotName, String aspect, EvidenceWeight weight, String detail) {
            rows.add(new LifeArcSynthesisEvidence(
                    rows.size() + 1,
                    sourceTechnique,
                    sourceMethodId,
                    weight.weightClass(),
                    timingLabel,
                    start,
                    end,
                    keyType,
                    key,
                    sign,
                    house,
                    planet,
                    point,
                    lotName,
                    aspect,
                    weight.value(),
                    detail
            ));
        }

        List<LifeArcSynthesisEvidence> rows() {
            return List.copyOf(rows);
        }
    }

    private record GroupKey(LifeArcEvidenceKeyType keyType, String key) {}
}
