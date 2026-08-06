package app.local;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.AppVersion;
import app.chart.ChartCalculator;
import app.chart.model.Subject;
import app.input.InquiryDateResolver;
import app.input.NatalInput;
import app.input.NatalInputLoader;
import app.input.SubjectFactory;
import app.io.MystroObjectMapper;
import app.reading.ReadingBundleReport;
import app.reading.description.NatalDescriptionReadingCalculator;
import app.reading.description.NatalDescriptionReadingReport;
import app.reading.description.common.model.LotEntry;
import app.reading.description.NatalChartCalculator;
import app.reading.lifearc.decennial.DecennialCalculator;
import app.reading.lifearc.decennial.DecennialTable;
import app.reading.lifearc.distribution.DistributionThroughBoundsCalculator;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.dorothean.calculator.DorotheanAnnualProfectionCalculator;
import app.reading.lifearc.dorothean.calculator.DorotheanMonthlyProfectionCalculator;
import app.reading.lifearc.firdaria.FirdariaCalculator;
import app.reading.lifearc.firdaria.FirdariaTable;
import app.reading.lifearc.lunar.LunarTimingCalculator;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;
import app.reading.lifearc.primarydirection.PrimaryDirectionCalculator;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;
import app.reading.lifearc.solarreturn.SolarReturnCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonCalculator;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnTable;
import app.reading.lifearc.synthesis.LifeArcSynthesisCalculator;
import app.reading.lifearc.synthesis.LifeArcSynthesisTable;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingCalculator;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

/**
 * Local-only reading dump harness.
 *
 * <p>
 * Run with:
 *
 * <pre>
 * mvn -Dtest=LocalReadingDumpRunner -Dlocal.reading=true -Dlocal.reading.alias=demo test
 * </pre>
 *
 * <p>
 * Default output directory: {@code output/<native-list-alias>/}
 * <p>
 * Default local output index: {@code output/<native-list-alias>/index.md}
 * <p>
 * Default natal-only JSON output: {@code output/<native-list-alias>/reading_output.json}
 * <p>
 * Default annual-profections Markdown output:
 * {@code output/<native-list-alias>/annual_profections.md}
 * <p>
 * Default monthly-profections Markdown output:
 * {@code output/<native-list-alias>/monthly_profections.md}
 * <p>
 * Default firdaria Markdown output: {@code output/<native-list-alias>/firdaria.md}
 * <p>
 * Default decennials Markdown output: {@code output/<native-list-alias>/decennials.md}
 * <p>
 * Default distributions-through-bounds Markdown output:
 * {@code output/<native-list-alias>/distributions_through_bounds.md}
 * <p>
 * Default extended distributions-through-bounds Markdown output:
 * {@code output/<native-list-alias>/distributions_extended.md}
 * <p>
 * Default primary-directions Markdown output:
 * {@code output/<native-list-alias>/primary_directions.md}
 * <p>
 * Default mundane/semi-arc primary-direction prototype Markdown output:
 * {@code output/<native-list-alias>/primary_directions_mundane.md}
 * <p>
 * Default lunar-timing overview Markdown output: {@code output/<native-list-alias>/lunar_timing.md}
 * <p>
 * Default lunar-timing eclipse tables Markdown output:
 * {@code output/<native-list-alias>/lunar_timing_eclipses.md}
 * <p>
 * Default lunar-timing full Markdown output:
 * {@code output/<native-list-alias>/lunar_timing_full.md}
 * <p>
 * Default solar-return Markdown output: {@code output/<native-list-alias>/solar_returns.md}
 * <p>
 * Default solar-return-to-natal comparison Markdown output:
 * {@code output/<native-list-alias>/solar_return_natal_comparison.md}
 * <p>
 * Default life-arc-synthesis Markdown output:
 * {@code output/<native-list-alias>/life_arc_synthesis.md}
 * <p>
 * Default topic-synthesis Markdown output directory: {@code output/<native-list-alias>/topics/}
 * <p>
 * Default AI brief Markdown output: {@code output/<native-list-alias>/life_arc_ai_brief.md}
 * <p>
 * Default Zodiacal Releasing L1 all-lots Markdown output:
 * {@code output/<native-list-alias>/zodiacal_releasing_l1_all_lots.md}
 * <p>
 * Default Zodiacal Releasing Markdown output directory:
 * {@code output/<native-list-alias>/zodiacal_releasing/}
 */
