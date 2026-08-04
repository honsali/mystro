package app.io;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class MystroObjectMapperTest {

    @Test
    void roundsDoublesToTwoDecimals() throws Exception {
        ObjectMapper objectMapper = MystroObjectMapper.create();

        String json = objectMapper.writeValueAsString(Map.of("value", 12.345));

        assertTrue(json.contains("12.35"));
    }
}
