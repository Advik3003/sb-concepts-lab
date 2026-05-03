package sb.concepts.lab.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String TRACEPARENT_HEADER = "traceparent";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String TRACE_ID_KEY = "traceId";
    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = firstNonBlank(
                request.getHeader(CORRELATION_ID_HEADER),
                request.getHeader(REQUEST_ID_HEADER),
                newRequestId()
        );
        String traceId = firstNonBlank(extractTraceId(request.getHeader(TRACEPARENT_HEADER)), requestId);

        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.setHeader(CORRELATION_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();
        try {
            log.info("Request started method={} path={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "Request completed method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );
            MDC.clear();
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return newRequestId();
    }

    private static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    private static String extractTraceId(String traceparent) {
        if (traceparent == null || traceparent.isBlank()) {
            return null;
        }

        String[] parts = traceparent.split("-");
        if (parts.length >= 2 && parts[1].length() == 32) {
            return parts[1];
        }
        return null;
    }
}
