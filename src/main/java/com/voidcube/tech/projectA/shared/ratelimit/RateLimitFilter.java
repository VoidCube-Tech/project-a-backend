package com.voidcube.tech.projectA.shared.ratelimit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.voidcube.tech.projectA.shared.exception.ApiErrorResponse;
import com.voidcube.tech.projectA.user.model.User;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final long BUCKET_CAPACITY = 100;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(10);
    private static final long MAXIMUM_CACHE_SIZE = 100_000;

    private final ObjectMapper objectMapper;
    private final Cache<String, Bucket> buckets;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.buckets = Caffeine.newBuilder()
                .maximumSize(MAXIMUM_CACHE_SIZE)
                .expireAfterAccess(CACHE_EXPIRATION)
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        Bucket bucket = buckets.get(clientKey, ignoredKey -> createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-RateLimit-Limit", String.valueOf(BUCKET_CAPACITY));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        wrpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63(request, response, probe);
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(BUCKET_CAPACITY)
                        .refillIntervally(BUCKET_CAPACITY, REFILL_PERIOD))
                .build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticated(authentication)
                && authentication.getPrincipal() instanceof User user
                && user.getTenant() != null
                && user.getTenant().getId() != null) {
            return "tenant:" + user.getTenant().getId();
        }

        return "ip:" + request.getRemoteAddr();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private void wrpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63(
            HttpServletRequest request,
            HttpServletResponse response,
            ConsumptionProbe probe
    ) throws IOException {
        long retryAfterSeconds = calculateRetryAfterSeconds(probe.getNanosToWaitForRefill());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Limite de requisições excedido. Tente novamente em "
                        + retryAfterSeconds + " segundos.",
                request.getRequestURI(),
                List.of()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }

    private long calculateRetryAfterSeconds(long nanosToWait) {
        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanosToWait);
        boolean hasRemainingNanos = nanosToWait % TimeUnit.SECONDS.toNanos(1) != 0;

        return Math.max(1, hasRemainingNanos ? seconds + 1 : seconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean optionsRequest = "OPTIONS".equalsIgnoreCase(request.getMethod());
        boolean apiRequest = request.getRequestURI().startsWith("/api/");

        return optionsRequest || !apiRequest;
    }
}