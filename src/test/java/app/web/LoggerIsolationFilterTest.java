package app.web;

import app.output.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggerIsolationFilterTest {

    private final LoggerIsolationFilter filter = new LoggerIsolationFilter();

    // --- shouldNotFilter scoping ---

    @Test
    void shouldNotFilterReturnsFalseForApiDescriptive() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/api/descriptive");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterReturnsFalseForApiDoctrines() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/api/doctrines");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterReturnsTrueForRootPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterReturnsTrueForNonApiPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    // --- API request isolation (doFilterInternal) ---

    @Test
    void apiRequestLoggingDoesNotPolluteGlobal() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        int globalBefore = Logger.instance.getEntries().size();

        filter.doFilterInternal(request, response, (req, res) -> {
            Logger.instance.info("filter-test", "inside-api-request");
            Logger.instance.error("filter-test", "inside-api-request-error");
        });

        assertEquals(globalBefore, Logger.instance.getEntries().size(),
                "Logger calls during /api/** request should not pollute global entries");
    }

    @Test
    void apiRequestLoggingRestoresAfterException() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        int globalBefore = Logger.instance.getEntries().size();

        try {
            filter.doFilterInternal(request, response, (req, res) -> {
                Logger.instance.info("filter-test", "before-throw");
                throw new ServletException("chain failed");
            });
        } catch (ServletException ignored) {
        }

        assertEquals(globalBefore, Logger.instance.getEntries().size(),
                "Logger calls during failed /api/** request should not pollute global entries");
    }

    // --- Non-API request end-to-end (doFilter with MockHttpServletRequest) ---

    @Test
    void nonApiRequestLoggingPollutesGlobal() throws ServletException, IOException {
        // Use MockHttpServletRequest for a realistic non-API request
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        int globalBefore = Logger.instance.getEntries().size();

        filter.doFilter(request, response, (req, res) -> {
            Logger.instance.info("filter-test", "non-api-request-log");
        });

        assertEquals(globalBefore + 1, Logger.instance.getEntries().size(),
                "Logger calls during non-API request should pollute global entries (filter does not isolate)");
    }
}
