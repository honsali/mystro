package app.common;

import java.util.ArrayList;
import java.util.List;

public final class Logger {
    public record ErrorEntry(String code, String name, String message) {
    }

    private static final Logger INSTANCE = new Logger();

    public static Logger getInstance() {
        return INSTANCE;
    }

    private final List<ErrorEntry> errors = new ArrayList<>();

    private Logger() {
    }

    public void clear() {
        errors.clear();
    }

    public void error(String code, String name, String message) {
        errors.add(new ErrorEntry(code, name, message));
    }

    public void missingNativeConfig(String name) {
        error("MISSING_NATIVE_CONFIG", name, "Missing native config entry");
    }

    public void missingAstroSeekHtml(String name) {
        error("MISSING_ASTROSEEK_HTML", name, "Missing Astro-Seek HTML file");
    }

    public void missingMystroJson(String name) {
        error("MISSING_MYSTRO_JSON", name, "Missing Mystro JSON output");
    }

    public void missingAstroSeekJson(String name) {
        error("MISSING_ASTROSEEK_JSON", name, "Missing Astro-Seek JSON output");
    }

    public boolean hasError(String code, String name) {
        return errors.stream().anyMatch(error -> error.code().equals(code) && error.name().equals(name));
    }

    public long count(String code) {
        return errors.stream().filter(error -> error.code().equals(code)).count();
    }

    public List<ErrorEntry> errors() {
        return List.copyOf(errors);
    }

    public void printErrors() {
        if (errors.isEmpty()) {
            return;
        }
        System.out.println("errors:");
        for (ErrorEntry error : errors) {
            System.out.println("- [" + error.code() + "] " + error.name() + ": " + error.message());
        }
    }
}
