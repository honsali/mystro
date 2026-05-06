package app.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorsPropertiesTest {

    @Test
    void normalizedAllowedOriginsTrimsWhitespace() {
        CorsProperties properties = new CorsProperties(
                List.of(" http://localhost:5173 ", " http://localhost:3000 "));

        assertEquals(
                List.of("http://localhost:5173", "http://localhost:3000"),
                properties.normalizedAllowedOrigins());
    }

    @Test
    void normalizedAllowedOriginsIgnoresBlankEntries() {
        CorsProperties properties = new CorsProperties(
                List.of("", "  ", "http://localhost:5173", " "));

        assertEquals(
                List.of("http://localhost:5173"),
                properties.normalizedAllowedOrigins());
    }

    @Test
    void normalizedAllowedOriginsFallsBackToDefaultsWhenMissing() {
        CorsProperties properties = new CorsProperties(null);

        assertEquals(CorsProperties.DEFAULT_ALLOWED_ORIGINS, properties.normalizedAllowedOrigins());
    }

    @Test
    void normalizedAllowedOriginsFallsBackToDefaultsWhenOnlyBlanks() {
        CorsProperties properties = new CorsProperties(List.of(" ", ""));

        assertEquals(CorsProperties.DEFAULT_ALLOWED_ORIGINS, properties.normalizedAllowedOrigins());
    }

    @Test
    void normalizedAllowedOriginsSingleOrigin() {
        CorsProperties properties = new CorsProperties(List.of("http://example.com"));

        assertEquals(List.of("http://example.com"), properties.normalizedAllowedOrigins());
    }
}
