package app.reading.description.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import app.chart.data.Planet;
import app.chart.data.PointKey;
import app.chart.data.ZodiacSign;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DerivedHouseFramesEntry(
        DerivedHouseFrameEntry fromFortune,
        DerivedHouseFrameEntry fromSpirit
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DerivedHouseFrameEntry(
            String lot,
            String displayName,
            String doctrine,
            ZodiacSign anchorSign,
            int anchorNatalHouse,
            List<DerivedHousePlaceEntry> places
    ) {
        public DerivedHouseFrameEntry {
            places = places == null ? List.of() : List.copyOf(places);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DerivedHousePlaceEntry(
            int houseFromLot,
            ZodiacSign sign,
            int natalHouse,
            Planet ruler,
            List<PointKey> occupiedPoints
    ) {
        public DerivedHousePlaceEntry {
            occupiedPoints = occupiedPoints == null ? List.of() : List.copyOf(occupiedPoints);
        }
    }
}
