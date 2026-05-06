package app.web.infra;

import app.output.Logger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Wraps {@code /api/**} requests in isolated per-thread logging.
 * Any {@link Logger#instance} calls during the request lifecycle
 * write to ephemeral thread-local entries that are cleared after the response.
 * Non-API requests are not affected by this filter.
 */
@Component
public class LoggerIsolationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Logger.instance.runIsolated(() -> {
                filterChain.doFilter(request, response);
                return null;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof ServletException se) throw se;
            if (e instanceof IOException ioe) throw ioe;
            throw new ServletException(e);
        }
    }
}
