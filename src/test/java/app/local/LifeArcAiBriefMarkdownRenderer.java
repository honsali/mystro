package app.local;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.TraditionalTables;
import app.chart.data.AspectType;
import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;
import app.chart.model.HousePosition;
import app.chart.model.NatalChart;
import app.chart.model.Subject;
import app.reading.description.common.data.SyzygyType;
import app.reading.description.common.model.LotEntry;
import app.reading.lifearc.decennial.DecennialPeriod;
import app.reading.lifearc.decennial.DecennialRulerCondition;
import app.reading.lifearc.decennial.DecennialSubperiod;
import app.reading.lifearc.decennial.DecennialTable;
import app.reading.lifearc.distribution.DistributionContactType;
import app.reading.lifearc.distribution.DistributionThroughBoundsContact;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.firdaria.FirdariaPeriod;
import app.reading.lifearc.firdaria.FirdariaSubperiod;
import app.reading.lifearc.firdaria.FirdariaTable;
import app.reading.lifearc.lunar.EclipseCandidateType;
import app.reading.lifearc.lunar.EclipseEvent;
import app.reading.lifearc.lunar.LunarReturnEntry;
import app.reading.lifearc.lunar.LunarTimingTable;
import app.reading.lifearc.lunar.LunationEntry;
import app.reading.lifearc.model.AnnualProfectionReference;
import app.reading.lifearc.model.AnnualProfectionReferenceEntry;
import app.reading.lifearc.model.AnnualProfectionTable;
import app.reading.lifearc.model.AnnualProfectionTableRow;
import app.reading.lifearc.model.MonthlyProfectionReferenceEntry;
import app.reading.lifearc.model.MonthlyProfectionTable;
import app.reading.lifearc.model.MonthlyProfectionTableRow;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.MundanePrimaryDirectionTable;
import app.reading.lifearc.primarydirection.PrimaryDirectionContactType;
import app.reading.lifearc.primarydirection.PrimaryDirectionCoordinate;
import app.reading.lifearc.primarydirection.PrimaryDirectionEvent;
import app.reading.lifearc.primarydirection.PrimaryDirectionPolarity;
import app.reading.lifearc.primarydirection.PrimaryDirectionTable;
import app.reading.lifearc.solarreturn.SolarReturnEntry;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonRow;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnNatalContact;
import app.reading.lifearc.solarreturn.SolarReturnPointEntry;
import app.reading.lifearc.solarreturn.SolarReturnPointOverlay;
import app.reading.lifearc.solarreturn.SolarReturnTable;
import app.reading.lifearc.synthesis.LifeArcSynthesisGroup;
import app.reading.lifearc.synthesis.LifeArcSynthesisTable;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingMarker;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingPeriod;
import app.reading.lifearc.zodiacalreleasing.ZodiacalReleasingTimeline;

