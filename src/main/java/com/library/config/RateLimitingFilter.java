package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter giới hạn số lượng request (Rate Limiting) để chống spam và brute force.
 * Chạy sau JwtAuthenticationFilter để đọc được thông tin ADMIN từ SecurityContext.
 *
 * Quy tắc áp dụng:
 * - ADMIN: không bị giới hạn
 * - Auth endpoints (/api/auth/**): giới hạn theo IP (chống brute force)
 * - Authenticated user (non-ADMIN): giới hạn theo username
 * - Anonymous: giới hạn theo IP
 */
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RETRY_AFTER = "Retry-After";
    private static final String AUTH_PATH_PREFIX = "/api/auth/";

    private final RateLimitProperties rateLimitProperties;
    private final ConcurrentHashMap<String, Bucket> rateLimitBucketStore;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        // ADMIN không bị giới hạn
        if (isAuthenticated && hasAdminRole(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        boolean isAuthEndpoint = requestUri.startsWith(AUTH_PATH_PREFIX);

        String bucketKey;
        long capacity;

        if (isAuthEndpoint) {
            bucketKey = "auth:" + resolveClientIp(request);
            capacity = rateLimitProperties.getAuthRequestsPerMinute();
        } else if (isAuthenticated) {
            bucketKey = "user:" + auth.getName();
            capacity = rateLimitProperties.getAuthenticatedRequestsPerMinute();
        } else {
            bucketKey = "ip:" + resolveClientIp(request);
            capacity = rateLimitProperties.getAnonymousRequestsPerMinute();
        }

        Bucket bucket = rateLimitBucketStore.computeIfAbsent(bucketKey, k -> createBucket(capacity));

        if (bucket.tryConsume(1)) {
            response.setHeader(HEADER_REMAINING, String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);
        } else {
            sendTooManyRequestsResponse(response);
        }
    }

    private boolean hasAdminRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Bucket createBucket(long capacity) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HEADER_REMAINING, "0");
        response.setHeader(HEADER_RETRY_AFTER, "60");

        Map<String, Object> body = Map.of(
                "error", "Too Many Requests",
                "message", "Vượt quá giới hạn request. Vui lòng thử lại sau.",
                "retryAfter", 60
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
