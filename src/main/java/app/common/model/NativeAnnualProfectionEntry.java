package app.common.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class NativeAnnualProfectionEntry {
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private int age;
    private String date;
    private String lordOfYearSign;
    private String lordOfYearRuler;
    private String lordOfOrbMod84;
    private String lordOfOrbMod12;
    private String mcSign;
    private String sunSign;
    private String moonSign;
    private String fortuneSign;

    public NativeAnnualProfectionEntry() {
    }

    public NativeAnnualProfectionEntry(int age, String date, String lordOfYearSign, String lordOfYearRuler, String lordOfOrbMod84, String lordOfOrbMod12, String mcSign, String sunSign, String moonSign, String fortuneSign) {
        this.age = age;
        setDate(date);
        this.lordOfYearSign = lordOfYearSign;
        this.lordOfYearRuler = lordOfYearRuler;
        this.lordOfOrbMod84 = lordOfOrbMod84;
        this.lordOfOrbMod12 = lordOfOrbMod12;
        this.mcSign = mcSign;
        this.sunSign = sunSign;
        this.moonSign = moonSign;
        this.fortuneSign = fortuneSign;
    }

    public int age() { return age; }
    public void age(int age) { this.age = age; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String date() { return date; }
    public void date(String date) { setDate(date); }
    public String getDate() { return date; }
    public void setDate(String date) {
        if (date == null || date.isBlank()) {
            this.date = date;
            return;
        }
        try {
            this.date = LocalDate.parse(date.trim(), INPUT_DATE_FORMAT).format(OUTPUT_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            this.date = date;
        }
    }

    public String lordOfYearSign() { return lordOfYearSign; }
    public void lordOfYearSign(String lordOfYearSign) { this.lordOfYearSign = lordOfYearSign; }
    public String getLordOfYearSign() { return lordOfYearSign; }
    public void setLordOfYearSign(String lordOfYearSign) { this.lordOfYearSign = lordOfYearSign; }

    public String lordOfYearRuler() { return lordOfYearRuler; }
    public void lordOfYearRuler(String lordOfYearRuler) { this.lordOfYearRuler = lordOfYearRuler; }
    public String getLordOfYearRuler() { return lordOfYearRuler; }
    public void setLordOfYearRuler(String lordOfYearRuler) { this.lordOfYearRuler = lordOfYearRuler; }

    public String lordOfOrbMod84() { return lordOfOrbMod84; }
    public void lordOfOrbMod84(String lordOfOrbMod84) { this.lordOfOrbMod84 = lordOfOrbMod84; }
    public String getLordOfOrbMod84() { return lordOfOrbMod84; }
    public void setLordOfOrbMod84(String lordOfOrbMod84) { this.lordOfOrbMod84 = lordOfOrbMod84; }

    public String lordOfOrbMod12() { return lordOfOrbMod12; }
    public void lordOfOrbMod12(String lordOfOrbMod12) { this.lordOfOrbMod12 = lordOfOrbMod12; }
    public String getLordOfOrbMod12() { return lordOfOrbMod12; }
    public void setLordOfOrbMod12(String lordOfOrbMod12) { this.lordOfOrbMod12 = lordOfOrbMod12; }

    public String mcSign() { return mcSign; }
    public void mcSign(String mcSign) { this.mcSign = mcSign; }
    public String getMcSign() { return mcSign; }
    public void setMcSign(String mcSign) { this.mcSign = mcSign; }

    public String sunSign() { return sunSign; }
    public void sunSign(String sunSign) { this.sunSign = sunSign; }
    public String getSunSign() { return sunSign; }
    public void setSunSign(String sunSign) { this.sunSign = sunSign; }

    public String moonSign() { return moonSign; }
    public void moonSign(String moonSign) { this.moonSign = moonSign; }
    public String getMoonSign() { return moonSign; }
    public void setMoonSign(String moonSign) { this.moonSign = moonSign; }

    public String fortuneSign() { return fortuneSign; }
    public void fortuneSign(String fortuneSign) { this.fortuneSign = fortuneSign; }
    public String getFortuneSign() { return fortuneSign; }
    public void setFortuneSign(String fortuneSign) { this.fortuneSign = fortuneSign; }
}
