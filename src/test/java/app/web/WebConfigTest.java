package app.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class WebConfigTest {

    @Test
    void parseOriginsTrimsWhitespace() {
        assertArrayEquals(
                new String[]{"http://localhost:5173", "http://localhost:3000"},
                WebConfig.parseOrigins(" http://localhost:5173 , http://localhost:3000 "));
    }

    @Test
    void parseOriginsIgnoresBlankEntries() {
        assertArrayEquals(
                new String[]{"http://localhost:5173"},
                WebConfig.parseOrigins(",  ,http://localhost:5173, ,"));
    }

    @Test
    void parseOriginsFallsBackToDefaultsWhenEmpty() {
        assertArrayEquals(
                new String[]{"http://localhost:5173", "http://localhost:3000"},
                WebConfig.parseOrigins(""));
    }

    @Test
    void parseOriginsFallsBackToDefaultsWhenOnlyBlanksAndCommas() {
        assertArrayEquals(
                new String[]{"http://localhost:5173", "http://localhost:3000"},
                WebConfig.parseOrigins(" , , "));
    }

    @Test
    void parseOriginsSingleOrigin() {
        assertArrayEquals(
                new String[]{"http://example.com"},
                WebConfig.parseOrigins("http://example.com"));
    }
}
