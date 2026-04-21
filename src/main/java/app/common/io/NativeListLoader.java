package app.common.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import app.common.model.NativeBirth;

public final class NativeListLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NativeListLoader() {
    }

    public static List<NativeBirth> load(Path path) throws IOException {
        return new ArrayList<>(loadByName(path).values());
    }

    public static List<String> loadNames(Path path) throws IOException {
        return new ArrayList<>(loadByName(path).keySet());
    }

    public static Map<String, NativeBirth> loadByName(Path path) throws IOException {
        Map<String, NativeBirth> byName = new LinkedHashMap<>();
        JsonNode root = MAPPER.readTree(path.toFile());
        for (JsonNode entry : root) {
            String name = entry.path("name").asText(null);
            if (name == null || name.isBlank()) {
                throw new IOException("Missing name in native-list entry: " + entry);
            }
            byName.put(name, MAPPER.treeToValue(entry, NativeBirth.class));
        }
        return byName;
    }
}
