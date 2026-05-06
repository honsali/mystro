package app.runtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring-managed engine version sourced from application configuration.
 */
@Component
public final class EngineVersion {

    private final String version;

    public EngineVersion(@Value("${mystro.engine-version}") String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("mystro.engine-version must be configured");
        }
        this.version = version.trim();
    }

    public String get() {
        return version;
    }
}
