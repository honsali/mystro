package app.local;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.chart.AstroMath;
import app.chart.BasicCalculator;
import app.chart.CalculationContext;
import app.chart.data.AspectType;
import app.chart.data.HouseSystem;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.Terms;
import app.chart.data.Triplicity;
import app.chart.data.ZodiacSign;
import app.chart.model.AnglePointEntry;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.PlanetPointEntry;
import app.chart.model.PointEntry;
import app.chart.model.Subject;
import app.input.NativeListInputLoader;
import app.input.ReadingInput;
import app.input.ReadingInputMapper;
import app.io.MystroObjectMapper;
import app.planetaryhours.PlanetaryHoursCalculation;
import app.planetaryhours.PlanetaryHoursCalculator;
import app.planetaryhours.PlanetaryHoursInput;
import app.reading.CoreDoctrineInfo;
import app.reading.description.NatalDescriptionReadingCalculator;
import app.reading.description.NatalDescriptionReadingReport;
import app.reading.description.common.model.LotEntry;
import app.reading.description.valens.ValensNatalDescriptionSpecialist;
import app.reading.lifearc.decennial.DecennialPeriod;
import app.reading.lifearc.decennial.DecennialSubperiod;
import app.reading.lifearc.decennial.DecennialTable;
import app.reading.lifearc.decennial.DecennialCalculator;
import app.reading.lifearc.distribution.DistributionThroughBoundsCalculator;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.dorothean.calculator.DorotheanAnnualProfectionCalculator;
import app.reading.lifearc.dorothean.calculator.DorotheanDailyProfectionCalculator;
import app.reading.lifearc.dorothean.calculator.DorotheanMonthlyProfectionCalculator;
import app.reading.lifearc.firdaria.FirdariaCalculator;
import app.reading.lifearc.firdaria.FirdariaPeriod;
import app.reading.lifearc.firdaria.FirdariaSubperiod;
import app.reading.lifearc.firdaria.FirdariaTable;
import app.reading.lifearc.lunar.LunarTimingCalculator;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.lunar.LunarZoomCalculator;
import app.reading.lifearc.lunar.LunarZoomTable;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;
import app.reading.lifearc.model.DailyProfectionTable;
import app.reading.lifearc.model.DailyProfectionTableRow;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;
import app.reading.lifearc.primarydirection.PrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;
import app.reading.lifearc.solarreturn.SolarReturnCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnTable;
import app.reading.lifearc.transit.ExactTransitHit;
import app.reading.lifearc.transit.ExactTransitSearchCalculator;
import app.reading.lifearc.transit.MonthlyTransitActivationReason;
import app.reading.lifearc.transit.TransitNatalTargetType;
import app.reading.lifearc.transit.TransitSearchWindow;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingCalculator;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

/**
 * Local-only high-zoom timing dump harness.
 *
 * <p>Run with:
 * <pre>
 * mvn -q -Dtest=LocalZoomDumpRunner -Dlocal.zoom=true -Dlocal.reading.alias=demo -Dlocal.zoom.date=15/06/2024 test
 * </pre>
 *
 * <p>Default output directory: {@code output/<native-list-alias>/<yyyyMMdd>/}
 */
public final class LocalZoomDumpRunner {
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int WINDOW_RADIUS_DAYS = 15;
    private static final int MAX_TRANSIT_WINDOWS = 360;
    private static final List<PointKey> TRANSIT_POINT_ORDER = List.of(
            PointKey.SUN,
            PointKey.MOON,
            PointKey.MERCURY,
            PointKey.VENUS,
            PointKey.MARS,
            PointKey.JUPITER,
            PointKey.SATURN,
            PointKey.NORTH_NODE,
            PointKey.SOUTH_NODE
    );
    private static final List<AspectType> TRANSIT_ASPECT_ORDER = List.of(
            AspectType.CONJUNCTION,
            AspectType.SEXTILE,
            AspectType.SQUARE,
            AspectType.TRINE,
            AspectType.OPPOSITION
    );
    private static final Set<PointKey> CORE_POINT_TARGETS = Set.of(
            PointKey.ASCENDANT,
            PointKey.MIDHEAVEN,
            PointKey.SUN,
            PointKey.MOON
    );
    private static final Set<String> CORE_LOT_TARGETS = Set.of("FORTUNE", "SPIRIT");
    private static final CoreDoctrineInfo ZOOM_CONVENTIONS = new CoreDoctrineInfo(
            "local_zoom_timing",
            "Local Zoom Timing",
            HouseSystem.WHOLE_SIGN,
            Terms.EGYPTIAN,
            Triplicity.DOROTHEAN
    );

