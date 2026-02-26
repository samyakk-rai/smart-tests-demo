package com.cloudbees.demo.notifications.tenants;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Simple sliding-window per-tenant rate limiter.
 *
 * <p>Not intended for production scale — backed by an in-memory deque per
 * tenant. Sufficient for the demo and for unit/integration tests.
 */
@Component
public class RateLimiter {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> hits = new HashMap<>();
    private final TenantService tenants;

    public RateLimiter(TenantService tenants) {
        this.tenants = tenants;
    }

    public synchronized void check(String tenantId) {
        Tenant tenant = tenants.get(tenantId);
        Deque<Instant> deque = hits.computeIfAbsent(tenantId, k -> new ArrayDeque<>());
        Instant now = Instant.now();
        Instant cutoff = now.minus(WINDOW);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
            deque.pollFirst();
        }
        if (deque.size() >= tenant.getRatePerMinute()) {
            throw new RateLimitExceededException(tenantId);
        }
        deque.addLast(now);
    }

    public synchronized int countInWindow(String tenantId) {
        Deque<Instant> deque = hits.get(tenantId);
        if (deque == null) return 0;
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
            deque.pollFirst();
        }
        return deque.size();
    }

    public synchronized void reset(String tenantId) {
        hits.remove(tenantId);
    }
}
