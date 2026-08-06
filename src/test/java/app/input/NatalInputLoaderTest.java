package app.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.testing.SyntheticTestData;

class NatalInputLoaderTest {

    private final NatalInputLoader loader = new NatalInputLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadsAliasFromNativeListWithoutInterpretingFields() throws Exception {
        Path nativeList = writeNativeList("""
                [
                  {
                    "name": " synthetic-j2000-greenwich ",
                    "birth_date": "01/01/2000",
                    "birth_time": "12:00",
                    "latitude": 51.4769,
                    "longitude": 0.0,
                    "elevation_meters": 46.0,
                    "utc_offset": "+00:00",
                    "inquiry_date": "15/01/2025"
                  }
                ]
                """);

        NatalInput input = loader.load(nativeList, "SYNTHETIC-J2000-GREENWICH");

        assertEquals(" synthetic-j2000-greenwich ", input.id());
        assertEquals("01/01/2000", input.birthDate());
        assertEquals("12:00", input.birthTime());
        assertEquals(SyntheticTestData.LATITUDE, input.latitude());
        assertEquals(SyntheticTestData.LONGITUDE, input.longitude());
        assertEquals(46.0, input.elevationMeters());
        assertEquals("+00:00", input.utcOffset());
        assertEquals("15/01/2025", input.inquiryDate());
    }

    @Test
    void rejectsUnknownAlias() throws Exception {
        Path nativeList = writeNativeList("""
                [
                  {
                    "name": "synthetic-j2000-greenwich",
                    "birth_date": "01/01/2000",
                    "birth_time": "12:00",
                    "latitude": 51.4769,
                    "longitude": 0.0,
                    "utc_offset": "+00:00"
                  }
                ]
                """);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> loader.load(nativeList, "missing"));

        assertEquals("No native-list.json entry found for alias: missing", ex.getMessage());
    }

    private Path writeNativeList(String json) throws Exception {
        Path path = tempDir.resolve("native-list.json");
        Files.writeString(path, json);
        return path;
    }
}
