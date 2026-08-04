package app.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.io.MystroObjectMapper;
import app.testing.SyntheticTestData;

class NativeListInputLoaderTest {

    private final NativeListInputLoader loader = new NativeListInputLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadsAliasFromNativeListAndNormalizesFields() throws Exception {
        Path nativeList = writeNativeList("""
                [
                  {
                    "name": "synthetic-j2000-greenwich",
                    "birth_date": "01/01/2000",
                    "birth_time": "12:00",
                    "latitude": 51.4769,
                    "longitude": 0.0,
                    "utc_offset": "+00:00",
                    "inquiry_date": "15/01/2025"
                  }
                ]
                """);

        ReadingInput input = loader.load(nativeList, "SYNTHETIC-J2000-GREENWICH", MystroObjectMapper.create());

        assertEquals(SyntheticTestData.SUBJECT_ID, input.getId());
        assertEquals("2000-01-01", input.getBirthDate());
        assertEquals("12:00:00", input.getBirthTime());
        assertEquals(SyntheticTestData.LATITUDE, input.getLatitude());
        assertEquals(SyntheticTestData.LONGITUDE, input.getLongitude());
        assertEquals("+00:00", input.getUtcOffset());
        assertEquals("2025-01-15", input.getInquiryDate());
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
                () -> loader.load(nativeList, "missing", MystroObjectMapper.create()));

        assertEquals("No native-list.json entry found for alias: missing (available aliases: synthetic-j2000-greenwich)", ex.getMessage());
    }

    private Path writeNativeList(String json) throws Exception {
        Path path = tempDir.resolve("native-list.json");
        Files.writeString(path, json);
        return path;
    }
}
