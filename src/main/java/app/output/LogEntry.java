package app.output;

public final class LogEntry {
    private final String level;
    private final String scope;
    private final String message;

    public LogEntry(String level, String scope, String message) {
        this.level = level;
        this.scope = scope;
        this.message = message;
    }

    public String getLevel() { return level; }
    public String getScope() { return scope; }
    public String getMessage() { return message; }
}
