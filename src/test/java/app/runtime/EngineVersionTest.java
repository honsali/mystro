package app.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EngineVersionTest {

    @Test
    void getReturnsCurrentProjectVersion() {
        String expected = readVersionFromPom();
        assertNotNull(expected, "Could not read version from pom.xml for test assertion");
        assertEquals(expected, EngineVersion.get());
    }

    @Test
    void getNeverReturnsNull() {
        assertNotNull(EngineVersion.get());
    }

    /**
     * Read the first {@code <version>} from pom.xml to avoid hardcoding.
     */
    private static String readVersionFromPom() {
        try {
            String pom = Files.readString(Path.of("pom.xml"));
            Matcher matcher = Pattern.compile("<version>([^<]+)</version>").matcher(pom);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