public final class LocalReadingDumpRunner {

    @Test
    void writesLocalReadingBundleToOutput() throws Exception {
        assumeTrue(Boolean.getBoolean("local.reading"),
                "Set -Dlocal.reading=true to run the local reading dump");

        String alias = System.getProperty("local.reading.alias");
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("Set -Dlocal.reading.alias=<native-list-alias> to run the local reading dump");
        }
        alias = alias.trim();
        if (alias.contains("/") || alias.contains("\\") || ".".equals(alias) || "..".equals(alias)
                || alias.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new IllegalArgumentException("local.reading.alias must be a native-list name, not a path: " + alias);
        }
        Path outputDir = Path.of("output", alias);
        Path outputIndex = Path.of(System.getProperty("local.outputIndex.output", outputDir.resolve("index.md").toString()));
        Path output = Path.of(System.getProperty("local.reading.output", outputDir.resolve("reading_output.json").toString()));
        Path annualProfectionsOutput = Path.of(System.getProperty("local.annualProfections.output", outputDir.resolve("annual_profections.md").toString()));
        Path monthlyProfectionsOutput = Path.of(System.getProperty("local.monthlyProfections.output", outputDir.resolve("monthly_profections.md").toString()));
        Path firdariaOutput = Path.of(System.getProperty("local.firdaria.output", outputDir.resolve("firdaria.md").toString()));
        Path decennialsOutput = Path.of(System.getProperty("local.decennials.output", outputDir.resolve("decennials.md").toString()));
        Path distributionsThroughBoundsOutput = Path.of(System.getProperty("local.distributionsThroughBounds.output", outputDir.resolve("distributions_through_bounds.md").toString()));
        Path distributionsExtendedOutput = Path.of(System.getProperty("local.distributionsExtended.output", outputDir.resolve("distributions_extended.md").toString()));
        Path primaryDirectionsOutput = Path.of(System.getProperty("local.primaryDirections.output", outputDir.resolve("primary_directions.md").toString()));
        Path mundanePrimaryDirectionsOutput = Path.of(System.getProperty("local.primaryDirectionsMundane.output", outputDir.resolve("primary_directions_mundane.md").toString()));
        Path lunarTimingOutput = Path.of(System.getProperty("local.lunarTiming.output", outputDir.resolve("lunar_timing.md").toString()));
        Path lunarTimingEclipsesOutput = Path.of(System.getProperty("local.lunarTimingEclipses.output", outputDir.resolve("lunar_timing_eclipses.md").toString()));
        Path lunarTimingFullOutput = Path.of(System.getProperty("local.lunarTimingFull.output", outputDir.resolve("lunar_timing_full.md").toString()));
        Path solarReturnsOutput = Path.of(System.getProperty("local.solarReturns.output", outputDir.resolve("solar_returns.md").toString()));
        Path solarReturnNatalComparisonOutput = Path.of(System.getProperty("local.solarReturnNatalComparison.output", outputDir.resolve("solar_return_natal_comparison.md").toString()));
        Path zodiacalReleasingL1AllLotsOutput = Path.of(System.getProperty("local.zodiacalReleasingL1AllLots.output", outputDir.resolve("zodiacal_releasing_l1_all_lots.md").toString()));
        Path lifeArcSynthesisOutput = Path.of(System.getProperty("local.lifeArcSynthesis.output", outputDir.resolve("life_arc_synthesis.md").toString()));
        Path topicSynthesisOutputDir = Path.of(System.getProperty("local.topicSynthesis.outputDir", outputDir.resolve("topics").toString()));
        Path lifeArcAiBriefOutput = Path.of(System.getProperty("local.lifeArcAiBrief.output", outputDir.resolve("life_arc_ai_brief.md").toString()));
        Path zodiacalReleasingOutputDir = Path.of(System.getProperty("local.zodiacalReleasing.outputDir", outputDir.resolve("zodiacal_releasing").toString()));

