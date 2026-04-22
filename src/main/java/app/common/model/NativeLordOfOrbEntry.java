package app.common.model;

public final class NativeLordOfOrbEntry {
    private int age;
    private String date;
    private String mod84;
    private String mod12;

    public NativeLordOfOrbEntry() {}

    public NativeLordOfOrbEntry(int age, String date, String mod84, String mod12) {
        this.age = age;
        this.date = date;
        this.mod84 = mod84;
        this.mod12 = mod12;
    }

    public int age() { return age; }
    public void age(int age) { this.age = age; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String date() { return date; }
    public void date(String date) { this.date = date; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String mod84() { return mod84; }
    public void mod84(String mod84) { this.mod84 = mod84; }
    public String getMod84() { return mod84; }
    public void setMod84(String mod84) { this.mod84 = mod84; }

    public String mod12() { return mod12; }
    public void mod12(String mod12) { this.mod12 = mod12; }
    public String getMod12() { return mod12; }
    public void setMod12(String mod12) { this.mod12 = mod12; }
}
