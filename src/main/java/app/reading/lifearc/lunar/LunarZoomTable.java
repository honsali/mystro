package app.reading.lifearc.lunar;

import java.time.OffsetDateTime;
import java.util.List;

public record LunarZoomTable(
        String methodId,
        String primaryDoctrine,
        String signIngressMethod,
        OffsetDateTime windowStartDateTime,
        OffsetDateTime windowEndDateTime,
        List<LunarSignIngressEntry> signIngresses
) {
    public LunarZoomTable {
        signIngresses = signIngresses == null ? List.of() : List.copyOf(signIngresses);
    }
}
