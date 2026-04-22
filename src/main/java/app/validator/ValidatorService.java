package app.validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import app.common.Config;
import app.common.Logger;
import app.common.io.JsonFileSupport;
import app.common.model.ChartPoint;
import app.common.model.NativeAnnualProfectionEntry;
import app.common.model.NativeAspect;
import app.common.model.NativeHermeticLot;
import app.common.model.NativeLordOfOrbEntry;
import app.common.model.NativeReport;
import app.validator.model.ComparisonEntry;
import app.validator.model.ComparisonSummary;

public final class ValidatorService {

    private final Path mystroDir;
    private final Path astroseekDir;
    private final Path summaryPath;

    public ValidatorService() {
        this.mystroDir = Config.MYSTRO_OUTPUT_DIR;
        this.astroseekDir = Config.ASTROSEEK_OUTPUT_DIR;
        this.summaryPath = Config.REPORT_PATH;
    }

    public ComparisonSummary compare(List<String> requestedNames) throws IOException {
        Logger logger = Logger.getInstance();
        List<ComparisonEntry> entries = new ArrayList<>();
        int matchCount = 0;
        int mismatchCount = 0;
        int comparedCount = 0;

        for (String name : requestedNames) {
            if (logger.hasError("MISSING_NATIVE_CONFIG", name)) {
                entries.add(new ComparisonEntry(name, "MISSING_NATIVE_CONFIG", List.of()));
                continue;
            }
            if (logger.hasError("MISSING_ASTROSEEK_HTML", name)) {
                entries.add(new ComparisonEntry(name, "MISSING_ASTROSEEK_HTML", List.of()));
                continue;
            }

            Path mystroPath = mystroDir.resolve(name + ".json");
            Path astroseekPath = astroseekDir.resolve(name + ".json");
            if (!Files.exists(mystroPath)) {
                logger.missingMystroJson(name);
                entries.add(new ComparisonEntry(name, "MISSING_MYSTRO_JSON", List.of()));
                continue;
            }
            if (!Files.exists(astroseekPath)) {
                logger.missingAstroSeekJson(name);
                entries.add(new ComparisonEntry(name, "MISSING_ASTROSEEK_JSON", List.of()));
                continue;
            }

            NativeReport mystroReport = JsonFileSupport.read(mystroPath, NativeReport.class);
            NativeReport astroseekReport = JsonFileSupport.read(astroseekPath, NativeReport.class);
            List<String> differences = compareReports(name, mystroReport, astroseekReport, logger);
            comparedCount++;
            if (differences.isEmpty()) {
                matchCount++;
                entries.add(new ComparisonEntry(name, "MATCH", List.of()));
            } else {
                mismatchCount++;
                entries.add(new ComparisonEntry(name, "MISMATCH", differences));
            }
        }

        ComparisonSummary summary = new ComparisonSummary(List.copyOf(requestedNames), requestedNames.size(), comparedCount, matchCount, mismatchCount, List.copyOf(entries));
        JsonFileSupport.write(summaryPath, summary);
        return summary;
    }

