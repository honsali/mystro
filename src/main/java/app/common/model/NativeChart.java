package app.common.model;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NativeChart {
    private OffsetDateTime birthDateTime;
    private double julianDayUt;
    private boolean diurnal;
    private String ascSign;
    private double ascSignLon;
    private String mcSign;
    private double mcSignLon;
    private Map<String, ChartPoint> points = new LinkedHashMap<>();
    private double[] cusps = new double[0];
    private double[] ascmc = new double[0];

    public NativeChart() {}

    public OffsetDateTime birthDateTime() {
        return birthDateTime;
    }

    public void birthDateTime(OffsetDateTime birthDateTime) {
        this.birthDateTime = birthDateTime;
    }

    public OffsetDateTime getBirthDateTime() {
        return birthDateTime;
    }

    public void setBirthDateTime(OffsetDateTime birthDateTime) {
        this.birthDateTime = birthDateTime;
    }

    public double julianDayUt() {
        return julianDayUt;
    }

    public void julianDayUt(double julianDayUt) {
        this.julianDayUt = julianDayUt;
    }

    public double getJulianDayUt() {
        return julianDayUt;
    }

    public void setJulianDayUt(double julianDayUt) {
        this.julianDayUt = julianDayUt;
    }

    public boolean diurnal() {
        return diurnal;
    }

    public void diurnal(boolean diurnal) {
        this.diurnal = diurnal;
    }

    public boolean isDiurnal() {
        return diurnal;
    }

    public void setDiurnal(boolean diurnal) {
        this.diurnal = diurnal;
    }

    public String ascSign() {
        return ascSign;
    }

    public void ascSign(String ascSign) {
        this.ascSign = ascSign;
    }

    public String getAscSign() {
        return ascSign;
    }

    public void setAscSign(String ascSign) {
        this.ascSign = ascSign;
    }

    public double ascSignLon() {
        return ascSignLon;
    }

    public void ascSignLon(double ascSignLon) {
        this.ascSignLon = ascSignLon;
    }

    public double getAscSignLon() {
        return ascSignLon;
    }

    public void setAscSignLon(double ascSignLon) {
        this.ascSignLon = ascSignLon;
    }

    public String mcSign() {
        return mcSign;
    }

    public void mcSign(String mcSign) {
        this.mcSign = mcSign;
    }

    public String getMcSign() {
        return mcSign;
    }

    public void setMcSign(String mcSign) {
        this.mcSign = mcSign;
    }

    public double mcSignLon() {
        return mcSignLon;
    }

    public void mcSignLon(double mcSignLon) {
        this.mcSignLon = mcSignLon;
    }

    public double getMcSignLon() {
        return mcSignLon;
    }

    public void setMcSignLon(double mcSignLon) {
        this.mcSignLon = mcSignLon;
    }

    public Map<String, ChartPoint> points() {
        return points;
    }

    public void points(Map<String, ChartPoint> points) {
        this.points = points == null ? new LinkedHashMap<>() : new LinkedHashMap<>(points);
    }

    public Map<String, ChartPoint> getPoints() {
        return points;
    }

    public void setPoints(Map<String, ChartPoint> points) {
        this.points = points == null ? new LinkedHashMap<>() : new LinkedHashMap<>(points);
    }

    public double[] cusps() {
        return cusps;
    }

    public void cusps(double[] cusps) {
        this.cusps = cusps == null ? new double[0] : cusps.clone();
    }

    public double[] getCusps() {
        return cusps;
    }

    public void setCusps(double[] cusps) {
        this.cusps = cusps == null ? new double[0] : cusps.clone();
    }

    public double[] ascmc() {
        return ascmc;
    }

    public void ascmc(double[] ascmc) {
        this.ascmc = ascmc == null ? new double[0] : ascmc.clone();
    }

    public double[] getAscmc() {
        return ascmc;
    }

    public void setAscmc(double[] ascmc) {
        this.ascmc = ascmc == null ? new double[0] : ascmc.clone();
    }

    public ChartPoint point(String id) {
        return points.get(id);
    }
}
