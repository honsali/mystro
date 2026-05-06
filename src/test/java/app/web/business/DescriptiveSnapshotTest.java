package app.web.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.output.RoundedDoubleSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression snapshot test for the representative ilia/Valens descriptive calculation.
 * Compares the full REST response JSON against a committed snapshot to catch
 * accidental changes to chart numbers, report structure, aspect data,
 * dignities/debilities, syzygy/lots, and serialization.
 *
 * The snapshot was generated from the current code and committed as a reference.
 * If the snapshot needs updating due to intentional calculation changes,
 * regenerate it and commit the new file.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DescriptiveSnapshotTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ILIA_VALENS_REQUEST = """
            {
                "id": "ilia",
                "birthDate": "1975-07-14",
                "birthTime": "22:55:00",
                "utcOffset": "+01:00",
                "latitude": 50.60600755996812,
                "longitude": 3.0333769552426793,
                "doctrine": "valens"
            }
            """;

    @Test
    void iliaValensDescriptiveMatchesSnapshot() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/descriptive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ILIA_VALENS_REQUEST))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();

        String actualJson = result.getResponse().getContentAsString();
        JsonNode actual = parseNormalized(actualJson);
        JsonNode expected = loadSnapshot();

        assertEquals(expected, actual, "Full ilia/valens descriptive response has changed. "
                + "If intentional, regenerate the snapshot and commit the updated file.");
    }

    private JsonNode parseNormalized(String json) throws Exception {
        ObjectMapper mapper = createMapper();
        JsonNode tree = mapper.readTree(json);
        // Re-serialize and re-parse to normalize field ordering
        String normalized = mapper.writeValueAsString(tree);
        return mapper.readTree(normalized);
    }

    private JsonNode loadSnapshot() throws Exception {
        ClassPathResource resource = new ClassPathResource("snapshots/descriptive/ilia-valens-response.json");
        try (InputStream is = resource.getInputStream()) {
            return createMapper().readTree(is);
        }
    }

    private ObjectMapper createMapper() {
        SimpleModule module = new SimpleModule()
                .addSerializer(Double.class, new RoundedDoubleSerializer())
                .addSerializer(Double.TYPE, new RoundedDoubleSerializer());
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .registerModule(module)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
