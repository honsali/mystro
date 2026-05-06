package app.web.infra;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying that the Mystro ObjectMapper is scoped to the REST
 * message converter and not exposed as the global application-wide bean.
 */
@SpringBootTest
class WebConfigObjectMapperTest {

    @Autowired
    private MappingJackson2HttpMessageConverter converter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void converterRoundsDoublesToSixDecimals() throws Exception {
        ObjectMapper converterMapper = converter.getObjectMapper();
        String json = converterMapper.writeValueAsString(1.1234567890123456);
        assertEquals("1.123457", json,
                "Converter ObjectMapper should use RoundedDoubleSerializer");
    }

    @Test
    void globalObjectMapperDoesNotRoundDoublesToSixDecimals() throws Exception {
        String json = objectMapper.writeValueAsString(1.1234567890123456);
        assertNotEquals("1.123457", json,
                "Global application ObjectMapper should not use RoundedDoubleSerializer");
    }
}