final class LifeArcAiBriefMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");
    private static final int TOP_GROUP_LIMIT = 20;
    private static final int DETAIL_LIMIT = 10;

    String render(Subject subject,
                  NatalChart chart,
                  LocalDate inquiryDate,
                  Path briefOutput,
                  List<FileReference> fileReferences,
                  AnnualProfectionTable annualProfections,
                  MonthlyProfectionTable monthlyProfections,
                  FirdariaTable firdaria,
                  DecennialTable decennials,
                  DistributionThroughBoundsTable distributions,
                  List<DistributionThroughBoundsTable> extendedDistributions,
                  PrimaryDirectionTable primaryDirections,
                  PrimaryDirectionTable primaryDirectionVariants,
                  MundanePrimaryDirectionTable mundanePrimaryDirections,
                  LunarTimingTable lunarTiming,
                  SolarReturnTable solarReturns,
                  SolarReturnNatalComparisonTable solarReturnNatalComparison,
                  LifeArcSynthesisTable synthesis,
                  List<ZodiacalReleasingBrief> zodiacalReleasing) {
        StringBuilder out = new StringBuilder();
        OffsetDateTime inquiryDateTime = inquiryDateTime(subject, inquiryDate);
        int completedAge = synthesis == null ? completedAgeYears(subject, inquiryDate) : synthesis.completedAgeYears();
        OffsetDateTime activeYearStart = synthesis == null && completedAge >= 0
                ? subject.getUtcBirthDateTime().plusYears(completedAge)
                : synthesis == null ? null : synthesis.activeYearStartDateTime();
        OffsetDateTime activeYearEnd = activeYearStart == null ? null : activeYearStart.plusYears(1);

        out.append("# Life-Arc AI Brief\n\n");
        appendSubjectSummary(out, subject, inquiryDate, inquiryDateTime, completedAge, activeYearStart, activeYearEnd, synthesis);
        appendCaveats(out);
        appendFileMap(out, briefOutput, fileReferences);
        appendRecommendedReadingOrder(out);
        appendTopEvidenceGroups(out, synthesis, briefOutput, fileReferences);
        appendProfections(out, annualProfections, monthlyProfections);
        appendFirdaria(out, firdaria);
        appendDecennials(out, decennials);
        appendZodiacalReleasing(out, chart, zodiacalReleasing, inquiryDateTime, briefOutput);
        appendDistributions(out, chart, distributions, extendedDistributions);
        appendPrimaryDirections(out, primaryDirections, primaryDirectionVariants, mundanePrimaryDirections);
        appendSolarReturns(out, solarReturns, solarReturnNatalComparison, inquiryDateTime);
        appendLunarTiming(out, lunarTiming, activeYearStart, activeYearEnd);
        return out.toString();
    }

    private void appendSubjectSummary(StringBuilder out,
                                      Subject subject,
                                      LocalDate inquiryDate,
                                      OffsetDateTime inquiryDateTime,
                                      int completedAge,
                                      OffsetDateTime activeYearStart,
                                      OffsetDateTime activeYearEnd,
                                      LifeArcSynthesisTable synthesis) {
        out.append("## Subject and inquiry\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Birth date/time (UTC): `").append(format(subject.getUtcBirthDateTime())).append("`\n");
        if (inquiryDate != null) {
            out.append("- Inquiry date: `").append(inquiryDate).append("`\n");
            out.append("- Inquiry date/time: `").append(format(inquiryDateTime)).append("`\n");
        }
        if (completedAge >= 0) {
            out.append("- Completed age at inquiry: `").append(completedAge).append("`\n");
        }
        if (activeYearStart != null && activeYearEnd != null) {
            out.append("- Active birthday-year window: `").append(format(activeYearStart)).append("` to `")
                    .append(format(activeYearEnd)).append("`\n");
        }
        if (synthesis != null) {
            out.append("- Synthesis evidence rows: `").append(synthesis.evidence().size()).append("`\n");
            out.append("- Synthesis evidence groups: `").append(synthesis.groups().size()).append("`\n");
        }
        out.append("\n");
    }

    private void appendCaveats(StringBuilder out) {
        out.append("## Caveats\n\n");
        out.append("- This is a local/research orientation file for AI analysis; the Java calculator records/classes remain the canonical calculation model.\n");
        out.append("- Treat every row as timing evidence. Do not read this brief, or any single technique file, as a standalone event claim or final narrative judgment.\n");
        out.append("- Use `reading_output.json` for natal promise/context, then use the life-arc files as activation evidence.\n");
        out.append("- Eclipse rows include Swiss Ephemeris true global eclipse events with magnitude/contact data and subject-location visibility when safely supported; mean-node proximity candidates remain supporting reference rows.\n");
        out.append("- Distributions and primary directions are explicitly normalized local/research variants; keep their method labels when citing them.\n\n");
    }

    private void appendFileMap(StringBuilder out, Path briefOutput, List<FileReference> fileReferences) {
        out.append("## File map\n\n");
        out.append("| Output | Link | Use first for |\n");
        out.append("|---|---|---|\n");
        for (FileReference ref : fileReferences) {
            out.append("| ").append(ref.label())
                    .append(" | ").append(fileLink(briefOutput, ref.path(), ref.label()))
                    .append(" | ").append(ref.purpose())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendRecommendedReadingOrder(StringBuilder out) {
        out.append("## Recommended AI reading order\n\n");
        out.append("1. Start with this `life_arc_ai_brief.md` file for orientation and the highest-density active evidence.\n");
        out.append("2. Open `reading_output.json` for natal promise, lots, house/topic scaffolding, hyleg/alcocoden, and Valens natal evidence.\n");
        out.append("3. Open `life_arc_synthesis.md` to audit the top repeated signs, houses, planets, points, lots, and aspects.\n");
        out.append("4. If the user asks a practical topic question, open `topics/index.md` and then the matching topic packet.\n");
        out.append("5. Check `annual_profections.md` and `monthly_profections.md` to anchor the Lord of the Year and Lord of the Month.\n");
        out.append("6. Review chronocrator context in `firdaria.md`, `decennials.md`, and `zodiacal_releasing/zr_index.md`.\n");
        out.append("7. Review the active solar-return files, then the distribution and primary-direction timing files, including the separately labelled mundane/semi-arc prototype only as supporting research evidence.\n");
        out.append("8. Use `lunar_timing.md` for shorter-period lunar corroboration; transits are emitted only by the separate bounded high-zoom pack.\n");
        out.append("9. Only open the large detailed tables as needed to verify a specific line of evidence.\n\n");
    }

    private void appendTopEvidenceGroups(StringBuilder out,
                                         LifeArcSynthesisTable synthesis,
                                         Path briefOutput,
                                         List<FileReference> fileReferences) {
        out.append("## Top evidence groups from life_arc_synthesis.md\n\n");
        Path synthesisPath = pathFor(fileReferences, "Life-arc synthesis");
        if (synthesisPath != null) {
            out.append("Source file: ").append(fileLink(briefOutput, synthesisPath, "life_arc_synthesis.md")).append(".\n\n");
        }
        if (synthesis == null || synthesis.groups().isEmpty()) {
            out.append("No synthesis groups were available for this brief.\n\n");
            return;
        }
        out.append("| Rank | Key type | Key | Weight | Evidence count | Evidence rows |\n");
        out.append("|---:|---|---|---:|---:|---|\n");
        int rank = 1;
        for (LifeArcSynthesisGroup group : synthesis.groups().stream().limit(TOP_GROUP_LIMIT).toList()) {
            out.append("| ").append(rank++)
                    .append(" | ").append(group.keyType())
                    .append(" | ").append(group.key())
                    .append(" | ").append(group.totalWeight())
                    .append(" | ").append(group.evidenceCount())
                    .append(" | ").append(group.evidenceSequenceIndexes().stream().map(index -> "#" + index).collect(Collectors.joining(", ")))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendProfections(StringBuilder out,
                                   AnnualProfectionTable annualProfections,
                                   MonthlyProfectionTable monthlyProfections) {
        out.append("## Active profections\n\n");
        AnnualProfectionTableRow annual = activeAnnual(annualProfections);
        if (annual == null) {
            out.append("No active annual profection row available.\n\n");
        } else {
            out.append("### Annual profection\n\n");
            out.append("- Active age/year: `").append(annual.ageYears()).append("`, cycle `")
                    .append(annual.cycleNumber()).append("`, year in cycle `").append(annual.yearInCycle()).append("`\n");
            out.append("- Active period: `").append(annual.periodStartDate()).append("` to `")
                    .append(annual.periodEndDateExclusive()).append("`\n\n");
            out.append("| Reference | Natal sign | Active house/sign | Lord |\n");
            out.append("|---|---|---|---|\n");
            for (AnnualProfectionReferenceEntry entry : annual.referenceProfections()) {
                out.append("| ").append(label(entry.reference()))
                        .append(" | ").append(entry.natalSign())
                        .append(" | H").append(entry.profectedHouse()).append(" ").append(entry.profectedSign())
                        .append(" | ").append(entry.lord())
                        .append(" |\n");
            }
            out.append("\n");
        }

        MonthlyProfectionTableRow monthly = activeMonthly(monthlyProfections);
        if (monthly == null) {
            out.append("No active monthly profection row available.\n\n");
            return;
        }
        out.append("### Monthly profection\n\n");
        out.append("- Active age/month: `").append(monthly.ageYears()).append("/M").append(monthly.monthInYear()).append("`\n");
        out.append("- Active period: `").append(format(monthly.periodStartDateTime())).append("` to `")
                .append(format(monthly.periodEndDateTimeExclusive())).append("`\n\n");
        out.append("| Reference | Natal sign | Annual sign | Active house/sign | Lord |\n");
        out.append("|---|---|---|---|---|\n");
        for (MonthlyProfectionReferenceEntry entry : monthly.referenceProfections()) {
            out.append("| ").append(label(entry.reference()))
                    .append(" | ").append(entry.natalSign())
                    .append(" | ").append(entry.annualSign())
                    .append(" | H").append(entry.profectedHouse()).append(" ").append(entry.profectedSign())
                    .append(" | ").append(entry.lord())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendFirdaria(StringBuilder out, FirdariaTable table) {
        out.append("## Active firdaria\n\n");
        if (table == null) {
            out.append("No firdaria table available.\n\n");
            return;
        }
        FirdariaPeriod active = table.periods().stream().filter(FirdariaPeriod::activeForInquiry).findFirst().orElse(null);
        if (active == null) {
            out.append("No firdaria period is marked active for the inquiry.\n\n");
            return;
        }
        FirdariaSubperiod sub = active.subperiods().stream().filter(FirdariaSubperiod::activeForInquiry).findFirst().orElse(null);
        out.append("| Layer | Ruler | Window |\n");
        out.append("|---|---|---|\n");
        out.append("| Main period | ").append(active.ruler())
                .append(" | ").append(format(active.startDateTime())).append(" → ").append(format(active.endDateTimeExclusive()))
                .append(" |\n");
        if (sub != null) {
            out.append("| Partner | ").append(sub.partner())
                    .append(" | ").append(format(sub.startDateTime())).append(" → ").append(format(sub.endDateTimeExclusive()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDecennials(StringBuilder out, DecennialTable table) {
        out.append("## Active decennial\n\n");
        if (table == null) {
            out.append("No decennial table available.\n\n");
            return;
        }
        DecennialPeriod active = table.periods().stream().filter(DecennialPeriod::activeForInquiry).findFirst().orElse(null);
        if (active == null) {
            out.append("No decennial period is marked active for the inquiry.\n\n");
            return;
        }
        DecennialSubperiod sub = active.subperiods().stream().filter(DecennialSubperiod::activeForInquiry).findFirst().orElse(null);
        out.append("| Layer | Ruler | Natal context | Window |\n");
        out.append("|---|---|---|---|\n");
        out.append("| Main period | ").append(active.ruler())
                .append(" | ").append(decennialContext(active.rulerNatalCondition()))
                .append(" | ").append(format(active.startDateTime())).append(" → ").append(format(active.endDateTimeExclusive()))
                .append(" |\n");
        if (sub != null) {
            out.append("| Partner | ").append(sub.partner())
                    .append(" | ").append(decennialContext(sub.partnerNatalCondition()))
                    .append(" | ").append(format(sub.startDateTime())).append(" → ").append(format(sub.endDateTimeExclusive()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendZodiacalReleasing(StringBuilder out,
                                         NatalChart chart,
                                         List<ZodiacalReleasingBrief> briefs,
                                         OffsetDateTime activeDateTime,
                                         Path briefOutput) {
        out.append("## Active Zodiacal Releasing chains\n\n");
        if (briefs == null || briefs.isEmpty() || activeDateTime == null) {
            out.append("No Zodiacal Releasing active-chain data available.\n\n");
            return;
        }
        out.append("| Lot | File | Source lot placement | Active chain |\n");
        out.append("|---|---|---|---|\n");
        for (ZodiacalReleasingBrief brief : briefs) {
            LotEntry lot = brief.lot();
            List<ZodiacalReleasingPeriod> chain = activeReleasingPath(brief.timeline().periods(), activeDateTime);
            out.append("| ").append(lot.name())
                    .append(" | ").append(fileLink(briefOutput, brief.file(), "zr_" + lot.name().toLowerCase(Locale.ROOT) + ".md"))
                    .append(" | ").append(placement(lot.sign(), lot.degreeInSign())).append(", H").append(lot.house()).append(", ruler ").append(lot.ruler())
                    .append(" | ").append(releasingChain(chart, chain))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendDistributions(StringBuilder out, NatalChart chart, DistributionThroughBoundsTable table,
                                     List<DistributionThroughBoundsTable> extendedTables) {
        out.append("## Active distributions through bounds\n\n");
        if (table == null) {
            out.append("No Ascendant distributions-through-bounds table available.\n\n");
        } else {
            DistributionThroughBoundsPeriod active = activeDistributionPeriod(table);
            if (active == null) {
                out.append("No Ascendant bound period is marked active for the inquiry.\n\n");
            } else {
                out.append("### Ascendant baseline\n\n");
                appendDistributionActiveTable(out, chart, List.of(table));
                appendDistributionContacts(out, "Top Ascendant active-bound contacts", active.contacts());
            }
        }

        out.append("### Extended directed-point distributions\n\n");
        if (extendedTables == null || extendedTables.isEmpty()) {
            out.append("No extended distribution tables were available.\n\n");
            return;
        }
        appendDistributionActiveTable(out, chart, extendedTables);
        List<DistributionThroughBoundsContact> topContacts = extendedTables.stream()
                .map(this::activeDistributionPeriod)
                .filter(java.util.Objects::nonNull)
                .flatMap(period -> period.contacts().stream())
                .sorted(Comparator.comparing(DistributionThroughBoundsContact::dateTime))
                .limit(DETAIL_LIMIT)
                .toList();
        appendDistributionContacts(out, "Top extended active-bound contacts", topContacts);
    }

    private void appendDistributionActiveTable(StringBuilder out, NatalChart chart, List<DistributionThroughBoundsTable> tables) {
        out.append("| Directed point | Active bound | Natal house | Window | Directed span | Contacts |\n");
        out.append("|---|---|---:|---|---|---:|\n");
        for (DistributionThroughBoundsTable table : tables) {
            DistributionThroughBoundsPeriod active = activeDistributionPeriod(table);
            if (active == null) {
                continue;
            }
            out.append("| ").append(table.directedPoint())
                    .append(" | ").append(active.sign()).append(" ").append(formatDecimal(active.boundStartDegreeInSign(), 2))
                    .append("–").append(formatDecimal(active.boundEndDegreeInSign(), 2)).append("° — ").append(active.boundRuler())
                    .append(" | H").append(houseForSign(chart, active.sign()))
                    .append(" | ").append(format(active.startDateTime())).append(" → ").append(format(active.endDateTimeExclusive()))
                    .append(" | ").append(placement(active.sign(), active.directedStartDegreeInSign())).append(" → ").append(placement(active.sign(), active.directedEndDegreeInSign()))
                    .append(" | ").append(active.contacts().size())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private DistributionThroughBoundsPeriod activeDistributionPeriod(DistributionThroughBoundsTable table) {
        return table.periods().stream()
                .filter(DistributionThroughBoundsPeriod::activeForInquiry)
                .findFirst()
                .orElse(null);
    }

    private void appendDistributionContacts(StringBuilder out, String heading, List<DistributionThroughBoundsContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            out.append("No exact body/ray contacts fall inside these active bound periods.\n\n");
            return;
        }
        out.append(heading).append(":\n\n");
        out.append("| Date | Age | Source planet | Contact | Directed placement | Bound lord |\n");
        out.append("|---|---:|---|---|---|---|\n");
        for (DistributionThroughBoundsContact contact : contacts.stream().limit(DETAIL_LIMIT).toList()) {
            out.append("| ").append(format(contact.dateTime()))
                    .append(" | ").append(formatDecimal(contact.ageYears(), 2))
                    .append(" | ").append(contact.sourcePlanet())
                    .append(" | ").append(distributionContact(contact))
                    .append(" | ").append(placement(contact.directedSign(), contact.directedDegreeInSign()))
                    .append(" | ").append(contact.boundRulerAtContact())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendPrimaryDirections(StringBuilder out,
                                          PrimaryDirectionTable directTable,
                                          PrimaryDirectionTable variantTable,
                                          MundanePrimaryDirectionTable mundaneTable) {
        out.append("## Primary directions in the active birthday year\n\n");
        out.append("Primary-direction variants are evidential timing rows only. The normalized direct zodiacal file remains the baseline; converse and mundane/semi-arc prototype rows should be cited with their method labels and treated as lower-confidence supporting evidence.\n\n");

        out.append("### Normalized zodiacal direct baseline\n\n");
        if (directTable == null) {
            out.append("No normalized direct primary-direction table available.\n\n");
        } else {
            appendZodiacalPrimaryDirectionEvents(out, directTable.events().stream()
                    .filter(PrimaryDirectionEvent::activeForInquiryYear)
                    .limit(DETAIL_LIMIT)
                    .toList());
        }

        out.append("### Normalized zodiacal converse variant\n\n");
        if (variantTable == null) {
            out.append("No normalized converse primary-direction variant table available.\n\n");
        } else {
            appendZodiacalPrimaryDirectionEvents(out, variantTable.events().stream()
                    .filter(PrimaryDirectionEvent::activeForInquiryYear)
                    .filter(event -> event.direction() == PrimaryDirectionPolarity.CONVERSE)
                    .limit(DETAIL_LIMIT)
                    .toList());
        }

        out.append("### Mundane/semi-arc prototype\n\n");
        if (mundaneTable == null) {
            out.append("No mundane/semi-arc primary-direction prototype table available.\n\n");
            return;
        }
        List<MundanePrimaryDirectionEvent> activeMundane = mundaneTable.events().stream()
                .filter(MundanePrimaryDirectionEvent::activeForInquiryYear)
                .limit(DETAIL_LIMIT)
                .toList();
        if (activeMundane.isEmpty()) {
            out.append("No exact mundane/semi-arc prototype contacts fall inside the active birthday-year window.\n\n");
            return;
        }
        out.append("| Date | Age | Significator | Promissor body | Target mundane position | Arc |\n");
        out.append("|---|---:|---|---|---|---:|\n");
        for (MundanePrimaryDirectionEvent event : activeMundane) {
            out.append("| ").append(format(event.dateTime()))
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(formatDecimal(event.targetMundanePositionDegrees(), 2)).append("° ").append(event.targetMundanePositionSegment())
                    .append(" | ").append(formatDecimal(event.arcDegrees(), 2)).append("°")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendZodiacalPrimaryDirectionEvents(StringBuilder out, List<PrimaryDirectionEvent> active) {
        if (active.isEmpty()) {
            out.append("No exact normalized zodiacal primary-direction contacts fall inside the active birthday-year window.\n\n");
            return;
        }
        out.append("| Date | Age | Direction | Significator | Coordinate | Promissor | Contact | Directed target |\n");
        out.append("|---|---:|---|---|---|---|---|---|\n");
        for (PrimaryDirectionEvent event : active) {
            out.append("| ").append(format(event.dateTime()))
                    .append(" | ").append(formatDecimal(event.ageYears(), 2))
                    .append(" | ").append(event.direction())
                    .append(" | ").append(event.significatorRole()).append(" / ").append(event.significatorPoint())
                    .append(" | ").append(coordinate(event.coordinate()))
                    .append(" | ").append(event.promissorPlanet())
                    .append(" | ").append(primaryDirectionContact(event))
                    .append(" | ").append(placement(event.targetSign(), event.targetDegreeInSign()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendSolarReturns(StringBuilder out,
                                    SolarReturnTable solarReturns,
                                    SolarReturnNatalComparisonTable comparison,
                                    OffsetDateTime activeDateTime) {
        out.append("## Active solar return\n\n");
        SolarReturnEntry activeReturn = solarReturns == null ? null : solarReturns.rows().stream()
                .filter(row -> active(row.returnDateTime(), row.periodEndDateTimeExclusive(), activeDateTime))
                .findFirst()
                .orElse(null);
        if (activeReturn == null) {
            out.append("No active solar-return row available.\n\n");
        } else {
            SolarReturnPointEntry sun = solarReturnPoint(activeReturn, PointKey.SUN);
            SolarReturnPointEntry moon = solarReturnPoint(activeReturn, PointKey.MOON);
            out.append("| Age | Period | SR Asc | SR MC | Sect | Sun house | Moon | Moon house |\n");
            out.append("|---:|---|---|---|---|---:|---|---:|\n");
            out.append("| ").append(activeReturn.ageYears())
                    .append(" | ").append(format(activeReturn.returnDateTime())).append(" → ").append(format(activeReturn.periodEndDateTimeExclusive()))
                    .append(" | ").append(placement(activeReturn.ascendantSign(), activeReturn.ascendantDegreeInSign()))
                    .append(" | ").append(placement(activeReturn.midheavenSign(), activeReturn.midheavenDegreeInSign()))
                    .append(" | ").append(activeReturn.sect())
                    .append(" | ").append(house(sun))
                    .append(" | ").append(placement(moon.sign(), moon.degreeInSign()))
                    .append(" | ").append(house(moon))
                    .append(" |\n\n");
        }

        out.append("### Active solar-return-to-natal comparison\n\n");
        SolarReturnNatalComparisonRow activeComparison = comparison == null ? null : comparison.rows().stream()
                .filter(SolarReturnNatalComparisonRow::activeForInquiry)
                .findFirst()
                .orElse(null);
        if (activeComparison == null) {
            out.append("No active solar-return-to-natal comparison row available.\n\n");
            return;
        }
        out.append("- Annual profection in active SR: `H").append(activeComparison.profectedHouse()).append(" ")
                .append(activeComparison.profectedSign()).append(" — ").append(activeComparison.lordOfYear()).append("`\n");
        out.append("- Lord of Year in solar return: `").append(lordOfYearOverlay(activeComparison.lordOfYearOverlay())).append("`\n");
        out.append("- SR Ascendant overlays natal house: `H").append(activeComparison.ascendantNatalHouseOverlay()).append("`\n");
        out.append("- SR Midheaven overlays natal house: `H").append(activeComparison.midheavenNatalHouseOverlay()).append("`\n");
        out.append("- SR points in profected sign: `").append(joinPointKeys(activeComparison.solarReturnPointsInProfectedSign())).append("`\n");
        out.append("- SR points overlaying profected house: `").append(joinPointKeys(activeComparison.solarReturnPointsOverlayingProfectedHouse())).append("`\n");
        List<SolarReturnNatalContact> tightContacts = activeComparison.conjunctions().stream()
                .filter(contact -> contact.orbDegrees() <= 1.0)
                .sorted(Comparator.comparingDouble(SolarReturnNatalContact::orbDegrees))
                .limit(DETAIL_LIMIT)
                .toList();
        if (tightContacts.isEmpty()) {
            out.append("- Tight SR-natal conjunctions ≤1°: `none`\n\n");
            return;
        }
        out.append("\nTight SR-natal conjunctions ≤1°:\n\n");
        out.append("| SR point | Natal target | Natal house | Orb |\n");
        out.append("|---|---|---:|---:|\n");
        for (SolarReturnNatalContact contact : tightContacts) {
            out.append("| ").append(contact.solarReturnPoint())
                    .append(" | ").append(contact.natalTargetName()).append(" ").append(placement(contact.natalTargetSign(), contact.natalTargetDegreeInSign()))
                    .append(" | H").append(contact.natalTargetHouse())
                    .append(" | ").append(formatDecimal(contact.orbDegrees(), 2)).append("°")
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendLunarTiming(StringBuilder out,
                                   LunarTimingTable table,
                                   OffsetDateTime activeYearStart,
                                   OffsetDateTime activeYearEnd) {
        out.append("## Active lunar timing\n\n");
        if (table == null) {
            out.append("No lunar-timing table available.\n\n");
            return;
        }
        LunarReturnEntry lunarReturn = table.lunarReturns().stream().filter(LunarReturnEntry::activeForInquiry).findFirst().orElse(null);
        LunationEntry lunation = table.lunations().stream().filter(LunationEntry::activeForInquiry).findFirst().orElse(null);
        out.append("| Layer | Date/window | Placement | Natal house | Node/eclipsing note |\n");
        out.append("|---|---|---|---:|---|\n");
        if (lunarReturn != null) {
            out.append("| Lunar return | ").append(format(lunarReturn.returnDateTime())).append(" → ").append(format(lunarReturn.periodEndDateTimeExclusive()))
                    .append(" | Moon ").append(placement(lunarReturn.moonSign(), lunarReturn.moonDegreeInSign()))
                    .append(" | H").append(lunarReturn.natalHouseOverlay())
                    .append(" | nearest ").append(node(lunarReturn.nearestNode())).append(" node, orb ").append(formatDecimal(lunarReturn.nearestNodeOrbDegrees(), 2)).append("°")
                    .append(" |\n");
        }
        if (lunation != null) {
            out.append("| Lunation | ").append(format(lunation.dateTime())).append(" → ").append(format(lunation.periodEndDateTimeExclusive()))
                    .append(" | ").append(lunationType(lunation.type())).append(" at ").append(placement(lunation.syzygySign(), lunation.syzygyDegreeInSign()))
                    .append(" | H").append(lunation.natalHouseOverlay())
                    .append(" | ").append(eclipse(lunation.eclipseType()))
                    .append(" |\n");
        }
        out.append("\n");

        List<EclipseEvent> activeYearTrueEclipses = table.eclipseEvents().stream()
                .filter(event -> activeYearStart != null && activeYearEnd != null
                        && !event.maximumDateTime().isBefore(activeYearStart)
                        && event.maximumDateTime().isBefore(activeYearEnd))
                .limit(DETAIL_LIMIT)
                .toList();
        if (!activeYearTrueEclipses.isEmpty()) {
            out.append("True eclipses in active birthday year with subject-location visibility:\n\n");
            out.append("| Maximum | Kind | Eclipse type | Syzygy | Natal house | Node orb | Magnitude | Candidate ref | Local visibility | Visible phases |\n");
            out.append("|---|---|---|---|---:|---:|---:|---|---|---|\n");
            for (EclipseEvent event : activeYearTrueEclipses) {
                out.append("| ").append(format(event.maximumDateTime()))
                        .append(" | ").append(event.kind())
                        .append(" | ").append(event.eclipseType())
                        .append(" | ").append(placement(event.syzygySign(), event.syzygyDegreeInSign()))
                        .append(" | H").append(event.natalHouseOverlay())
                        .append(" | ").append(formatDecimal(event.nearestNodeOrbDegrees(), 2)).append("°")
                        .append(" | ").append(formatOptional(event.magnitude()))
                        .append(" | ").append(eclipse(event.candidateReference()))
                        .append(" | ").append(event.visibility().localVisibility())
                        .append(" | ").append(visiblePhases(event))
                        .append(" |\n");
            }
            out.append("\n");
        }

        List<LunationEntry> activeYearCandidates = table.lunations().stream()
                .filter(row -> row.eclipseType() != EclipseCandidateType.NONE)
                .filter(row -> activeYearStart != null && activeYearEnd != null
                        && !row.dateTime().isBefore(activeYearStart)
                        && row.dateTime().isBefore(activeYearEnd))
                .limit(DETAIL_LIMIT)
                .toList();
        if (activeYearCandidates.isEmpty()) {
            out.append("No mean-node eclipse candidates fall inside the active birthday year.\n\n");
            return;
        }
        out.append("Mean-node eclipse candidates in active birthday year:\n\n");
        out.append("| Date | Type | Syzygy | Natal house | Node | Node orb | Candidate |\n");
        out.append("|---|---|---|---:|---|---:|---|\n");
        for (LunationEntry row : activeYearCandidates) {
            out.append("| ").append(format(row.dateTime()))
                    .append(" | ").append(lunationType(row.type()))
                    .append(" | ").append(placement(row.syzygySign(), row.syzygyDegreeInSign()))
                    .append(" | H").append(row.natalHouseOverlay())
                    .append(" | ").append(node(row.nearestNode()))
                    .append(" | ").append(formatDecimal(row.nearestNodeOrbDegrees(), 2)).append("°")
                    .append(" | ").append(eclipse(row.eclipseType()))
                    .append(" |\n");
        }
        out.append("\n");
    }

    private AnnualProfectionTableRow activeAnnual(AnnualProfectionTable table) {
        if (table == null) {
            return null;
        }
        return table.rows().stream().filter(AnnualProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private MonthlyProfectionTableRow activeMonthly(MonthlyProfectionTable table) {
        if (table == null) {
            return null;
        }
        return table.rows().stream().filter(MonthlyProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private List<ZodiacalReleasingPeriod> activeReleasingPath(List<ZodiacalReleasingPeriod> periods,
                                                             OffsetDateTime activeDateTime) {
        for (ZodiacalReleasingPeriod period : periods) {
            if (active(period.startDateTime(), period.endDateTimeExclusive(), activeDateTime)) {
                java.util.ArrayList<ZodiacalReleasingPeriod> path = new java.util.ArrayList<>();
                path.add(period);
                path.addAll(activeReleasingPath(period.subPeriods(), activeDateTime));
                return List.copyOf(path);
            }
        }
        return List.of();
    }

    private String releasingChain(NatalChart chart, List<ZodiacalReleasingPeriod> chain) {
        if (chain == null || chain.isEmpty()) {
            return "—";
        }
        return chain.stream()
                .map(period -> "L" + period.level() + " " + period.sign()
                        + " H" + houseForSign(chart, period.sign())
                        + " — " + TraditionalTables.domicileRuler(period.sign())
                        + markerSuffix(period.markers()))
                .collect(Collectors.joining("; "));
    }

    private String markerSuffix(List<ZodiacalReleasingMarker> markers) {
        if (markers == null || markers.isEmpty()) {
            return "";
        }
        return " [" + markers.stream().map(this::marker).collect(Collectors.joining(", ")) + "]";
    }

    private String marker(ZodiacalReleasingMarker marker) {
        return switch (marker) {
            case PREPARATORY_LOOSING_OF_BOND -> "pLB";
            case LOOSING_OF_BOND -> "LB";
            case CULMINATION -> "Cu.";
            case COMPLETION -> "Co.";
        };
    }

    private SolarReturnPointEntry solarReturnPoint(SolarReturnEntry row, PointKey point) {
        return row.points().stream()
                .filter(candidate -> candidate.point() == point)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing solar-return point " + point));
    }

    private boolean active(OffsetDateTime start, OffsetDateTime end, OffsetDateTime activeDateTime) {
        return activeDateTime != null && start != null && end != null
                && !activeDateTime.isBefore(start)
                && activeDateTime.isBefore(end);
    }

    private int houseForSign(NatalChart chart, ZodiacSign sign) {
        return chart.getHouses().stream()
                .filter(candidate -> candidate.getSign() == sign)
                .map(HousePosition::getHouse)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing natal house for sign " + sign));
    }

    private String decennialContext(DecennialRulerCondition condition) {
        return placement(condition.sign(), condition.degreeInSign())
                + ", H" + condition.house()
                + ", rules " + houses(condition.ruledNatalHouses());
    }

    private String houses(List<Integer> houses) {
        if (houses == null || houses.isEmpty()) {
            return "—";
        }
        return houses.stream().map(house -> "H" + house).collect(Collectors.joining(", "));
    }

    private String distributionContact(DistributionThroughBoundsContact contact) {
        if (contact.contactType() == DistributionContactType.BODY) {
            return "BODY " + aspect(contact.aspect());
        }
        return "RAY " + aspect(contact.aspect()) + " " + contact.rayDirection();
    }

    private String primaryDirectionContact(PrimaryDirectionEvent event) {
        if (event.contactType() == PrimaryDirectionContactType.BODY) {
            return "BODY " + aspect(event.aspect());
        }
        return "RAY " + aspect(event.aspect()) + " " + event.rayDirection();
    }

    private String lordOfYearOverlay(SolarReturnPointOverlay overlay) {
        return overlay.point() + " " + placement(overlay.sign(), overlay.degreeInSign())
                + ", SR H" + house(overlay.solarReturnHouse())
                + ", natal H" + overlay.natalHouseOverlay();
    }

    private String joinPointKeys(List<PointKey> points) {
        if (points == null || points.isEmpty()) {
            return "—";
        }
        return points.stream().map(PointKey::name).collect(Collectors.joining(", "));
    }

    private String house(SolarReturnPointEntry point) {
        return house(point.house());
    }

    private String house(Integer house) {
        return house == null ? "—" : Integer.toString(house);
    }

    private String coordinate(PrimaryDirectionCoordinate coordinate) {
        return switch (coordinate) {
            case OBLIQUE_ASCENSION_AT_BIRTH_LATITUDE -> "OA";
            case RIGHT_ASCENSION -> "RA";
        };
    }

    private String label(AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> "Ascendant";
            case MIDHEAVEN -> "Midheaven";
            case SUN -> "Sun";
            case MOON -> "Moon";
            case LOT_FORTUNE -> "Fortune";
            case LOT_SPIRIT -> "Spirit";
        };
    }

    private String lunationType(SyzygyType type) {
        return type == SyzygyType.NEW_MOON ? "New Moon" : "Full Moon";
    }

    private String eclipse(EclipseCandidateType eclipseType) {
        return eclipseType == EclipseCandidateType.NONE ? "—" : eclipseType.name();
    }

    private String visiblePhases(EclipseEvent event) {
        if (event.visibility().visibleContactPhases().isEmpty()) {
            return "—";
        }
        return event.visibility().visibleContactPhases().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private String node(Planet node) {
        if (node == Planet.NORTH_NODE) {
            return "North";
        }
        if (node == Planet.SOUTH_NODE) {
            return "South";
        }
        return node == null ? "—" : node.name();
    }

    private String aspect(AspectType aspect) {
        return aspect == null ? "—" : aspect.name();
    }

    private String placement(ZodiacSign sign, double degreeInSign) {
        return sign + " " + formatDecimal(degreeInSign, 2) + "°";
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatOptional(Double value) {
        return value == null ? "—" : formatDecimal(value, 3);
    }

    private String formatDecimal(double value, int places) {
        return String.format(Locale.ROOT, "%." + places + "f", value);
    }

    private OffsetDateTime inquiryDateTime(Subject subject, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            return null;
        }
        return OffsetDateTime.of(
                inquiryDate,
                subject.getUtcBirthDateTime().toLocalTime(),
                subject.getUtcBirthDateTime().getOffset()
        );
    }

    private int completedAgeYears(Subject subject, LocalDate inquiryDate) {
        if (inquiryDate == null) {
            return -1;
        }
        LocalDate birthDate = subject.getUtcBirthDateTime().toLocalDate();
        int years = inquiryDate.getYear() - birthDate.getYear();
        if (inquiryDate.isBefore(birthDate.plusYears(years))) {
            years--;
        }
        return years;
    }

    private Path pathFor(List<FileReference> fileReferences, String label) {
        if (fileReferences == null) {
            return null;
        }
        return fileReferences.stream()
                .filter(ref -> label.equals(ref.label()))
                .map(FileReference::path)
                .findFirst()
                .orElse(null);
    }

    private String fileLink(Path briefOutput, Path target, String label) {
        if (target == null) {
            return "—";
        }
        String href = target.toString();
        Path base = briefOutput == null ? null : briefOutput.getParent();
        if (base != null) {
            try {
                href = base.normalize().relativize(target.normalize()).toString();
            } catch (IllegalArgumentException ignored) {
                href = target.toString();
            }
        }
        return "[" + label + "](" + href.replace('\\', '/') + ")";
    }

    record FileReference(String label, Path path, String purpose) {}

    record ZodiacalReleasingBrief(LotEntry lot, ZodiacalReleasingTimeline timeline, Path file) {}
}
