package com.voidcube.tech.projectA.shared.ratelimit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
            String clientKey = resolveClientKey(request);

            Bucket bucket = buckets.get(clientKey, ignoredKey -> createBucket());

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            response.setHeader("X-RateLimit-Limit", String.valueOf(BUCKET_CAPACITY));

            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

            if(probe.isConsumed()) {
                filterChain.doFilter(request, response);
                return;
            }

            writeTooManyRequestResponse(request, response, probe);
            
        }

        private Bucket createBucket() {
            return Bucket.builder()
                .addLimit(limit -> limit
                .capacity(BUCKET_CAPACITY)
                .refillIntervally(BUCKET_CAPACITY, REFILL_PERIOD))
                .build();
        }

        private String resolveClientKey(HttpServletRequest request) {
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

            if(isAuthenticated(authentication) && authentication.getPrincipal() instanceof User user
            && user.getTenant() != null
            && user.getTenant().getId() != null) {
            return "tenant" + user.getTenant().getId();
            }

            return "ip" + request.getRemoteAddr();
        }

        private boolean isAuthenticated(Authentication authentication) {
            return authentication != null && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
        }

        private void writeTooManyRequestResponse(HttpServletRequest request, HttpServletResponse response, ConsumptionProbe probe) throws IOException {
            long retryAfterSeconds = calculateRetryAfterSeconds(probe.getNanosToWaitForRefill());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

            ApiErrorResponse errorResponse = new ApiErrorResponse(LocalDateTime.now(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), 
            "Limite de requisições excedido" + "Tente novamente em " + retryAfterSeconds + " segundos.", 
            request.getRequestURI(), null);

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        }

        private long calculateRetryAfterSeconds(long nanosToWait) {
            long completeSeconds = TimeUnit.NANOSECONDS
                .toSeconds(nanosToWait);

            boolean hasRemainingNanos = nanosToWait
                % TimeUnit.SECONDS.toNanos(1)
                != 0;

            if(hasRemainingNanos) {
                completeSeconds ++;
            }

            return Math.max(1, completeSeconds);
        }

        protected boolean shouldNotFilter(HttpServletRequest request) {
            boolean isOptionRequest = "OPTIONS"
                .equalsIgnoreCase(request.getMethod());

            boolean isApiRequest = request.getRequestURI()
                .startsWith("/api/");

            return isOptionRequest || !isApiRequest;
        }
}
