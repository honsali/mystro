package app.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads one local native-list.json alias entry into the public NatalInput DTO.
 */
public final class NatalInputLoader {

    public static final Path DEFAULT_PATH = Path.of("native-list.json");

    public NatalInput load(String alias) throws IOException {
        return load(DEFAULT_PATH, alias);
    }

    public NatalInput load(Path nativeListPath, String alias) throws IOException {
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("Native-list alias is required");
        }
        if (nativeListPath == null || !Files.exists(nativeListPath)) {
            Path missingPath = nativeListPath == null ? DEFAULT_PATH : nativeListPath;
            throw new IllegalArgumentException("Native-list file not found: " + missingPath.toAbsolutePath());
        }
        NatalInput[] entries = new ObjectMapper().readValue(nativeListPath.toFile(), NatalInput[].class);
        return Arrays.stream(entries == null ? new NatalInput[0] : entries)
                .filter(candidate -> matchesAlias(candidate, alias))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No native-list.json entry found for alias: " + alias));
    }

    private boolean matchesAlias(NatalInput entry, String alias) {
        return entry != null
                && entry.id() != null
                && entry.id().trim().equalsIgnoreCase(alias.trim());
    }
}
