package app.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the engine version from the best available source.
 * Resolution order:
 * <ol>
 *   <li>Package implementation version (works in packaged jars)</li>
 *   <li>Classpath Maven metadata ({@code /META-INF/maven/mystro/mystro/pom.properties})</li>
 *   <li>Project-root {@code pom.xml} (development runs)</li>
 *   <li>{@code "unknown"} as final fallback</li>
 * </ol>
 */
public final class EngineVersion {

    private static final String VERSION = resolve();

    public static String get() {
        return VERSION;
    }

    private static String resolve() {
        String v = fromPackageImplementation();
        if (v != null) return v;

        v = fromMavenProperties();
        if (v != null) return v;

        v = fromPomXml();
        if (v != null) return v;

        return "unknown";
    }

    private static String fromPackageImplementation() {
        Package pkg = EngineVersion.class.getPackage();
        if (pkg != null) {
            String v = pkg.getImplementationVersion();
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String fromMavenProperties() {
        try (InputStream is = EngineVersion.class.getResourceAsStream(
                "/META-INF/maven/mystro/mystro/pom.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String v = props.getProperty("version");
                if (v != null && !v.isBlank()) {
                    return v.trim();
                }
            }
        } catch (IOException ignored) {
            // Fall through
        }
        return null;
    }

    private static String fromPomXml() {
        try {
            String pom = Files.readString(Path.of("pom.xml"));
            Matcher matcher = Pattern.compile("<version>([^<]+)</version>").matcher(pom);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (IOException ignored) {
            // Fall through
        }
        return null;
    }

    private EngineVersion() {}
}