        ObjectMapper objectMapper = MystroObjectMapper.create();
        NatalInput request = new NatalInputLoader().load(alias);

        NatalDescriptionReadingCalculator natalDescriptionReadingCalculator = new NatalDescriptionReadingCalculator(
                new ChartCalculator(),
                new NatalChartCalculator());
        Subject subject = new SubjectFactory().create(request);
        LocalDate inquiryDate = new InquiryDateResolver().resolve(
                request.inquiryDate(),
                subject.getUtcBirthDateTime().toLocalDate());
        NatalDescriptionReadingReport natalDescription = natalDescriptionReadingCalculator.calculate(subject);

        ReadingBundleReport report = new ReadingBundleReport(AppVersion.get(), subject, List.of(natalDescription));

        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report) + System.lineSeparator());
        System.out.println("Wrote Mystro local natal-only reading bundle to " + output.toAbsolutePath());

        AnnualProfectionTable annualProfectionTable = new DorotheanAnnualProfectionCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path annualProfectionsParent = annualProfectionsOutput.getParent();
        if (annualProfectionsParent != null) {
            Files.createDirectories(annualProfectionsParent);
        }
        String markdown = new AnnualProfectionMarkdownRenderer().render(subject, inquiryDate, annualProfectionTable);
        Files.writeString(annualProfectionsOutput, markdown);
        System.out.println("Wrote annual profections table to " + annualProfectionsOutput.toAbsolutePath());

        MonthlyProfectionTable monthlyProfectionTable = writeMonthlyProfectionFile(subject, natalDescription, inquiryDate, monthlyProfectionsOutput);
        FirdariaTable firdariaTable = writeFirdariaFile(subject, natalDescription, inquiryDate, firdariaOutput);
        DecennialTable decennialTable = writeDecennialFile(subject, natalDescription, inquiryDate, decennialsOutput);
        DistributionThroughBoundsTable distributionsThroughBoundsTable = writeDistributionsThroughBoundsFile(subject, natalDescription, inquiryDate, distributionsThroughBoundsOutput);
        List<DistributionThroughBoundsTable> distributionsExtendedTables = writeExtendedDistributionsThroughBoundsFile(subject, natalDescription, inquiryDate, distributionsExtendedOutput);
        PrimaryDirectionTable primaryDirectionTable = writePrimaryDirectionsFile(subject, natalDescription, inquiryDate, primaryDirectionsOutput);
        PrimaryDirectionTable primaryDirectionVariantTable = calculatePrimaryDirectionVariantTable(subject, natalDescription, inquiryDate);
        MundanePrimaryDirectionTable mundanePrimaryDirectionTable = writeMundanePrimaryDirectionsFile(subject, natalDescription, inquiryDate, mundanePrimaryDirectionsOutput);
        LunarTimingTable lunarTimingTable = writeLunarTimingFile(subject, natalDescription, inquiryDate, lunarTimingOutput, lunarTimingEclipsesOutput, lunarTimingFullOutput);
        SolarReturnTable solarReturnTable = writeSolarReturnFile(subject, natalDescription, inquiryDate, solarReturnsOutput);
        SolarReturnNatalComparisonTable solarReturnNatalComparisonTable = writeSolarReturnNatalComparisonFile(subject, natalDescription, inquiryDate, solarReturnTable, solarReturnNatalComparisonOutput);
        LifeArcSynthesisTable lifeArcSynthesisTable = writeLifeArcSynthesisFile(subject, natalDescription, inquiryDate, lifeArcSynthesisOutput);
        writeTopicSynthesisFiles(subject, natalDescription, inquiryDate, lifeArcSynthesisTable, topicSynthesisOutputDir);
        List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> zodiacalReleasingBriefs = writeZodiacalReleasingFiles(subject, natalDescription, inquiryDate, zodiacalReleasingOutputDir, zodiacalReleasingL1AllLotsOutput);
        writeLifeArcAiBriefFile(
                subject,
                natalDescription,
                inquiryDate,
                lifeArcAiBriefOutput,
                outputIndex,
                output,
                annualProfectionsOutput,
                monthlyProfectionsOutput,
                firdariaOutput,
                decennialsOutput,
                distributionsThroughBoundsOutput,
                distributionsExtendedOutput,
                primaryDirectionsOutput,
                mundanePrimaryDirectionsOutput,
                lunarTimingOutput,
                lunarTimingEclipsesOutput,
                lunarTimingFullOutput,
                solarReturnsOutput,
                solarReturnNatalComparisonOutput,
                zodiacalReleasingL1AllLotsOutput,
                lifeArcSynthesisOutput,
                topicSynthesisOutputDir,
                zodiacalReleasingOutputDir,
                annualProfectionTable,
                monthlyProfectionTable,
                firdariaTable,
                decennialTable,
                distributionsThroughBoundsTable,
                distributionsExtendedTables,
                primaryDirectionTable,
                primaryDirectionVariantTable,
                mundanePrimaryDirectionTable,
                lunarTimingTable,
                solarReturnTable,
                solarReturnNatalComparisonTable,
                lifeArcSynthesisTable,
                zodiacalReleasingBriefs);
        writeOutputIndexFile(
                subject,
                inquiryDate,
                outputIndex,
                lifeArcAiBriefOutput,
                output,
                annualProfectionsOutput,
                monthlyProfectionsOutput,
                firdariaOutput,
                decennialsOutput,
                distributionsThroughBoundsOutput,
                distributionsExtendedOutput,
                primaryDirectionsOutput,
                mundanePrimaryDirectionsOutput,
                lunarTimingOutput,
                lunarTimingEclipsesOutput,
                lunarTimingFullOutput,
                solarReturnsOutput,
                solarReturnNatalComparisonOutput,
                zodiacalReleasingL1AllLotsOutput,
                lifeArcSynthesisOutput,
                topicSynthesisOutputDir,
                zodiacalReleasingOutputDir);
    }

    private MonthlyProfectionTable writeMonthlyProfectionFile(Subject subject, NatalDescriptionReadingReport natalDescription, LocalDate inquiryDate, Path output) throws Exception {
        MonthlyProfectionTable table = new DorotheanMonthlyProfectionCalculator().calculateTable(subject, natalDescription.getNatalChart(), inquiryDate, 0, 100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new MonthlyProfectionMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote monthly profections table to " + output.toAbsolutePath());
        return table;
    }

    private FirdariaTable writeFirdariaFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        FirdariaTable table = new FirdariaCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new FirdariaMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote firdaria table to " + output.toAbsolutePath());
        return table;
    }

    private DecennialTable writeDecennialFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        DecennialTable table = new DecennialCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new DecennialMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote decennials table to " + output.toAbsolutePath());
        return table;
    }

    private DistributionThroughBoundsTable writeDistributionsThroughBoundsFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        DistributionThroughBoundsTable table = new DistributionThroughBoundsCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new DistributionThroughBoundsMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote distributions through bounds to " + output.toAbsolutePath());
        return table;
    }

    private List<DistributionThroughBoundsTable> writeExtendedDistributionsThroughBoundsFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        List<DistributionThroughBoundsTable> tables = new DistributionThroughBoundsCalculator().calculateExtendedTables(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new DistributionThroughBoundsMarkdownRenderer().renderExtended(subject, inquiryDate, tables);
        Files.writeString(output, markdown);
        System.out.println("Wrote extended distributions through bounds to " + output.toAbsolutePath());
        return tables;
    }

    private PrimaryDirectionTable writePrimaryDirectionsFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        PrimaryDirectionTable table = new PrimaryDirectionCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new PrimaryDirectionMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote primary directions to " + output.toAbsolutePath());
        return table;
    }

    private PrimaryDirectionTable calculatePrimaryDirectionVariantTable(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate) {
        return new PrimaryDirectionCalculator().calculateDirectConverseTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
    }

    private MundanePrimaryDirectionTable writeMundanePrimaryDirectionsFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        MundanePrimaryDirectionTable table = new MundanePrimaryDirectionCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new MundanePrimaryDirectionMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote mundane/semi-arc primary direction prototype to " + output.toAbsolutePath());
        return table;
    }

    private LunarTimingTable writeLunarTimingFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output,
            Path eclipsesOutput,
            Path fullOutput) throws Exception {
        LunarTimingTable table = new LunarTimingCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path eclipsesParent = eclipsesOutput.getParent();
        if (eclipsesParent != null) {
            Files.createDirectories(eclipsesParent);
        }
        Path fullParent = fullOutput.getParent();
        if (fullParent != null) {
            Files.createDirectories(fullParent);
        }
        LunarTimingMarkdownRenderer renderer = new LunarTimingMarkdownRenderer();
        Files.writeString(output, renderer.render(subject, inquiryDate, table));
        Files.writeString(eclipsesOutput, renderer.renderEclipseTables(subject, inquiryDate, table));
        Files.writeString(fullOutput, renderer.renderFullTables(subject, inquiryDate, table));
        System.out.println("Wrote lunar timing overview to " + output.toAbsolutePath());
        System.out.println("Wrote lunar timing eclipse tables to " + eclipsesOutput.toAbsolutePath());
        System.out.println("Wrote lunar timing full tables to " + fullOutput.toAbsolutePath());
        return table;
    }

    private SolarReturnTable writeSolarReturnFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        SolarReturnTable table = new SolarReturnCalculator().calculateTable(
                subject,
                natalDescription.getNatalChart(),
                0,
                100);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new SolarReturnMarkdownRenderer().render(subject, table, activeDateTime(subject, inquiryDate));
        Files.writeString(output, markdown);
        System.out.println("Wrote solar returns table to " + output.toAbsolutePath());
        return table;
    }

    private SolarReturnNatalComparisonTable writeSolarReturnNatalComparisonFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            SolarReturnTable solarReturnTable,
            Path output) throws Exception {
        SolarReturnNatalComparisonTable table = new SolarReturnNatalComparisonCalculator().calculate(
                subject,
                natalDescription.getNatalChart(),
                solarReturnTable,
                inquiryDate);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new SolarReturnNatalComparisonMarkdownRenderer().render(subject, inquiryDate, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote solar return to natal comparison to " + output.toAbsolutePath());
        return table;
    }

    private LifeArcSynthesisTable writeLifeArcSynthesisFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output) throws Exception {
        if (inquiryDate == null) {
            return null;
        }
        LifeArcSynthesisTable table = new LifeArcSynthesisCalculator().calculate(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String markdown = new LifeArcSynthesisMarkdownRenderer().render(subject, table);
        Files.writeString(output, markdown);
        System.out.println("Wrote life-arc synthesis to " + output.toAbsolutePath());
        return table;
    }

    private void writeTopicSynthesisFiles(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            LifeArcSynthesisTable lifeArcSynthesisTable,
            Path outputDir) throws Exception {
        if (inquiryDate == null) {
            return;
        }
        Files.createDirectories(outputDir);
        TopicSynthesisMarkdownRenderer renderer = new TopicSynthesisMarkdownRenderer();
        List<TopicSynthesisMarkdownRenderer.TopicPacket> packets = renderer.packets(natalDescription.getNatalChart(), lifeArcSynthesisTable);
        for (TopicSynthesisMarkdownRenderer.TopicPacket packet : packets) {
            Files.writeString(outputDir.resolve(packet.bucket().fileName()), renderer.renderTopic(subject, inquiryDate, packet));
        }
        Files.writeString(outputDir.resolve("index.md"), renderer.renderIndex(subject, inquiryDate, outputDir, packets));
        System.out.println("Wrote topic synthesis packets to " + outputDir.toAbsolutePath());
    }

    private void writeLifeArcAiBriefFile(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path output,
            Path outputIndexOutput,
            Path readingBundleOutput,
            Path annualProfectionsOutput,
            Path monthlyProfectionsOutput,
            Path firdariaOutput,
            Path decennialsOutput,
            Path distributionsThroughBoundsOutput,
            Path distributionsExtendedOutput,
            Path primaryDirectionsOutput,
            Path mundanePrimaryDirectionsOutput,
            Path lunarTimingOutput,
            Path lunarTimingEclipsesOutput,
            Path lunarTimingFullOutput,
            Path solarReturnsOutput,
            Path solarReturnNatalComparisonOutput,
            Path zodiacalReleasingL1AllLotsOutput,
            Path lifeArcSynthesisOutput,
            Path topicSynthesisOutputDir,
            Path zodiacalReleasingOutputDir,
            AnnualProfectionTable annualProfectionTable,
            MonthlyProfectionTable monthlyProfectionTable,
            FirdariaTable firdariaTable,
            DecennialTable decennialTable,
            DistributionThroughBoundsTable distributionsThroughBoundsTable,
            List<DistributionThroughBoundsTable> distributionsExtendedTables,
            PrimaryDirectionTable primaryDirectionTable,
            PrimaryDirectionTable primaryDirectionVariantTable,
            MundanePrimaryDirectionTable mundanePrimaryDirectionTable,
            LunarTimingTable lunarTimingTable,
            SolarReturnTable solarReturnTable,
            SolarReturnNatalComparisonTable solarReturnNatalComparisonTable,
            LifeArcSynthesisTable lifeArcSynthesisTable,
            List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> zodiacalReleasingBriefs) throws Exception {
        if (inquiryDate == null) {
            return;
        }
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<LifeArcAiBriefMarkdownRenderer.FileReference> fileReferences = lifeArcFileReferences(
                true,
                outputIndexOutput,
                readingBundleOutput,
                annualProfectionsOutput,
                monthlyProfectionsOutput,
                firdariaOutput,
                decennialsOutput,
                distributionsThroughBoundsOutput,
                distributionsExtendedOutput,
                primaryDirectionsOutput,
                mundanePrimaryDirectionsOutput,
                lunarTimingOutput,
                lunarTimingEclipsesOutput,
                lunarTimingFullOutput,
                solarReturnsOutput,
                solarReturnNatalComparisonOutput,
                zodiacalReleasingL1AllLotsOutput,
                lifeArcSynthesisOutput,
                topicSynthesisOutputDir,
                zodiacalReleasingOutputDir);
        String markdown = new LifeArcAiBriefMarkdownRenderer().render(
                subject,
                natalDescription.getNatalChart(),
                inquiryDate,
                output,
                fileReferences,
                annualProfectionTable,
                monthlyProfectionTable,
                firdariaTable,
                decennialTable,
                distributionsThroughBoundsTable,
                distributionsExtendedTables,
                primaryDirectionTable,
                primaryDirectionVariantTable,
                mundanePrimaryDirectionTable,
                lunarTimingTable,
                solarReturnTable,
                solarReturnNatalComparisonTable,
                lifeArcSynthesisTable,
                zodiacalReleasingBriefs);
        Files.writeString(output, markdown);
        System.out.println("Wrote life-arc AI brief to " + output.toAbsolutePath());
    }

    private void writeOutputIndexFile(Subject subject,
            LocalDate inquiryDate,
            Path outputIndex,
            Path lifeArcAiBriefOutput,
            Path readingBundleOutput,
            Path annualProfectionsOutput,
            Path monthlyProfectionsOutput,
            Path firdariaOutput,
            Path decennialsOutput,
            Path distributionsThroughBoundsOutput,
            Path distributionsExtendedOutput,
            Path primaryDirectionsOutput,
            Path mundanePrimaryDirectionsOutput,
            Path lunarTimingOutput,
            Path lunarTimingEclipsesOutput,
            Path lunarTimingFullOutput,
            Path solarReturnsOutput,
            Path solarReturnNatalComparisonOutput,
            Path zodiacalReleasingL1AllLotsOutput,
            Path lifeArcSynthesisOutput,
            Path topicSynthesisOutputDir,
            Path zodiacalReleasingOutputDir) throws Exception {
        Path parent = outputIndex.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<LifeArcAiBriefMarkdownRenderer.FileReference> fileReferences = lifeArcFileReferences(
                inquiryDate != null,
                outputIndex,
                readingBundleOutput,
                annualProfectionsOutput,
                monthlyProfectionsOutput,
                firdariaOutput,
                decennialsOutput,
                distributionsThroughBoundsOutput,
                distributionsExtendedOutput,
                primaryDirectionsOutput,
                mundanePrimaryDirectionsOutput,
                lunarTimingOutput,
                lunarTimingEclipsesOutput,
                lunarTimingFullOutput,
                solarReturnsOutput,
                solarReturnNatalComparisonOutput,
                zodiacalReleasingL1AllLotsOutput,
                lifeArcSynthesisOutput,
                topicSynthesisOutputDir,
                zodiacalReleasingOutputDir);
        List<LifeArcAiBriefMarkdownRenderer.FileReference> indexReferences = new ArrayList<>();
        if (inquiryDate != null) {
            indexReferences.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Life-arc AI brief", lifeArcAiBriefOutput, "Compact active-period orientation and recommended reading order"));
        }
        indexReferences.addAll(fileReferences);
        String markdown = new LocalOutputIndexMarkdownRenderer().render(subject, inquiryDate, outputIndex, indexReferences);
        Files.writeString(outputIndex, markdown);
        System.out.println("Wrote local output index to " + outputIndex.toAbsolutePath());
    }

    private List<LifeArcAiBriefMarkdownRenderer.FileReference> lifeArcFileReferences(boolean includeInquiryOutputs,
            Path outputIndexOutput,
            Path readingBundleOutput,
            Path annualProfectionsOutput,
            Path monthlyProfectionsOutput,
            Path firdariaOutput,
            Path decennialsOutput,
            Path distributionsThroughBoundsOutput,
            Path distributionsExtendedOutput,
            Path primaryDirectionsOutput,
            Path mundanePrimaryDirectionsOutput,
            Path lunarTimingOutput,
            Path lunarTimingEclipsesOutput,
            Path lunarTimingFullOutput,
            Path solarReturnsOutput,
            Path solarReturnNatalComparisonOutput,
            Path zodiacalReleasingL1AllLotsOutput,
            Path lifeArcSynthesisOutput,
            Path topicSynthesisOutputDir,
            Path zodiacalReleasingOutputDir) {
        List<LifeArcAiBriefMarkdownRenderer.FileReference> references = new ArrayList<>();
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Output index", outputIndexOutput, "Master local output map and large-file notes"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Reading bundle JSON", readingBundleOutput, "Natal promise and public reading-bundle context"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Annual profections", annualProfectionsOutput, "Lord of the Year and annual house/sign activation"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Monthly profections", monthlyProfectionsOutput, "Lord of the Month and monthly house/sign activation"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Firdaria", firdariaOutput, "Medieval main/partner chronocrator periods"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Decennials", decennialsOutput, "Normalized decennial main/partner chronocrator periods"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Zodiacal Releasing L1 all lots", zodiacalReleasingL1AllLotsOutput, "Compact all-lots L1 Zodiacal Releasing macro table"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Zodiacal Releasing index", zodiacalReleasingOutputDir.resolve("zr_index.md"), "Per-lot Zodiacal Releasing file index"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Distributions through bounds", distributionsThroughBoundsOutput, "Ascendant distribution bound lord and exact body/ray contacts"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Extended distributions through bounds", distributionsExtendedOutput, "Selected hyleg, MC, Fortune, Spirit, Sun, and Moon distribution bounds and contacts"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Primary directions", primaryDirectionsOutput, "0-100 normalized direct hyleg/angle direction contacts"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Mundane primary-direction prototype", mundanePrimaryDirectionsOutput, "Separate normalized mundane/semi-arc body-contact prototype; supporting evidence only"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Solar returns", solarReturnsOutput, "Exact annual solar-return charts"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Solar return to natal comparison", solarReturnNatalComparisonOutput, "0-100 SR overlays to natal houses, profection context, and conjunctions"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Lunar timing overview", lunarTimingOutput, "Lunar returns, lunations, and eclipse pointers across the configured age span"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Lunar timing eclipse tables", lunarTimingEclipsesOutput, "True global eclipses with local visibility plus mean-node eclipse candidates"));
        references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Lunar timing full tables", lunarTimingFullOutput, "Full 0-100 lunar-return and lunation tables"));
        if (includeInquiryOutputs) {
            references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Life-arc synthesis", lifeArcSynthesisOutput, "Grouped active evidence by repeated signs/houses/planets/points/lots/aspects"));
            references.add(new LifeArcAiBriefMarkdownRenderer.FileReference("Topic synthesis index", topicSynthesisOutputDir.resolve("index.md"), "Practical topic packets with natal promise refs plus active timing evidence"));
        }
        return List.copyOf(references);
    }

    private OffsetDateTime activeDateTime(Subject subject, LocalDate inquiryDate) {
        return inquiryDate == null
                ? null
                : OffsetDateTime.of(
                        inquiryDate,
                        subject.getUtcBirthDateTime().toLocalTime(),
                        subject.getUtcBirthDateTime().getOffset());
    }

    private List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> writeZodiacalReleasingFiles(Subject subject,
            NatalDescriptionReadingReport natalDescription,
            LocalDate inquiryDate,
            Path outputDir,
            Path l1AllLotsOutput) throws Exception {
        if (natalDescription.getNatalChart().getLots() == null || natalDescription.getNatalChart().getLots().isEmpty()) {
            return List.of();
        }
        Files.createDirectories(outputDir);

        OffsetDateTime activeDateTime = activeDateTime(subject, inquiryDate);
        OffsetDateTime endDateTime = subject.getUtcBirthDateTime().plusYears(100);
        ZodiacalReleasingCalculator calculator = new ZodiacalReleasingCalculator();
        ZodiacalReleasingMarkdownRenderer renderer = new ZodiacalReleasingMarkdownRenderer();

        List<String> indexRows = new ArrayList<>();
        List<LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief> briefs = new ArrayList<>();
        for (LotEntry lot : natalDescription.getNatalChart().getLots()) {
            ZodiacalReleasingTimeline timeline = calculator.calculate(lot.sign(), subject.getUtcBirthDateTime(), endDateTime);
            String fileName = "zr_" + lot.name().toLowerCase(Locale.ROOT) + ".md";
            Path file = outputDir.resolve(fileName);
            Files.writeString(file, renderer.render(subject, natalDescription.getNatalChart(), lot, timeline, activeDateTime));
            indexRows.add("| " + lot.name() + " | " + lot.displayName() + " | " + lot.sign() + " | " + lot.house() + " | " + lot.ruler() + " | [" + fileName + "](" + fileName + ") |");
            briefs.add(new LifeArcAiBriefMarkdownRenderer.ZodiacalReleasingBrief(lot, timeline, file));
        }

        StringBuilder index = new StringBuilder();
        index.append("# Zodiacal Releasing files\n\n");
        index.append("- Subject: `").append(subject.getId()).append("`\n");
        index.append("- Birth date/time (UTC): `").append(subject.getUtcBirthDateTime()).append("`\n");
        if (activeDateTime != null) {
            index.append("- Inquiry date/time: `").append(activeDateTime).append("`\n");
        }
        index.append("- Span: birth through the 100th birthday\n\n");
        index.append("| Lot | Display name | Start sign | House | Ruler | File |\n");
        index.append("|---|---|---|---:|---|---|\n");
        for (String row : indexRows) {
            index.append(row).append("\n");
        }
        Files.writeString(outputDir.resolve("zr_index.md"), index.toString());

        Path l1Parent = l1AllLotsOutput.getParent();
        if (l1Parent != null) {
            Files.createDirectories(l1Parent);
        }
        Files.writeString(l1AllLotsOutput, new ZodiacalReleasingL1AllLotsMarkdownRenderer().render(
                subject,
                natalDescription.getNatalChart(),
                briefs,
                activeDateTime));
        System.out.println("Wrote Zodiacal Releasing L1 all-lots file to " + l1AllLotsOutput.toAbsolutePath());
        System.out.println("Wrote Zodiacal Releasing files to " + outputDir.toAbsolutePath());
        return List.copyOf(briefs);
    }
}
