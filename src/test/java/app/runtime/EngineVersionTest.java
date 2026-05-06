package app.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EngineVersionTest {

    @Autowired
    private EngineVersion engineVersion;

    @Autowired
    private Environment environment;

    @Test
    void getReturnsConfiguredApplicationVersion() {
        String expected = environment.getProperty("mystro.engine-version");
        assertNotNull(expected, "mystro.engine-version must be configured for the test");
        assertEquals(expected, engineVersion.get());
    }

    @Test
    void getNeverReturnsNull() {
        assertNotNull(engineVersion.get());
    }
}
