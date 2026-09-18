package com.example.tts.ratelimit;

import com.example.tts.config.RateLimitProperties;
import com.example.tts.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protects POST /api/tts - the one endpoint that actually costs money
 * (AWS Polly characters) - from being hammered. Every other endpoint
 * (health, voices, history, audio playback) is left untouched.
 * <p>
 * A client that exceeds the limit gets HTTP 429 with a JSON body in the
 * same shape as GlobalExceptionHandler's other error responses, plus a
 * standard Retry-After header.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final SlidingWindowRateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitFilter(SlidingWindowRateLimiter rateLimiter, RateLimitProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        boolean isProtectedEndpoint = "/api/tts".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());

        if (!properties.isEnabled() || !isProtectedEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = resolveClientId(request);
        boolean allowed = rateLimiter.tryConsume(
                clientId,
                properties.getRequestsPerWindow(),
                properties.getWindowSeconds()
        );

        if (!allowed) {
            long retryAfterSeconds = rateLimiter.secondsUntilReset(clientId, properties.getWindowSeconds());

            ErrorResponseDto body = new ErrorResponseDto(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    String.format(
                            "Rate limit exceeded: max %d requests per %d seconds. Try again in %d second(s).",
                            properties.getRequestsPerWindow(), properties.getWindowSeconds(), retryAfterSeconds
                    ),
                    request.getRequestURI()
            );

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Prefers X-Forwarded-For (set by reverse proxies like Render/Railway/nginx
     * when you deploy) so limiting works correctly behind a proxy, falling
     * back to the raw socket address for local development.
     */
    private String resolveClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}