package app.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "mystro.cors")
public record CorsProperties(List<String> allowedOrigins) {

    static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:3000");

    public List<String> normalizedAllowedOrigins() {
        List<String> normalized = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        return normalized.isEmpty() ? DEFAULT_ALLOWED_ORIGINS : normalized;
    }
}
