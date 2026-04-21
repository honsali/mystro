package app.common.model;

public final class NativeHermeticLot {
    private String lot;
    private String formula;
    private String sign;
    private double signLon;
    private int house;
    private String ruler;
    private String rulerSign;
    private double rulerSignLon;
    private int rulerHouse;
    private String lotToRuler;
    private String fortuneToRuler;

    public NativeHermeticLot() {
    }

    public NativeHermeticLot(String lot, String formula, String sign, double signLon, int house, String ruler, String rulerSign, double rulerSignLon, int rulerHouse, String lotToRuler, String fortuneToRuler) {
        this.lot = lot;
        this.formula = formula;
        this.sign = sign;
        this.signLon = signLon;
        this.house = house;
        this.ruler = ruler;
        this.rulerSign = rulerSign;
        this.rulerSignLon = rulerSignLon;
        this.rulerHouse = rulerHouse;
        this.lotToRuler = lotToRuler;
        this.fortuneToRuler = fortuneToRuler;
    }

    public String lot() {
        return lot;
    }

    public void lot(String lot) {
        this.lot = lot;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String formula() {
        return formula;
    }

    public void formula(String formula) {
        this.formula = formula;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String sign() {
        return sign;
    }

    public void sign(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public double signLon() {
        return signLon;
    }

    public void signLon(double signLon) {
        this.signLon = signLon;
    }

    public double getSignLon() {
        return signLon;
    }

    public void setSignLon(double signLon) {
        this.signLon = signLon;
    }

    public int house() {
        return house;
    }

    public void house(int house) {
        this.house = house;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }

    public String ruler() {
        return ruler;
    }

    public void ruler(String ruler) {
        this.ruler = ruler;
    }

    public String getRuler() {
        return ruler;
    }

    public void setRuler(String ruler) {
        this.ruler = ruler;
    }

    public String rulerSign() {
        return rulerSign;
    }

    public void rulerSign(String rulerSign) {
        this.rulerSign = rulerSign;
    }

    public String getRulerSign() {
        return rulerSign;
    }

    public void setRulerSign(String rulerSign) {
        this.rulerSign = rulerSign;
    }

    public double rulerSignLon() {
        return rulerSignLon;
    }

    public void rulerSignLon(double rulerSignLon) {
        this.rulerSignLon = rulerSignLon;
    }

    public double getRulerSignLon() {
        return rulerSignLon;
    }

    public void setRulerSignLon(double rulerSignLon) {
        this.rulerSignLon = rulerSignLon;
    }

    public int rulerHouse() {
        return rulerHouse;
    }

    public void rulerHouse(int rulerHouse) {
        this.rulerHouse = rulerHouse;
    }

    public int getRulerHouse() {
        return rulerHouse;
    }

    public void setRulerHouse(int rulerHouse) {
        this.rulerHouse = rulerHouse;
    }

    public String lotToRuler() {
        return lotToRuler;
    }

    public void lotToRuler(String lotToRuler) {
        this.lotToRuler = lotToRuler;
    }

    public String getLotToRuler() {
        return lotToRuler;
    }

    public void setLotToRuler(String lotToRuler) {
        this.lotToRuler = lotToRuler;
    }

    public String fortuneToRuler() {
        return fortuneToRuler;
    }

    public void fortuneToRuler(String fortuneToRuler) {
        this.fortuneToRuler = fortuneToRuler;
    }

    public String getFortuneToRuler() {
        return fortuneToRuler;
    }

    public void setFortuneToRuler(String fortuneToRuler) {
        this.fortuneToRuler = fortuneToRuler;
    }
}
