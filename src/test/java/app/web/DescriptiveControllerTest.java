package app.web;

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
    void doctrinesReturnsAllRegisteredDoctrinesWithExactMetadata() throws Exception {
        mockMvc.perform(get("/api/doctrines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctrines").isArray())
                .andExpect(jsonPath("$.doctrines.length()").value(3))
                // Dorotheus
                .andExpect(jsonPath("$.doctrines[0].id").value("dorotheus"))
                .andExpect(jsonPath("$.doctrines[0].name").value("Dorotheus"))
                .andExpect(jsonPath("$.doctrines[0].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$.doctrines[0].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$.doctrines[0].terms").value("EGYPTIAN"))
                .andExpect(jsonPath("$.doctrines[0].triplicity").value("DOROTHEAN"))
                .andExpect(jsonPath("$.doctrines[0].nodeType").value("MEAN"))
                // Ptolemy
                .andExpect(jsonPath("$.doctrines[1].id").value("ptolemy"))
                .andExpect(jsonPath("$.doctrines[1].name").value("Ptolemy"))
                .andExpect(jsonPath("$.doctrines[1].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$.doctrines[1].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$.doctrines[1].terms").value("PTOLEMAIC"))
                .andExpect(jsonPath("$.doctrines[1].triplicity").value("PTOLEMAIC"))
                .andExpect(jsonPath("$.doctrines[1].nodeType").value("MEAN"))
                // Valens
                .andExpect(jsonPath("$.doctrines[2].id").value("valens"))
                .andExpect(jsonPath("$.doctrines[2].name").value("Valens"))
                .andExpect(jsonPath("$.doctrines[2].houseSystem").value("WHOLE_SIGN"))
                .andExpect(jsonPath("$.doctrines[2].zodiac").value("TROPICAL"))
                .andExpect(jsonPath("$.doctrines[2].terms").value("EGYPTIAN"))
                .andExpect(jsonPath("$.doctrines[2].triplicity").value("DOROTHEAN"))
                .andExpect(jsonPath("$.doctrines[2].nodeType").value("MEAN"));
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
    void descriptiveReturnsSingleReportWithExpectedTopLevelFields() throws Exception {
        mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.report").exists())
                .andExpect(jsonPath("$.report.engineVersion").value(EngineVersion.get()))
                .andExpect(jsonPath("$.report.subject").exists())
                .andExpect(jsonPath("$.report.subject.id").value("test-subject"))
                .andExpect(jsonPath("$.report.doctrine").exists())
                .andExpect(jsonPath("$.report.doctrine.id").value("valens"))
                .andExpect(jsonPath("$.report.natalChart").exists())
                .andExpect(jsonPath("$.report.basicChart").doesNotExist())
                .andExpect(jsonPath("$.report.descriptive").doesNotExist())
                .andExpect(jsonPath("$.report.calculationSetting").doesNotExist())
                .andExpect(jsonPath("$.suggestedFilename").value("test-subject-valens-descriptive.json"));
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
                .andExpect(jsonPath("$.report.subject.latitude").value(50.606008))
                .andExpect(jsonPath("$.report.subject.longitude").value(3.033377));
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
