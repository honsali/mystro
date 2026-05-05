package app.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerTest {

    @AfterEach
    void cleanUp() {
        // Ensure no thread-local leaks between tests
        Logger.instance.getEntries().forEach(e -> {});
        // Reset by running an empty isolated block to clear any stale thread-local
        Logger.instance.runIsolatedVoid(() -> {});
    }

    @Test
    void globalInfoAddsToGlobalEntries() {
        int before = Logger.instance.getEntries().size();
        Logger.instance.info("test", "global message");
        assertEquals(before + 1, Logger.instance.getEntries().size());
    }

    @Test
    void isolatedInfoDoesNotPolluteGlobalEntries() {
        int globalBefore = Logger.instance.getEntries().size();
        Logger.instance.runIsolatedVoid(() -> {
            Logger.instance.info("test", "isolated message");
        });
        assertEquals(globalBefore, Logger.instance.getEntries().size());
    }

    @Test
    void isolatedEntriesAreClearedAfterBlock() {
        assertFalse(Logger.isIsolated());
        Logger.instance.runIsolatedVoid(() -> {
            assertTrue(Logger.isIsolated());
            Logger.instance.info("test", "inside block");
        });
        assertFalse(Logger.isIsolated());
    }

    @Test
    void isolatedEntriesAreClearedAfterException() {
        assertFalse(Logger.isIsolated());
        try {
            Logger.instance.runIsolated(() -> {
                Logger.instance.info("test", "before throw");
                throw new RuntimeException("boom");
            });
        } catch (Exception ignored) {
        }
        assertFalse(Logger.isIsolated());
        // Global entries should not contain the isolated log
        boolean hasIsolated = Logger.instance.getEntries().stream()
                .anyMatch(e -> "before throw".equals(e.getMessage()));
        assertFalse(hasIsolated);
    }

    @Test
    void isolatedHasErrorsReflectsIsolatedEntries() {
        Logger.instance.runIsolatedVoid(() -> {
            assertFalse(Logger.instance.hasErrors());
            Logger.instance.error("test", "isolated error");
            assertTrue(Logger.instance.hasErrors());
        });
    }

    @Test
    void runIsolatedReturnsCallableResult() throws Exception {
        String result = Logger.instance.runIsolated(() -> {
            Logger.instance.info("test", "inside");
            return "hello";
        });
        assertEquals("hello", result);
    }
}
