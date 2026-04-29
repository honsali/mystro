package app.model.basic;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonInclude;
import app.model.data.Angularity;
import app.model.data.Planet;
import app.model.data.PointType;
import app.model.data.SyzygyType;
import app.model.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PointEntry {
    private final PointType type;
    private final Double longitude;
    private final ZodiacSign sign;
    private final Double degreeInSign;
    private final Double latitude;
    private final Double declination;
    private final Double speed;
    private final Double meanDailySpeed;
    private final Double speedRatio;
    private final Boolean retrograde;
    private final Integer house;
    private final Angularity angularity;
    private final Double antisciaLongitude;
    private final Double contraAntisciaLongitude;
    private final Planet domicileRuler;
    private final Planet exaltationRuler;
    private final TriplicityRulers triplicityRulers;
    private final Planet termRuler;
    private final Planet faceRuler;
    private final Planet detrimentRuler;
    private final Planet fallRuler;
    private final SyzygyType syzygyType;
    private final Double julianDay;
    private final Instant approximateUtcInstant;
    private final Double sunLongitudeAtSyzygy;
    private final Double moonLongitudeAtSyzygy;
    private final Double angularSeparation;
    private final ZodiacSign sunSignAtSyzygy;
    private final ZodiacSign moonSignAtSyzygy;

    private PointEntry(Builder builder) {
        this.type = builder.type;
        this.longitude = builder.longitude;
        this.sign = builder.sign;
        this.degreeInSign = builder.degreeInSign;
        this.latitude = builder.latitude;
        this.declination = builder.declination;
        this.speed = builder.speed;
        this.meanDailySpeed = builder.meanDailySpeed;
        this.speedRatio = builder.speedRatio;
        this.retrograde = builder.retrograde;
        this.house = builder.house;
        this.angularity = builder.angularity;
        this.antisciaLongitude = builder.antisciaLongitude;
        this.contraAntisciaLongitude = builder.contraAntisciaLongitude;
        this.domicileRuler = builder.domicileRuler;
        this.exaltationRuler = builder.exaltationRuler;
        this.triplicityRulers = builder.triplicityRulers;
        this.termRuler = builder.termRuler;
        this.faceRuler = builder.faceRuler;
        this.detrimentRuler = builder.detrimentRuler;
        this.fallRuler = builder.fallRuler;
        this.syzygyType = builder.syzygyType;
        this.julianDay = builder.julianDay;
        this.approximateUtcInstant = builder.approximateUtcInstant;
        this.sunLongitudeAtSyzygy = builder.sunLongitudeAtSyzygy;
        this.moonLongitudeAtSyzygy = builder.moonLongitudeAtSyzygy;
        this.angularSeparation = builder.angularSeparation;
        this.sunSignAtSyzygy = builder.sunSignAtSyzygy;
        this.moonSignAtSyzygy = builder.moonSignAtSyzygy;
    }

    public static Builder builder(PointType type) {
        return new Builder(type);
    }

    public PointType getType() { return type; }
    public Double getLongitude() { return longitude; }
    public ZodiacSign getSign() { return sign; }
    public Double getDegreeInSign() { return degreeInSign; }
    public Double getLatitude() { return latitude; }
    public Double getDeclination() { return declination; }
    public Double getSpeed() { return speed; }
    public Double getMeanDailySpeed() { return meanDailySpeed; }
    public Double getSpeedRatio() { return speedRatio; }
    public Boolean getRetrograde() { return retrograde; }
    public Integer getHouse() { return house; }
    public Angularity getAngularity() { return angularity; }
    public Double getAntisciaLongitude() { return antisciaLongitude; }
    public Double getContraAntisciaLongitude() { return contraAntisciaLongitude; }
    public Planet getDomicileRuler() { return domicileRuler; }
    public Planet getExaltationRuler() { return exaltationRuler; }
    public TriplicityRulers getTriplicityRulers() { return triplicityRulers; }
    public Planet getTermRuler() { return termRuler; }
    public Planet getFaceRuler() { return faceRuler; }
    public Planet getDetrimentRuler() { return detrimentRuler; }
    public Planet getFallRuler() { return fallRuler; }
    public SyzygyType getSyzygyType() { return syzygyType; }
    public Double getJulianDay() { return julianDay; }
    public Instant getApproximateUtcInstant() { return approximateUtcInstant; }
    public Double getSunLongitudeAtSyzygy() { return sunLongitudeAtSyzygy; }
    public Double getMoonLongitudeAtSyzygy() { return moonLongitudeAtSyzygy; }
    public Double getAngularSeparation() { return angularSeparation; }
    public ZodiacSign getSunSignAtSyzygy() { return sunSignAtSyzygy; }
    public ZodiacSign getMoonSignAtSyzygy() { return moonSignAtSyzygy; }

    public static final class Builder {
        private final PointType type;
        private Double longitude;
        private ZodiacSign sign;
        private Double degreeInSign;
        private Double latitude;
        private Double declination;
        private Double speed;
        private Double meanDailySpeed;
        private Double speedRatio;
        private Boolean retrograde;
        private Integer house;
        private Angularity angularity;
        private Double antisciaLongitude;
        private Double contraAntisciaLongitude;
        private Planet domicileRuler;
        private Planet exaltationRuler;
        private TriplicityRulers triplicityRulers;
        private Planet termRuler;
        private Planet faceRuler;
        private Planet detrimentRuler;
        private Planet fallRuler;
        private SyzygyType syzygyType;
        private Double julianDay;
        private Instant approximateUtcInstant;
        private Double sunLongitudeAtSyzygy;
        private Double moonLongitudeAtSyzygy;
        private Double angularSeparation;
        private ZodiacSign sunSignAtSyzygy;
        private ZodiacSign moonSignAtSyzygy;

        private Builder(PointType type) {
            this.type = type;
        }

        public Builder longitude(double longitude) { this.longitude = longitude; return this; }
        public Builder sign(ZodiacSign sign) { this.sign = sign; return this; }
        public Builder degreeInSign(double degreeInSign) { this.degreeInSign = degreeInSign; return this; }
        public Builder latitude(double latitude) { this.latitude = latitude; return this; }
        public Builder declination(double declination) { this.declination = declination; return this; }
        public Builder speed(double speed) { this.speed = speed; return this; }
        public Builder meanDailySpeed(double meanDailySpeed) { this.meanDailySpeed = meanDailySpeed; return this; }
        public Builder speedRatio(double speedRatio) { this.speedRatio = speedRatio; return this; }
        public Builder retrograde(boolean retrograde) { this.retrograde = retrograde; return this; }
        public Builder house(int house) { this.house = house; return this; }
        public Builder angularity(Angularity angularity) { this.angularity = angularity; return this; }
        public Builder antisciaLongitude(double antisciaLongitude) { this.antisciaLongitude = antisciaLongitude; return this; }
        public Builder contraAntisciaLongitude(double contraAntisciaLongitude) { this.contraAntisciaLongitude = contraAntisciaLongitude; return this; }
        public Builder domicileRuler(Planet domicileRuler) { this.domicileRuler = domicileRuler; return this; }
        public Builder exaltationRuler(Planet exaltationRuler) { this.exaltationRuler = exaltationRuler; return this; }
        public Builder triplicityRulers(TriplicityRulers triplicityRulers) { this.triplicityRulers = triplicityRulers; return this; }
        public Builder termRuler(Planet termRuler) { this.termRuler = termRuler; return this; }
        public Builder faceRuler(Planet faceRuler) { this.faceRuler = faceRuler; return this; }
        public Builder detrimentRuler(Planet detrimentRuler) { this.detrimentRuler = detrimentRuler; return this; }
        public Builder fallRuler(Planet fallRuler) { this.fallRuler = fallRuler; return this; }
        public Builder syzygyType(SyzygyType syzygyType) { this.syzygyType = syzygyType; return this; }
        public Builder julianDay(double julianDay) { this.julianDay = julianDay; return this; }
        public Builder approximateUtcInstant(Instant approximateUtcInstant) { this.approximateUtcInstant = approximateUtcInstant; return this; }
        public Builder sunLongitudeAtSyzygy(double sunLongitudeAtSyzygy) { this.sunLongitudeAtSyzygy = sunLongitudeAtSyzygy; return this; }
        public Builder moonLongitudeAtSyzygy(double moonLongitudeAtSyzygy) { this.moonLongitudeAtSyzygy = moonLongitudeAtSyzygy; return this; }
        public Builder angularSeparation(double angularSeparation) { this.angularSeparation = angularSeparation; return this; }
        public Builder sunSignAtSyzygy(ZodiacSign sunSignAtSyzygy) { this.sunSignAtSyzygy = sunSignAtSyzygy; return this; }
        public Builder moonSignAtSyzygy(ZodiacSign moonSignAtSyzygy) { this.moonSignAtSyzygy = moonSignAtSyzygy; return this; }
        public PointEntry build() { return new PointEntry(this); }
    }
}
