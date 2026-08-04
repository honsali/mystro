package app.ephemeris;

/**
 * Swiss Ephemeris constants still used by Mystro's calculation layer.
 *
 * <p>The native binding exposes typed enums for new code. This deliberately
 * small compatibility set keeps the engine migration focused and prevents
 * the old Java port's very large constants surface from leaking back in.</p>
 */
public final class SweConstants {
    public static final int ERR = -1;
    public static final int OK = 0;

    public static final int SE_ECL_NUT = -1;
    public static final int SE_SUN = 0;
    public static final int SE_MOON = 1;
    public static final int SE_MERCURY = 2;
    public static final int SE_VENUS = 3;
    public static final int SE_MARS = 4;
    public static final int SE_JUPITER = 5;
    public static final int SE_SATURN = 6;
    public static final int SE_MEAN_NODE = 10;

    public static final int SEFLG_SWIEPH = 2;
    public static final int SEFLG_MOSEPH = 4;
    public static final int SEFLG_SPEED = 256;
    public static final int SEFLG_EQUATORIAL = 2_048;

    public static final int SE_ECL_TOTAL = 4;
    public static final int SE_ECL_ANNULAR = 8;
    public static final int SE_ECL_PARTIAL = 16;
    public static final int SE_ECL_ANNULAR_TOTAL = 32;
    public static final int SE_ECL_PENUMBRAL = 64;
    public static final int SE_ECL_VISIBLE = 128;
    public static final int SE_ECL_MAX_VISIBLE = 256;
    public static final int SE_ECL_1ST_VISIBLE = 512;
    public static final int SE_ECL_PARTBEG_VISIBLE = 512;
    public static final int SE_ECL_2ND_VISIBLE = 1_024;
    public static final int SE_ECL_TOTBEG_VISIBLE = 1_024;
    public static final int SE_ECL_3RD_VISIBLE = 2_048;
    public static final int SE_ECL_TOTEND_VISIBLE = 2_048;
    public static final int SE_ECL_4TH_VISIBLE = 4_096;
    public static final int SE_ECL_PARTEND_VISIBLE = 4_096;
    public static final int SE_ECL_PENUMBBEG_VISIBLE = 8_192;
    public static final int SE_ECL_PENUMBEND_VISIBLE = 16_384;

    public static final int SE_CALC_RISE = 1;
    public static final int SE_CALC_SET = 2;
    public static final int SE_ECL2HOR = 0;

    private SweConstants() {
    }
}
