package app.reading.lifearc.lunar;

/**
 * Session 7-9 method notes for replacing mean-node eclipse candidates with true eclipse rows and local visibility.
 *
 * <p>Scope decision: global eclipse reality remains the row backbone. Subject-location visibility is
 * calculated as an attached visibility summary using the local Swiss Ephemeris eclipse APIs; true
 * global rows are retained even when the subject location cannot see the eclipse.</p>
 *
 * <p>Reviewed Swiss Ephemeris APIs available in the bundled source:</p>
 * <ul>
 *   <li>{@code swe_sol_eclipse_when_glob}: next global solar eclipse; returns {@code tret[0]}
 *       maximum, {@code tret[2]/[3]} global begin/end, {@code tret[4]/[5]} totality begin/end,
 *       {@code tret[6]/[7]} center-line begin/end.</li>
 *   <li>{@code swe_sol_eclipse_where}: central/maximum geographic location and solar attributes at
 *       a UT instant.</li>
 *   <li>{@code swe_sol_eclipse_how}: solar attributes for a given location and UT instant;
 *       attr[0] magnitude, attr[2] obscuration, attr[8] NASA-style magnitude, attr[9]/[10] Saros.</li>
 *   <li>{@code swe_sol_eclipse_when_loc}: local solar eclipse contacts and local visibility.</li>
 *   <li>{@code swe_lun_eclipse_when}: next global lunar eclipse; returns {@code tret[0]} maximum,
 *       {@code tret[2]/[3]} partial begin/end, {@code tret[4]/[5]} totality begin/end,
 *       {@code tret[6]/[7]} penumbral begin/end.</li>
 *   <li>{@code swe_lun_eclipse_how}: lunar attributes; attr[0] umbral magnitude, attr[1]
 *       penumbral magnitude, attr[8] umbral magnitude, attr[9]/[10] Saros.</li>
 *   <li>{@code swe_lun_eclipse_when_loc}: local lunar contacts and visibility.</li>
 * </ul>
 *
 * <p>Fallback/reference rule: existing {@link EclipseCandidateType} mean-node labels remain useful
 * as supporting candidate evidence. They should not be overwritten silently; true {@link EclipseEvent}
 * rows carry the matching candidate label when applicable so Markdown can compare
 * "candidate" versus "true eclipse" evidence.</p>
 */
public final class TrueEclipseCalculationDesign {
    public static final String METHOD_ID = "TRUE_ECLIPSE_EVENTS_SWISS_EPHEMERIS_GLOBAL_LOCAL_VISIBILITY_V2";
    public static final String SCOPE_DECISION = "GLOBAL_ECLIPSE_REALITY_WITH_SUBJECT_LOCATION_VISIBILITY; MEAN_NODE_CANDIDATES_RETAINED_AS_FALLBACK_REFERENCE";
    public static final String GLOBAL_SOLAR_APIS = "swe_sol_eclipse_when_glob + swe_sol_eclipse_where/swe_sol_eclipse_how";
    public static final String LOCAL_SOLAR_API = "swe_sol_eclipse_when_loc";
    public static final String GLOBAL_LUNAR_APIS = "swe_lun_eclipse_when + swe_lun_eclipse_how";
    public static final String LOCAL_LUNAR_API = "swe_lun_eclipse_when_loc";

    private TrueEclipseCalculationDesign() {}
}
