package app.web.business;

import app.runtime.EngineVersion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DescriptiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EngineVersion engineVersion;

    private static final String VALID_REQUEST = """
            {
                "id": "test-subject",
                "birthDate": "1975-07-14",
                "birthTime": "22:55:00",
                "utcOffset": "+01:00",
                "latitude": 50.606008,
                "longitude": 3.033377,
                "doctrine": "valens"
            }
            """;

    // --- GET /api/doctrines ---

    @Test
    void doctrinesReturnsDirectArrayOfRegisteredDoctrines() throws Exception {
        mockMvc.perform(get("/api/doctrines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                // Dorotheus
                .andExpect(jsonPath("$[0].id").value("dorotheus"))
                .andExpect(jsonPath("$[0].name").value("Dorotheus"))
                .andExpect(jsonPath("$[0].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$[0].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$[0].terms").value("EGYPTIAN"))
                .andExpect(jsonPath("$[0].triplicity").value("DOROTHEAN"))
                .andExpect(jsonPath("$[0].nodeType").value("MEAN"))
                // Ptolemy
                .andExpect(jsonPath("$[1].id").value("ptolemy"))
                .andExpect(jsonPath("$[1].name").value("Ptolemy"))
                .andExpect(jsonPath("$[1].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$[1].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$[1].terms").value("PTOLEMAIC"))
                .andExpect(jsonPath("$[1].triplicity").value("PTOLEMAIC"))
                .andExpect(jsonPath("$[1].nodeType").value("MEAN"))
                // Valens
                .andExpect(jsonPath("$[2].id").value("valens"))
                .andExpect(jsonPath("$[2].name").value("Valens"))
                .andExpect(jsonPath("$[2].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$[2].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$[2].terms").value("EGYPTIAN"))
                .andExpect(jsonPath("$[2].triplicity").value("DOROTHEAN"))
                .andExpect(jsonPath("$[2].nodeType").value("MEAN"));
    }

    // --- CORS preflight ---

    @Test
    void corsAllowsConfiguredReactOrigin() throws Exception {
        mockMvc.perform(options("/api/descriptive")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsAllowsSecondConfiguredReactOrigin() throws Exception {
        mockMvc.perform(options("/api/descriptive")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void corsRejectsDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/descriptive")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    // --- POST /api/descriptive success ---

    @Test
    void descriptiveReturnsDirectReportWithExpectedTopLevelFields() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.engineVersion").value(engineVersion.get()))
                .andExpect(jsonPath("$.subject").exists())
                .andExpect(jsonPath("$.subject.id").value("test-subject"))
                .andExpect(jsonPath("$.doctrine").exists())
                .andExpect(jsonPath("$.doctrine.id").value("valens"))
                .andExpect(jsonPath("$.natalChart").exists())
                .andExpect(jsonPath("$.basicChart").doesNotExist())
                .andExpect(jsonPath("$.descriptive").doesNotExist())
                .andExpect(jsonPath("$.calculationSetting").doesNotExist())
                .andExpect(jsonPath("$.report").doesNotExist())
                .andExpect(jsonPath("$.suggestedFilename").doesNotExist());
    }

    @Test
    void descriptiveRoundsDoublesToSixDecimals() throws Exception {
        String request = """
                {
                    "id": "rounding-test",
                    "birthDate": "1975-07-14",
                    "birthTime": "22:55:00",
                    "utcOffset": "+01:00",
                    "latitude": 50.60600755996812,
                    "longitude": 3.0333769552426793,
                    "doctrine": "valens"
                }
                """;
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject.latitude").value(50.606008))
                .andExpect(jsonPath("$.subject.longitude").value(3.033377));
    }

    // --- POST /api/descriptive validation errors ---

    @Test
    void descriptiveReturns400ForUnknownDoctrine() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-subject",
                                    "birthDate": "1975-07-14",
                                    "birthTime": "22:55:00",
                                    "utcOffset": "+01:00",
                                    "latitude": 50.606008,
                                    "longitude": 3.033377,
                                    "doctrine": "nonexistent"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.error").value("Unknown doctrine: nonexistent"));
    }

    @Test
    void descriptiveReturns400ForMissingDoctrine() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-subject",
                                    "birthDate": "1975-07-14",
                                    "birthTime": "22:55:00",
                                    "utcOffset": "+01:00",
                                    "latitude": 50.606008,
                                    "longitude": 3.033377
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctrine id is required"));
    }

    @Test
    void descriptiveReturns400ForNullDoctrine() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-subject",
                                    "birthDate": "1975-07-14",
                                    "birthTime": "22:55:00",
                                    "utcOffset": "+01:00",
                                    "latitude": 50.606008,
                                    "longitude": 3.033377,
                                    "doctrine": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctrine id is required"));
    }

    @Test
    void descriptiveReturns400ForBlankDoctrine() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-subject",
                                    "birthDate": "1975-07-14",
                                    "birthTime": "22:55:00",
                                    "utcOffset": "+01:00",
                                    "latitude": 50.606008,
                                    "longitude": 3.033377,
                                    "doctrine": "  "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctrine id is required"));
    }

    @Test
    void descriptiveReturns400ForInvalidLatitude() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-subject",
                                    "birthDate": "1975-07-14",
                                    "birthTime": "22:55:00",
                                    "utcOffset": "+01:00",
                                    "latitude": 999,
                                    "longitude": 3.033377,
                                    "doctrine": "valens"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Latitude out of range: 999.0"));
    }

    @Test
    void descriptiveReturns400ForMalformedBody() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed or missing request body"));
    }

    @Test
    void descriptiveReturns400ForEmptyBody() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request body is required"));
    }

    @Test
    void descriptiveReturns400ForLiteralJsonNullBody() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request body is required"));
    }
}
