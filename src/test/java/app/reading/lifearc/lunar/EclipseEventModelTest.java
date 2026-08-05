package app.reading.lifearc.lunar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.chart.data.Planet;
import app.chart.data.ZodiacSign;
import app.chart.data.SyzygyType;

class EclipseEventModelTest {
    @Test
    void eclipseEventCarriesGlobalRealityAndFallbackCandidateReference() {
        OffsetDateTime maximum = OffsetDateTime.of(2026, 2, 17, 12, 0, 0, 0, ZoneOffset.UTC);
        EclipseContact maximumContact = new EclipseContact(
                EclipseContactPhase.MAXIMUM,
                maximum,
                2461089.0,
                EclipseVisibilityStatus.UNKNOWN,
                "global maximum; local visibility unresolved in model fixture"
        );

        EclipseEvent event = new EclipseEvent(
                1,
                TrueEclipseCalculationDesign.METHOD_ID,
                EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY,
                EclipseEventKind.SOLAR,
                SyzygyType.NEW_MOON,
                EclipseEventType.SOLAR_PARTIAL,
                EclipseCandidateType.SOLAR_ECLIPSE_CANDIDATE,
                maximum,
                2461089.0,
                330.25,
                ZodiacSign.PISCES,
                0.25,
                1,
                Planet.NORTH_NODE,
                331.0,
                0.75,
                0.42,
                0.16,
                null,
                150,
                17,
                List.of(maximumContact),
                EclipseVisibility.globalOnly("global true eclipse; local visibility not supplied in model fixture")
        );

        assertEquals(EclipseEventKind.SOLAR, event.kind());
        assertEquals(SyzygyType.NEW_MOON, event.syzygy());
        assertEquals(EclipseCandidateType.SOLAR_ECLIPSE_CANDIDATE, event.candidateReference());
        assertEquals(EclipseVisibilityStatus.UNKNOWN, event.visibility().localVisibility());
        assertTrue(event.visibility().globallyOccurs());
        assertEquals(List.of(maximumContact), event.contacts());
    }

    @Test
    void eclipseEventRejectsSyzygyMismatchAndInvalidNode() {
        OffsetDateTime maximum = OffsetDateTime.of(2026, 3, 3, 12, 0, 0, 0, ZoneOffset.UTC);

        assertThrows(IllegalArgumentException.class, () -> new EclipseEvent(
                1,
                TrueEclipseCalculationDesign.METHOD_ID,
                EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY,
                EclipseEventKind.SOLAR,
                SyzygyType.FULL_MOON,
                EclipseEventType.SOLAR_PARTIAL,
                EclipseCandidateType.NONE,
                maximum,
                2461103.0,
                163.0,
                ZodiacSign.VIRGO,
                13.0,
                7,
                Planet.NORTH_NODE,
                160.0,
                3.0,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                EclipseVisibility.globalOnly("test")
        ));

        assertThrows(IllegalArgumentException.class, () -> new EclipseEvent(
                1,
                TrueEclipseCalculationDesign.METHOD_ID,
                EclipseCalculationScope.GLOBAL_ECLIPSE_REALITY,
                EclipseEventKind.LUNAR,
                SyzygyType.FULL_MOON,
                EclipseEventType.LUNAR_TOTAL,
                EclipseCandidateType.LUNAR_ECLIPSE_CANDIDATE,
                maximum,
                2461103.0,
                163.0,
                ZodiacSign.VIRGO,
                13.0,
                7,
                Planet.MOON,
                160.0,
                3.0,
                1.2,
                null,
                1.8,
                null,
                null,
                List.of(),
                EclipseVisibility.globalOnly("test")
        ));
    }

    @Test
    void designNotesRecordReviewedSwissApisAndScopeDecision() {
        assertEquals("TRUE_ECLIPSE_EVENTS_SWISS_EPHEMERIS_GLOBAL_LOCAL_VISIBILITY_V2", TrueEclipseCalculationDesign.METHOD_ID);
        assertTrue(TrueEclipseCalculationDesign.SCOPE_DECISION.contains("GLOBAL_ECLIPSE_REALITY_WITH_SUBJECT_LOCATION_VISIBILITY"));
        assertTrue(TrueEclipseCalculationDesign.GLOBAL_SOLAR_APIS.contains("swe_sol_eclipse_when_glob"));
        assertTrue(TrueEclipseCalculationDesign.LOCAL_LUNAR_API.contains("swe_lun_eclipse_when_loc"));
    }
}
