package app.output;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import app.input.model.Input;

public final class Logger {

    public static final Logger instance = new Logger();

    private final Instant startedAt = Instant.now();
    private final List<LogEntry> entries = new ArrayList<>();

    private Logger() {}

    public Instant getStartedAt() {
        return startedAt;
    }

    public List<LogEntry> getEntries() {
        return List.copyOf(entries);
    }

    public void info(String scope, String message) {
        entries.add(new LogEntry("INFO", scope, message));
    }

    public void error(Input input, String message) {
        entries.add(new LogEntry("ERROR", input.getSubject().getId(), message));
    }

    public void error(String scope, String message) {
        entries.add(new LogEntry("ERROR", scope, message));
    }

    public boolean hasErrors() {
        return entries.stream().anyMatch(entry -> "ERROR".equals(entry.getLevel()));
    }
}