    private List<String> compareReports(String name, NativeReport mystroReport, NativeReport astroseekReport, Logger logger) {
        List<String> differences = new ArrayList<>();
        compareField("name", mystroReport.name(), astroseekReport.name(), differences);
        compareField("birth.birthDate", mystroReport.birth().birthDate(), astroseekReport.birth().birthDate(), differences);
        compareField("birth.birthTime", mystroReport.birth().birthTime(), astroseekReport.birth().birthTime(), differences);
        compareCoordinate("birth.latitude", mystroReport.birth().latitude(), astroseekReport.birth().latitude(), differences);
        compareCoordinate("birth.longitude", mystroReport.birth().longitude(), astroseekReport.birth().longitude(), differences);
        compareField("birth.utcOffset", mystroReport.birth().utcOffset(), astroseekReport.birth().utcOffset(), differences);
        compareField("birth.houseSystem", mystroReport.birth().houseSystem(), astroseekReport.birth().houseSystem(), differences);
        compareField("birth.zodiac", mystroReport.birth().zodiac(), astroseekReport.birth().zodiac(), differences);
        compareField("birth.terms", mystroReport.birth().terms(), astroseekReport.birth().terms(), differences);

        if (mystroReport.planetaryHour() == null || astroseekReport.planetaryHour() == null) {
            compareField("planetaryHour", mystroReport.planetaryHour(), astroseekReport.planetaryHour(), differences);
        } else {
            compareField("planetaryHour.hourNumber", mystroReport.planetaryHour().hourNumber(), astroseekReport.planetaryHour().hourNumber(), differences);
            compareField("planetaryHour.dayRuler", mystroReport.planetaryHour().dayRuler(), astroseekReport.planetaryHour().dayRuler(), differences);
            compareField("planetaryHour.hourRuler", mystroReport.planetaryHour().hourRuler(), astroseekReport.planetaryHour().hourRuler(), differences);
            compareNumericField("planetaryHour.last", mystroReport.planetaryHour().last(), astroseekReport.planetaryHour().last(), 1.0, differences);
            compareNumericField("planetaryHour.next", mystroReport.planetaryHour().next(), astroseekReport.planetaryHour().next(), 1.0, differences);
        }

        if (mystroReport.syzygy() == null || astroseekReport.syzygy() == null) {
            compareField("syzygy", mystroReport.syzygy(), astroseekReport.syzygy(), differences);
        } else {
            compareField("syzygy.phase", mystroReport.syzygy().phase(), astroseekReport.syzygy().phase(), differences);
            long minuteDelta = Math.abs(mystroReport.syzygy().syzygyDateTime().toEpochSecond() - astroseekReport.syzygy().syzygyDateTime().toEpochSecond()) / 60;
            if (minuteDelta > 1) {
                differences.add("syzygy.dateTime: mystro=" + mystroReport.syzygy().syzygyDateTime() + ", astroseek=" + astroseekReport.syzygy().syzygyDateTime() + ", delta_minutes=" + minuteDelta);
            }
        }

        compareLordOfOrb(mystroReport, astroseekReport, differences);
        compareAnnualProfections(mystroReport, astroseekReport, differences);

        for (Map.Entry<String, NativeHermeticLot> entry : astroseekReport.lots().entrySet()) {
            NativeHermeticLot mystroLot = mystroReport.lots().get(entry.getKey());
            NativeHermeticLot astroseekLot = entry.getValue();
            if (mystroLot == null) {
                differences.add("lots." + entry.getKey() + ": missing in mystro");
                continue;
            }
            compareField("lots." + entry.getKey() + ".lot", mystroLot.lot(), astroseekLot.lot(), differences);
            compareField("lots." + entry.getKey() + ".sign", mystroLot.sign(), astroseekLot.sign(), differences);
            comparePosition("lots." + entry.getKey() + ".signLon", mystroLot.signLon(), astroseekLot.signLon(), differences);
            compareField("lots." + entry.getKey() + ".house", mystroLot.house(), astroseekLot.house(), differences);
            compareField("lots." + entry.getKey() + ".ruler", mystroLot.ruler(), astroseekLot.ruler(), differences);
            compareField("lots." + entry.getKey() + ".rulerSign", mystroLot.rulerSign(), astroseekLot.rulerSign(), differences);
            comparePosition("lots." + entry.getKey() + ".rulerSignLon", mystroLot.rulerSignLon(), astroseekLot.rulerSignLon(), differences);
            compareField("lots." + entry.getKey() + ".rulerHouse", mystroLot.rulerHouse(), astroseekLot.rulerHouse(), differences);
            compareField("lots." + entry.getKey() + ".lotToRuler", mystroLot.lotToRuler(), astroseekLot.lotToRuler(), differences);
            compareField("lots." + entry.getKey() + ".fortuneToRuler", mystroLot.fortuneToRuler(), astroseekLot.fortuneToRuler(), differences);
        }

        comparePointMaps("planets", mystroReport.planets(), astroseekReport.planets(), differences);
        comparePointMaps("houses", mystroReport.houses(), astroseekReport.houses(), differences);
        compareAspectLists("mainAspects", mystroReport.mainAspects(), astroseekReport.mainAspects(), differences);
        compareAspectLists("otherAspects", mystroReport.otherAspects(), astroseekReport.otherAspects(), differences);
        comparePointMaps("dodecatemoria", mystroReport.dodecatemoria(), astroseekReport.dodecatemoria(), differences);
        comparePointMaps("novenaria", mystroReport.novenaria(), astroseekReport.novenaria(), differences);
        comparePointMaps("antiscia", mystroReport.antiscia(), astroseekReport.antiscia(), differences);
        comparePointMaps("contraAntiscia", mystroReport.contraAntiscia(), astroseekReport.contraAntiscia(), differences);
        return differences;
    }