    @Test
    void writesZoomTimingPackToOutput() throws Exception {
        assumeTrue(Boolean.getBoolean("local.zoom"),
                "Set -Dlocal.zoom=true to run the local zoom timing dump");

        String alias = validatedAlias(System.getProperty("local.reading.alias"));
        LocalDate focusDate = parseFocusDate(System.getProperty("local.zoom.date"));
        String focusDirectoryName = focusDate.format(OUTPUT_DATE_FORMAT);
        Path outputDir = Path.of("output", alias, focusDirectoryName);
        Path overviewOutput = outputDir.resolve("zoom_overview.md");
        Path activePeriodsOutput = outputDir.resolve("active_periods.md");
        Path zodiacalReleasingOutput = outputDir.resolve("zodiacal_releasing_active.md");
        Path dailyProfectionsOutput = outputDir.resolve("daily_profections.md");
        Path planetaryHoursOutput = outputDir.resolve("planetary_hours.md");
        Path lunarZoomOutput = outputDir.resolve("lunar_30d.md");
        Path solarReturnFocusOutput = outputDir.resolve("solar_return_focus.md");
        Path directionsZoomOutput = outputDir.resolve("directions_30d.md");
        Path transitsOutput = outputDir.resolve("transits_30d.md");

        ObjectMapper objectMapper = MystroObjectMapper.create();
        ReadingInput request = new NativeListInputLoader().load(alias, objectMapper);
        request.setInquiryDate(focusDate.toString());

        ReadingInputMapper mapper = new ReadingInputMapper();
        ReadingInputMapper.ResolvedBundle resolved = mapper.resolve(request);
        Subject subject = resolved.subject();
        NatalDescriptionReadingCalculator natalDescriptionReadingCalculator = new NatalDescriptionReadingCalculator(
                new BasicCalculator(),
                new ValensNatalDescriptionSpecialist()
        );
        NatalDescriptionReadingReport natalDescription = natalDescriptionReadingCalculator.calculate(subject);
        NatalChart chart = natalDescription.getNatalChart();

        OffsetDateTime focusDateTime = OffsetDateTime.of(
                focusDate,
                subject.getLocalBirthDateTime().toLocalTime(),
                subject.getLocalBirthDateTime().getOffset()
        );
        OffsetDateTime windowStart = focusDateTime.minusDays(WINDOW_RADIUS_DAYS);
        OffsetDateTime windowEnd = focusDateTime.plusDays(WINDOW_RADIUS_DAYS);
        int focusAgeYears = activeAgeYears(subject, focusDate);
        int windowAgeStartYears = Math.max(0, activeAgeYears(subject, windowStart.toLocalDate()));
        int windowAgeEndYears = Math.max(windowAgeStartYears, activeAgeYears(subject, windowEnd.toLocalDate()));
        PlanetaryHoursCalculation planetaryHours = new PlanetaryHoursCalculator().calculateFullPlanetaryDay(
                new PlanetaryHoursInput(
                        subject.getId(),
                        focusDate,
                        subject.getLocalBirthDateTime().getOffset(),
                        subject.getLatitude(),
                        subject.getLongitude()
                )
        );

        AnnualProfectionTable annualProfections = new DorotheanAnnualProfectionCalculator().calculateTable(
                subject,
                chart,
                focusDate,
                focusAgeYears,
                focusAgeYears
        );
        MonthlyProfectionTable monthlyProfections = new DorotheanMonthlyProfectionCalculator().calculateTable(
                subject,
                chart,
                focusDate,
                focusAgeYears,
                focusAgeYears
        );
        DailyProfectionTable dailyProfections = new DorotheanDailyProfectionCalculator().calculateWindow(
                subject,
                chart,
                focusDate,
                WINDOW_RADIUS_DAYS
        );
        FirdariaTable firdaria = new FirdariaCalculator().calculateTable(subject, chart, focusDate, focusAgeYears, focusAgeYears);
        DecennialTable decennials = new DecennialCalculator().calculateTable(subject, chart, focusDate, focusAgeYears, focusAgeYears);
        DistributionThroughBoundsCalculator distributionCalculator = new DistributionThroughBoundsCalculator();
        DistributionThroughBoundsTable distribution = distributionCalculator.calculateTable(subject, chart, focusDate, windowAgeStartYears, windowAgeEndYears);
        List<DistributionThroughBoundsTable> extendedDistributions = distributionCalculator.calculateExtendedTables(subject, chart, focusDate, windowAgeStartYears, windowAgeEndYears);
        PrimaryDirectionTable primaryDirections = new PrimaryDirectionCalculator().calculateTable(subject, chart, focusDate, windowAgeStartYears, windowAgeEndYears);
        MundanePrimaryDirectionTable mundanePrimaryDirections = new MundanePrimaryDirectionCalculator().calculateTable(subject, chart, focusDate, windowAgeStartYears, windowAgeEndYears);
        SolarReturnTable solarReturns = new SolarReturnCalculator().calculateTable(subject, chart, focusAgeYears, focusAgeYears);
        SolarReturnNatalComparisonTable solarReturnComparison = new SolarReturnNatalComparisonCalculator().calculate(
                subject,
                chart,
                solarReturns,
                focusDate
        );
        LunarTimingTable lunarTiming = new LunarTimingCalculator().calculateTable(subject, chart, focusDate, windowAgeStartYears, windowAgeEndYears);
        LunarZoomTable lunarZoom = new LunarZoomCalculator().calculate(subject, chart, windowStart, windowEnd);
        List<ZodiacalReleasingActiveMarkdownRenderer.LotTimeline> zodiacalReleasingTimelines = zodiacalReleasingTimelines(subject, chart);

        List<TransitSearchWindow> lunarTransitWindows = buildLunarTransitWindowsFromDailyProfections(
                subject,
                chart,
                focusDateTime,
                windowStart,
                windowEnd,
                dailyProfections
        );
        List<ExactTransitHit> lunarTransitHits = new ExactTransitSearchCalculator().calculateHits(subject, lunarTransitWindows);

        List<TransitSearchWindow> transitWindows = buildZoomTransitWindows(
                subject,
                chart,
                focusDateTime,
                windowStart,
                windowEnd,
                annualProfections,
                monthlyProfections,
                firdaria,
                decennials,
                distribution,
                extendedDistributions
        );
        List<ExactTransitHit> transitHits = new ExactTransitSearchCalculator().calculateHits(subject, transitWindows);

        Files.createDirectories(outputDir);
        Files.writeString(activePeriodsOutput, new ZoomActivePeriodsMarkdownRenderer().render(
                subject,
                focusDateTime,
                windowStart,
                windowEnd,
                annualProfections,
                monthlyProfections,
                firdaria,
                decennials,
                distribution,
                extendedDistributions,
                solarReturns,
                solarReturnComparison,
                lunarTiming
        ));
        Files.writeString(zodiacalReleasingOutput, new ZodiacalReleasingActiveMarkdownRenderer().render(
                subject,
                chart,
                focusDateTime,
                windowStart,
                windowEnd,
                zodiacalReleasingTimelines
        ));
        Files.writeString(dailyProfectionsOutput, new DailyProfectionMarkdownRenderer().render(
                subject,
                focusDate,
                dailyProfections
        ));
        Files.writeString(planetaryHoursOutput, new PlanetaryHoursMarkdownRenderer().render(
                subject,
                focusDateTime,
                planetaryHours
        ));
        Files.writeString(lunarZoomOutput, new LunarZoomMarkdownRenderer().render(
                subject,
                focusDateTime,
                windowStart,
                windowEnd,
                lunarZoom,
                lunarTiming,
                dailyProfections,
                lunarTransitHits
        ));
        Files.writeString(solarReturnFocusOutput, new SolarReturnFocusMarkdownRenderer().render(
                subject,
                focusDateTime,
                solarReturns,
                solarReturnComparison,
                annualProfections,
                monthlyProfections
        ));
        Files.writeString(directionsZoomOutput, new DirectionsZoomMarkdownRenderer().render(
                subject,
                focusDateTime,
                windowStart,
                windowEnd,
                distribution,
                extendedDistributions,
                primaryDirections,
                mundanePrimaryDirections
        ));
        Files.writeString(transitsOutput, new ZoomTransitMarkdownRenderer().render(
                subject,
                focusDateTime,
                windowStart,
                windowEnd,
                transitWindows,
                transitHits
        ));
        Files.writeString(overviewOutput, new ZoomOverviewMarkdownRenderer().render(
                subject,
                focusDateTime,
                windowStart,
                windowEnd,
                outputDir,
                List.of(
                        new ZoomOverviewMarkdownRenderer.FileReference(activePeriodsOutput, "Active profections, chronocrators, distributions, solar-return context, and lunar focus"),
                        new ZoomOverviewMarkdownRenderer.FileReference(zodiacalReleasingOutput, "Active Zodiacal Releasing chains and boundaries inside the requested window"),
                        new ZoomOverviewMarkdownRenderer.FileReference(dailyProfectionsOutput, "Daily profection rows across the requested ±15-day window"),
                        new ZoomOverviewMarkdownRenderer.FileReference(planetaryHoursOutput, "Planetary hours for the focus planetary day"),
                        new ZoomOverviewMarkdownRenderer.FileReference(lunarZoomOutput, "Moon ingresses, lunations/eclipses, and exact Moon hits inside the requested window"),
                        new ZoomOverviewMarkdownRenderer.FileReference(solarReturnFocusOutput, "Active solar-return chart, natal overlays, and conjunctions"),
                        new ZoomOverviewMarkdownRenderer.FileReference(directionsZoomOutput, "Distribution and primary-direction contacts inside the requested window"),
                        new ZoomOverviewMarkdownRenderer.FileReference(transitsOutput, "Exact transit hits inside the requested ±15-day window")
                )
        ));

        System.out.println("Wrote zoom overview to " + overviewOutput.toAbsolutePath());
        System.out.println("Wrote active periods to " + activePeriodsOutput.toAbsolutePath());
        System.out.println("Wrote Zodiacal Releasing active zoom to " + zodiacalReleasingOutput.toAbsolutePath());
        System.out.println("Wrote daily profections to " + dailyProfectionsOutput.toAbsolutePath());
        System.out.println("Wrote planetary hours to " + planetaryHoursOutput.toAbsolutePath());
        System.out.println("Wrote lunar 30-day zoom to " + lunarZoomOutput.toAbsolutePath());
        System.out.println("Wrote solar return focus to " + solarReturnFocusOutput.toAbsolutePath());
        System.out.println("Wrote directions 30-day zoom to " + directionsZoomOutput.toAbsolutePath());
        System.out.println("Wrote exact transit 30-day zoom to " + transitsOutput.toAbsolutePath());
    }

