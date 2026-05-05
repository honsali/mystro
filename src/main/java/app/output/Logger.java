package app.output;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public final class Logger {

    public static final Logger instance = new Logger();

    private static final ThreadLocal<List<LogEntry>> isolatedEntries = new ThreadLocal<>();

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
        LogEntry entry = new LogEntry("INFO", scope, message);
        List<LogEntry> isolated = isolatedEntries.get();
        if (isolated != null) {
            isolated.add(entry);
        } else {
            entries.add(entry);
        }
    }

    public void error(String scope, String message) {
        LogEntry entry = new LogEntry("ERROR", scope, message);
        List<LogEntry> isolated = isolatedEntries.get();
        if (isolated != null) {
            isolated.add(entry);
        } else {
            entries.add(entry);
        }
    }

    public boolean hasErrors() {
        List<LogEntry> isolated = isolatedEntries.get();
        if (isolated != null) {
            return isolated.stream().anyMatch(entry -> "ERROR".equals(entry.getLevel()));
        }
        return entries.stream().anyMatch(entry -> "ERROR".equals(entry.getLevel()));
    }

    /**
     * Run a block with isolated per-thread logging. Calls to {@link #info} and
     * {@link #error} during the block write to a thread-local list instead of the
     * global CLI log. The isolated list is cleared when the block finishes
     * (normally or exceptionally).
     */
    public <T> T runIsolated(Callable<T> callable) throws Exception {
        isolatedEntries.set(new ArrayList<>());
        try {
            return callable.call();
        } finally {
            isolatedEntries.remove();
        }
    }

    /**
     * Run a block with isolated per-thread logging (void variant).
     */
    public void runIsolatedVoid(Runnable runnable) {
        isolatedEntries.set(new ArrayList<>());
        try {
            runnable.run();
        } finally {
            isolatedEntries.remove();
        }
    }

    /**
     * Returns true if isolated logging is active on the current thread.
     * Package-private for testing.
     */
    static boolean isIsolated() {
        return isolatedEntries.get() != null;
    }
}
