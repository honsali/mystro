package app;

public final class AppVersion {
    private static final String FALLBACK_ENGINE_VERSION = "1.33.0";

    public static String get() {
        String implementationVersion = AppVersion.class.getPackage().getImplementationVersion();
        return implementationVersion == null || implementationVersion.isBlank()
                ? FALLBACK_ENGINE_VERSION
                : implementationVersion;
    }

    private AppVersion() {}
}
