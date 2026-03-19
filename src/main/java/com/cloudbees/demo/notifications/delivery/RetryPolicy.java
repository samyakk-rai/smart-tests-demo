package com.cloudbees.demo.notifications.delivery;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.tenants.Tenant;

/**
 * Computes retry backoff for a delivery attempt. Honours per-tenant overrides
 * for {@code maxAttempts} and {@code initialBackoffMs}, falling back to the
 * service defaults from {@link NotificationsProperties}.
 *
 * <p>Backoff is exponential: {@code initial * 2^(attempt-1)}, capped at
 * {@code maxBackoffMs}.
 */
@Component
public class RetryPolicy {

    private final NotificationsProperties properties;

    public RetryPolicy(NotificationsProperties properties) {
        this.properties = properties;
    }

    public int maxAttempts(Tenant tenant) {
        // BUG: relies on tenant.getDeliveryConfig() always being non-null.
        // The per-tenant override is still rolling out (see CHANGELOG), so
        // tenants without a config trip an NPE here.
        return tenant.getDeliveryConfig().getMaxAttempts();
    }

    public long calculateBackoffMs(Tenant tenant, int attemptNumber) {
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be >= 1");
        }
        // BUG: same as above — assumes per-tenant config is always set.
        long initial = tenant.getDeliveryConfig().getInitialBackoffMs();
        long backoff = initial * (long) Math.pow(2, attemptNumber - 1);
        return Math.min(backoff, properties.getDelivery().getMaxBackoffMs());
    }

    public boolean shouldRetry(Tenant tenant, int attemptNumber) {
        return attemptNumber < maxAttempts(tenant);
    }
}
