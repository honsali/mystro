package app.local;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.chart.data.Planet;
import app.chart.model.Subject;
import app.reading.lifearc.decennial.DecennialPeriod;
import app.reading.lifearc.decennial.DecennialSubperiod;
import app.reading.lifearc.decennial.DecennialTable;
import app.reading.lifearc.distribution.DistributionThroughBoundsPeriod;
import app.reading.lifearc.distribution.DistributionThroughBoundsTable;
import app.reading.lifearc.firdaria.FirdariaPeriod;
import app.reading.lifearc.firdaria.FirdariaSubperiod;
import app.reading.lifearc.firdaria.FirdariaTable;
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
import app.reading.lifearc.solarreturn.SolarReturnEntry;
import app.reading.lifearc.solarreturn.SolarReturnNatalComparisonTable;
import app.reading.lifearc.solarreturn.SolarReturnTable;

final class ZoomActivePeriodsMarkdownRenderer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx");

    String render(Subject subject,
                  OffsetDateTime focusDateTime,
                  OffsetDateTime windowStart,
                  OffsetDateTime windowEnd,
                  AnnualProfectionTable annualProfections,
                  MonthlyProfectionTable monthlyProfections,
                  FirdariaTable firdaria,
                  DecennialTable decennials,
                  DistributionThroughBoundsTable distribution,
                  List<DistributionThroughBoundsTable> extendedDistributions,
                  SolarReturnTable solarReturns,
                  SolarReturnNatalComparisonTable solarReturnComparison,
                  LunarTimingTable lunarTiming) {
        StringBuilder out = new StringBuilder();
        out.append("# Active Periods at Zoom Date\n\n");
        out.append("- Subject: `").append(subject.getId()).append("`\n");
        out.append("- Focus date/time: `").append(format(focusDateTime)).append("`\n");
        out.append("- Window: `").append(format(windowStart)).append("` to `").append(format(windowEnd)).append("`\n\n");
        appendProfections(out, annualProfections, monthlyProfections);
        appendChronocrators(out, firdaria, decennials);
        appendDistributions(out, distribution, extendedDistributions);
        appendSolarReturn(out, focusDateTime, solarReturns, solarReturnComparison);
        appendLunar(out, windowStart, windowEnd, lunarTiming);
        return out.toString();
    }

    private void appendProfections(StringBuilder out, AnnualProfectionTable annualProfections, MonthlyProfectionTable monthlyProfections) {
        out.append("## Profections\n\n");
        AnnualProfectionTableRow annual = activeAnnual(annualProfections);
        MonthlyProfectionTableRow monthly = activeMonthly(monthlyProfections);
        if (annual == null) {
            out.append("No active annual profection row was available.\n\n");
        } else {
            AnnualProfectionReferenceEntry asc = annualEntry(annual, AnnualProfectionReference.ASCENDANT);
            out.append("- Annual Asc/Lord-of-Year profection: `age ").append(annual.ageYears())
                    .append(", H").append(asc.profectedHouse())
                    .append(" ").append(asc.profectedSign())
                    .append(" — ").append(asc.lord())
                    .append("` (`").append(annual.periodStartDate()).append("` to `")
                    .append(annual.periodEndDateExclusive()).append("`)\n");
            appendAnnualReferenceTable(out, annual);
        }
        if (monthly == null) {
            out.append("No active monthly profection row was available.\n\n");
        } else {
            MonthlyProfectionReferenceEntry asc = monthlyEntry(monthly, AnnualProfectionReference.ASCENDANT);
            out.append("- Monthly Asc/Lord-of-Month profection: `age ").append(monthly.ageYears())
                    .append("/M").append(monthly.monthInYear())
                    .append(", H").append(asc.profectedHouse())
                    .append(" ").append(asc.profectedSign())
                    .append(" — ").append(asc.lord())
                    .append("` (`").append(format(monthly.periodStartDateTime())).append("` to `")
                    .append(format(monthly.periodEndDateTimeExclusive())).append("`)\n");
            appendMonthlyReferenceTable(out, monthly);
        }
    }

    private void appendAnnualReferenceTable(StringBuilder out, AnnualProfectionTableRow row) {
        out.append("\nAnnual references\n\n");
        out.append("| Reference | Natal sign | Profected sign | House | Lord |\n");
        out.append("|---|---|---|---:|---|\n");
        for (AnnualProfectionReferenceEntry entry : row.referenceProfections()) {
            out.append("| ").append(label(entry.reference()))
                    .append(" | ").append(entry.natalSign())
                    .append(" | ").append(entry.profectedSign())
                    .append(" | H").append(entry.profectedHouse())
                    .append(" | ").append(entry.lord())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendMonthlyReferenceTable(StringBuilder out, MonthlyProfectionTableRow row) {
        out.append("Monthly references\n\n");
        out.append("| Reference | Natal sign | Annual sign | Monthly sign | House | Lord |\n");
        out.append("|---|---|---|---|---:|---|\n");
        for (MonthlyProfectionReferenceEntry entry : row.referenceProfections()) {
            out.append("| ").append(label(entry.reference()))
                    .append(" | ").append(entry.natalSign())
                    .append(" | ").append(entry.annualSign())
                    .append(" | ").append(entry.profectedSign())
                    .append(" | H").append(entry.profectedHouse())
                    .append(" | ").append(entry.lord())
                    .append(" |\n");
        }
        out.append("\n");
    }

    private void appendChronocrators(StringBuilder out, FirdariaTable firdaria, DecennialTable decennials) {
        out.append("## Main chronocrators\n\n");
        FirdariaPeriod firdariaPeriod = activeFirdaria(firdaria);
        FirdariaSubperiod firdariaSubperiod = firdariaPeriod == null ? null : activeFirdariaSubperiod(firdariaPeriod);
        DecennialPeriod decennialPeriod = activeDecennial(decennials);
        DecennialSubperiod decennialSubperiod = decennialPeriod == null ? null : activeDecennialSubperiod(decennialPeriod);
        out.append("| Technique | Main ruler | Partner/sub-ruler | Period | Subperiod |\n");
        out.append("|---|---|---|---|---|\n");
        out.append("| Firdaria | ").append(planet(firdariaPeriod == null ? null : firdariaPeriod.ruler()))
                .append(" | ").append(planet(firdariaSubperiod == null ? null : firdariaSubperiod.partner()))
                .append(" | ").append(period(firdariaPeriod == null ? null : firdariaPeriod.startDateTime(), firdariaPeriod == null ? null : firdariaPeriod.endDateTimeExclusive()))
                .append(" | ").append(period(firdariaSubperiod == null ? null : firdariaSubperiod.startDateTime(), firdariaSubperiod == null ? null : firdariaSubperiod.endDateTimeExclusive()))
                .append(" |\n");
        out.append("| Decennials | ").append(planet(decennialPeriod == null ? null : decennialPeriod.ruler()))
                .append(" | ").append(planet(decennialSubperiod == null ? null : decennialSubperiod.partner()))
                .append(" | ").append(period(decennialPeriod == null ? null : decennialPeriod.startDateTime(), decennialPeriod == null ? null : decennialPeriod.endDateTimeExclusive()))
                .append(" | ").append(period(decennialSubperiod == null ? null : decennialSubperiod.startDateTime(), decennialSubperiod == null ? null : decennialSubperiod.endDateTimeExclusive()))
                .append(" |\n\n");
    }

    private void appendDistributions(StringBuilder out,
                                     DistributionThroughBoundsTable distribution,
                                     List<DistributionThroughBoundsTable> extendedDistributions) {
        out.append("## Distributions through bounds\n\n");
        out.append("| Directed point | Active sign/bound | Bound ruler | Period | Contacts in retained row |\n");
        out.append("|---|---|---|---|---:|\n");
        appendDistributionRow(out, distribution);
        if (extendedDistributions != null) {
            for (DistributionThroughBoundsTable table : extendedDistributions) {
                appendDistributionRow(out, table);
            }
        }
        out.append("\n");
    }

    private void appendDistributionRow(StringBuilder out, DistributionThroughBoundsTable table) {
        DistributionThroughBoundsPeriod period = activeDistribution(table);
        if (table == null || period == null) {
            return;
        }
        out.append("| ").append(table.directedPoint())
                .append(" | ").append(period.sign()).append(" bound ").append(period.boundIndexInSign())
                .append(" (`").append(formatDecimal(period.boundStartDegreeInSign())).append("°–")
                .append(formatDecimal(period.boundEndDegreeInSign())).append("°`)")
                .append(" | ").append(period.boundRuler())
                .append(" | ").append(period(period.startDateTime(), period.endDateTimeExclusive()))
                .append(" | ").append(period.contacts().size())
                .append(" |\n");
    }

    private void appendSolarReturn(StringBuilder out,
                                   OffsetDateTime focusDateTime,
                                   SolarReturnTable solarReturns,
                                   SolarReturnNatalComparisonTable solarReturnComparison) {
        out.append("## Solar return context\n\n");
        SolarReturnEntry active = activeSolarReturn(solarReturns, focusDateTime);
        if (active == null) {
            out.append("No active solar return row was available.\n\n");
            return;
        }
        out.append("- Active solar return age: `").append(active.ageYears()).append("`\n");
        out.append("- Return: `").append(format(active.returnDateTime())).append("` to `")
                .append(format(active.periodEndDateTimeExclusive())).append("`\n");
        out.append("- SR Ascendant: `").append(active.ascendantSign()).append(" ")
                .append(formatDecimal(active.ascendantDegreeInSign())).append("°`\n");
        out.append("- SR Midheaven: `").append(active.midheavenSign()).append(" ")
                .append(formatDecimal(active.midheavenDegreeInSign())).append("°`\n");
        out.append("- SR sect: `").append(active.sect()).append("`\n");
        if (solarReturnComparison != null) {
            out.append("- Solar-return comparison rows: `").append(solarReturnComparison.rows().size()).append("`\n");
        }
        out.append("\n");
    }

    private void appendLunar(StringBuilder out, OffsetDateTime windowStart, OffsetDateTime windowEnd, LunarTimingTable lunarTiming) {
        out.append("## Lunar focus\n\n");
        if (lunarTiming == null) {
            out.append("No lunar timing table was available.\n\n");
            return;
        }
        LunarReturnEntry activeReturn = lunarTiming.lunarReturns().stream()
                .filter(LunarReturnEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        LunationEntry activeLunation = lunarTiming.lunations().stream()
                .filter(LunationEntry::activeForInquiry)
                .findFirst()
                .orElse(null);
        out.append("- Active lunar return: ").append(activeReturn == null ? "—" : "`" + format(activeReturn.returnDateTime()) + "` to `" + format(activeReturn.periodEndDateTimeExclusive()) + "`, Moon `" + activeReturn.moonSign() + " " + formatDecimal(activeReturn.moonDegreeInSign()) + "°`").append("\n");
        out.append("- Active lunation: ").append(activeLunation == null ? "—" : "`" + activeLunation.type() + "` at `" + format(activeLunation.dateTime()) + "`, syzygy `" + activeLunation.syzygySign() + " " + formatDecimal(activeLunation.syzygyDegreeInSign()) + "°`, house `H" + activeLunation.natalHouseOverlay() + "`").append("\n\n");

        List<LunarReturnEntry> returnsInWindow = lunarTiming.lunarReturns().stream()
                .filter(entry -> inside(entry.returnDateTime(), windowStart, windowEnd))
                .toList();
        List<LunationEntry> lunationsInWindow = lunarTiming.lunations().stream()
                .filter(entry -> inside(entry.dateTime(), windowStart, windowEnd))
                .toList();
        List<EclipseEvent> eclipsesInWindow = lunarTiming.eclipseEvents().stream()
                .filter(entry -> inside(entry.maximumDateTime(), windowStart, windowEnd))
                .toList();

        out.append("Lunar events inside the ±15-day window\n\n");
        out.append("| Type | Date/time | Placement | House | Extra |\n");
        out.append("|---|---|---|---:|---|\n");
        for (LunarReturnEntry entry : returnsInWindow) {
            out.append("| Lunar return | ").append(format(entry.returnDateTime()))
                    .append(" | ").append(entry.moonSign()).append(" ").append(formatDecimal(entry.moonDegreeInSign())).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | nearest node ").append(entry.nearestNode()).append(" orb ").append(formatDecimal(entry.nearestNodeOrbDegrees())).append("° |\n");
        }
        for (LunationEntry entry : lunationsInWindow) {
            out.append("| ").append(entry.type())
                    .append(" | ").append(format(entry.dateTime()))
                    .append(" | ").append(entry.syzygySign()).append(" ").append(formatDecimal(entry.syzygyDegreeInSign())).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | eclipse candidate ").append(entry.eclipseType()).append(" |\n");
        }
        for (EclipseEvent entry : eclipsesInWindow) {
            out.append("| True ").append(entry.kind()).append(" eclipse")
                    .append(" | ").append(format(entry.maximumDateTime()))
                    .append(" | ").append(entry.syzygySign()).append(" ").append(formatDecimal(entry.syzygyDegreeInSign())).append("°")
                    .append(" | H").append(entry.natalHouseOverlay())
                    .append(" | ").append(entry.eclipseType()).append(", visibility ").append(entry.visibility().localVisibility())
                    .append(" |\n");
        }
        if (returnsInWindow.isEmpty() && lunationsInWindow.isEmpty() && eclipsesInWindow.isEmpty()) {
            out.append("| — | — | — | — | No lunar return, lunation, or true eclipse starts inside this window. |\n");
        }
        out.append("\n");
    }

    private AnnualProfectionTableRow activeAnnual(AnnualProfectionTable table) {
        return table == null ? null : table.rows().stream().filter(AnnualProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private MonthlyProfectionTableRow activeMonthly(MonthlyProfectionTable table) {
        return table == null ? null : table.rows().stream().filter(MonthlyProfectionTableRow::activeForInquiry).findFirst().orElse(null);
    }

    private FirdariaPeriod activeFirdaria(FirdariaTable table) {
        return table == null ? null : table.periods().stream().filter(FirdariaPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private FirdariaSubperiod activeFirdariaSubperiod(FirdariaPeriod period) {
        return period.subperiods().stream().filter(FirdariaSubperiod::activeForInquiry).findFirst().orElse(null);
    }

    private DecennialPeriod activeDecennial(DecennialTable table) {
        return table == null ? null : table.periods().stream().filter(DecennialPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private DecennialSubperiod activeDecennialSubperiod(DecennialPeriod period) {
        return period.subperiods().stream().filter(DecennialSubperiod::activeForInquiry).findFirst().orElse(null);
    }

    private DistributionThroughBoundsPeriod activeDistribution(DistributionThroughBoundsTable table) {
        return table == null ? null : table.periods().stream().filter(DistributionThroughBoundsPeriod::activeForInquiry).findFirst().orElse(null);
    }

    private SolarReturnEntry activeSolarReturn(SolarReturnTable table, OffsetDateTime focusDateTime) {
        return table == null ? null : table.rows().stream()
                .filter(row -> !focusDateTime.isBefore(row.returnDateTime()) && focusDateTime.isBefore(row.periodEndDateTimeExclusive()))
                .findFirst()
                .orElse(null);
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

    private String label(AnnualProfectionReference reference) {
        return switch (reference) {
            case ASCENDANT -> "Asc / Lord of Year";
            case MIDHEAVEN -> "MC";
            case SUN -> "Sun";
            case MOON -> "Moon";
            case LOT_FORTUNE -> "Fortune";
            case LOT_SPIRIT -> "Spirit";
        };
    }

    private boolean inside(OffsetDateTime candidate, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
        return candidate != null && !candidate.isBefore(windowStart) && !candidate.isAfter(windowEnd);
    }

    private String period(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            return "—";
        }
        return format(start) + " to " + format(end);
    }

    private String planet(Planet planet) {
        return planet == null ? "—" : planet.name();
    }

    private String format(OffsetDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