    private String validatedAlias(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Set -Dlocal.reading.alias=<native-list-alias> to run the local zoom dump");
        }
        String alias = value.trim();
        if (alias.contains("/") || alias.contains("\\") || ".".equals(alias) || "..".equals(alias)
                || alias.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new IllegalArgumentException("local.reading.alias must be a native-list name, not a path: " + alias);
        }
        return alias;
    }

    private LocalDate parseFocusDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Set -Dlocal.zoom.date=<dd/MM/yyyy> to run the local zoom dump");
        }
        try {
            return LocalDate.parse(value.trim(), INPUT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid local.zoom.date: " + value + " (expected dd/MM/yyyy)");
        }
    }

    private int activeAgeYears(Subject subject, LocalDate focusDate) {
        LocalDate birthDate = subject.getLocalBirthDateTime().toLocalDate();
        LocalDate birthdayThisYear = MonthDay.from(birthDate).atYear(focusDate.getYear());
        int ageYears = focusDate.getYear() - birthDate.getYear();
        return focusDate.isBefore(birthdayThisYear) ? ageYears - 1 : ageYears;
    }

    private List<ZodiacalReleasingActiveMarkdownRenderer.LotTimeline> zodiacalReleasingTimelines(Subject subject, NatalChart chart) {
        if (chart.getLots() == null || chart.getLots().isEmpty()) {
            return List.of();
        }
        OffsetDateTime endDateTime = subject.getLocalBirthDateTime().plusYears(100);
        ZodiacalReleasingCalculator calculator = new ZodiacalReleasingCalculator();
        List<ZodiacalReleasingActiveMarkdownRenderer.LotTimeline> timelines = new ArrayList<>();
        for (LotEntry lot : chart.getLots()) {
            ZodiacalReleasingTimeline timeline = calculator.calculate(lot.sign(), subject.getLocalBirthDateTime(), endDateTime);
            timelines.add(new ZodiacalReleasingActiveMarkdownRenderer.LotTimeline(lot, timeline));
        }
        return List.copyOf(timelines);
    }

    private List<TransitSearchWindow> buildLunarTransitWindowsFromDailyProfections(Subject subject,
                                                                                   NatalChart chart,
                                                                                   OffsetDateTime focusDateTime,
                                                                                   OffsetDateTime windowStart,
                                                                                   OffsetDateTime windowEnd,
                                                                                   DailyProfectionTable dailyProfections) {
        List<LunarNatalTarget> targets = dailyActivatedTargets(chart, dailyProfections);
        if (targets.isEmpty()) {
            return List.of();
        }
        double focusMoonLongitude = focusTransitLongitudes(subject, focusDateTime).get(PointKey.MOON);
        List<TransitSearchWindow> windows = new ArrayList<>();
        for (LunarNatalTarget target : targets) {
            for (AspectType aspect : TRANSIT_ASPECT_ORDER) {
                double angularSeparation = AstroMath.rawAngularSeparation(focusMoonLongitude, target.longitude());
                double focusOrb = Math.abs(angularSeparation - aspect.getExactAngle());
                windows.add(new TransitSearchWindow(
                        windows.size() + 1,
                        "ZOOM_DAILY_PROFECTION_LUNAR_FILTER",
                        "LOCAL_ZOOM_LUNAR_TRANSIT_WINDOWS_V1",
                        1,
                        focusDateTime,
                        windowStart,
                        windowEnd,
                        PointKey.MOON,
                        false,
                        false,
                        target.type(),
                        target.name(),
                        target.longitude(),
                        target.sign(),
                        target.degreeInSign(),
                        target.house(),
                        aspect,
                        angularSeparation,
                        focusOrb,
                        List.of(),
                        1
                ));
            }
        }
        return List.copyOf(windows);
    }

    private List<LunarNatalTarget> dailyActivatedTargets(NatalChart chart, DailyProfectionTable dailyProfections) {
        Map<String, LunarNatalTarget> targets = new LinkedHashMap<>();
        for (DailyProfectionTableRow row : dailyProfections.rows()) {
            row.activatedNatalPoints().forEach(point -> {
                PointEntry pointEntry = chart.getPoints().get(point.point());
                if (pointEntry == null) {
                    throw new IllegalArgumentException("Missing natal point " + point.point());
                }
                PointPlacement placement = pointPlacement(chart, point.point(), pointEntry);
                String key = TransitNatalTargetType.POINT + ":" + point.point().name();
                targets.putIfAbsent(key, new LunarNatalTarget(
                        TransitNatalTargetType.POINT,
                        point.point().name(),
                        placement.longitude(),
                        placement.sign(),
                        placement.degreeInSign(),
                        placement.house()
                ));
            });
            row.activatedLots().forEach(activatedLot -> {
                LotEntry lot = chart.getLots().stream()
                        .filter(candidate -> candidate.name().equals(activatedLot.name()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Missing natal lot " + activatedLot.name()));
                String key = TransitNatalTargetType.LOT + ":" + lot.name();
                targets.putIfAbsent(key, new LunarNatalTarget(
                        TransitNatalTargetType.LOT,
                        lot.name(),
                        lot.longitude(),
                        lot.sign(),
                        lot.degreeInSign(),
                        lot.house()
                ));
            });
        }
        return List.copyOf(targets.values());
    }

    private List<TransitSearchWindow> buildZoomTransitWindows(Subject subject,
                                                              NatalChart chart,
                                                              OffsetDateTime focusDateTime,
                                                              OffsetDateTime windowStart,
                                                              OffsetDateTime windowEnd,
                                                              AnnualProfectionTable annualProfections,
                                                              MonthlyProfectionTable monthlyProfections,
                                                              FirdariaTable firdaria,
                                                              DecennialTable decennials,
                                                              DistributionThroughBoundsTable distribution,
                                                              List<DistributionThroughBoundsTable> extendedDistributions) {
        AnnualProfectionTableRow annual = activeAnnual(annualProfections);
        MonthlyProfectionTableRow monthly = activeMonthly(monthlyProfections);
        if (annual == null || monthly == null) {
            return List.of();
        }

        AnnualProfectionReferenceEntry annualAsc = annualEntry(annual, AnnualProfectionReference.ASCENDANT);
        MonthlyProfectionReferenceEntry monthlyAsc = monthlyEntry(monthly, AnnualProfectionReference.ASCENDANT);
        Set<PointKey> requiredPointTargets = requiredPointTargets(
                annualAsc,
                monthlyAsc,
                firdaria,
                decennials,
                distribution,
                extendedDistributions
        );
        List<ZoomNatalTarget> targets = selectedTargets(chart, annualAsc, monthlyAsc, requiredPointTargets);
        Map<PointKey, Double> focusTransitLongitudes = focusTransitLongitudes(subject, focusDateTime);

        List<TransitWindowCandidate> candidates = new ArrayList<>();
        for (PointKey transitPoint : TRANSIT_POINT_ORDER) {
            boolean transitLordOfYear = transitPoint == PointKey.of(annualAsc.lord());
            boolean transitLordOfMonth = transitPoint == PointKey.of(monthlyAsc.lord());
            for (ZoomNatalTarget target : targets) {
                int weight = target.weight() + (transitLordOfYear ? 2 : 0) + (transitLordOfMonth ? 2 : 0);
                List<MonthlyTransitActivationReason> reasons = new ArrayList<>(target.reasons());
                if (transitLordOfYear) {
                    reasons.add(MonthlyTransitActivationReason.TRANSIT_LORD_OF_YEAR);
                }
                if (transitLordOfMonth) {
                    reasons.add(MonthlyTransitActivationReason.TRANSIT_LORD_OF_MONTH);
                }
                for (AspectType aspect : TRANSIT_ASPECT_ORDER) {
                    double angularSeparation = AstroMath.rawAngularSeparation(focusTransitLongitudes.get(transitPoint), target.longitude());
                    double focusOrb = Math.abs(angularSeparation - aspect.getExactAngle());
                    candidates.add(new TransitWindowCandidate(
                            transitPoint,
                            transitLordOfYear,
                            transitLordOfMonth,
                            target,
                            aspect,
                            angularSeparation,
                            focusOrb,
                            List.copyOf(reasons),
                            Math.max(1, weight)
                    ));
                }
            }
        }

        List<TransitWindowCandidate> retained = candidates.stream()
                .sorted(Comparator
                        .comparingInt(TransitWindowCandidate::weight).reversed()
                        .thenComparingDouble(TransitWindowCandidate::focusOrb)
                        .thenComparing(candidate -> candidate.transitPoint().ordinal())
                        .thenComparing(candidate -> candidate.target().name())
                        .thenComparing(candidate -> candidate.aspect().ordinal()))
                .limit(MAX_TRANSIT_WINDOWS)
                .toList();

        List<TransitSearchWindow> windows = new ArrayList<>();
        for (int i = 0; i < retained.size(); i++) {
            TransitWindowCandidate candidate = retained.get(i);
            ZoomNatalTarget target = candidate.target();
            windows.add(new TransitSearchWindow(
                    i + 1,
                    "ZOOM_DATE_ACTIVE_TIMING_FILTER",
                    "LOCAL_ZOOM_TRANSIT_WINDOWS_V1",
                    1,
                    focusDateTime,
                    windowStart,
                    windowEnd,
                    candidate.transitPoint(),
                    candidate.transitLordOfYear(),
                    candidate.transitLordOfMonth(),
                    target.type(),
                    target.name(),
                    target.longitude(),
                    target.sign(),
                    target.degreeInSign(),
                    target.house(),
                    candidate.aspect(),
                    candidate.angularSeparation(),
                    candidate.focusOrb(),
                    candidate.reasons(),
                    candidate.weight()
            ));
        }
        return List.copyOf(windows);
    }

    private List<ZoomNatalTarget> selectedTargets(NatalChart chart,
                                                  AnnualProfectionReferenceEntry annualAsc,
                                                  MonthlyProfectionReferenceEntry monthlyAsc,
                                                  Set<PointKey> requiredPointTargets) {
        List<ZoomNatalTarget> targets = new ArrayList<>();
        for (Map.Entry<PointKey, PointEntry> entry : chart.getPoints().entrySet()) {
            PointPlacement placement = pointPlacement(chart, entry.getKey(), entry.getValue());
            List<MonthlyTransitActivationReason> reasons = targetReasons(
                    TransitNatalTargetType.POINT,
                    entry.getKey().name(),
                    placement.sign(),
                    placement.house(),
                    annualAsc,
                    monthlyAsc
            );
            boolean core = CORE_POINT_TARGETS.contains(entry.getKey());
            boolean required = requiredPointTargets.contains(entry.getKey());
            if (core || required || !reasons.isEmpty()) {
                int weight = 1 + reasons.size() + (core ? 1 : 0) + (required ? 2 : 0);
                targets.add(new ZoomNatalTarget(
                        TransitNatalTargetType.POINT,
                        entry.getKey().name(),
                        placement.longitude(),
                        placement.sign(),
                        placement.degreeInSign(),
                        placement.house(),
                        List.copyOf(reasons),
                        weight
                ));
            }
        }
        if (chart.getLots() != null) {
            for (LotEntry lot : chart.getLots()) {
                List<MonthlyTransitActivationReason> reasons = targetReasons(
                        TransitNatalTargetType.LOT,
                        lot.name(),
                        lot.sign(),
                        lot.house(),
                        annualAsc,
                        monthlyAsc
                );
                boolean core = CORE_LOT_TARGETS.contains(lot.name());
                if (core || !reasons.isEmpty()) {
                    int weight = 1 + reasons.size() + (core ? 1 : 0);
                    targets.add(new ZoomNatalTarget(
                            TransitNatalTargetType.LOT,
                            lot.name(),
                            lot.longitude(),
                            lot.sign(),
                            lot.degreeInSign(),
                            lot.house(),
                            List.copyOf(reasons),
                            weight
                    ));
                }
            }
        }
        return targets.stream()
                .sorted(Comparator
                        .comparingInt(ZoomNatalTarget::weight).reversed()
                        .thenComparing(target -> target.type().ordinal())
                        .thenComparing(ZoomNatalTarget::name))
                .toList();
    }

    private List<MonthlyTransitActivationReason> targetReasons(TransitNatalTargetType type,
                                                               String name,
                                                               ZodiacSign sign,
                                                               int house,
                                                               AnnualProfectionReferenceEntry annualAsc,
                                                               MonthlyProfectionReferenceEntry monthlyAsc) {
        List<MonthlyTransitActivationReason> reasons = new ArrayList<>();
        if (sign == annualAsc.profectedSign()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_ANNUAL_PROFECTED_SIGN);
        }
        if (house == annualAsc.profectedHouse()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_ANNUAL_PROFECTED_HOUSE);
        }
        if (sign == monthlyAsc.profectedSign()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_MONTHLY_PROFECTED_SIGN);
        }
        if (house == monthlyAsc.profectedHouse()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_MONTHLY_PROFECTED_HOUSE);
        }
        if (type == TransitNatalTargetType.POINT && name.equals(PointKey.of(annualAsc.lord()).name())) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LORD_OF_YEAR_NATAL_POINT);
        }
        if (type == TransitNatalTargetType.POINT && name.equals(PointKey.of(monthlyAsc.lord()).name())) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LORD_OF_MONTH_NATAL_POINT);
        }
        if (type == TransitNatalTargetType.LOT && sign == annualAsc.profectedSign()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LOT_IN_ANNUAL_PROFECTED_SIGN);
        }
        if (type == TransitNatalTargetType.LOT && sign == monthlyAsc.profectedSign()) {
            reasons.add(MonthlyTransitActivationReason.TARGET_LOT_IN_MONTHLY_PROFECTED_SIGN);
        }
        return deduplicateReasons(reasons);
    }

    private List<MonthlyTransitActivationReason> deduplicateReasons(List<MonthlyTransitActivationReason> reasons) {
        return new ArrayList<>(new LinkedHashSet<>(reasons));
    }

    private PointPlacement pointPlacement(NatalChart chart, PointKey key, PointEntry entry) {
        if (entry instanceof PlanetPointEntry planetPoint) {
            return new PointPlacement(
                    planetPoint.longitude(),
                    planetPoint.sign(),
                    planetPoint.degreeInSign(),
                    planetPoint.house()
            );
        }
        if (entry instanceof AnglePointEntry anglePoint) {
            return new PointPlacement(
                    anglePoint.longitude(),
                    anglePoint.sign(),
                    anglePoint.degreeInSign(),
                    houseForSign(chart, anglePoint.sign())
            );
        }
        throw new IllegalArgumentException("Unsupported point entry " + key);
    }

    private Set<PointKey> requiredPointTargets(AnnualProfectionReferenceEntry annualAsc,
                                               MonthlyProfectionReferenceEntry monthlyAsc,
                                               FirdariaTable firdaria,
                                               DecennialTable decennials,
                                               DistributionThroughBoundsTable distribution,
                                               List<DistributionThroughBoundsTable> extendedDistributions) {
        Set<PointKey> targets = new LinkedHashSet<>();
        targets.add(PointKey.of(annualAsc.lord()));
        targets.add(PointKey.of(monthlyAsc.lord()));
        addFirdariaTargets(targets, firdaria);
        addDecennialTargets(targets, decennials);
        addDistributionTarget(targets, distribution);
        if (extendedDistributions != null) {
            for (DistributionThroughBoundsTable table : extendedDistributions) {
                addDistributionTarget(targets, table);
            }
        }
        return targets;
    }

    private void addFirdariaTargets(Set<PointKey> targets, FirdariaTable table) {
        FirdariaPeriod period = activeFirdaria(table);
        if (period == null) {
            return;
        }
        targets.add(PointKey.of(period.ruler()));
        FirdariaSubperiod subperiod = period.subperiods().stream()
                .filter(FirdariaSubperiod::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (subperiod != null) {
            targets.add(PointKey.of(subperiod.partner()));
        }
    }

    private void addDecennialTargets(Set<PointKey> targets, DecennialTable table) {
        DecennialPeriod period = activeDecennial(table);
        if (period == null) {
            return;
        }
        targets.add(PointKey.of(period.ruler()));
        DecennialSubperiod subperiod = period.subperiods().stream()
                .filter(DecennialSubperiod::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (subperiod != null) {
            targets.add(PointKey.of(subperiod.partner()));
        }
    }

    private void addDistributionTarget(Set<PointKey> targets, DistributionThroughBoundsTable table) {
        DistributionThroughBoundsPeriod period = activeDistribution(table);
        if (period != null) {
            targets.add(PointKey.of(period.boundRuler()));
        }
    }

    private Map<PointKey, Double> focusTransitLongitudes(Subject subject, OffsetDateTime focusDateTime) {
        Subject transitSubject = new Subject(
                subject.getId() + "-zoom-transits-" + focusDateTime.toLocalDate(),
                focusDateTime,
                focusDateTime.toInstant(),
                subject.getLatitude(),
                subject.getLongitude()
        );
        NatalChart transitChart = new BasicCalculator().calculate(new CalculationContext(transitSubject, ZOOM_CONVENTIONS));
        Map<PointKey, Double> longitudes = new EnumMap<>(PointKey.class);
        for (PointKey point : TRANSIT_POINT_ORDER) {
            PointEntry entry = transitChart.getPoints().get(point);
            if (!(entry instanceof PlanetPointEntry planetPoint)) {
                throw new IllegalArgumentException("Missing transit point " + point);
            }
            longitudes.put(point, planetPoint.longitude());
        }
        return longitudes;
    }

    private AnnualProfectionTableRow activeAnnual(AnnualProfectionTable table) {
        return table.rows().stream().filter(AnnualProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private MonthlyProfectionTableRow activeMonthly(MonthlyProfectionTable table) {
        return table.rows().stream().filter(MonthlyProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private FirdariaPeriod activeFirdaria(FirdariaTable table) {
        return table == null ? null : table.periods().stream().filter(FirdariaPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private DecennialPeriod activeDecennial(DecennialTable table) {
        return table == null ? null : table.periods().stream().filter(DecennialPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private DistributionThroughBoundsPeriod activeDistribution(DistributionThroughBoundsTable table) {
        return table == null ? null : table.periods().stream().filter(DistributionThroughBoundsPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private AnnualProfectionReferenceEntry annualEntry(AnnualProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(entry -> entry.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing annual profection reference " + reference));
    }

    private MonthlyProfectionReferenceEntry monthlyEntry(MonthlyProfectionTableRow row, AnnualProfectionReference reference) {
        return row.referenceProfections().stream()
                .filter(entry -> entry.reference() == reference)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing monthly profection reference " + reference));
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(house -> house.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing house for sign " + sign));
    }

    private record PointPlacement(double longitude, ZodiacSign sign, double degreeInSign, int house) {}

    private record LunarNatalTarget(
            TransitNatalTargetType type,
            String name,
            double longitude,
            ZodiacSign sign,
            double degreeInSign,
            int house
    ) {}

    private record ZoomNatalTarget(
            TransitNatalTargetType type,
            String name,
            double longitude,
            ZodiacSign sign,
            double degreeInSign,
            int house,
            List<MonthlyTransitActivationReason> reasons,
            int weight
    ) {}

    private record TransitWindowCandidate(
            PointKey transitPoint,
            boolean transitLordOfYear,
            boolean transitLordOfMonth,
            ZoomNatalTarget target,
            AspectType aspect,
            double angularSeparation,
            double focusOrb,
            List<MonthlyTransitActivationReason> reasons,
            int weight
    ) {}
}
