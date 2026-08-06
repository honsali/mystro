package app.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Raw natal data loaded from one native-list.json entry. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NatalInput(
        @JsonProperty("name") String id,
        @JsonProperty("birth_date") String birthDate,
        @JsonProperty("birth_time") String birthTime,
        @JsonProperty("inquiry_date") String inquiryDate,
        @JsonProperty("utc_offset") String utcOffset,
        @JsonProperty("latitude") Double latitude,
        @JsonProperty("longitude") Double longitude,
        @JsonProperty("elevation_meters") Double elevationMeters
) {}
