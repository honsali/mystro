package app.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NativeBirth {
    @JsonProperty("birth_date")
    private String birthDate;
    @JsonProperty("birth_time")
    private String birthTime;
    private double latitude;
    private double longitude;
    @JsonProperty("utc_offset")
    private String utcOffset;
    @JsonProperty("house_system")
    private String houseSystem = "Whole Sign";
    private String zodiac = "Tropical";
    private String terms = "Egyptian";

    public NativeBirth() {}

    public NativeBirth(String birthDate, String birthTime, double latitude, double longitude, String utcOffset) {
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.utcOffset = utcOffset;
    }

    public String birthDate() {
        return birthDate;
    }

    public void birthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String birthTime() {
        return birthTime;
    }

    public void birthTime(String birthTime) {
        this.birthTime = birthTime;
    }

    public String getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(String birthTime) {
        this.birthTime = birthTime;
    }

    public double latitude() {
        return latitude;
    }

    public void latitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double longitude() {
        return longitude;
    }

    public void longitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String utcOffset() {
        return utcOffset;
    }

    public void utcOffset(String utcOffset) {
        this.utcOffset = utcOffset;
    }

    public String getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(String utcOffset) {
        this.utcOffset = utcOffset;
    }

    public String houseSystem() {
        return houseSystem;
    }

    public void houseSystem(String houseSystem) {
        this.houseSystem = houseSystem;
    }

    public String getHouseSystem() {
        return houseSystem;
    }

    public void setHouseSystem(String houseSystem) {
        this.houseSystem = houseSystem;
    }

    public String zodiac() {
        return zodiac;
    }

    public void zodiac(String zodiac) {
        this.zodiac = zodiac;
    }

    public String getZodiac() {
        return zodiac;
    }

    public void setZodiac(String zodiac) {
        this.zodiac = zodiac;
    }

    public String terms() {
        return terms;
    }

    public void terms(String terms) {
        this.terms = terms;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }
}