    private void compareLordOfOrb(NativeReport mystroReport, NativeReport astroseekReport, List<String> differences) {
        if (mystroReport.lordOfOrb() == null || astroseekReport.lordOfOrb() == null) {
            compareField("lordOfOrb", mystroReport.lordOfOrb(), astroseekReport.lordOfOrb(), differences);
            return;
        }
        if (astroseekReport.lordOfOrb().startingRuler() != null) {
            compareField("lordOfOrb.startingRuler", mystroReport.lordOfOrb().startingRuler(), astroseekReport.lordOfOrb().startingRuler(), differences);
        }
        Map<Integer, NativeLordOfOrbEntry> mystroYearsByAge = mystroReport.lordOfOrb().years().stream()
                .collect(java.util.stream.Collectors.toMap(NativeLordOfOrbEntry::age, year -> year, (left, right) -> left));
        for (NativeLordOfOrbEntry astroseekYear : astroseekReport.lordOfOrb().years()) {
            NativeLordOfOrbEntry mystroYear = mystroYearsByAge.get(astroseekYear.age());
            if (mystroYear == null) {
                differences.add("lordOfOrb.years[age=" + astroseekYear.age() + "]: missing in mystro");
                continue;
            }
            compareField("lordOfOrb.years[age=" + astroseekYear.age() + "].mod84", mystroYear.mod84(), astroseekYear.mod84(), differences);
            compareField("lordOfOrb.years[age=" + astroseekYear.age() + "].mod12", mystroYear.mod12(), astroseekYear.mod12(), differences);
        }
    }

    private void compareAnnualProfections(NativeReport mystroReport, NativeReport astroseekReport, List<String> differences) {
        if (mystroReport.annualProfections() == null || astroseekReport.annualProfections() == null) {
            compareField("annualProfections", mystroReport.annualProfections(), astroseekReport.annualProfections(), differences);
            return;
        }
        Map<Integer, NativeAnnualProfectionEntry> mystroYearsByAge = mystroReport.annualProfections().years().stream()
                .collect(java.util.stream.Collectors.toMap(NativeAnnualProfectionEntry::age, year -> year, (left, right) -> left));
        for (NativeAnnualProfectionEntry astroseekYear : astroseekReport.annualProfections().years()) {
            NativeAnnualProfectionEntry mystroYear = mystroYearsByAge.get(astroseekYear.age());
            if (mystroYear == null) {
                differences.add("annualProfections.years[age=" + astroseekYear.age() + "]: missing in mystro");
                continue;
            }
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].date", mystroYear.date(), astroseekYear.date(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].lordOfYearSign", mystroYear.lordOfYearSign(), astroseekYear.lordOfYearSign(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].lordOfYearRuler", mystroYear.lordOfYearRuler(), astroseekYear.lordOfYearRuler(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].lordOfOrbMod84", mystroYear.lordOfOrbMod84(), astroseekYear.lordOfOrbMod84(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].lordOfOrbMod12", mystroYear.lordOfOrbMod12(), astroseekYear.lordOfOrbMod12(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].mcSign", mystroYear.mcSign(), astroseekYear.mcSign(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].sunSign", mystroYear.sunSign(), astroseekYear.sunSign(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].moonSign", mystroYear.moonSign(), astroseekYear.moonSign(), differences);
            compareField("annualProfections.years[age=" + astroseekYear.age() + "].fortuneSign", mystroYear.fortuneSign(), astroseekYear.fortuneSign(), differences);
        }
    }

    private void comparePointMaps(String section, Map<String, ChartPoint> mystroPoints, Map<String, ChartPoint> astroseekPoints, List<String> differences) {
        for (Map.Entry<String, ChartPoint> entry : astroseekPoints.entrySet()) {
            ChartPoint mystroPoint = mystroPoints.get(entry.getKey());
            ChartPoint astroseekPoint = entry.getValue();
            if (mystroPoint == null) {
                differences.add(section + "." + entry.getKey() + ": missing in mystro");
                continue;
            }
            compareField(section + "." + entry.getKey() + ".sign", mystroPoint.sign(), astroseekPoint.sign(), differences);
            comparePosition(section + "." + entry.getKey() + ".signLon", mystroPoint.signLon(), astroseekPoint.signLon(), differences);
            if (astroseekPoint.wholeSignHouse() != 0 || mystroPoint.wholeSignHouse() != 0) {
                compareField(section + "." + entry.getKey() + ".house", mystroPoint.wholeSignHouse(), astroseekPoint.wholeSignHouse(), differences);
            }
            if (astroseekPoint.retrograde() || mystroPoint.retrograde()) {
                boolean stationSensitive = "planets".equals(section) && Math.abs(mystroPoint.speed()) <= 0.1;
                if (!stationSensitive) {
                    compareField(section + "." + entry.getKey() + ".retrograde", mystroPoint.retrograde(), astroseekPoint.retrograde(), differences);
                }
            }
        }
    }

    private void compareAspectLists(String section, List<NativeAspect> mystroAspects, List<NativeAspect> astroseekAspects, List<String> differences) {
        if (mystroAspects.size() != astroseekAspects.size()) {
            differences.add(section + ".size: mystro=" + mystroAspects.size() + ", astroseek=" + astroseekAspects.size());
        }
        int max = Math.min(mystroAspects.size(), astroseekAspects.size());
        for (int i = 0; i < max; i++) {
            compareField(section + "[" + i + "].left", mystroAspects.get(i).left(), astroseekAspects.get(i).left(), differences);
            compareField(section + "[" + i + "].aspect", mystroAspects.get(i).aspect(), astroseekAspects.get(i).aspect(), differences);
            compareField(section + "[" + i + "].right", mystroAspects.get(i).right(), astroseekAspects.get(i).right(), differences);
            if (astroseekAspects.get(i).orb() != null || mystroAspects.get(i).orb() != null) {
                compareNumericField(section + "[" + i + "].orb", mystroAspects.get(i).orb(), astroseekAspects.get(i).orb(), 0.25, differences);
            }
            if (astroseekAspects.get(i).motion() != null || mystroAspects.get(i).motion() != null) {
                compareField(section + "[" + i + "].motion", mystroAspects.get(i).motion(), astroseekAspects.get(i).motion(), differences);
            }
        }
    }

    private void comparePosition(String field, double mystro, double astroseek, List<String> differences) {
        if (Math.abs(mystro - astroseek) > (Config.ACCEPTED_DIFF_ANGLE_DEGREES + 1e-9)) {
            differences.add(field + ": mystro=" + mystro + ", astroseek=" + astroseek);
        }
    }

    private void compareCoordinate(String field, double mystro, double astroseek, List<String> differences) {
        if (Math.abs(mystro - astroseek) > (Config.ACCEPTED_DIFF_COORDINATE_DEGREES + 1e-9)) {
            differences.add(field + ": mystro=" + mystro + ", astroseek=" + astroseek);
        }
    }

    private void compareNumericField(String field, Double mystro, Double astroseek, double tolerance, List<String> differences) {
        if (mystro == null || astroseek == null) {
            compareField(field, mystro, astroseek, differences);
            return;
        }
        if (Math.abs(mystro - astroseek) > tolerance) {
            differences.add(field + ": mystro=" + mystro + ", astroseek=" + astroseek);
        }
    }

    private void compareField(String field, Object mystro, Object astroseek, List<String> differences) {
        String normalizedMystro = normalizeValue(mystro);
        String normalizedAstroseek = normalizeValue(astroseek);
        if (!normalizedMystro.equals(normalizedAstroseek)) {
            differences.add(field + ": mystro=" + mystro + ", astroseek=" + astroseek);
        }
    }

    private String normalizeValue(Object value) {
        String text = String.valueOf(value).trim();
        if (text.equals("—") || text.equals("–") || text.equals("-")) {
            return "-";
        }
        return text;
    }
}
